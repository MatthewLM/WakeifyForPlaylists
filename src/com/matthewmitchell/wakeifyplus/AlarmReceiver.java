//  Copyright (c) 2014 Matthew Mitchell
//
//  This file is part of Wakeify for Playlists. It is subject to the license terms
//  in the LICENSE file found in the top-level directory of this
//  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
//  No part of Wakeify for Playlists, including this file, may be copied, modified,
//  propagated, or distributed except according to the terms contained in the LICENSE file.

package com.matthewmitchell.wakeifyplus;

import java.sql.Date;
import java.util.Calendar;

import com.matthewmitchell.wakeifyplus.database.AlarmDatabase;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.net.Uri;

public class AlarmReceiver extends BroadcastReceiver {
	
	public static void addAlarm(Context context, long id, long time){
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context,AlarmReceiver.class);
		intent.putExtra("ID", id);
		// THE REQUEST CODE IS IMPLEMENTED. THE DOCS ARE STUPID. ALLOWS FOR UNIQUE PENDING INTENTS
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,(int)id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
	}
	
	public static void removeAlarm(Context context, long id){
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context,AlarmReceiver.class);
		intent.putExtra("ID", id);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,(int)id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
		am.cancel(pendingIntent);
	}
	
	public static void scheduleAlarm(Context context, long id, boolean remove){
		// Obtain time and days from database
		AlarmDatabase database = new AlarmDatabase(context);
		SQLiteDatabase write = database.getWritableDatabase();
		Cursor row = write.query(AlarmDatabase.ALARMS_TABLE,
				new String[]{"time","days"},
				"_id = " + id, null,null,null,null);
		if (!row.moveToFirst()){
			row.close();
			write.close();
			database.close();
			return;
		}
		long time = row.getLong(row.getColumnIndex("time"));
		int days = row.getInt(row.getColumnIndex("days"));
		row.close();
		write.close();
		database.close();
		
		// Create hours minutes for the alarm time 
		Calendar ctime = Calendar.getInstance();
		ctime.setTime(new Date(time));
		int hours = ctime.get(Calendar.HOUR_OF_DAY);
		int minutes = ctime.get(Calendar.MINUTE);
		
		// Remove existing alarm if asked
		if (remove)
			AlarmReceiver.removeAlarm(context, id);
		
		Calendar now = Calendar.getInstance();
		int next = 0;
		// Find movement to the alarm time
		next += 1000 * 60 * 60 * (hours - now.get(Calendar.HOUR_OF_DAY));
		next += 1000 * 60 * (minutes - now.get(Calendar.MINUTE));
		next -= 1000 * now.get(Calendar.SECOND);
		int nextDay = 0;
		if (next <= 0){
			nextDay = 1;
			next += 24 * 60 * 60 * 1000;
		}
		if (days != 0){
			// Find next day we can use
			int day = 0;
			switch (now.get(Calendar.DAY_OF_WEEK)){
    			case Calendar.MONDAY:
    				day = 0 + nextDay;
    				break;
    			case Calendar.TUESDAY:
    				day = 1 + nextDay;
    				break;
    			case Calendar.WEDNESDAY:
    				day = 2 + nextDay;
    				break;
    			case Calendar.THURSDAY:
    				day = 3 + nextDay;
    				break;
    			case Calendar.FRIDAY:
    				day = 4 + nextDay;
    				break;
    			case Calendar.SATURDAY:
    				day = 5 + nextDay;
    				break;
    			case Calendar.SUNDAY:
    				if (nextDay == 1)
    					day = 0;
    				else
    					day = 6;
    				break;
			}
			int fday = day;
			for (int x = 0; x < 7; x++){
				if ((days & (1 << day)) != 0){
					// Got a day
					break;
				}
				if (day == 6)
					day = 0;
				else
					day++;
			}
			next += 24 * 60 * 60 * 1000 * (day - fday);
			if (day < fday)
				next += 7 * 24 * 60 * 60 * 1000;
		}
		AlarmReceiver.addAlarm(context, id, now.getTimeInMillis() + next);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		StaticWakeLock.acquireWakeLock(context);
        long id = intent.getLongExtra("ID", -1);
        if (id != -1){
            // Open AlarmPlayActivity
            Intent alarmPlay = new Intent(context, AlarmPlayActivity.class);
        	alarmPlay.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	alarmPlay.putExtra("ID", id);
            // Get days information to see if another alarm should be scheduled.
        	AlarmDatabase database = new AlarmDatabase(context);
            SQLiteDatabase write = database.getWritableDatabase();
            Cursor row = write.query(AlarmDatabase.ALARMS_TABLE,
            		new String[]{"days","time","volume"},
            		"_id = " + id, null, null, null, null);
            if (row.moveToFirst()){
                // Get days
        		int days = row.getInt(row.getColumnIndex("days"));
        		if (days == 0) {
        			// Turn off alarm in database and do not schedule another
        			ContentValues values = new ContentValues(1);
        			values.put("onOff", 0);
        			write.update(AlarmDatabase.ALARMS_TABLE, values, "_id = " + id, null);
        		}else{
        			// Schedule another alarm.
        			Calendar time = Calendar.getInstance();
        		    time.setTime(new Date(row.getLong(row.getColumnIndex("time"))));
        			AlarmReceiver.scheduleAlarm(context, id, false);
        		}
        		// Change system volume
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            	audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    		(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * row.getInt(row.getColumnIndex("volume"))) / 100
                    		, 0);
            }
            // Now play alarm
        	context.startActivity(alarmPlay);
            row.close();
            write.close();
            database.close();
        }
	}
}
