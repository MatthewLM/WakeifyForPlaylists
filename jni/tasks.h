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



/*
 * tasks.h
 *
 *  Created on: Nov 9, 2012
 *      Author: johanneskaehlare
 */

#ifndef TASKS_H_
#define TASKS_H_

#include <list>
#include <string>

#include <api.h>

using namespace std;

void login(list<int> int_params, list<string> string_params, sp_session *session);
void logout(list<int> int_params, list<string> string_params, sp_session *session);
void autoLogin(list<int> int_params, list<string> string_params, sp_session *session);
void loadPlaylists(list<int> int_params, list<string> string_params, sp_session *session);
void loadPlaylists(list<int> int_params, list<string> string_params, sp_session *session);
void playPlaylist(list<int> int_params, list<string> string_params, sp_session *session);
void onPlaylistForPlayingStateChanged(sp_playlist * playlist, void * session);
void onPlaylistForPlayingLoaded(sp_playlist * playlist, sp_session * session);
void * volumeRampingFunction(void * foo);
void metadataUpdated(sp_session * session);
void onTrackLoaded(sp_session * session);
void stopPlaylist(list<int> int_params, list<string> string_params, sp_session *session);
void nextSong(sp_session * session);
void destroy(list<int> int_params, list<string> string_params, sp_session *session);

// Callbacks to java
void on_logged_in(list<int> int_params, list<string> string_params, sp_session *session);
void on_logout(list<int> int_params, list<string> string_params, sp_session *session);
void on_playlists_loaded(sp_playlistcontainer * playlists, void *userdata);
void on_playlist_loaded(sp_playlist * playlist);
void onConnectionError(void);
void on_playlist_state_changed(sp_playlist * playlist, void * userdata);

#endif /* TASKS_H_ */
