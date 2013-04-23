package com.cis6930.geotaskit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.cis6930.geotaskit.service.LocationNotificationService;

public class SettingsActivity extends PreferenceActivity implements
    OnSharedPreferenceChangeListener {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.settings);
  }

  @Override
  protected void onResume() {
    super.onResume();
    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onPause() {
    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    super.onPause();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    // Check changes to the settings
    // get autoupdate key
    String key_autoupdate = getString(R.string.pref_user_autoupdate_key);
    boolean isUpdating = sharedPreferences.getBoolean(key_autoupdate, false);
    // get frequency key
    String key_frequency = getString(R.string.pref_user_frequency_key);
    // Settings are related to the service, define intent to interact with it
    Intent i = new Intent(this, LocationNotificationService.class);
    // if the Frequency was changed AND service was running, then stop it
    if (key.equals(key_frequency) && isUpdating)
      stopService(i);
    // check whether to (re)start or stop the service
    if (isUpdating)
      startService(i);
    else
      stopService(i);
  }
}
