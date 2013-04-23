package com.cis6930.geotaskit.fragment;

/*
 * This class is the Map Fragment that shows all the tasks on a Google Map
 */
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

public class MyMapFragment extends SupportMapFragment implements LocationSource, LocationListener,
    OnInfoWindowClickListener, OnMapClickListener, OnMapLongClickListener {
  ArrayList<Task> taskList = null;
  public static final int REQUEST_CODE = 0;
  public static boolean PICK_LOCATION_FOR_NEW_TASK = false;
  // PICK_LOCATION_FOR_NEW_TASK
  // set to true inside PickLocationActivity.
  // Indicates that we arrive at the map fragment from the add a new task
  // EditorActivity
  // If false, we're in the map activity because the user chose it in the
  // actionbar.
  private DatabaseInterface db;
  private GoogleMap map;
  private View view;
  // right now, just a dummy class used to display the dot at the
  // location if you want actual location-tracking, make this
  // class do something!
  private OnLocationChangedListener listener;
  // will be used to retrieve task information when the
  // popup is shown
  private HashMap<Marker, Task> taskHash;
  private LocationManager locationManager;
  private Criteria locationCriteria;
  private MyMapPopupHandler myMapPopupHandler;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    // add an InfoWindowAdapter that will handle the marker popups
    myMapPopupHandler = new MyMapPopupHandler(inflater);
    view = inflater.inflate(R.layout.fragment_map, container, false);
    return view;
  }

  // this stupid hack is needed to clear the view when we switch away from this fragment
  // or else you see a gray screen when you switch back
  @Override
  public void onDestroyView() {
    System.out.println("**MapFragment onDestroyView()**");
    super.onDestroyView();
    try {
      SupportMapFragment mapFragment = (SupportMapFragment) (getFragmentManager()
          .findFragmentById(R.id.map));
      getFragmentManager().beginTransaction().remove(mapFragment).commit();
    }
    catch (Exception e) {
    }
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    try {
      MapsInitializer.initialize(getActivity());
      // R.id.map is auto-generated
      map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
      if (map != null) {
        map.setOnMapClickListener(this);
        map.setOnMapLongClickListener(this);
        // Will be used to retrieve task information when the
        // popup is shown set zoom and other UI stuff on map
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);
        // enable location tracking in map
        // we use our own LocationSource (this class) instead of Google Maps default
        // location source because with the default source, the map keeps centering
        // on the current location even after the user moves the map around manually
        map.setLocationSource(this);
        map.setMyLocationEnabled(true);
        // register a listener that will respond to clicks on the
        // marker's popup window when the popup window is
        // clicked, we simply transition to EditorActivity
        map.setOnInfoWindowClickListener(this);
        // add an InfoWindowAdapter that will handle the marker popups
        map.setInfoWindowAdapter(myMapPopupHandler);
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
      }
    }
    catch (GooglePlayServicesNotAvailableException e) {
      e.printStackTrace();
    }
  }

  private void initializeMarkerPositions() {
    map.clear(); // clear old markers first!
    db = new DatabaseInterface(getActivity().getApplicationContext());
    taskList = new ArrayList<Task>();
    taskList = db.getTasks();
    taskHash = new HashMap<Marker, Task>();
    for (Task item : taskList) {
      System.out.println("[ " + item.lattitude + ", " + item.longitude + "]");
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
          Toast.makeText(getActivity(), R.string.getting_gps_infomsg, Toast.LENGTH_SHORT).show();
          locationCriteria.setAccuracy(Criteria.ACCURACY_FINE);
          locationManager.requestLocationUpdates(
              locationManager.getBestProvider(locationCriteria, true), 20000, 50, this);
          // TODO: change the time criteria to what the user sets
        }
        else { // we found the most accurate loc possible (using network; GPS is
          // off)
          Toast.makeText(getActivity(), R.string.gps_disabled_infomsg, Toast.LENGTH_SHORT).show();
          // we need the location only when the map is opened the first time
          // no more updates needed
          locationManager.removeUpdates(this);
        }
      }
      else if (locationCriteria.getAccuracy() == Criteria.ACCURACY_FINE) {
        // we found the most accurate loc possible (using GPS)
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
    Task task = taskHash.get(marker);
    Intent intent = new Intent(getActivity().getApplicationContext(), EditorActivity.class);
    intent.putExtra(Task.TAG, task);
    intent.putExtra(EditorActivity.KEY_CONTEXT, EditorActivity.CONTEXT_EDIT);
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
    // add a marker for each task
    initializeMarkerPositions();
    // update the popup handler's task hash
    myMapPopupHandler.setTaskHash(taskHash);
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
      // checking PICK_LOCATION_FOR_NEW_TASK ensures that long presses will NOT
      // work when
      // we arrive at the map by clicking on the "Pick Location" button in
      // EditorActivity
      // Thus, we don't have a stack of many instances of EditorActivity
      EditorActivity.LOCATION_PICKED = true;
      Intent markerIntent = new Intent(getActivity().getApplicationContext(), EditorActivity.class);
      markerIntent.putExtra(OpenHelper.KEY_LATTITUDE, point.latitude);
      markerIntent.putExtra(OpenHelper.KEY_LONGITUDE, point.longitude);
      markerIntent.putExtra(EditorActivity.KEY_CONTEXT, EditorActivity.CONTEXT_ADD);
      startActivity(markerIntent);
    }
  }
}
