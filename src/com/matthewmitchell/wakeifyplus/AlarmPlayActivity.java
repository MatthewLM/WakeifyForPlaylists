//  Copyright (c) 2014 Matthew Mitchell
//
//  This file is part of Wakeify for Playlists. It is subject to the license terms
//  in the LICENSE file found in the top-level directory of this
//  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
//  No part of Wakeify for Playlists, including this file, may be copied, modified,
//  propagated, or distributed except according to the terms contained in the LICENSE file.

package com.matthewmitchell.wakeifyplus;

import com.matthewmitchell.wakeifyplus.R;
import com.matthewmitchell.wakeifyplus.ServiceBinder.ServiceBinderDelegate;
import com.matthewmitchell.wakeifyplus.SpotifyService.LoginDelegate;
import com.matthewmitchell.wakeifyplus.SpotifyService.PlaylistPlayDelegate;
import com.matthewmitchell.wakeifyplus.database.AlarmDatabase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class AlarmPlayActivity extends Activity {
	
	private long id = -1;
	private int snooze;
	private int rampingSeconds;
	private int volume;
	private int currentSecond;
	private String name;
	private String playlistURI;
	private boolean handled;
	private static Ringtone ringtone = null;
	private ServiceBinder binder = null;
	
	@Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_play);
        // Get the layout id
        final LinearLayout root = (LinearLayout) findViewById(R.id.root);
        final Intent intent = getIntent();
        id = intent.getLongExtra("ID", -1);
        
        // Get the data from the alarms database
        AlarmDatabase database = new AlarmDatabase(this);
		SQLiteDatabase write = database.getWritableDatabase();
		Cursor row = write.query(AlarmDatabase.ALARMS_TABLE,
				new String[]{"name","time","snoozeMinutes","playlistURI","volume","rampingMinutes","rampingSeconds","time"},
				"_id = " + id, null,null,null,null);
		if (!row.moveToFirst()){
			row.close();
			write.close();
			database.close();
			return;
		}
		// Get saved name if it exists
		if (savedInstanceState == null)
			name = row.getString(row.getColumnIndex("name"));
		else
			name = (String)savedInstanceState.getSerializable("Name");
		snooze = row.getInt(row.getColumnIndex("snoozeMinutes"));
		playlistURI = row.getString(row.getColumnIndex("playlistURI"));
		volume = row.getInt(row.getColumnIndex("volume"));
		rampingSeconds = row.getInt(row.getColumnIndex("rampingMinutes")) * 60 + row.getInt(row.getColumnIndex("rampingSeconds")); 
		row.close();
		write.close();
		database.close();
		
        handled = false;
        
        final Context context = this;
        
        root.post(new Runnable() { 
            public void run() {
                Rect rect = new Rect(); 
                Window win = getWindow();  // Get the Window
                win.getDecorView().getWindowVisibleDisplayFrame(rect); 
                // Get the height of Status Bar 
                int statusBarHeight = rect.top; 
                // Get the height occupied by the decoration contents 
                int contentViewTop = win.findViewById(Window.ID_ANDROID_CONTENT).getTop(); 
                // Calculate titleBarHeight by deducting statusBarHeight from contentViewTop  
                int titleBarHeight = contentViewTop - statusBarHeight; 
                // By now we got the height of titleBar & statusBar
                // Now lets get the screen size
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);   
                int screenHeight = metrics.heightPixels;
                // Now calculate the height that our layout can be set
                // If you know that your application doesn't have statusBar added, then don't add here also. Same applies to application bar also 
                int layoutHeight = screenHeight - (titleBarHeight + statusBarHeight);
                // Add song name to alarm name for now
                final TextView songName = (TextView)findViewById(R.id.song_name);
                songName.setText(name);
                // Modify alarm buttons
                int buttonHeight = (layoutHeight - 90 * (int) metrics.density - songName.getHeight())/2;
                Button button = (Button) findViewById(R.id.snooze);
                button.setHeight(buttonHeight);
                button = (Button) findViewById(R.id.turn_off);
                button.setHeight(buttonHeight);
                
                // Attempt to play playlist
                
                if (playlistURI.isEmpty()){
                	// No URI, thus play the system alarm
                	if (savedInstanceState == null)
                		playSystemAlarm();
                }else{
                	
                    binder = new ServiceBinder(context);
                    binder.incRef(ServiceBinder.PLAY);
            		binder.bindService(new ServiceBinderDelegate() {
            			
            			@Override
            			public void onIsBound() {
            				
            				// First ensure we are logged in
            				
            				binder.getService().autoLogin(new LoginDelegate(){
    
    							@Override
    							public void onLogin() {
    								// Now we can try playing the playlist
    								binder.getService().playPlaylist(playlistURI, rampingSeconds, new PlaylistPlayDelegate(){
    
    									@Override
    									public void onPlaylistPlayFailed() {
    										// Needs to play system alarm.
    										if (savedInstanceState == null)
    					                		playSystemAlarm();
    									}

										@Override
										public void onSongPlaying(String song, String artist) {
											// Set song name
											name = song + " - " + artist;
							                songName.setText(name);
										}
    		        					
    		        				});
    							}
    
    							@Override
    							public void onLoginFailed(String message) {
    								if (savedInstanceState == null)
    			                		playSystemAlarm();
    							}
            					
            				});
            				
            			}
            			
            		});
                }
            } 
        });
	}
	
	public void playSystemAlarm(){
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		if (notification == null)
			notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
		if (ringtone == null)
			return;
		ringtone.play();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	    	if (!handled)
				onTurnOff(null);
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onPause() {
		if (binder != null)
			binder.doUnbindService();
		if (isFinishing())
			binder.decRef(ServiceBinder.PLAY);
		super.onPause();
	}
	
	public void onTurnOff(View v){
		// Turn off the alarm
		turnOffMusic();
		// Close activity
		handled = true;
		finish();
	}
	
	public void turnOffMusic(){
		if (ringtone != null){
			ringtone.stop();
			ringtone = null;
		}else{
			binder.getService().stopPlaylist();
		}
		StaticWakeLock.releaseWakeLock();
	}
	
	public void onSnooze(View v) {
		turnOffMusic();
		// Schedule alarm after snooze time
		AlarmReceiver.addAlarm(this, id, System.currentTimeMillis() + snooze * 60000);
		handled = true;
		finish();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  savedInstanceState.putString("Name", name);
	}
	
}
