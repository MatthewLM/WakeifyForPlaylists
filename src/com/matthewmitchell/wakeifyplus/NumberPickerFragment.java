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
import android.widget.NumberPicker;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class NumberPickerFragment extends DialogFragment{
	
	private AlarmModifierBase activity;
	private int defaultNum;
	
	public NumberPickerFragment(AlarmModifierBase activity,int defaultNum){
		super();
		this.activity = activity;
		this.defaultNum = defaultNum;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		final NumberPicker np = new NumberPicker(activity);
		np.setMaxValue(60);
		np.setMinValue(1);
		np.setValue(defaultNum);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Set Snooze Minutes")
        	   .setView(np)
               .setPositiveButton("Set", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					activity.snoozeMinutes = np.getValue();
					TextView edit = (TextView) activity.findViewById(R.id.snooze_edit);
				    edit.setText("" + activity.snoozeMinutes);
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
