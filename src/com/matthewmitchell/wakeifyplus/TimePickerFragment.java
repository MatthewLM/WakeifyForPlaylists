//  Copyright (c) 2014 Matthew Mitchell
//
//  This file is part of Wakeify for Playlists. It is subject to the license terms
//  in the LICENSE file found in the top-level directory of this
//  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
//  No part of Wakeify for Playlists, including this file, may be copied, modified,
//  propagated, or distributed except according to the terms contained in the LICENSE file.

package com.matthewmitchell.wakeifyplus;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.text.format.DateFormat;

@SuppressLint("ValidFragment")
public class TimePickerFragment extends DialogFragment{
	
	private Activity activity;
	private int hour;
	private int minute;
	
	public TimePickerFragment(Activity activity,int hour,int minute){
		super();
		this.activity = activity;
		this.hour = hour;
		this.minute = minute;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), (OnTimeSetListener) activity, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

}
