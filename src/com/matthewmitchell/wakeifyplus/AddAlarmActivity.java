//  Copyright (c) 2014 Matthew Mitchell
//
//  This file is part of Wakeify for Playlists. It is subject to the license terms
//  in the LICENSE file found in the top-level directory of this
//  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
//  No part of Wakeify for Playlists, including this file, may be copied, modified,
//  propagated, or distributed except according to the terms contained in the LICENSE file.

package com.matthewmitchell.wakeifyplus;

import com.matthewmitchell.wakeifyplus.R;
import com.matthewmitchell.wakeifyplus.database.AlarmDatabase;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.widget.SeekBar;

public class AddAlarmActivity extends AlarmModifierBase {
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.done_edit:
				// Add to database
				AlarmDatabase database = new AlarmDatabase(this);
				SQLiteDatabase write = database.getWritableDatabase();
				ContentValues values = new ContentValues(4);
				values.put("name", name);
				values.put("time", time.getTimeInMillis());
				values.put("days", daysSelected);
				SeekBar bar = (SeekBar) findViewById(R.id.volume_edit);
				values.put("volume", bar.getProgress());
				values.put("onOff", 0);
				values.put("snoozeMinutes", snoozeMinutes);
				values.put("rampingMinutes", rampingMinutes);
				values.put("rampingSeconds", rampingSeconds);
				values.put("playlistURI", playlistURI);
				values.put("playlistName", playlistName);
				write.insert(AlarmDatabase.ALARMS_TABLE, null, values);
				write.close();
				database.close();
				finish();
				return true;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_add_alarm, menu);
        return true;
    }
	
}
