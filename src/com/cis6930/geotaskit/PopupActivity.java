package com.cis6930.geotaskit;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class PopupActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_popup);

    Button button_dismiss = (Button) findViewById(R.id.activity_popup_button_dismiss);
    Button button_view = (Button) findViewById(R.id.activity_popup_button_view);

    button_dismiss.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });

    button_view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(PopupActivity.this, MainActivity.class));
        finish();
      }
    });
  }

  @Override
  public void onBackPressed() {
    finish();
    super.onBackPressed();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.popup, menu);
    return true;
  }
}
