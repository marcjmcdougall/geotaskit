package com.cis6930.geotaskit;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.cis6930.geotaskit.backends.DatabaseInterface;
import com.cis6930.geotaskit.backends.OpenHelper;
import com.cis6930.geotaskit.fragment.MyMapFragment;

public class EditorActivity extends Activity implements OnClickListener {

	//public static keys for intent extras
	public static final String INTENT_EDITING = "editor_extra_key_isEditing";	//for boolean value to check if it is in editing mode
	public static final String INTENT_TASK_ID = "editor_extra_key_task_id";
	public static final String INTENT_TASK_PRIORITY = "editor_extra_key_task_priority";
	public static final String INTENT_TASK_NAME = "editor_extra_key_task_name";
	public static final String INTENT_TASK_DESCRIPTION = "editor_extra_key_task_desc";
	public static final String INTENT_TASK_LATITUDE = "editor_extra_key_task_lat";
	public static final String INTENT_TASK_LONGITUD = "editor_extra_key_task_lon";
	
	private boolean isEditing = false;
	
	// Small bar that indicates the priority selected on top of the priority
	// spinner
	View view_priority_indicator;
	Spinner spinner;
	AlphaAnimation anim_alpha;

	EditText name;
	EditText description;

	Button add_task;
	Button pick_location;

	TextView latTextView, latLabelTextView;
	TextView longTextView, longLabelTextView;

	private final int REQUEST_CODE = 1;

	private double latitude, longitude;

	public static boolean LOCATION_PICKED = false;

	private DatabaseInterface db;
	
	private Task task_to_edit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editor);

		// Initialize the database interface
		db = new DatabaseInterface(this);

		// Get views
		spinner = (Spinner) findViewById(R.id.editor_spinner_priority);
		view_priority_indicator = findViewById(R.id.editor_view_priority_indicator);

		add_task = (Button) findViewById(R.id.editor_button_add);
		pick_location = (Button) findViewById(R.id.editor_button_pick_location);

		latTextView = (TextView) findViewById(R.id.editor_latitude);
		longTextView = (TextView) findViewById(R.id.editor_longitude);
		latLabelTextView = (TextView) findViewById(R.id.editor_latitude_label);
		longLabelTextView = (TextView) findViewById(R.id.editor_longitude_label);

		name = (EditText) findViewById(R.id.editor_edit_name);
		description = (EditText) findViewById(R.id.editor_edit_description);

		// Create adapter, set view for drop-down list, and assign to spinner
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.editor_priority_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		// Set listener to whenever a priority is selected
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
				updatePriorityIndicator();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		
		// If this activity is Editing a task, then get data, and set to views
		if( isEditing = getIntent().getBooleanExtra(INTENT_EDITING, false) ){
			
			// Update button text for editing
			add_task.setText(getText(R.string.editor_button_add_when_editing));
			
			// Get intent, and retrieve extras
			Intent intent_extras = getIntent();
			
			long id = intent_extras.getIntExtra(INTENT_TASK_ID, 0);
			int priority = intent_extras.getIntExtra(INTENT_TASK_PRIORITY, Task.PRIORITY_NORMAL);
			String name = intent_extras.getStringExtra(INTENT_TASK_NAME);
			String desc = intent_extras.getStringExtra(INTENT_TASK_DESCRIPTION);
			latitude = intent_extras.getFloatExtra(INTENT_TASK_LATITUDE, 0.0f);
			longitude = intent_extras.getFloatExtra(INTENT_TASK_LONGITUD, 0.0f);
			
			System.out.println(":Prio:"+intent_extras.getIntExtra(INTENT_TASK_PRIORITY, Task.PRIORITY_NORMAL)+"-"+priority);
			
			LOCATION_PICKED = true;
			
			// Build Task object to delete via DatabaseInterface
			task_to_edit = new Task(priority, name, desc, "", (float) latitude, (float) longitude);
			task_to_edit.setId(id);
			
			// Set values for views accordingly
			
			// Priority is set as low by default, check for normal and high
			if(priority == Task.PRIORITY_NORMAL)
				spinner.setSelection(1);
			else if(priority == Task.PRIORITY_HIGH)
				spinner.setSelection(2);
			
			updatePriorityIndicator();
			
			this.name.setText(name);
			this.description.setText(desc);
			
			setCoordinatesLabel();
		}
		
		initializeContextualViews();
	}
	
	private void setCoordinatesLabel(){
		String lat = String.format("%9.6f",latitude);
		String longi = String.format("%9.6f",longitude);
		latTextView.setText(lat);
		longTextView.setText(longi);
		latTextView.setVisibility(View.VISIBLE);
		longTextView.setVisibility(View.VISIBLE);
		latLabelTextView.setVisibility(View.VISIBLE);
		longLabelTextView.setVisibility(View.VISIBLE);
	}

	private void updatePriorityIndicator() {

		// Set the color of the indicator based on the chosen option in the spinner
		if (spinner.getSelectedItem().toString().toLowerCase().equals("high"))
			view_priority_indicator.setBackgroundColor(Task.PRIORITY_HIGH);
		else if (spinner.getSelectedItem().toString().toLowerCase().equals("normal"))
			view_priority_indicator.setBackgroundColor(Task.PRIORITY_NORMAL);
		else if (spinner.getSelectedItem().toString().toLowerCase().equals("low"))
			view_priority_indicator.setBackgroundColor(Task.PRIORITY_LOW);

		// Set alpha animation and start it with the indicator
		if (anim_alpha == null) {
			anim_alpha = new AlphaAnimation(0.0f, 1.0f);
			anim_alpha.setDuration(800);
		}

		view_priority_indicator.startAnimation(anim_alpha);
	}
	
	private void removeTask(Task task){
		db.removeTask(task);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.editor, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		
		//IF clauses for whenever an option in the action bar is selected
		if (item.getItemId() == R.id.action_discard) {
			//discarding... just finish activity
			finish();
			return true;
		}else if(item.getItemId() == R.id.action_delete){
			//delete task if editing, finish activity
			if(isEditing)
				removeTask(task_to_edit);
			finish();
			return true;
		}
		return false;
	}

	private void initializeContextualViews() {

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		System.out.println("**Entered initializeContextualViews()**");

		if (extras.getInt(MyMapFragment.KEY_CONTEXT) == MyMapFragment.CONTEXT_ADD) {
			// add a new task. We get here if the user clicks on
			// a. the '+' button in the actionbar OR
			// b. anything other than a marker on the map
			System.out.println("**CONTEXT_ADD**");

			Button pick_location = (Button) findViewById(R.id.editor_button_pick_location);

			if (!LOCATION_PICKED) {
				// if the user hasn't chosen a location so far, hide the lat and long text fields
				latTextView.setVisibility(View.GONE);
				longTextView.setVisibility(View.GONE);
				longLabelTextView.setVisibility(View.GONE);
				latLabelTextView.setVisibility(View.GONE);
			}
			else{
				// if the user has already selected a location (e.g. if we arrived here by long clicking
				// on the map
				if(!isEditing)
					initTextFields(getIntent());
			}

			// handle click on pick location button (allow user to choose a new
			// location)
			pick_location.setOnClickListener(this);

			// Assign a click listener on the 'Add Task' button
			add_task.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (LOCATION_PICKED) { // user has picked a location. okay to proceed
						// with saving task
						int priority = Task.PRIORITY_LOW;

						// Create the Task from the fields
						if (spinner.getSelectedItem().toString().toLowerCase().equals("high")) {
							priority = Task.PRIORITY_HIGH;
						}
						else if (spinner.getSelectedItem().toString().toLowerCase().equals("normal")) {
							priority = Task.PRIORITY_NORMAL;
						}

//						Task task = new Task(priority, name.getText().toString(), description.getText()
//								.toString(), "0.0",(float) getIntent().getExtras().getDouble(
//										OpenHelper.KEY_LATTITUDE), (float) getIntent().getExtras().getDouble(
//												OpenHelper.KEY_LONGITUDE));
						
						Task task = new Task(priority, name.getText().toString(), description.getText().toString(), "", (float) latitude, (float) longitude);

//						DEBUG
						System.out.println("**LATITUDE: " + latitude + ", " + "LONGITUDE: " + longitude);
						
						// If this activity is editing a task, then remove current and add new with current data 
						if(isEditing)
							removeTask(task_to_edit);
						
						// Add the new Task to the SQL database
						db.addTask(task);

						// Close the editor window
						finish();
					}
					else { // user has not picked a location for this task as yet!
						Toast.makeText(getApplicationContext(), "Please pick a location!", Toast.LENGTH_SHORT)
						.show();
					}
				}
			});
		}
		else if (extras.getInt(MyMapFragment.KEY_CONTEXT) == MyMapFragment.CONTEXT_EDIT) {
			// edit existing task. We get here if the user clicks ON a marker on the
			// map

			final Task task = (Task) extras.getSerializable(Task.TAG);

			String name = task.name;
			String desc = task.description;
			int priority = task.color_priority;

			System.out.println("**CONTEXT_EDIT** " + priority + ", low: " + Task.PRIORITY_LOW
					+ ", norm: " + Task.PRIORITY_NORMAL + ", high: " + Task.PRIORITY_HIGH);

			latTextView.setText(String.valueOf(task.lattitude));
			longTextView.setText(String.valueOf(task.longitude));

			latTextView.setVisibility(View.VISIBLE);
			longTextView.setVisibility(View.VISIBLE);
			latLabelTextView.setVisibility(View.VISIBLE);
			longLabelTextView.setVisibility(View.VISIBLE);

			// handle click on pick location button (allow user to choose a new
			// location)
			pick_location.setOnClickListener(this);

			this.name.setText(name);
			this.description.setText(desc);

			if (priority == Task.PRIORITY_LOW) {
				spinner.setSelection(0);
			}
			else if (priority == Task.PRIORITY_NORMAL) {
				spinner.setSelection(1);
			}
			else if (priority == Task.PRIORITY_HIGH) {
				System.out.println("**Should be moving to selection 2**");
				spinner.setSelection(2);
			}

			add_task.setText(getResources().getString(R.string.editor_button_confirm));

			add_task.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					task.name = EditorActivity.this.name.getText().toString();
					task.description = EditorActivity.this.description.getText().toString();

					Intent result = new Intent();

					int newPriority = Task.PRIORITY_LOW;

					// Create the Task from the fields
					if (spinner.getSelectedItem().toString().toLowerCase().equals("high")) {
						newPriority = Task.PRIORITY_HIGH;
					}
					else if (spinner.getSelectedItem().toString().toLowerCase().equals("normal")) {
						newPriority = Task.PRIORITY_NORMAL;
					}

					task.color_priority = newPriority;

					result.putExtra(Task.TAG, task);

					// Set the result for viewing later
					setResult(RESULT_OK, result);

					// Close the editor
					finish();
				}
			});
		}
	}

	// the map just passed us back lat and long for the location the user clicked
	// on.
	// populate our text fields with these values and then make them visible
	// again.
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {

			initTextFields(data);
		}
	}

	private void initTextFields(Intent intent) {
		
		LOCATION_PICKED = true;
		latitude = intent.getDoubleExtra(OpenHelper.KEY_LATTITUDE, 0.0);
		longitude = intent.getDoubleExtra(OpenHelper.KEY_LONGITUDE, 0.0);
		setCoordinatesLabel();
	}

	@Override
	public void onClick(View view) {
		startActivityForResult(new Intent(EditorActivity.this, PickLocationActivity.class),
				REQUEST_CODE);
	}

}
