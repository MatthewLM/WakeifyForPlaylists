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
 * Interface to the JNI. All communication goes through this class.
 */

package com.matthewmitchell.wakeifyplus;

import com.matthewmitchell.wakeifyplus.SpotifyService.LoginDelegate;
import com.matthewmitchell.wakeifyplus.SpotifyService.LogoutDelegate;
import com.matthewmitchell.wakeifyplus.SpotifyService.PlaylistLoadDelegate;
import com.matthewmitchell.wakeifyplus.SpotifyService.PlaylistPlayDelegate;

import android.os.Handler;

public class LibSpotifyWrapper {

	private static Handler handler = new Handler();
	private static LoginDelegate mLoginDelegate;
	private static LogoutDelegate mLogoutDelegate = null;
	private static PlaylistLoadDelegate mPlaylistLoadDelegate;
	private static PlaylistPlayDelegate mPlaylistPlayDelegate;
	private static boolean failNotified;

	native public static void init(ClassLoader loader, String storagePath);

	native public static void destroy();

	native private static void login(String username, String password);
	
	native private static void logout();
	
	native private static void autoLogin();
	
	native private static void loadPlaylists();

	native private static void playPlaylist(String uri, int rampingSeconds);
	
	native public static void stopPlaylistNative();

	public static void loginUser(String username, String password, LoginDelegate loginDelegate) {
		mLoginDelegate = loginDelegate;
		login(username, password);
	}
	
	public static void logout(LogoutDelegate logoutDelegate){
		mLogoutDelegate = logoutDelegate;
		logout();
	}
	
	public static void autoLogin(LoginDelegate autoLoginDelegate){
		mLoginDelegate = autoLoginDelegate;
		autoLogin();
	}
	
	public static void loadPlaylists(PlaylistLoadDelegate playlistDelegate){
		mPlaylistLoadDelegate = playlistDelegate;
		loadPlaylists();
	}

	public static void playPlaylist(String uri, int rampingSeconds, PlaylistPlayDelegate playlistPlayDelegate) {
		mPlaylistPlayDelegate = playlistPlayDelegate;
		failNotified = false;
		playPlaylist(uri, rampingSeconds);
	}
	
	public static void stopPlaylist(){
		mPlaylistPlayDelegate = null;
		stopPlaylistNative();
	}

	public static void onLogin(final boolean success, final String message) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (success)
					mLoginDelegate.onLogin();
				else
					mLoginDelegate.onLoginFailed(message);
			}
		});

	}
	
	public static void onLogout() {
		if (mLogoutDelegate != null){
    		handler.post(new Runnable() {
    			@Override
    			public void run() {
    				mLogoutDelegate.onLogout();
    				mLogoutDelegate = null;
    			}
    		});
		}
	}
	
	public static void onPlaylistLoaded(final String playlistName, final String playlistURI){
		handler.post(new Runnable() {
			@Override
			public void run() {
				mPlaylistLoadDelegate.onLoadedPlaylist(playlistName, playlistURI);
			}
		});
	}

	public static void onPlaylistPlayFailed(){
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (mPlaylistPlayDelegate != null && !failNotified){
					mPlaylistPlayDelegate.onPlaylistPlayFailed();
					failNotified = true;
				}
			}
		});
	}
	
	public static void onSongPlaying(final String songName, final String artistName){
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (mPlaylistPlayDelegate != null && !failNotified){
					mPlaylistPlayDelegate.onSongPlaying(songName, artistName);
				}
			}
		});
	}

}
