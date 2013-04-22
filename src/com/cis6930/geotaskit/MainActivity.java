package com.cis6930.geotaskit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.cis6930.geotaskit.fragment.ListFragment;
import com.cis6930.geotaskit.fragment.MyMapFragment;

public class MainActivity extends SherlockFragmentActivity implements ActionBar.OnNavigationListener {

  Fragment f;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get the context from ActionBarSherlock (ABS)
    Context context = getSupportActionBar().getThemedContext();

    // build the list of items in the spinner based on a resource AND set the
    // view resource to the spinner with the adapter
    ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(context, R.array.actionbar_navigation_list, R.layout.sherlock_spinner_item);
    list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

    // set the type of navigation and then set the list adapter along with the
    // listener for whenever an option from the drop-down is selected
    getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    getSupportActionBar().setListNavigationCallbacks(list, this);
  }

  @Override
  protected void onResume() {

    System.out.println("**Resuming Now**");

    if (f != null) {

      f.onResume();
    }

    super.onResume();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    // we inflate the menu based on the R.menu.main xml file
    MenuInflater inflater = getSupportMenuInflater();
    inflater.inflate(R.menu.main, menu);
    return true;
  }

  // invoke the settings activity or the add/edit task activity
  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    // called whenever an action or menu item is selected

    // switch the ids for respective execution
    switch (item.getItemId()) {

    // refresh the task list from the server
    case R.id.action_refresh:
      f.onResume();
      break;

    // add a new task
    case R.id.action_add: {

      Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
      EditorActivity.LOCATION_PICKED = false;
      intent.putExtra(MyMapFragment.KEY_CONTEXT, MyMapFragment.CONTEXT_ADD);
      startActivity(intent);

      /*
       * f = new MyMapFragment();
       * 
       * // FragmentTransaction is obtained in order to change the fragment in
       * the // container (R.id.content at activity_main.xml) with the requested
       * // fragment FragmentTransaction ft =
       * getSupportFragmentManager().beginTransaction();
       * 
       * // make the change, setting the new fragment
       * ft.replace(R.id.main_content, f);
       * 
       * // and commit changes to the FragmentTransaction object ft.commit();
       */

      break;
    }

    // open app settings
    case R.id.action_settings:
      // call the settings activity
      startActivity(new Intent(this, SettingsActivity.class));
      break;
    }

    return true;
  }

  // Show the tasks either on the map view or the list view
  @Override
  public boolean onNavigationItemSelected(int itemPosition, long itemId) {
    // called whenever a navigation item is selected from the drop-down

    // declare the fragment that will replace the one that is currently shown
    f = null;

    // based on the option selected in the navigation list, set the fragment to
    // be either ListFragment or MapFragment
    switch (itemPosition) {
    case 0:
      // "List" is selected
      f = new ListFragment();
      break;
    case 1:
      // "Map" is selected
      f = new MyMapFragment();
      break;
    }

    // FragmentTransaction is obtained in order to change the fragment in the
    // container (R.id.content at activity_main.xml) with the requested fragment
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    // make the change, setting the new fragment
    ft.replace(R.id.main_content, f);
    // and commit changes to the FragmentTransaction object
    ft.commit();

    return false;
  }
}