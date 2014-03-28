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
 * Tasks that can be added to the queue of tasks running on the libspotify thread
 */
#include "tasks.h"
#include "run_loop.h"
#include "jni_glue.h"
#include "logger.h"
#include "sound_driver.h"
#include <list>

static int s_player_position = 0;
static string s_current_uri;
static bool s_is_playing = false;
static bool s_is_waiting_for_metadata = false;
static bool s_play_after_loaded = false;
bool areDestroying;

static void on_pause();
static void on_play();

extern string path;

void login(list<int> int_params, list<string> string_params, sp_session *session) {
	if (session == NULL)
		exitl("Logged in before session was initialized");
	string username = string_params.front();
	string password = string_params.back();
	sp_session_login(session, username.c_str(), password.c_str(), false, NULL);
}

void logout(list<int> int_params, list<string> string_params, sp_session *session) {
	areDestroying = false;
	sp_session_logout(session);
}

void autoLogin(list<int> int_params, list<string> string_params, sp_session *session) {
	if (session == NULL)
		exitl("Logged in before session was initialized");
	log("Running autoLogin function");
	// Check to see if the user is already logged in.
	if (sp_session_connectionstate(session) == SP_CONNECTION_STATE_LOGGED_IN){
		// All OK
		list<int> int_paramsb;
		int_paramsb.push_back(SP_ERROR_OK);
		addTask(on_logged_in, "login_callback", int_paramsb);
		return;
	}
	// Get username and blob
	FILE * usernameFile = fopen((path + "/username.dat").c_str(), "r");
	if (usernameFile){
		log("Username File Exists");
		FILE * blobFile = fopen((path + "/blob.dat").c_str(), "r");
		if (blobFile){
			// Read username and blob
			char username[100]; // 100 chars probably safe
			char blob[256]; // Only 64 bytes but use 256 in case the blob increases in the future.
			fseek(usernameFile,0,SEEK_END);
			int usernameSize = ftell(usernameFile);
			fseek(usernameFile,0,SEEK_SET);
			fseek(blobFile,0,SEEK_END);
			int blobSize = ftell(blobFile);
			fseek(blobFile,0,SEEK_SET);
			fread(username, 1, usernameSize, usernameFile);
			fread(blob, 1, blobSize, blobFile);
			sp_session_login(session, username, NULL, false, blob);
			fclose(usernameFile);
			fclose(blobFile);
			return;
		}
		fclose(usernameFile);
	}
	log("No credentials.");
	// No credentials
	list<int> int_paramsb;
	int_paramsb.push_back(SP_ERROR_NO_CREDENTIALS);
	addTask(on_logged_in, "login_callback", int_paramsb);
}

sp_playlistcontainer_callbacks pc_callbacks;
sp_playlist_callbacks pl_callbacks;

void loadPlaylists(list<int> int_params, list<string> string_params, sp_session *session) {
	sp_playlistcontainer * playlists = sp_session_playlistcontainer(session);
	log("Playlist container loaded? %i", sp_playlistcontainer_is_loaded(playlists));
	if (sp_playlistcontainer_is_loaded(playlists)){
		 on_playlists_loaded(playlists, NULL);
	}
	memset(&pc_callbacks,0,sizeof(pc_callbacks));
	pc_callbacks.container_loaded = &on_playlists_loaded;
	sp_playlistcontainer_add_callbacks(playlists,&pc_callbacks,NULL);
}

int rampingSeconds;
bool playing = false;

void playPlaylist(list<int> int_params, list<string> string_params, sp_session *session){
	string uri = string_params.front();
	rampingSeconds = int_params.front();
	// See if a playlist is already playing
	if (playing)
		return;
	playing = true;
	// Get the playlist from the uri
	sp_link * link = sp_link_create_from_string(uri.c_str());
	sp_playlist * playlist = sp_playlist_create(session, link);
	if (! sp_playlist_is_loaded(playlist)){
		memset(&pl_callbacks, 0, sizeof(pl_callbacks));
		pl_callbacks.playlist_state_changed = &onPlaylistForPlayingStateChanged;
		sp_playlist_add_callbacks(playlist, &pl_callbacks, session);
	}else{
		onPlaylistForPlayingLoaded(playlist, session);
	}
	sp_link_release(link);
}

void onPlaylistForPlayingStateChanged(sp_playlist * playlist, void * session){
	if (sp_playlist_is_loaded(playlist)) {
		sp_playlist_remove_callbacks(playlist, &pl_callbacks, session);
		onPlaylistForPlayingLoaded(playlist, (sp_session *) session);
	}
}

int lastThreeSongs[3] = {-1,-1,-1};
int lastThreeSongsNext;
sp_playlist * currentPlaylist;
sp_track * currentTrack;
bool waitingForMetadata = false;

bool getRandomPlayableTrack(sp_playlist * playlist, sp_session * session){
	int numTracks = sp_playlist_num_tracks(playlist);
	int randNum = rand() % numTracks;
	int start = randNum;
	// Find playable track
	int playable = -1;
	for(;;){
		currentTrack = sp_playlist_track(playlist, randNum);
		sp_link * track_link = sp_link_create_from_track(currentTrack, 0);
		if (sp_link_type(track_link) == SP_LINKTYPE_TRACK
				&& (!sp_track_is_loaded(currentTrack)
						|| sp_track_get_availability(session, currentTrack) == SP_TRACK_AVAILABILITY_AVAILABLE)){
			playable = randNum;
			// Check to see if this has been played recently.
			bool played = false;
			for (int x = 0; x < 3; x++){
				if (randNum == lastThreeSongs[x]){
					played = true;
					break;
				}
			}
			if (!played){
				sp_link_release(track_link);
				lastThreeSongs[lastThreeSongsNext] = randNum;
				if (lastThreeSongsNext == 2){
					lastThreeSongsNext = 0;
				}else{
					lastThreeSongsNext++;
				}
				return true;
			}
		}
		sp_link_release(track_link);
		if (randNum == numTracks - 1){
			randNum = 0;
		}else {
			randNum++;
		}
		if (randNum == start){
			if (playable == -1)
				return false;
			// Play the playable song even though it has been played recently
			lastThreeSongs[lastThreeSongsNext] = playable;
			if (lastThreeSongsNext == 2){
				lastThreeSongsNext = 0;
			}else{
				lastThreeSongsNext++;
			}
			currentTrack = sp_playlist_track(playlist, playable);
			return true;
		}
	}

}

pthread_t volumeRampingThread;
bool volumeRampingRun = false;
int volume;

void onPlaylistForPlayingLoaded(sp_playlist * playlist, sp_session * session){
	currentPlaylist = playlist;
	JNIEnv * env;
	jclass class_libspotify = find_class_from_native_thread(&env);
	jmethodID methodId = env->GetStaticMethodID(class_libspotify, "onPlaylistPlayFailed", "()V");
	if (! sp_playlist_num_tracks(playlist)){
		log("No tracks in playlist. Cannot play!");
		env->CallStaticVoidMethod(class_libspotify, methodId);
		env->DeleteLocalRef(class_libspotify);
		playing = false;
		return;
	}
	// Select first random song
	lastThreeSongsNext = 0;
	if (!getRandomPlayableTrack(playlist, session)){
		log("No playable tracks!");
		env->CallStaticVoidMethod(class_libspotify, methodId);
		env->DeleteLocalRef(class_libspotify);
		playing = false;
		return;
	}

	sp_track_add_ref(currentTrack);
	if (sp_track_error(currentTrack) == SP_ERROR_OTHER_PERMANENT){
		log("Track error! %s", sp_error_message(sp_track_error(currentTrack)));
		env->CallStaticVoidMethod(class_libspotify, methodId);
		env->DeleteLocalRef(class_libspotify);
		playing = false;
		return;
	}else if (sp_track_is_loaded(currentTrack)){
		onTrackLoaded(session);
	}else{
		// Else wait for metadata callback...
		waitingForMetadata = true;
	}

	// Begin volume ramping

	volumeRampingRun = true;
	volume = 0;
	if (pthread_create(&volumeRampingThread, NULL, volumeRampingFunction, NULL) == 0){
		// Success, set volume to zero.
		log("Setting volume initially to zero.");
		setVolume(0);
	}
}

void * volumeRampingFunction(void * foo){
	while(volumeRampingRun && volume != 100) {
		// Continue to ramp the volume
		timespec time;
		time.tv_sec = rampingSeconds / 100;
		time.tv_nsec = rampingSeconds * 10000000 - (rampingSeconds / 100) * 1000000000;
		nanosleep(&time, NULL);
		setVolume(++volume);
		log("Setting volume to %i", volume);
	}
	pthread_exit(NULL);
}

void metadataUpdated(sp_session * session) {
	if (waitingForMetadata && sp_track_is_loaded(currentTrack)){
		waitingForMetadata = false;
		onTrackLoaded(session);
	}
}

void onTrackLoaded(sp_session * session){
	// Check that we can play the track
	if (sp_track_get_availability(session, currentTrack) == SP_TRACK_AVAILABILITY_AVAILABLE){
		sp_session_player_load(session, currentTrack);
		sp_session_player_play(session, true);
		sp_artist * artist = sp_track_artist(currentTrack, 0);
		JNIEnv * env;
		jclass class_libspotify = find_class_from_native_thread(&env);
		jmethodID methodId = env->GetStaticMethodID(class_libspotify, "onSongPlaying", "(Ljava/lang/String;Ljava/lang/String;)V");
		env->CallStaticVoidMethod(class_libspotify, methodId,
				env->NewStringUTF(sp_track_name(currentTrack)),
				env->NewStringUTF(sp_artist_name(artist)));
		env->DeleteLocalRef(class_libspotify);
	}else{
		// Else we need another song
		nextSong(session);
	}
}

void nextSong(sp_session * session){
	// Select a random song to play
	sp_track_release(currentTrack);
	if (!getRandomPlayableTrack(currentPlaylist, session)){
		log("No playable tracks!");
		JNIEnv * env;
		jclass class_libspotify = find_class_from_native_thread(&env);
		jmethodID methodId = env->GetStaticMethodID(class_libspotify, "onPlaylistPlayFailed", "()V");
		env->CallStaticVoidMethod(class_libspotify, methodId);
		env->DeleteLocalRef(class_libspotify);
		playing = false;
		return;
	}
	sp_track_add_ref(currentTrack);
	// Play this song
	if (sp_track_is_loaded(currentTrack)){
		onTrackLoaded(session);
	}else{
		// Else we are waiting for the metadata.
		waitingForMetadata = true;
	}
}

void stopPlaylist(list<int> int_params, list<string> string_params, sp_session *session){
	if (volumeRampingRun) {
		volumeRampingRun = false;
		pthread_join(volumeRampingThread, NULL);
	}
	sp_session_player_unload(session);
	playing = false;
}

void on_logged_in(list<int> int_params, list<string> string_params, sp_session *session) {
	sp_error error = (sp_error)int_params.front();
	bool success = (SP_ERROR_OK == error) ? true : false;

	JNIEnv *env;
	jclass class_libspotify = find_class_from_native_thread(&env);

	jmethodID methodId = env->GetStaticMethodID(class_libspotify, "onLogin", "(ZLjava/lang/String;)V");
	log("Login = %s", sp_error_message(error));
	env->CallStaticVoidMethod(class_libspotify, methodId, success, env->NewStringUTF(sp_error_message(error)));
	env->DeleteLocalRef(class_libspotify);
}

void on_logout(list<int> int_params, list<string> string_params, sp_session *session) {
	if (areDestroying){
		sp_session_release(session);
		return;
	}
	JNIEnv *env;
	jclass class_libspotify = find_class_from_native_thread(&env);
	jmethodID methodId = env->GetStaticMethodID(class_libspotify, "onLogout", "()V");
	env->CallStaticVoidMethod(class_libspotify, methodId);
	env->DeleteLocalRef(class_libspotify);
}

void on_playlists_loaded(sp_playlistcontainer * playlists, void *userdata){
	int i;
	log("Num PLaylists = %i",sp_playlistcontainer_num_playlists(playlists));
	memset(&pl_callbacks, 0, sizeof(pl_callbacks));
	pl_callbacks.playlist_state_changed = &on_playlist_state_changed;
	for (i = 0; i < sp_playlistcontainer_num_playlists(playlists); i++) {
		sp_playlist * playlist = sp_playlistcontainer_playlist(playlists, i);
		if (sp_playlist_is_loaded(playlist)) {
			on_playlist_loaded(playlist);
		}else{
			sp_playlist_add_callbacks(playlist, &pl_callbacks, NULL);
		}
	}
	log("Done loop");
}

void on_playlist_state_changed(sp_playlist * playlist, void * userdata){
	if (sp_playlist_is_loaded(playlist)) {
		sp_playlist_remove_callbacks(playlist, &pl_callbacks, NULL);
		on_playlist_loaded(playlist);
	}
}

void on_playlist_loaded(sp_playlist * playlist){
	JNIEnv * env;
	jclass class_libspotify = find_class_from_native_thread(&env);
	// Set playlist name
	jstring playlistName = env->NewStringUTF(sp_playlist_name(playlist));
	// Set playlist URI
	sp_link * link = sp_link_create_from_playlist(playlist);
	if (link == NULL)
		// Could not get the link.
		return;
	char playlistURIBuffer[256];
	sp_link_as_string(link, playlistURIBuffer, 256);
	sp_link_release(link);
	jstring playlistURI = env->NewStringUTF(playlistURIBuffer);
	// Give information to Wakeify
	jmethodID methodId = env->GetStaticMethodID(class_libspotify, "onPlaylistLoaded", "(Ljava/lang/String;Ljava/lang/String;)V");
	env->CallStaticVoidMethod(class_libspotify, methodId, playlistName, playlistURI);
	env->DeleteLocalRef(class_libspotify);
}

void onConnectionError(void){
	JNIEnv * env;
	jclass class_libspotify = find_class_from_native_thread(&env);
	jmethodID methodId = env->GetStaticMethodID(class_libspotify, "onPlaylistPlayFailed", "()V");
	env->CallStaticVoidMethod(class_libspotify, methodId);
	env->DeleteLocalRef(class_libspotify);
}

void destroy(list<int> int_params, list<string> string_params, sp_session *session) {
	areDestroying = true;
	destroy_audio_player();
}
