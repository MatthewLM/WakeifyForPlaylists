//  Copyright (c) 2014 Matthew Mitchell
//
//  This file is part of Wakeify for Playlists. It is subject to the license terms
//  in the LICENSE file found in the top-level directory of this
//  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
//  No part of Wakeify for Playlists, including this file, may be copied, modified,
//  propagated, or distributed except according to the terms contained in the LICENSE file.

package com.matthewmitchell.wakeifyplus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.matthewmitchell.wakeifyplus.R;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

public class AlarmModifierBase  extends SherlockFragmentActivity implements TimePickerDialog.OnTimeSetListener {
	
	protected Calendar time;
	protected int snoozeMinutes;
	protected int rampingMinutes;
	protected int rampingSeconds;
	protected int daysSelected;
	protected String playlistURI;
	protected String playlistName;
	protected SimpleDateFormat date_format = new SimpleDateFormat("HH:mm");
	protected String name;
	protected int onOff;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		overridePendingTransition(R.anim.pull_in, R.anim.hold);
	    setContentView(R.layout.alarm_edit);
	    SeekBar bar = (SeekBar) findViewById(R.id.volume_edit);
	    if (bundle != null){
	    	time = (Calendar)bundle.getSerializable("Time");
	    	bar.setProgress(bundle.getInt("Volume"));
	    	daysSelected = bundle.getInt("Weekdays");
	    	playlistURI = bundle.getString("PlaylistURI");
	    	playlistName = bundle.getString("PlaylistName");
	    	name = (String)bundle.getSerializable("Name");
	    	snoozeMinutes = bundle.getInt("Snooze");
	    	onOff = bundle.getInt("onOff");
	    	rampingMinutes = bundle.getInt("rampingMinutes");
	    	rampingSeconds = bundle.getInt("rampingSeconds");
	    }else{
    	    onCreateData(bar);
	    }
	    TextView text = (TextView)findViewById(R.id.name_edit);
	    text.setText(name);
	    TextView edit = (TextView) findViewById(R.id.time_edit);
	    edit.setText(date_format.format(time.getTime()));
	    TextView snooze = (TextView) findViewById(R.id.snooze_edit);
	    snooze.setText("" + snoozeMinutes);
	    TextView ramping = (TextView) findViewById(R.id.volume_ramping);
	    ramping.setText(rampingMinutes + "m" + rampingSeconds + "s");
	    TextView playlist = (TextView) findViewById(R.id.playlist_edit);
	    playlist.setText(playlistName);
	}
	
	protected void setAlarmResult(){
		
	}
	
	protected void onCreateData(SeekBar bar){
		// Set alarm time to now
	    time = Calendar.getInstance();
	    // Set volume to max
	    bar.setProgress(100);
	    daysSelected = 0;
	    playlistURI = "";
	    playlistName = "None";
	    name = "";
	    snoozeMinutes = 5;
	    onOff = 0;
	    rampingMinutes = 0;
	    rampingSeconds = 0;
	}
	
	@Override
	protected void onPause(){
		overridePendingTransition(R.anim.hold, R.anim.push_out);
		super.onPause();
	}
	
	public void onTimeClick(View v){
		TimePickerFragment newFragment = new TimePickerFragment(this,time.get(Calendar.HOUR_OF_DAY),time.get(Calendar.MINUTE));
	    newFragment.show(getSupportFragmentManager(), "timePicker");
	}
	
	public void onSnoozeTimeClick(View v){
		NumberPickerFragment newFragment = new NumberPickerFragment(this,snoozeMinutes);
		newFragment.show(getSupportFragmentManager(), "snoozePicker");
	}
	
	public void onVolumeRampingClick(View v){
		MinutesSecondsFragment newFragment = new MinutesSecondsFragment(this,rampingMinutes,rampingSeconds);
		newFragment.show(getSupportFragmentManager(), "volumeRampingPicker");
	}
	
	public void onNameClick(View v){
		final EditText input = new EditText(this);
		final TextView text = (TextView)findViewById(R.id.name_edit);
		input.setText(name);
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		Editable etext = input.getText();
		Selection.setSelection(etext, etext.length());
		AlertDialog.Builder dialogBuild = new AlertDialog.Builder(this);
	    dialogBuild.setTitle("Name");
	    dialogBuild.setMessage("Enter the alarm name");
	    dialogBuild.setView(input);
	    dialogBuild.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            Editable value = input.getText();
	            name = value.toString();
	            text.setText(name);
	        }
	    });
	    dialogBuild.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    });
	    AlertDialog dialog = dialogBuild.create();
	    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	    dialog.show();
	}
	
	public void onWeekdaysClick(View v){
		Intent weekdaySelector = new Intent(this, WeekdaySelectionActivity.class);
		weekdaySelector.putExtra("Monday", (daysSelected & 1) == 1);
		weekdaySelector.putExtra("Tuesday", (daysSelected & 2) == 2);
		weekdaySelector.putExtra("Wednesday", (daysSelected & 4) == 4);
		weekdaySelector.putExtra("Thursday", (daysSelected & 8) == 8);
		weekdaySelector.putExtra("Friday", (daysSelected & 16) == 16);
		weekdaySelector.putExtra("Saturday", (daysSelected & 32) == 32);
		weekdaySelector.putExtra("Sunday", (daysSelected & 64) == 64);
		startActivityForResult(weekdaySelector,0);
	}
	
	public void onPlaylistsClick(View v){
		Log.i("infodb", "GOT TO PLAYLISTS");
		Intent editPlaylists = new Intent(this, EditPlaylistActivity.class);
		startActivityForResult(editPlaylists,1);
	}
	
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
		time.set(Calendar.HOUR_OF_DAY, hourOfDay);
		time.set(Calendar.MINUTE, minute);
		TextView edit = (TextView) findViewById(R.id.time_edit);
	    edit.setText(date_format.format(time.getTime()));
    }
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data){
		if (resultCode == RESULT_CANCELED)
			return;
		if (requestCode == 0){
    		daysSelected = 0;
    		if (data.getBooleanExtra("Monday", false))
    			daysSelected |= 1;
    		if (data.getBooleanExtra("Tuesday", false))
    			daysSelected |= 2;
    		if (data.getBooleanExtra("Wednesday", false))
    			daysSelected |= 4;
    		if (data.getBooleanExtra("Thursday", false))
    			daysSelected |= 8;
    		if (data.getBooleanExtra("Friday", false))
    			daysSelected |= 16;
    		if (data.getBooleanExtra("Saturday", false))
    			daysSelected |= 32;
    		if (data.getBooleanExtra("Sunday", false))
    			daysSelected |= 64;
		}else{
			playlistURI = data.getStringExtra("PlaylistURI");
			playlistName = data.getStringExtra("PlaylistName");
			TextView playlist = (TextView) findViewById(R.id.playlist_edit);
		    playlist.setText(playlistName);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  savedInstanceState.putInt("Weekdays", daysSelected);
	  savedInstanceState.putSerializable("Time", time);
	  savedInstanceState.putSerializable("PlaylistURI", playlistURI);
	  savedInstanceState.putSerializable("PlaylistName", playlistName);
	  savedInstanceState.putString("Name", name);
	  SeekBar bar = (SeekBar) findViewById(R.id.volume_edit);
	  savedInstanceState.putInt("Volume",bar.getProgress());
	  savedInstanceState.putInt("Snooze", snoozeMinutes);
	  savedInstanceState.putInt("onOff",onOff);
	  savedInstanceState.putInt("rampingMinutes",rampingMinutes);
	  savedInstanceState.putInt("rampingSeconds",rampingSeconds);
	}

}