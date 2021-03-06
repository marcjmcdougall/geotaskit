package com.cis6930.geotaskit.fragment;

import java.util.HashMap;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.cis6930.geotaskit.R;
import com.cis6930.geotaskit.Task;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class MyMapPopupHandler implements InfoWindowAdapter {
  private View view;
  private HashMap<Marker, Task> taskHash;

  public HashMap<Marker, Task> getTaskHash() {
    return taskHash;
  }

  public void setTaskHash(HashMap<Marker, Task> taskHash) {
    this.taskHash = taskHash;
  }

  public MyMapPopupHandler(LayoutInflater inflater) {
    this.view = inflater.inflate(R.layout.mymapmarkerpopup, null); // this is
                                                                   // the xml
                                                                   // file that
                                                                   // defines
                                                                   // the layout
                                                                   // of the
                                                                   // popup
                                                                   // balloon
  }

  // if this returns null, getInfoContents is called
  @Override
  public View getInfoWindow(Marker marker) {
    return null;
  }

  @Override
  public View getInfoContents(Marker marker) {
    // retrieve the task information and display on the popup
    Task thisTask = taskHash.get(marker);
    if (thisTask != null) {
      ((TextView) (view.findViewById(R.id.balloon_task_title))).setText(thisTask.name);
      ((TextView) (view.findViewById(R.id.balloon_task_description))).setText(thisTask.description);
      View balloon_priority = view.findViewById(R.id.balloon_task_priority);
      balloon_priority.setBackgroundColor(thisTask.color_priority);
    }
    else {
      System.out.println("MyMapPopupHandler: Task was empty!");
    }
    return view;
  }
}