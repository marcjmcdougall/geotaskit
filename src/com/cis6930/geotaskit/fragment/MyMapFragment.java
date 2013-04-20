package com.cis6930.geotaskit.fragment;

/*
 * This class is the Map Fragment that shows all the tasks on a Google Map
 */
// TODO: Add a new task on long click on Map
// TODO: Beautify popup and add functionality on clicking different popup elements
// TODO: How do we fetch the task information from the database?

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cis6930.geotaskit.R;
import com.cis6930.geotaskit.Task;
import com.cis6930.geotaskit.backends.DatabaseInterface;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MyMapFragment extends Fragment implements LocationSource, LocationListener, OnInfoWindowClickListener {

  // create some dummy data for demo

  ArrayList<Task> taskList = null;
  
  private DatabaseInterface db;

  private GoogleMap map;
  private static View view;
  private LayoutInflater inflater;
  private OnLocationChangedListener listener; // right now, just a dummy class
                                              // used to display the dot at the
                                              // location
                                              // if you want actual
                                              // location-tracking, make this
                                              // class do something!
  private HashMap<Marker, MyMapTaskInfo> taskHash; // will be used to retrieve
                                                   // task information when the
                                                   // popup is shown
  private LocationManager locationManager;
  private Criteria locationCriteria;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    this.inflater = inflater;

    // For when we switch back to this fragment after moving away from it
    // Remove the old view first. See http://stackoverflow.com/a/14695397
    if (view != null) {
      ViewGroup parent = (ViewGroup) view.getParent();
      if (parent != null)
        parent.removeView(view);
    }

    try {
      view = inflater.inflate(R.layout.fragment_map, container, false);
    }
    catch (InflateException e) {
      
    	// map is already there, just return view as it is
    }

    return view;
  }

  @Override
  // we use onActivityCreated instead of onCreate because we can't use
  // findFragmentById until the view has been inflated
  public void onActivityCreated(Bundle savedInstanceState) {
   
	  super.onActivityCreated(savedInstanceState);
	  
	  db = new DatabaseInterface(getActivity().getApplicationContext());
	  
    try {
      MapsInitializer.initialize(getActivity());

      if (map == null)
        map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.fragment_map_map)).getMap();
      
      if (map != null) {
        // Currently populated with dummy task data
        // MARC PUT YOUR CODE TO FETCH TASK INFO FROM DB HERE
        // Note that there is more code that you need to change further on down. Please search for
        // your name in the file.
        // Note also that the Task object (Task.java) I received from Armando doesn't have a location in
        // it so I've hardcoded locations in an array. I'm assuming you've added the location to the task class. If so, you
        // can get rid of the locations array and simply init the location from the Task object. The locations array is
        // locationCoordArray
        taskList = db.getTasks();
        taskList.add(new Task(Task.PRIORITY_HIGH, "Take pic for Neeraj", "Take a pic of an aligator and send it to him", "3.2", 29.65133f, -82.342822f));
        taskList.add(new Task(Task.PRIORITY_LOW, "Visit John", "Pay a visit whenever possible", "1.3", 29.650377f, -82.342857f));
//        LatLng[] locationCoordArray = { new LatLng(29.65133, -82.342822), new LatLng(29.650377, -82.342857) };
        
        map.setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng point) {
				
				Toast.makeText(getActivity().getApplicationContext(), "CLICK DETECTED", Toast.LENGTH_SHORT).show();
				
				taskList.add(new Task(Task.PRIORITY_HIGH, "TEST MAP", "TEST", "" + 0.0f, (float) point.latitude, (float) point.longitude)); 
			}
		});
        
        taskHash = new HashMap<Marker, MyMapTaskInfo>(); // will be used to
                                                         // retrieve task
                                                         // information when the
                                                         // popup is shown

        // set zoom and other UI stuff on map
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);

        // enable location tracking in map
        map.setLocationSource(this); // we use our own LocationSource (this
                                     // class) instead of Google Maps default
                                     // location source because with the default
                                     // source, the map keeps centering
                                     // on the current location even after the
                                     // user moves the map around manually
        map.setMyLocationEnabled(true);

        locationManager = (LocationManager) getActivity().getSystemService(Activity.LOCATION_SERVICE);
        locationCriteria = new Criteria();
        locationCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        locationCriteria.setPowerRequirement(Criteria.POWER_LOW);
        locationCriteria.setBearingRequired(true);
        locationCriteria.setSpeedRequired(true);

        // Get a coarse fix very fast so impatient users at least see a
        // reasonably accurate location on their maps
        // update every refreshInterval milliseconds. We read this value from
        // the user's settings. A default value of 1 minute is used.
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int refreshInterval = Integer.parseInt(sharedPrefs.getString(getString(R.string.pref_user_frequency_key), "1")) * 60 * 1000;
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, refreshInterval, 50, this);

        // add a marker for each task
        for (Task item : taskList) {
	       // MARC YOU ALSO NEED TO MODIFY THIS CODE
        	// Here, we're actually adding the markers to the map
          // As I said before, I'm using hardcoded locations (locationCoordArray) but you might want to pull the
          // location from your Task object (Task.java) if you've added location fields to it.
        	LatLng cursorGPS = new LatLng(item.lattitude, item.longitude);
        	
        	Marker myMarker = map.addMarker(new MarkerOptions().position(cursorGPS).title(item.name).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        	taskHash.put(myMarker, new MyMapTaskInfo(item.name, item.description, item.color_priority));
        }

        // add an InfoWindowAdapter that will handle the marker popups
        map.setInfoWindowAdapter(new MyMapPopupHandler(this.inflater, this.taskHash));
        map.setOnInfoWindowClickListener(this); //register a listener that will respond to clicks on the marker's popup window
                                                //when the popup window is clicked, we simply transition to EditorActivity
      }
    }
    catch (GooglePlayServicesNotAvailableException e) {
      e.printStackTrace();
    }
  }

  // overrides of LocationSource
  @Override
  public void activate(OnLocationChangedListener listener) {
    this.listener = listener;
  }

  @Override
  public void deactivate() {
    this.listener = null;
  }

  // overrides of LocationListener
  @Override
  public void onLocationChanged(Location loc) {
    if (this.listener != null) {
      this.listener.onLocationChanged(loc);
      map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 17));
      if (locationCriteria.getAccuracy() == Criteria.ACCURACY_COARSE) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
          Toast.makeText(getActivity(), R.string.getting_gps_infomsg, Toast.LENGTH_LONG).show();
          locationCriteria.setAccuracy(Criteria.ACCURACY_FINE);
          locationManager.requestLocationUpdates(locationManager.getBestProvider(locationCriteria, true), 20000, 50, this); // TODO:
                                                                                                                            // change
                                                                                                                            // the
                                                                                                                            // time
                                                                                                                            // criteria
                                                                                                                            // to
                                                                                                                            // what
                                                                                                                            // the
                                                                                                                            // user
                                                                                                                            // sets
        }
        else { // we found the most accurate loc possible (using network; GPS is
               // off)
          Toast.makeText(getActivity(), R.string.gps_disabled_infomsg, Toast.LENGTH_LONG).show();
          // we need the location only when the map is opened the first time
          // no more updates needed
          locationManager.removeUpdates(this);

        }
      }
      else if (locationCriteria.getAccuracy() == Criteria.ACCURACY_FINE) { // we
                                                                           // found
                                                                           // the
                                                                           // most
                                                                           // accurate
                                                                           // loc
                                                                           // possible
                                                                           // (using
                                                                           // GPS)
        // we need the location only when the map is opened the first time
        // no more updates needed
        locationManager.removeUpdates(this);
      }
    }
  }

  @Override
  public void onProviderDisabled(String provider) {
    Toast.makeText(getActivity(), "No location providers found", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onProviderEnabled(String provider) {
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
  }

  @Override
  public void onInfoWindowClick(Marker marker) {
    
	  // MARC this is the final piece of code you need to write. This is actually what I spoke
    // to you on the phone about. I haven't done it because, as of now, EditorActivity does
    // not have any hook to populate its fields.
    // What's happening here:
    // We end up here when we click on a marker's pop-up window. What we want is to open up
    // an edit activity for the task so the user can edit the task details. This can be done by
    // passing the task information to EditorActivity (once you've added hooks to EditorActivity to  
    // populate its fields).
    // To get the associated task info, please call taskHash.get(marker) and this will return you a
    // Task object with the tasks's title, description, priority etc.
    // Once you have the Task's info, you need to pass it to Armando's EditorActivity activity.
  }

}
