//  Copyright (c) 2014 Matthew Mitchell
//
//  This file is part of Wakeify for Playlists. It is subject to the license terms
//  in the LICENSE file found in the top-level directory of this
//  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
//  No part of Wakeify for Playlists, including this file, may be copied, modified,
//  propagated, or distributed except according to the terms contained in the LICENSE file.

package com.matthewmitchell.wakeifyplus;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class StaticWakeLock {
	private static WakeLock wl = null;
	
	public static void acquireWakeLock(Context context){
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "");
        wl.acquire();
	}
	
	public static void releaseWakeLock(){
		if (wl != null && wl.isHeld())
			wl.release();
	}
	
}
