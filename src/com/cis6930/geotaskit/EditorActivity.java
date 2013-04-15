package com.cis6930.geotaskit;

import android.R.anim;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class EditorActivity extends Activity {

	// Small bar that indicates the priority selected on top of the priority spinner 
	View view_priority_indicator;
	Spinner spinner;
	AlphaAnimation anim_alpha;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editor);
		
		// Get views
		spinner = (Spinner) findViewById(R.id.editor_spinner_priority);
		view_priority_indicator = findViewById(R.id.editor_view_priority_indicator);
		
		// Create adapter, set view for drop-down list, and assign to spinner
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.editor_priority_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		// Set listener to whenever a priority is selected
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
				updatePriorityIndicator();
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}
	
	private void updatePriorityIndicator(){
		
		// Set the color of the indicator based on the chosen option in the spinner
		if(spinner.getSelectedItem().toString().toLowerCase().equals("high"))
			view_priority_indicator.setBackgroundColor(Task.PRIORITY_HIGH);
		else if(spinner.getSelectedItem().toString().toLowerCase().equals("normal"))
			view_priority_indicator.setBackgroundColor(Task.PRIORITY_NORMAL);
		else if(spinner.getSelectedItem().toString().toLowerCase().equals("low"))
			view_priority_indicator.setBackgroundColor(Task.PRIORITY_LOW);
		
		// Set alpha animation and start it with the indicator
		if(anim_alpha == null){
			anim_alpha = new AlphaAnimation(0.0f, 1.0f);
			anim_alpha.setDuration(800);
		}
		
		view_priority_indicator.startAnimation(anim_alpha);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.editor, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(item.getItemId() == R.id.action_discard){
			finish();
			return true;
		}
		return false;
	}

}
