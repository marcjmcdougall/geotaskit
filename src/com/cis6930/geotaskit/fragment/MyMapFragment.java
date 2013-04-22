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
import android.content.Intent;
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

import com.cis6930.geotaskit.EditorActivity;
import com.cis6930.geotaskit.PickLocationActivity;
import com.cis6930.geotaskit.R;
import com.cis6930.geotaskit.Task;
import com.cis6930.geotaskit.backends.DatabaseInterface;
import com.cis6930.geotaskit.backends.OpenHelper;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MyMapFragment extends Fragment implements LocationSource, LocationListener,
    OnInfoWindowClickListener, OnMapClickListener, OnMapLongClickListener {

  ArrayList<Task> taskList = null;

  public static final int REQUEST_CODE = 0;
  public static final String KEY_CONTEXT = "context";
  public static final int CONTEXT_ADD = 0;
  public static final int CONTEXT_EDIT = 1;
  public static boolean PICK_LOCATION_FOR_NEW_TASK = false;

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
  private HashMap<Marker, Task> taskHash; // will be used to retrieve
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
        map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.fragment_map_map))
            .getMap();

      if (map != null) {
        taskList = db.getTasks();

        System.out.println(taskList.size());

        for (Task task : taskList) {
          System.out.println("[ " + task.lattitude + ", " + task.longitude + "]");
        }

        map.setOnMapClickListener(this);
        map.setOnMapLongClickListener(this);

        taskHash = new HashMap<Marker, Task>(); // Will be used to
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

        locationManager = (LocationManager) getActivity().getSystemService(
            Activity.LOCATION_SERVICE);
        locationCriteria = new Criteria();
        locationCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        locationCriteria.setPowerRequirement(Criteria.POWER_LOW);
        locationCriteria.setBearingRequired(true);
        locationCriteria.setSpeedRequired(true);

        // Get a coarse fix very fast so impatient users at least see a
        // reasonably accurate location on their maps
        // update every refreshInterval milliseconds. We read this value from
        // the user's settings. A default value of 1 minute is used.
        SharedPreferences sharedPrefs = PreferenceManager
            .getDefaultSharedPreferences(getActivity());
        int refreshInterval = Integer.parseInt(sharedPrefs.getString(
            getString(R.string.pref_user_frequency_key), "1")) * 60 * 1000;
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, refreshInterval,
            50, this);

        // add an InfoWindowAdapter that will handle the marker popups
        map.setInfoWindowAdapter(new MyMapPopupHandler(this.inflater, this.taskHash));
        map.setOnInfoWindowClickListener(this); // register a listener that will
                                                // respond to clicks on the
                                                // marker's popup window
                                                // when the popup window is
                                                // clicked, we simply transition
                                                // to EditorActivity
      }
    }
    catch (GooglePlayServicesNotAvailableException e) {
      e.printStackTrace();
    }
  }

  private void initializeMarkerPositions() {
    for (Task item : taskList) {
      LatLng cursorGPS = new LatLng(item.lattitude, item.longitude);
      Marker myMarker = map.addMarker(new MarkerOptions().position(cursorGPS).title(item.name)
          .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
      taskHash.put(myMarker, item);
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
      map.moveCamera(CameraUpdateFactory.newLatLngZoom(
          new LatLng(loc.getLatitude(), loc.getLongitude()), 17));
      if (locationCriteria.getAccuracy() == Criteria.ACCURACY_COARSE) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

          Toast.makeText(getActivity(), R.string.getting_gps_infomsg, Toast.LENGTH_LONG).show();
          locationCriteria.setAccuracy(Criteria.ACCURACY_FINE);
          locationManager.requestLocationUpdates(
              locationManager.getBestProvider(locationCriteria, true), 20000, 50, this); // TODO:
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

    Toast
        .makeText(getActivity().getApplicationContext(), "Info Window Clicked", Toast.LENGTH_SHORT)
        .show();

    Task task = taskHash.get(marker);

    Intent intent = new Intent(getActivity().getApplicationContext(), EditorActivity.class);
    intent.putExtra(Task.TAG, task);
    intent.putExtra(KEY_CONTEXT, CONTEXT_EDIT);

    startActivityForResult(intent, REQUEST_CODE);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == REQUEST_CODE) {
        // db.editTask(task);
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void onResume() {
    System.out.println("**MapFragment onResume()**");
    taskList = db.getTasks();
    // add a marker for each task
    initializeMarkerPositions();
    super.onResume();
  }

  // overrides of OnMapClickListener
  @Override
  public void onMapClick(LatLng point) {
    
	  // used when the "Pick Location" button is clicked on in EditorActivity
    // here, we basically pick a location for the task the user is now creating
    // once a location is selected, transition back to EditorActivity handing
    // back the coordinates
    if (MyMapFragment.PICK_LOCATION_FOR_NEW_TASK) {
      PICK_LOCATION_FOR_NEW_TASK = false; // reset for future
      PickLocationActivity pickLocationActivity = (PickLocationActivity) getActivity();
      pickLocationActivity.latitude = point.latitude;
      pickLocationActivity.longitude = point.longitude;
      pickLocationActivity.end();
    }
  }

  // override of OnMapLongClickListener
  @Override
  public void onMapLongClick(LatLng point) {
    // open up EditorActivity to create a new task
    // used when the user long clicks on a blank spot on the map
    if (!MyMapFragment.PICK_LOCATION_FOR_NEW_TASK) {
      //checking PICK_LOCATION_FOR_NEW_TASK ensures that long presses will NOT work when
      //we arrive at the map by clicking on the "Pick Location" button in EditorActivity
      // Thus, we don't have a stack of many instances of EditorActivity
      EditorActivity.LOCATION_PICKED = true;
      Intent markerIntent = new Intent(getActivity().getApplicationContext(), EditorActivity.class);

      markerIntent.putExtra(OpenHelper.KEY_LATTITUDE, point.latitude);
      markerIntent.putExtra(OpenHelper.KEY_LONGITUDE, point.longitude);
      markerIntent.putExtra(KEY_CONTEXT, CONTEXT_ADD);

      startActivity(markerIntent);
    }
  }
}
