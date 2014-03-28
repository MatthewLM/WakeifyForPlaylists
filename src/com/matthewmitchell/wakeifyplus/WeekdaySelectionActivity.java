//  Copyright (c) 2014 Matthew Mitchell
//
//  This file is part of Wakeify for Playlists. It is subject to the license terms
//  in the LICENSE file found in the top-level directory of this
//  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
//  No part of Wakeify for Playlists, including this file, may be copied, modified,
//  propagated, or distributed except according to the terms contained in the LICENSE file.

package com.matthewmitchell.wakeifyplus;

import com.actionbarsherlock.app.SherlockActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.matthewmitchell.wakeifyplus.R;

import android.view.View;
import android.widget.CheckBox;

public class WeekdaySelectionActivity extends SherlockActivity {
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		overridePendingTransition(R.anim.pull_in, R.anim.hold);
	    setContentView(R.layout.weekday_edit);
	    // Select days and suppress the warning. We know we are using Days
	    CheckBox box;
	    box = (CheckBox)findViewById(R.id.mondayBox);
    	box.setChecked(this.getIntent().getBooleanExtra("Monday", false));
	    box = (CheckBox)findViewById(R.id.tuesdayBox);
	    box.setChecked(this.getIntent().getBooleanExtra("Tuesday", false));
	    box = (CheckBox)findViewById(R.id.wednesdayBox);
	    box.setChecked(this.getIntent().getBooleanExtra("Wednesday", false));
	    box = (CheckBox)findViewById(R.id.thursdayBox);
	    box.setChecked(this.getIntent().getBooleanExtra("Thursday", false));
	    box = (CheckBox)findViewById(R.id.fridayBox);
	    box.setChecked(this.getIntent().getBooleanExtra("Friday", false));
	    box = (CheckBox)findViewById(R.id.saturdayBox);
	    box.setChecked(this.getIntent().getBooleanExtra("Saturday", false));
	    box = (CheckBox)findViewById(R.id.sundayBox);
	    box.setChecked(this.getIntent().getBooleanExtra("Sunday", false));
	}
	
	@Override
	protected void onPause(){
		overridePendingTransition(R.anim.hold, R.anim.push_out);
		super.onPause();
	}
	
	public void updateResult(View v){
		Intent result = new Intent();
		result.putExtra("Monday", ((CheckBox)findViewById(R.id.mondayBox)).isChecked());
		result.putExtra("Tuesday", ((CheckBox)findViewById(R.id.tuesdayBox)).isChecked());
		result.putExtra("Wednesday", ((CheckBox)findViewById(R.id.wednesdayBox)).isChecked());
		result.putExtra("Thursday", ((CheckBox)findViewById(R.id.thursdayBox)).isChecked());
		result.putExtra("Friday", ((CheckBox)findViewById(R.id.fridayBox)).isChecked());
		result.putExtra("Saturday", ((CheckBox)findViewById(R.id.saturdayBox)).isChecked());
		result.putExtra("Sunday", ((CheckBox)findViewById(R.id.sundayBox)).isChecked());
		setResult(RESULT_OK, result);
	}
	
	public void onMondayClick(View v){
		CheckBox box = (CheckBox)findViewById(R.id.mondayBox);
		box.performClick();
		updateResult(null);
	}
	
	public void onTuesdayClick(View v){
		CheckBox box = (CheckBox)findViewById(R.id.tuesdayBox);
		box.performClick();
		updateResult(null);
	}
	
	public void onWednesdayClick(View v){
		CheckBox box = (CheckBox)findViewById(R.id.wednesdayBox);
		box.performClick();
		updateResult(null);
	}
	public void onThursdayClick(View v){
		CheckBox box = (CheckBox)findViewById(R.id.thursdayBox);
		box.performClick();
		updateResult(null);
	}
	
	public void onFridayClick(View v){
		CheckBox box = (CheckBox)findViewById(R.id.fridayBox);
		box.performClick();
		updateResult(null);
	}
	
	public void onSaturdayClick(View v){
		CheckBox box = (CheckBox)findViewById(R.id.saturdayBox);
		box.performClick();
		updateResult(null);
	}
	
	public void onSundayClick(View v){
		CheckBox box = (CheckBox)findViewById(R.id.sundayBox);
		box.performClick();
		updateResult(null);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_weekday_seletion, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.done_weekday_selection:
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
	
}
