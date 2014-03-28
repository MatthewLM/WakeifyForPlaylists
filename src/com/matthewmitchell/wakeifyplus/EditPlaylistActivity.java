//  Copyright (c) 2014 Matthew Mitchell
//
//  This file is part of Wakeify for Playlists. It is subject to the license terms
//  in the LICENSE file found in the top-level directory of this
//  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
//  No part of Wakeify for Playlists, including this file, may be copied, modified,
//  propagated, or distributed except according to the terms contained in the LICENSE file.

package com.matthewmitchell.wakeifyplus;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.matthewmitchell.wakeifyplus.ServiceBinder.ServiceBinderDelegate;
import com.matthewmitchell.wakeifyplus.SpotifyService.PlaylistLoadDelegate;
import com.matthewmitchell.wakeifyplus.R;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class EditPlaylistActivity extends SherlockActivity implements OnItemClickListener {
	
	private ArrayList<String> mPlaylists = new ArrayList<String>();
	private ArrayList<String> mPlaylistURIs = new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private ServiceBinder binder;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		overridePendingTransition(R.anim.pull_in, R.anim.hold);
	    setContentView(R.layout.activity_edit_playlist);
	    ListView list = (ListView)findViewById(R.id.playlist_list);
	    adapter = new ArrayAdapter<String>(this, R.layout.playlist_row, mPlaylists);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        Log.i("infodb", "ON CREATE PLAYLISTS");
        // Begin loading playlists
        binder = new ServiceBinder(this);
		binder.bindService(new ServiceBinderDelegate() {
			@Override
			public void onIsBound() {
				binder.getService().loadPlaylists(new PlaylistLoadDelegate(){

					@Override
					public void onLoadedPlaylist(String playlistName, String playlistURI) {
						// Place playlist data into arrays
						// First see if we already have it
						if (mPlaylistURIs.contains(playlistURI)){
							return;
						}
						mPlaylistURIs.add(playlistURI);
						mPlaylists.add(playlistName);
						
						// Update playlist list
						adapter.notifyDataSetChanged();
						
						Log.i("infodb", "Loaded Playlist = " + playlistName);
					}
					
				});
			}
		});
	}
	
	@Override
	protected void onPause(){
		overridePendingTransition(R.anim.hold, R.anim.push_out);
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		binder.doUnbindService();
		super.onDestroy();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_edit_playlist, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
          case R.id.facebook:
        	  Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/Wakeify"));
        	  startActivity(myIntent);
        	  return true;
          case R.id.report:
        	  ReportProblem.reportProblem(this);
        	  return true;
      }
      return super.onOptionsItemSelected(item);
    }
	

	@Override
	public void onItemClick(AdapterView<?> list, View arg1, int position, long arg3) {
		// Pass playlist information back to activity
		Intent result = new Intent();
		result.putExtra("PlaylistURI", mPlaylistURIs.get(position));
		result.putExtra("PlaylistName", mPlaylists.get(position));
		setResult(RESULT_OK,result);
		finish();
	}
	
}
