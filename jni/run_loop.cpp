/*
 Copyright (c) 2012, Spotify AB
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of Spotify AB nor the names of its contributors may
 be used to endorse or promote products derived from this software
 without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL SPOTIFY AB BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

//  Copyright (c) 2014 Matthew Mitchell
//
//  This file is part of Wakeify for Playlists. It is subject to the license terms
//  in the LICENSE file found in the top-level directory of this
//  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
//  No part of Wakeify for Playlists, including this file, may be copied, modified,
//  propagated, or distributed except according to the terms contained in the LICENSE file.



/**
 * The main runloop handling libspotify events and posting tasks to libspotify.
 * There is a queue of tasks that will be processed in order. The loop will sleep
 * when there is no work to do.
 */
#include <pthread.h>
#include <errno.h>
#include <list>
#include <string>

#include "run_loop.h"
#include "logger.h"
#include "key.h"
#include "tasks.h"

using namespace std;

string path;

// Defined in the sound_driver to keep the buffer logic together
int music_delivery(sp_session *sess, const sp_audioformat *format, const void *frames, int num_frames);

// A task contains the function, parameters and a task name
struct Task {
	task_fptr fptr;
	list<int> int_params;
	list<string> string_params;
	string name;

	Task(task_fptr _fptr, string _name, list<int> _int_params, list<string> _string_params) :
			fptr(_fptr), name(_name), int_params(_int_params), string_params(_string_params) {
	}
};

// The queue of tasks, thread safeness is important!
static list<Task> s_tasks;
// Mutex and condition for tasks and processing of tasks
static pthread_mutex_t s_notify_mutex;
static pthread_cond_t s_notify_cond;

// Keep track of next time sp_session_process_events should run
static int s_next_timeout = 0;

void addTask(task_fptr fptr, string name, list<int> int_params, list<string> string_params) {
	pthread_mutex_lock(&s_notify_mutex);
	Task task(fptr, name, int_params, string_params);
	s_tasks.push_back(task);
	pthread_cond_signal(&s_notify_cond);
	pthread_mutex_unlock(&s_notify_mutex);
}

void addTask(task_fptr fptr, string name, list<string> string_params) {
	list<int> int_params;
	addTask(fptr, name, int_params, string_params);
}

void addTask(task_fptr fptr, string name, list<int> int_params) {
	list<string> string_params;
	addTask(fptr, name, int_params, string_params);
}

void addTask(task_fptr fptr, string name) {
	list<int> int_params;
	list<string> string_params;
	addTask(fptr, name, int_params, string_params);
}

static void connection_error(sp_session *session, sp_error error) {
	log("------------- Connection error: %s\n -------------", sp_error_message(error));
	sp_session_player_unload(session);
	onConnectionError();
}
static void logged_out(sp_session *session) {
	addTask(on_logout, "logout_callback");
}
static void log_message(sp_session *session, const char *data) {
	log("************* Message: %s *************", data);
}

static void credentials_blob_updated(sp_session *session, const char * blob){
	// Update blob file
	FILE * file = fopen((path + "/blob.dat").c_str(), "w");
	fwrite(blob, 1, strlen(blob) + 1, file);
	fclose(file);
}

static void logged_in(sp_session *session, sp_error error) {
	log("Logged in");

	if (error == SP_ERROR_OK){
		// Update username file
		FILE * file = fopen((path + "/username.dat").c_str(), "w");
		log("Filename = %s",(path + "/username.dat").c_str());
		if (file){
			char const * username = sp_user_canonical_name(sp_session_user(session));
			fwrite(username, 1, strlen(username) + 1, file);
			fclose(file);
		}
	}

	list<int> int_params;
	int_params.push_back(error);
	addTask(on_logged_in, "login_callback", int_params);
}

static void process_events(list<int> int_params, list<string> string_params, sp_session *session) {
	do {
		sp_session_process_events(session, &s_next_timeout);
	} while (s_next_timeout == 0);
}

// run process_events on the libspotify thread
static void notify_main_thread(sp_session *session) {
	addTask(process_events, "Notify main thread: process_events");
}

void message_to_user(sp_session *session, const char *message){
	return;
}

void play_token_lost(sp_session *session){
	return;
}

static sp_session_callbacks callbacks = {
	&logged_in,
	&logged_out,
	&metadataUpdated,
	&connection_error,
	&message_to_user,
	&notify_main_thread,
	&music_delivery,
	&play_token_lost,
	&log_message,
	&nextSong
};

// The main loop takes care of executing tasks on the libspotify thread.
static void libspotify_loop(sp_session *session) {

	while (true) {
		pthread_mutex_lock(&s_notify_mutex);

		// If no tasks then sleep until there is, or a timeout happens
		if (s_tasks.size() == 0) {
			struct timespec ts;
			clock_gettime(CLOCK_REALTIME, &ts);
			struct timespec timer_start = ts;
			ts.tv_sec += s_next_timeout / 1000;
			ts.tv_nsec += (s_next_timeout % 1000) * 1000000;
			int reason = pthread_cond_timedwait(&s_notify_cond, &s_notify_mutex, &ts);
			// If timeout then process_events should be added to the queue
			if (reason == ETIMEDOUT) {
				pthread_mutex_unlock(&s_notify_mutex);
				addTask(process_events, "Timeout: process events");
				pthread_mutex_lock(&s_notify_mutex);
			} else { // calculate a new timeout (assuming the tasks below takes 0 time)
				struct timespec timer_end;
				clock_gettime(CLOCK_REALTIME, &timer_end);
				int delta = 0;
				delta += (timer_end.tv_sec - timer_start.tv_sec) * 1000;
				delta += (timer_end.tv_nsec - timer_start.tv_nsec) / 1000000;
				s_next_timeout -= delta;
			}
		}
		// create a copy of the list of operations since other thread can manipulate it and clear the real one
		list<Task> tasks_copy = s_tasks;
		s_tasks.clear();
		pthread_mutex_unlock(&s_notify_mutex);

		for (list<Task>::iterator it = tasks_copy.begin(); it != tasks_copy.end(); it++) {
			Task task = (*it);
			task.fptr(task.int_params, task.string_params, session);
		}
	}
}

void* start_spotify(void *storage_path) {
	path = (char *)storage_path;

	pthread_mutex_init(&s_notify_mutex, NULL);
	pthread_cond_init(&s_notify_cond, NULL);

	sp_session *session;
	sp_session_config config;

	// Libspotify does not guarantee that the structures are freshly initialized
	memset(&config, 0, sizeof(config));

	string cache_location = path + "/cache";
	string settings_location = path + "/settings";

	config.api_version = SPOTIFY_API_VERSION;
	config.cache_location = cache_location.c_str();
	config.settings_location = settings_location.c_str();
	config.application_key = g_appkey;
	config.application_key_size = g_appkey_size;
	config.user_agent = "WakeifyPlus";
	config.callbacks = &callbacks;
	config.tracefile = NULL;

	callbacks.credentials_blob_updated = &credentials_blob_updated;

	sp_error error = sp_session_create(&config, &session);

	sp_session_preferred_bitrate(session, SP_BITRATE_160k);

	if (SP_ERROR_OK != error)
		exitl("failed to create session: %s\n", sp_error_message(error));

	// start the libspotify loop
	libspotify_loop(session);
}
