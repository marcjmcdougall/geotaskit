package com.cis6930.geotaskit.service;

import com.cis6930.geotaskit.MainActivity;
import com.cis6930.geotaskit.PopupActivity;
import com.cis6930.geotaskit.R;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class LocationNotificationService extends Service {

  // private int MINUTES = 1000 * 60 * 5; // (1 sec in milliseconds) * (1 minute
  // in seconds) * (how many minutes)
  private int METERS = 200;

  private int NOTIFICATION_ID = 1;

  private LocationManager locationManager;
  private LocationListener loc_listener;

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

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
        Log.i("#info", "Loc ( " + location.getLatitude() + " , " + location.getLongitude() + " )");
        compareToTasks(location);
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
    final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    // if the gps is disabled by settings then notify the user and prompt the
    // settings/location screen
    if (!gpsEnabled) {
      Log.e("#info", "gps is disabled");
      enableLocationSettings();
    }
    else {

      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

      int minutes = Integer.parseInt(prefs.getString(getString(R.string.pref_user_frequency_key), getString(R.string.pref_user_frequency_default)));

      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minutes * 60 * 1000, METERS, loc_listener);
    }

    // return STICKY to maintain the service running
    return Service.START_STICKY;
  }

  private void enableLocationSettings() {
    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    startActivity(settingsIntent);
  }

  private void compareToTasks(Location location) {
    // Get stored tasks from the local database that are inside the notification
    // radius
    // Compare to Location argument
    // if within reminder distance
    // if single task
    // title set as "Nearby:" + task.name
    // body set as task.description
    // else
    // title set as # + "pending tasks nearby"
    // body: for(t:tasks){ body += ", "+ task.name }
    // create/update notification OR pop-up dialog (DEPENDS ON THE PROPERTY TYPE
    // OF ALERT)

    // --------------- AFTER THIS, A NOTIFICATION IS DISPLAYED/UPDATED (WHENEVER
    // SET BY THE USER) Delete this comment after following above struture

    // create dummy notification
    NotificationCompat.Builder notif = new NotificationCompat.Builder(this);
    // set basic elements to the builder
    notif.setSmallIcon(R.drawable.notif);
    notif.setContentTitle("# Task(s) nearby!");
    notif.setContentText("Buy things, Do stuff, Sleep, Eat, Whatever, Damn you Jane");
    notif.setTicker("GeoTaskIt - # Task(s) nearby!");
    notif.setAutoCancel(true);

    // Creates an explicit intent for an Activity in your app
    Intent resultIntent = new Intent(this, MainActivity.class);

    // The stack builder object will contain an artificial back stack for the
    // started Activity.
    // This ensures that navigating backward from the Activity leads out of
    // your application to the Home screen.
    // TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

    // Adds the back stack for the Intent (but not the Intent itself)
    // stackBuilder.addParentStack(MainActivity.class);

    // Adds the Intent that starts the Activity to the top of the stack
    // stackBuilder.addNextIntent(resultIntent);
    /*
     * PendingIntent resultPendingIntent = stackBuilder.getPendingIntent( 0,
     * PendingIntent.FLAG_UPDATE_CURRENT );
     */

    // set the activity that will be called when notification is clicked
    // notif.setContentIntent(resultPendingIntent);
    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    // create the notification (or update with the id)
    // mNotificationManager.notify(NOTIFICATION_ID, notif.build());

    // --------------- AFTER THIS, THE POP-UP DIALOG IS DISPLAYED (WHENEVER SET
    // BY THE USER)

    // create and call the pop-up reminder (dialog-themed activity) for
    // important tasks
    Intent i = new Intent(this, PopupActivity.class);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
    startActivity(i);
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
