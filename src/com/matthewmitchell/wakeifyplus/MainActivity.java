//  Copyright (c) 2014 Matthew Mitchell
//
//  This file is part of Wakeify for Playlists. It is subject to the license terms
//  in the LICENSE file found in the top-level directory of this
//  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
//  No part of Wakeify for Playlists, including this file, may be copied, modified,
//  propagated, or distributed except according to the terms contained in the LICENSE file.

package com.matthewmitchell.wakeifyplus;

import java.io.File;
import java.sql.Date;
import java.util.Calendar;

import com.matthewmitchell.wakeifyplus.R;
import com.matthewmitchell.wakeifyplus.ServiceBinder.ServiceBinderDelegate;
import com.matthewmitchell.wakeifyplus.SpotifyService.LogoutDelegate;
import com.matthewmitchell.wakeifyplus.contentprovider.AlarmContentProvider;
import com.matthewmitchell.wakeifyplus.database.AlarmCursorAdapter;
import com.matthewmitchell.wakeifyplus.database.AlarmDatabase;

import android.net.Uri;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.support.v4.content.CursorLoader;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MainActivity extends SherlockFragmentActivity implements LoaderCallbacks<Cursor> {
	
	public SimpleCursorAdapter mAdapter;
	
	private ServiceBinder binder;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Bind to the spotify service since it is required by the app.
        binder = new ServiceBinder(this);
        binder.incRef(ServiceBinder.MAIN);
		binder.bindService(new ServiceBinderDelegate() {

			@Override
			public void onIsBound() {
			}
			
		});
        if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
            ArrayListFragment list = new ArrayListFragment();
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, list).commit();
        }
        getSupportLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    public void onPause() {
		if (binder != null)
			binder.doUnbindService();
		if (isFinishing())
			binder.decRef(ServiceBinder.MAIN);
		super.onPause();
	}
    
    @Override
    public void onPostResume(){
    	getSupportLoaderManager().restartLoader(0, null, this);
    	super.onPostResume();
    }
    
    @Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data){
		getSupportLoaderManager().restartLoader(0, null, this);
		Log.i("infodb","GOT RESULT " + requestCode);
		if (requestCode == 1){
			// If alarm is on, turn off and on again
			Log.i("infodb","GOT ON " + data.getIntExtra("ON", -1));
			if (data.getIntExtra("ON", 0) == 1){
				// Alarm is on
				long id = data.getLongExtra("ID",-1);
				Log.i("infodb","GOT ID " + id);
				if (id != -1)
					AlarmReceiver.scheduleAlarm(this, id, true);
			}
		}
    }
    
    public static class ArrayListFragment extends ListFragment {
    	
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            update();
        }
        
        public void update(){
        	// For the cursor adapter, specify which columns go into which views
            String[] fromColumns = {"time","name","onOff"};
            int[] toViews = {R.id.time,R.id.name,R.id.alarm_switch};

            // Create an empty adapter we will use to display the loaded data.
            // We pass null for the cursor, then update it in onLoadFinished()
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.mAdapter = new AlarmCursorAdapter(mainActivity, 
                    R.layout.alarm_row, null,
                    fromColumns, toViews);
            setListAdapter(mainActivity.mAdapter); 
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
          View view = inflater.inflate(R.layout.activity_main,
              container, false);
          return view;
        }
        
        @Override 
        public void onListItemClick(ListView l, View v, int position, long id) {
        	Intent editAlarm = new Intent(getActivity(), EditAlarmActivity.class);
        	editAlarm.putExtra("ID", id);
        	getActivity().startActivityForResult(editAlarm, 1);
        }
    }
    
    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this, AlarmContentProvider.CONTENT_URI,
        		new String[] {"_id","time","name","onOff"}, "", null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.add_alarm:
    	  startActivityForResult(new Intent(this, AddAlarmActivity.class),0);
    	  return true;
      case R.id.logout:
    	  binder.getService().logout(new LogoutDelegate(){

			@Override
			public void onLogout() {
				// Forget user.
	        	String path = getExternalFilesDir(null).getAbsolutePath();
	        	path += "/blob.dat";
	        	Log.i("infodb", "Deleting " + path);
	        	File blob = new File(path);
	        	blob.delete();
	        	// Go to login activity
	        	Intent intent = new Intent(MainActivity.this, LoginActivity.class);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	        	startActivity(intent);
			}
    		  
    	  });
    	  /*// Forget user.
        	String path = getExternalFilesDir(null).getAbsolutePath();
        	path += "/blob.dat";
        	Log.i("infodb", "Deleting " + path);
        	File blob = new File(path);
        	blob.delete();
        	// Restart the app. Spotify logout does not function so we need a hack
        	Intent mStartActivity = new Intent(this, LoginActivity.class);
        	int mPendingIntentId = 123456;
        	PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        	AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        	mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        	System.exit(0);*/
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

	@Override
	public void onLoadFinished(Loader<Cursor> cursor, Cursor data) {
		// Swap the new cursor in.  (The framework will take care of closing the old cursor once we return.)
        mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
	}
    
}
