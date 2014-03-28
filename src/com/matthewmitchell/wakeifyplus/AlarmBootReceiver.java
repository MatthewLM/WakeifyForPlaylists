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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AlarmBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		// Schedule alarms
		AlarmDatabase database = new AlarmDatabase(context);
		SQLiteDatabase read = database.getReadableDatabase();
		Cursor alarms = read.query(AlarmDatabase.ALARMS_TABLE,
				new String[]{"_id"}, "onOff = 1", null, null, null, null);
		if (alarms.moveToFirst()){
			do{
				AlarmReceiver.scheduleAlarm(context, alarms.getLong(alarms.getColumnIndex("_id")), false);
			}while(alarms.moveToNext());
		}
		alarms.close();
		read.close();
		database.close();
	}

}
