package com.cis6930.geotaskit.fragment;

/*
 * This class is the Map Fragment that shows all the tasks on a Google Map
 */
// TODO: Center map on user's current location every time this fragment gains focus

import java.util.HashMap;

import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cis6930.geotaskit.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MyMapFragment extends Fragment implements LocationSource, LocationListener {
  static final LatLng[] locationCoordArray = { new LatLng(29.65133, -82.342822), new LatLng(29.650377, -82.342857) };
  static final String[] locationNamesArray = { "Library West", "Plaza of the Americas" };
  private GoogleMap map;
  private static View view;
  private LayoutInflater inflater;
  private OnLocationChangedListener listener; // right now, just a dummy class used to display the dot at the location
                                              // if you want actual location-tracking, make this class do something!
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
    try {
      MapsInitializer.initialize(getActivity());

      if (map == null)
        map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.fragment_map_map)).getMap();

      if (map != null) {
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
        map.setLocationSource(this); // we use our own locationsource (this
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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 20000, 50, this); // TODO:
                                                                                                   // change
                                                                                                   // the
                                                                                                   // time
                                                                                                   // criteria
                                                                                                   // to
                                                                                                   // what
                                                                                                   // the
                                                                                                   // user
                                                                                                   // sets

        // add the markers
        for (int i = 0; i < locationCoordArray.length; i++) {
          Marker myMarker = map.addMarker(new MarkerOptions().position(locationCoordArray[i]).title(locationNamesArray[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
          taskHash.put(myMarker, new MyMapTaskInfo(locationNamesArray[i]));
        }

        // add an InfoWindowAdapter that will handle the marker popups
        MyMapPopupHandler myInfoWindowAdapter = new MyMapPopupHandler(this.inflater, this.taskHash);
        map.setInfoWindowAdapter(myInfoWindowAdapter);
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

}
