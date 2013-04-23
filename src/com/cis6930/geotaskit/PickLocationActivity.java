package com.cis6930.geotaskit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.cis6930.geotaskit.backends.OpenHelper;
import com.cis6930.geotaskit.fragment.MyMapFragment;

// This class created only to host MyMapFragment. This way, the user can use
// MyMapFragment from EditorActivity via this activity to pick a location
// WHEN CREATING A NEW TASK
public class PickLocationActivity extends SherlockFragmentActivity {
  public Double latitude;
  public Double longitude;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    MyMapFragment f = new MyMapFragment();
    MyMapFragment.PICK_LOCATION_FOR_NEW_TASK = true;
    // FragmentTransaction is obtained in order to change the fragment in the
    // container (R.id.content at activity_main.xml) with the requested fragment
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    // make the change, setting the new fragment
    ft.replace(R.id.main_content, f);
    // and commit changes to the FragmentTransaction object
    ft.commit();
  }

  // called from within the map fragment once Latitude and Longitude have been
  // set. Simply pass the latitude and longitude back to the calling activity
  // (EditorActivity)
  public void end() {
    Intent intent = getIntent();
    intent.putExtra(OpenHelper.KEY_LATTITUDE, latitude);
    intent.putExtra(OpenHelper.KEY_LONGITUDE, longitude);
    setResult(RESULT_OK, intent);
    finish();
  }
}
