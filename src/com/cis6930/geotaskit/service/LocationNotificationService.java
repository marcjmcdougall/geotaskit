package com.cis6930.geotaskit.service;

import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.cis6930.geotaskit.MainActivity;
import com.cis6930.geotaskit.PopupActivity;
import com.cis6930.geotaskit.R;
import com.cis6930.geotaskit.Task;
import com.cis6930.geotaskit.backends.DatabaseInterface;

public class LocationNotificationService extends Service {

	private int METERS_REMINDER = 1000;
	private int NOTIFICATION_ID = 1;
	private LocationManager locationManager;
	private LocationListener loc_listener;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		// Obtain the preferences of the user on how frequent to check for the location
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean toReschedule = prefs.getBoolean(getString(R.string.pref_user_autoupdate_key), false);

		// Check whether the user changed the preference or not, if so, stop
		if(!toReschedule)
			stopSelf();
		
		// Location listener that will be called whenever the LocationManager
		// returns a request of updated location
		loc_listener = new LocationListener() {
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onLocationChanged(Location location) {
				// Once got the location stop tracking location
				locationManager.removeUpdates(loc_listener);

				Log.i("#info", "Loc ( " + location.getLatitude() + " , " + location.getLongitude() + " )");

				// Compare tasks with location obtained
				compareToTasks(location);

				// Reschedule service to run as set by the user
				rescheduleCheck();

				// Now this service can end
				stopSelf();
			}
		};
		/* START REQUESTING LOCATION UPDATES */
		// This verification should be done during onStart() because the system
		// calls
		// this method when the user returns to the activity, which ensures the
		// desired
		// location provider is enabled each time the activity resumes from the
		// stopped state.

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Set criteria with fine accuracy and helping variables to obtain and use providers
		boolean isCapable = true;
		String provider = null;
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		List<String> list_providers = null;

		// Get all enabled providers with fine accuracy
		list_providers = locationManager.getProviders(criteria, true);

		/* If no provider was found then try with no accuracy requirement and if the same case, set as not capable via isCapable flag
		 * Nothing to do if there are no providers, notify user
		 */
		if(list_providers == null){
			criteria.setAccuracy(Criteria.NO_REQUIREMENT);
			list_providers = locationManager.getProviders(criteria, true);
			if(list_providers == null)
				isCapable = false;
		}


		// If is capable
		if(isCapable){

			// Select first (or only) provider 
			provider = list_providers.get(0);

			// Get location with selected [enabled] provider (once location is received in listener, stop updates, reschedule service, and kill service
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, loc_listener);

		}else{
			// Notify the user there are no means to get location, suggest enabling (try to open activity), and end service
			Toast.makeText(LocationNotificationService.this, getString(R.string.error_location_providers_not_found), Toast.LENGTH_LONG).show();
			enableLocationSettings();
			stopSelf();
		}

		// return STICKY to maintain the service running
		return Service.START_STICKY;
	}

	private void rescheduleCheck(){

		// Obtain the preferences of the user on how frequent to check for the location
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int minutes = Integer.parseInt(prefs.getString(getString(R.string.pref_user_frequency_key), getString(R.string.pref_user_frequency_default)));
		long now = System.currentTimeMillis();
		
		// Set the intent that will start the service
		Intent intent = new Intent(LocationNotificationService.this, LocationNotificationService.class);
		PendingIntent pi = PendingIntent.getService(LocationNotificationService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Set through the AlarmManager to start previously-set intent in the stated period + current time
		AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, now + (minutes * 60 * 1000), pi);
	}

	private void enableLocationSettings() {
		try{
			Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(settingsIntent);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void compareToTasks(Location location) {
		// Get stored tasks from the local database that are inside the notification
		DatabaseInterface db = new DatabaseInterface(LocationNotificationService.this);
		List<Task> list_tasks = db.getTasks();
		
		
		String notification_message = null;
		
		int counter_nearby = 0;
		// Compare distance and tasks
		for(Task task : list_tasks){
			
			// Get distance between current location and task
			float[] results = new float[3];
			Location.distanceBetween(task.latitude, task.longitude, location.getLatitude(), location.getLongitude(), results);
			
			System.out.println("Distance with "+task.name+": "+results[0]);
			
			// If task is within distance of reminder, then add to String of reminders to the notification
			if(results[0] <= METERS_REMINDER){
				counter_nearby++;
				
				if(notification_message == null)
					notification_message = task.name;
				else
					notification_message += ", " + task.name;
			}
		}
		
		// If there are no tasks nearby then return
		if(counter_nearby < 0)
			return;
		
		// Set title of notification
		String notification_title = counter_nearby + " task(s) nearby";

		// create/update notification OR pop-up dialog (DEPENDS ON THE PROPERTY TYPE
		// OF ALERT)
		
		NotificationCompat.Builder notif = new NotificationCompat.Builder(this);
		// set basic elements to the builder
		notif.setSmallIcon(R.drawable.notif);
		notif.setContentTitle(notification_title);
		notif.setContentText(notification_message);
		notif.setTicker("GeoTaskIt: "+notification_title);
		notif.setAutoCancel(true);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT );
		
		// set the activity that will be called when notification is clicked
		notif.setContentIntent(resultPendingIntent);
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, notif.build());

		// --------------- AFTER THIS, THE POP-UP DIALOG IS DISPLAYED (WHENEVER SET
		// BY THE USER)
		// create and call the pop-up reminder (dialog-themed activity) for
		// important tasks
		/*
		Intent i = new Intent(this, PopupActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		startActivity(i);
		*/
	}

	@Override
	public void onDestroy() {
		// if destroy by the system then stop requesting location updates (it will
		// be restored once the service is restarted when possible)
		/* STOP REQUESTING LOCATION UPDATES */
		locationManager.removeUpdates(loc_listener);
		super.onDestroy();
	}
}
