package com.cis6930.geotaskit.fragment;

/*
 * This class is the Map Fragment that shows all the tasks on a Google Map
 */
// TODO: Center map on user's current location every time this fragment gains focus

import java.util.HashMap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cis6930.geotaskit.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MyMapFragment extends Fragment {
  static final LatLng[] locationCoordArray = { new LatLng(29.65133, -82.342822), new LatLng(29.650377,-82.342857) };
  static final String[] locationNamesArray = { "Library West", "Plaza of the Americas" };
  private GoogleMap map;
  private static View view;
  private LayoutInflater inflater;
  private HashMap<Marker, TaskInfo> taskHash; // will be used to retrieve task
                                              // information when the popup is
                                              // shown

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    this.inflater = inflater;
    /*
     * //http://stackoverflow.com/a/14695397 if (view != null) { ViewGroup
     * parent = (ViewGroup) view.getParent(); if (parent != null)
     * parent.removeView(view); } try { view =
     * inflater.inflate(R.layout.fragment_map, container, false); } catch
     * (InflateException e) { map is already there, just return view as it is }
     */
    if (view == null)
      view = inflater.inflate(R.layout.fragment_map, container, false);
    return view;
  }

  @Override
  // we use onActivityCreated instead of onCreate because we can't use
  // findFragmentById
  // until the view has been inflated
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    try {
      MapsInitializer.initialize(getActivity());

      map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.fragment_map_map)).getMap();

      if (map != null) {
        // set zoom and other UI stuff on map
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);

        taskHash = new HashMap<Marker, TaskInfo>();

        // add the markers
        for (int i = 0; i < locationCoordArray.length; i++) {
          Marker myMarker = map.addMarker(new MarkerOptions().position(locationCoordArray[i]).title(locationNamesArray[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
          taskHash.put(myMarker, new TaskInfo(locationNamesArray[i]));
        }

        MyMapInfoWindowAdapter myInfoWindowAdapter = new MyMapInfoWindowAdapter(this.inflater, this.taskHash);

        map.setInfoWindowAdapter(myInfoWindowAdapter);
        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(17), 500, null);
      }
    }
    catch (GooglePlayServicesNotAvailableException e) {
      e.printStackTrace();
    }
  }

}
