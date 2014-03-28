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
 * Sets a high priority on the service by bringing it to foreground.
 * Also acts like a bridge to the LibSpotify methods.
 * 
 * Should be rules for when to go to foreground. Wakelock is also needed.
 */
package com.matthewmitchell.wakeifyplus;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class SpotifyService extends Service {
	private final IBinder mBinder = new LocalBinder();
	private WifiLock mWifiLock;

	static interface LoginDelegate {
		void onLogin();
		void onLoginFailed(String message);
	}
	
	static interface LogoutDelegate {
		void onLogout();
	}
	
	static interface PlaylistLoadDelegate {
		void onLoadedPlaylist(String playlistName, String playlistURI);
	}

	static interface PlaylistPlayDelegate {
		void onPlaylistPlayFailed();
		void onSongPlaying(final String songName, final String artistName);
	}

	public class LocalBinder extends Binder {
		SpotifyService getService() {
			return SpotifyService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		System.loadLibrary("spotify");
		System.loadLibrary("spotifywrapper");

		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			throw new RuntimeException("Storage card not available");
		LibSpotifyWrapper.init(LibSpotifyWrapper.class.getClassLoader(), getExternalFilesDir(null).getAbsolutePath());
		
		mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
		mWifiLock.acquire();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		mWifiLock.release();

		// Tell the user we stopped.
		Toast.makeText(this, "The local service has stopped", Toast.LENGTH_SHORT).show();
		Log.i("infodb", "On destroy.");
		LibSpotifyWrapper.destroy();
		System.exit(0);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void login(String email, String password, LoginDelegate loginDelegate) {
		LibSpotifyWrapper.loginUser(email, password, loginDelegate);
	}
	
	public void logout(LogoutDelegate logoutDelegate) {
		LibSpotifyWrapper.logout(logoutDelegate);
	}
	
	public void autoLogin(LoginDelegate autoLoginDelegate){
		LibSpotifyWrapper.autoLogin(autoLoginDelegate);
	}
	
	public void loadPlaylists(PlaylistLoadDelegate playlistDelegate){
		LibSpotifyWrapper.loadPlaylists(playlistDelegate);
	}

	public void playPlaylist(String uri, int rampingSeconds, PlaylistPlayDelegate playlistPlayDelegate) {
		LibSpotifyWrapper.playPlaylist(uri, rampingSeconds, playlistPlayDelegate);
	}
	
	public void stopPlaylist() {
		LibSpotifyWrapper.stopPlaylist();
	}

	public void destroy() {
		LibSpotifyWrapper.destroy();
	}

}