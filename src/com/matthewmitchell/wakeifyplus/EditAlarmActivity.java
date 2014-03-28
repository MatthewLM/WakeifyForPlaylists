//  Copyright (c) 2014 Matthew Mitchell
//
//  This file is part of Wakeify for Playlists. It is subject to the license terms
//  in the LICENSE file found in the top-level directory of this
//  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
//  No part of Wakeify for Playlists, including this file, may be copied, modified,
//  propagated, or distributed except according to the terms contained in the LICENSE file.

package com.matthewmitchell.wakeifyplus;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Log;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.widget.SeekBar;

import com.matthewmitchell.wakeifyplus.R;
import com.matthewmitchell.wakeifyplus.database.AlarmDatabase;

public class EditAlarmActivity extends AlarmModifierBase {
	
	@Override
	protected void onCreateData(SeekBar bar){
		Intent data = getIntent();
		long id = data.getLongExtra("ID", -1);
		if (id == -1){
			super.onCreateData(bar);
			return;
		}
		// Get data from database
		AlarmDatabase database = new AlarmDatabase(this);
		SQLiteDatabase read = database.getReadableDatabase();
		Cursor row = read.query(AlarmDatabase.ALARMS_TABLE,
				new String[]{"name","time","days","volume","onOff","snoozeMinutes", "rampingMinutes",
				"rampingSeconds", "playlistURI", "playlistName"},
				"_id = " + id, null, null, null, null);
		row.moveToFirst();
	    name = row.getString(row.getColumnIndex("name"));
	    onOff = row.getInt(row.getColumnIndex("onOff"));
	    time = Calendar.getInstance();
	    time.setTime(new Date(row.getLong(row.getColumnIndex("time"))));
	    bar.setProgress(row.getInt(row.getColumnIndex("volume")));
	    daysSelected = row.getInt(row.getColumnIndex("days"));
	    snoozeMinutes = row.getInt(row.getColumnIndex("snoozeMinutes"));
	    rampingMinutes = row.getInt(row.getColumnIndex("rampingMinutes"));
	    rampingSeconds = row.getInt(row.getColumnIndex("rampingSeconds"));
	    playlistURI = row.getString(row.getColumnIndex("playlistURI"));
	    playlistName = row.getString(row.getColumnIndex("playlistName"));
	    row.close();
	    database.close();
	}
	
	@Override
	public void finish(){
		updateAlarm();
		setAlarmResult();
	    super.finish();
	}
	
	@Override
	protected void setAlarmResult(){
		// Create result Intent
		Intent data = getIntent();
		long id = data.getLongExtra("ID", -1);
		Log.i("infodb","CREATING RESULT INTENT " + id);
	    Intent result = new Intent();
	    result.putExtra("ID", id);
	    result.putExtra("ON", onOff);
	    setResult(RESULT_OK, result);
	}
	
	protected void updateAlarm(){
		// Get id
		Intent data = getIntent();
		long id = data.getLongExtra("ID", -1);
		if (id == -1)
			return;
	    // Add to database
		AlarmDatabase database = new AlarmDatabase(this);
		SQLiteDatabase write = database.getWritableDatabase();
		ContentValues values = new ContentValues(4);
		values.put("name", name);
		values.put("time", time.getTimeInMillis());
		values.put("days", daysSelected);
		values.put("snoozeMinutes", snoozeMinutes);
		values.put("rampingMinutes", rampingMinutes);
		values.put("rampingSeconds", rampingSeconds);
		SeekBar bar = (SeekBar) findViewById(R.id.volume_edit);
		values.put("volume", bar.getProgress());
		values.put("playlistURI", playlistURI);
		values.put("playlistName", playlistName);
		write.update(AlarmDatabase.ALARMS_TABLE, values, "_id = " + id, null);
		write.close();
		database.close();
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
			case R.id.done_edit:
				finish();
				return true;
			case R.id.delete:
				Intent data = getIntent();
				final long id = data.getLongExtra("ID", -1);
				if (id == -1)
					return true;
				final Activity activity = this;
				AlertDialog.Builder dialogBuild = new AlertDialog.Builder(this);
			    dialogBuild.setTitle("WARNING");
			    dialogBuild.setMessage("Are you sure you wish to remove this alarm?");
			    dialogBuild.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			        	AlarmDatabase database = new AlarmDatabase(activity);
						SQLiteDatabase write = database.getWritableDatabase();
						write.delete(AlarmDatabase.ALARMS_TABLE, "_id = " + id, null);
						write.close();
						database.close();
						finish();
			        }
			    });
			    dialogBuild.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			            // Do nothing.
			        }
			    });
			    AlertDialog dialog = dialogBuild.create();
			    dialog.show();
				return true;
				
      }
      return super.onOptionsItemSelected(item);
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_edit_alarm, menu);
        return true;
    }
	
}
