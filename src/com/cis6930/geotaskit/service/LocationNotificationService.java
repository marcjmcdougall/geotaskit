package com.cis6930.geotaskit.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class LocationNotificationService extends Service{
	
	//private int MINUTES = 1000 * 60 * 5;	// (1 sec in milliseconds) * (1 minute in seconds) * (how many minutes)
	private int MINUTES = 5000;
	private int METERS = 100;
	
	private int NOTIFICATION_ID = 1;
	
	private LocationManager locationManager;
	private LocationListener loc_listener;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		
		//Location listener that will be called whenever the LocationManager returns a request of updated location
		loc_listener = new LocationListener() {
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			
			@Override
			public void onProviderEnabled(String provider) {}
			
			@Override
			public void onProviderDisabled(String provider) {}
			
			@Override
			public void onLocationChanged(Location location) {
				Log.i("#info", "Loc ( "+location.getLatitude()+" , "+location.getLongitude()+" )");
				compareToTasks(location);
			}
		};
		
		/*START REQUESTING LOCATION UPDATES*/
		
		// This verification should be done during onStart() because the system calls
		// this method when the user returns to the activity, which ensures the desired
		// location provider is enabled each time the activity resumes from the stopped state.
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		//if the gps is disabled by settings then notify the user and prompt the settings/location screen 
		if (!gpsEnabled) {
			Log.e("#info", "gps is disabled");
			enableLocationSettings();
		}else
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINUTES, METERS, loc_listener);
		
		//return STICKY to maintain the service running
		return Service.START_STICKY;
	}
	
	private void enableLocationSettings() {
	    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	    startActivity(settingsIntent);
	}
	
	private void compareToTasks(Location location){
		// Get stored tasks from the local database that are inside the notification radius
		//Compare to Location argument
		//if within reminder distance
			//if single task
				//title set as "Nearby:" + task.name
				//body set as task.description
			//else
				//title set as # + "pending tasks nearby"
				//body: for(t:tasks){ body += ", "+ task.name } 
			//create or update notification (id, icon, title, body)
		//toast for demonstration purposes
		Toast.makeText(this, "Task nearby", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onDestroy() {
		//if destroy by the system then stop requesting location updates (it will be restored once the service is restarted when possible)
		
		/*STOP REQUESTING LOCATION UPDATES*/
		locationManager.removeUpdates(loc_listener);
		super.onDestroy();
	}
}
