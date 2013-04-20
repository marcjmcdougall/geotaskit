package com.cis6930.geotaskit;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.cis6930.geotaskit.backends.DatabaseInterface;

public class EditorActivity extends Activity {

	// Small bar that indicates the priority selected on top of the priority spinner 
	View view_priority_indicator;
	Spinner spinner;
	AlphaAnimation anim_alpha;
	
	EditText name;
	EditText description;
	
	Button add_task_button;
	
	private DatabaseInterface db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editor);
		
		// Initialize the database interface
		db = new DatabaseInterface(this);
		
		// Get views
		spinner = (Spinner) findViewById(R.id.editor_spinner_priority);
		view_priority_indicator = findViewById(R.id.editor_view_priority_indicator);
		add_task_button = (Button) findViewById(R.id.editor_button_add);
		name = (EditText) findViewById(R.id.editor_edit_name);
		description = (EditText) findViewById(R.id.editor_edit_description);
		
		// Assign a click listener on the button
		add_task_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				int priority = Task.PRIORITY_LOW;
				
				// Create the Task from the fields
				if(spinner.getSelectedItem().toString().toLowerCase().equals("high")){
					
					priority = Task.PRIORITY_HIGH;
				}
				else if(spinner.getSelectedItem().toString().toLowerCase().equals("normal")){
					
					priority = Task.PRIORITY_NORMAL;
				}
				
				// TODO: This currently sets the miles left, lat, and long values to 0.0 (as the Map Activity is not working for me)
				Task task = new Task(priority, name.getText().toString(), description.getText().toString(), "0.0", 0.0f, 0.0f);
				
				// Add the new Task to the SQL database
				db.addTask(task);
				
				// Close the editor window
				finish();
			}
		});
		
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
