//  Copyright (c) 2014 Matthew Mitchell
//
//  This file is part of Wakeify for Playlists. It is subject to the license terms
//  in the LICENSE file found in the top-level directory of this
//  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
//  No part of Wakeify for Playlists, including this file, may be copied, modified,
//  propagated, or distributed except according to the terms contained in the LICENSE file.

package com.matthewmitchell.wakeifyplus;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

public class ReportProblem {
	static void reportProblem(Context c){
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
  	  	emailIntent.setType("plain/text");
  	  	String to[] = {"matthewmitchell@thelibertyportal.com"};
  	  	emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, to); 
  	  	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "WAKEIFY FOR PLAYLISTS ISSUE: ");
  	  	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "App Version:\nDevice Model:\nAndroid Version:\n\nPlease explain here what problem you are having. Please describe your precise settings for the alarm and what causes the problem if you know."); 
  	  	try{
  	  		c.startActivity(emailIntent);
  	  	}catch (ActivityNotFoundException e){
  	  	}finally{}
	}
}
