//  Copyright (c) 2014 Matthew Mitchell
//
//  This file is part of Wakeify for Playlists. It is subject to the license terms
//  in the LICENSE file found in the top-level directory of this
//  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
//  No part of Wakeify for Playlists, including this file, may be copied, modified,
//  propagated, or distributed except according to the terms contained in the LICENSE file.

package com.matthewmitchell.wakeifyplus;

import com.matthewmitchell.wakeifyplus.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TableRow;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class MinutesSecondsFragment extends DialogFragment {

	private AlarmModifierBase activity;
	private int defaultMinute;
	private int defaultSecond;
	
	public  MinutesSecondsFragment(AlarmModifierBase activity,int defaultMinute,int defaultSecond){
		super();
		this.activity = activity;
		this.defaultMinute = defaultMinute;
		this.defaultSecond = defaultSecond;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		TableRow.LayoutParams twoLP = new TableRow.LayoutParams(0, 0, 0.2f);
		TableRow.LayoutParams threeLP = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0.3f);
		
		View spacer = new View(activity);
		spacer.setLayoutParams(twoLP);
		spacer.setVisibility(View.INVISIBLE);
		
		final NumberPicker minutes = new NumberPicker(activity);
		minutes.setMaxValue(30);
		minutes.setMinValue(0);
		minutes.setValue(defaultMinute);
		minutes.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		
		LinearLayout minutesLayout = new LinearLayout(activity);
		minutesLayout.addView(minutes);
		minutesLayout.setGravity(Gravity.CENTER);
		minutesLayout.setLayoutParams(threeLP);
		
		final NumberPicker seconds = new NumberPicker(activity);
		seconds.setMaxValue(59);
		seconds.setMinValue(0);
		seconds.setValue(defaultSecond);
		seconds.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
	
		LinearLayout secondsLayout = new LinearLayout(activity);
		secondsLayout.addView(seconds);
		secondsLayout.setGravity(Gravity.CENTER);
		secondsLayout.setLayoutParams(threeLP);
		
		LinearLayout layout = new LinearLayout(activity);
		layout.addView(spacer);
		layout.addView(minutesLayout);
		layout.addView(secondsLayout);
		layout.setWeightSum(1.0f);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Set Volume Ramping Time")
        	   .setView(layout)
               .setPositiveButton("Set", new DialogInterface.OnClickListener() {
				
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					activity.rampingMinutes = minutes.getValue();
    					activity.rampingSeconds = seconds.getValue();
    					TextView edit = (TextView) activity.findViewById(R.id.volume_ramping);
    				    edit.setText(activity.rampingMinutes + "m" + activity.rampingSeconds + "s");
    				}
    				
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Do nothing.
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
        
    }
	
}
