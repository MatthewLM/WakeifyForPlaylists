//  Copyright (c) 2014 Matthew Mitchell
//
//  This file is part of Wakeify for Playlists. It is subject to the license terms
//  in the LICENSE file found in the top-level directory of this
//  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
//  No part of Wakeify for Playlists, including this file, may be copied, modified,
//  propagated, or distributed except according to the terms contained in the LICENSE file.

package com.matthewmitchell.wakeifyplus.database;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.matthewmitchell.wakeifyplus.MainActivity;
import com.matthewmitchell.wakeifyplus.R;
import com.matthewmitchell.wakeifyplus.AlarmReceiver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class AlarmCursorAdapter extends SimpleCursorAdapter {
	
    private int layout;
    private SimpleDateFormat date_format = new SimpleDateFormat("HH:mm");
    private MainActivity main;

    public AlarmCursorAdapter(MainActivity main, int layout, Cursor c, String[] from, int[] to) {
		super(main, layout, c, from, to, 0);
        this.layout = layout;
        this.main = main;
	}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        Cursor c = getCursor();
        
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(layout, parent, false);
        
        bindView(v,context,c);

        return v;
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {

    	final String name = c.getString(c.getColumnIndex("name"));
    	final long time = c.getLong(c.getColumnIndex("time"));
  
    	TextView time_text = (TextView) v.findViewById(R.id.time);
        if (time_text != null) {
            time_text.setText(date_format.format(new Date(time)));
        }
    	
        TextView name_text = (TextView) v.findViewById(R.id.name);
        if (name_text != null) {
            name_text.setText(name);
        }
        
        // Do on/off button alarm code
        
        CompoundButton onOff = (CompoundButton)v.findViewById(R.id.alarm_switch);
        // Make sure the change listener is not set for setting value from database.
        onOff.setOnCheckedChangeListener(null);
        onOff.setChecked(c.getInt(c.getColumnIndex("onOff")) == 1);
        final Context contextl = context;
        final long id = c.getInt(c.getColumnIndex("_id"));
        onOff.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				AlarmDatabase database = new AlarmDatabase(contextl);
				SQLiteDatabase write = database.getWritableDatabase();
				ContentValues values = new ContentValues(1);
				if (isChecked){
					// Alarm has been turned on
					values.put("onOff",1);
					// Add system alarm
					AlarmReceiver.scheduleAlarm(contextl, id, false);
				}else{
					// Alarm has been switched off
					values.put("onOff",0);
					AlarmReceiver.removeAlarm(contextl, id);
				}
				write.update(AlarmDatabase.ALARMS_TABLE, values, "_id = " + id, null);
				write.close();
				database.close();
				// Reload cursor
				main.getSupportLoaderManager().restartLoader(0, null, main);
			}
        	
        });
    }
    
}
