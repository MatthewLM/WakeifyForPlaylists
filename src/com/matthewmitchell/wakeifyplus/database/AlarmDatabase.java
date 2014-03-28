//  Copyright (c) 2014 Matthew Mitchell
//
//  This file is part of Wakeify for Playlists. It is subject to the license terms
//  in the LICENSE file found in the top-level directory of this
//  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
//  No part of Wakeify for Playlists, including this file, may be copied, modified,
//  propagated, or distributed except according to the terms contained in the LICENSE file.

package com.matthewmitchell.wakeifyplus.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlarmDatabase extends SQLiteOpenHelper{

	private static final int DATABASE_VERSION = 1;
	public static String ALARMS_TABLE = "alarms";
	
	public AlarmDatabase(Context context) {
		super(context, "alarm_database", null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + ALARMS_TABLE + " (" +
				"_id INTEGER PRIMARY KEY," +
				"name TEXT," +
				"time INTEGER," +
				"days INTEGER," +
				"volume INTEGER," +
				"onOff INTEGER," +
				"rampingMinutes INTEGER," +
				"rampingSeconds INTEGER," +
				"snoozeMinutes INTEGER," +
				"playlistURI TEXT," +
				"playlistName TEXT" +
				");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
