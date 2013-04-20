package com.cis6930.geotaskit.backends;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.SyncStateContract.Helpers;

import com.cis6930.geotaskit.Task;

public class DatabaseInterface {

	private OpenHelper opener;
	
	private SQLiteDatabase readDatabase;
	private SQLiteDatabase writeDatabase;
	
	public DatabaseInterface(Context context){
		
		this.opener = new OpenHelper(context);
		
		this.readDatabase = opener.getReadableDatabase();
		this.writeDatabase = opener.getWritableDatabase();
	}
	
	/**
	 * Convenience method that simply returns all of the tasks currently in progress by the user.
	 * 
	 * @return A list of every task currently in progress.
	 */
	public ArrayList<Task> getTasks(){
		
		ArrayList<Task> output = null;
		
		String[] columnsToRead = new String[OpenHelper.COLUMN_TOTAL];
		columnsToRead[0] = OpenHelper.KEY_PRIORITY;
		columnsToRead[1] = OpenHelper.KEY_DESCRIPTION_SHORT;
		columnsToRead[2] = OpenHelper.KEY_DESCRIPTION;
		columnsToRead[3] = OpenHelper.KEY_DISTANCE;
		columnsToRead[4] = OpenHelper.KEY_LATTITUDE;
		columnsToRead[5] = OpenHelper.KEY_LONGITUDE;
		
		String orderBy = OpenHelper.KEY_PRIORITY;
		
		Cursor returnData = readDatabase.query(OpenHelper.DICTIONARY_TABLE_NAME, columnsToRead, null, null, null, null, orderBy);
		
		output = extractArrayList(returnData);
		
		return output;
	}
	
	/**
	 * Convenience method that extracts an arrayList of tasks that contain the specific keyword in their description.
	 * 
	 * @param keyword The word to search for.
	 * @return An ArrayList containing all of the information (every column is selected by default).
	 */
	public ArrayList<Task> getTasksWithKeyword(String keyword){
		
		ArrayList<Task> output = null;
		
		String[] columnsToRead = new String[OpenHelper.COLUMN_TOTAL];
		columnsToRead[0] = OpenHelper.KEY_PRIORITY;
		columnsToRead[1] = OpenHelper.KEY_DESCRIPTION_SHORT;
		columnsToRead[2] = OpenHelper.KEY_DESCRIPTION;
		columnsToRead[3] = OpenHelper.KEY_DISTANCE;
		columnsToRead[4] = OpenHelper.KEY_LATTITUDE;
		columnsToRead[5] = OpenHelper.KEY_LONGITUDE;
		
		// Note that there WHERE is excluded for our purposes, just assume that it is the first word in the String
		String whereClause = OpenHelper.KEY_DESCRIPTION + " LIKE %" + keyword + "% OR " + OpenHelper.KEY_DESCRIPTION_SHORT + " LIKE %" + keyword + "%";
		
		String orderBy = OpenHelper.KEY_PRIORITY;
		
		Cursor returnData = readDatabase.query(OpenHelper.DICTIONARY_TABLE_NAME, columnsToRead, whereClause, null, null, null, orderBy);
		
		output = extractArrayList(returnData);
		
		// Return the constructed ArrayList
		return output;
	}

	/**
	 * Helper method that extracts the ArrayList from a returned cursor.  
	 * 
	 * Note that this method assumes that you want ALL of the columns to be returned.  If the cursor does
	 * not provide this data, then an Exception will be thrown.
	 * 
	 * @param returnData The Cursor representing the values returned from the SQLDatabase
	 * @return An ArrayList describing the Tasks
	 */
	private ArrayList<Task> extractArrayList(Cursor returnData) {
		
		// The output ArrayList is initialized
		ArrayList<Task> output = new ArrayList<Task>();
		
		// Move the counter to the first item in the return data
		returnData.moveToFirst();
		int count = 0;
		
		// While there are still values in the return data
		while(!returnData.isAfterLast()){
			
			// Add the new Task to the ArrayList
			// Data format is (int, String, String, String, float, float)
			output.add(count, new Task(returnData.getInt(0), returnData.getString(1), returnData.getString(2), returnData.getString(3), returnData.getFloat(4), returnData.getFloat(5)));
			
			// Advance the Cursor
			returnData.moveToNext();
			
			// Advance the counter
			count++;
		}
		
		// Return the ArrayList
		return output;
	}
	
	/**
	 * Convenience method for adding a new Task to the SQLite database.
	 * 
	 * @param task The new task to add.
	 */
	public void addTask(Task task){
		
		ContentValues newValue = new ContentValues(4);
		
		newValue.put(OpenHelper.KEY_PRIORITY, task.color_priority);
		newValue.put(OpenHelper.KEY_DESCRIPTION_SHORT, task.name);
		newValue.put(OpenHelper.KEY_DESCRIPTION, task.description);
		newValue.put(OpenHelper.KEY_DISTANCE, task.miles_left);
		
		// Insert the item into the database
		writeDatabase.insert(OpenHelper.DICTIONARY_TABLE_NAME, null, newValue);
	}
	
	public int removeTask(Task task){
		
		// Match on every field in the Task
		String whereClause = 	OpenHelper.KEY_PRIORITY + " = \'" + task.color_priority + "\'" +
					" AND " + 	OpenHelper.KEY_DESCRIPTION_SHORT + " = \'" + task.name + "\'" +
					" AND " +	OpenHelper.KEY_DESCRIPTION + " = \'" + task.description + "\'" +
					" AND " + 	OpenHelper.KEY_DISTANCE + " = \'" + task.miles_left + "\'";
		
		// Return the total number of rows removed
		return writeDatabase.delete(OpenHelper.DICTIONARY_TABLE_NAME, whereClause, null);
	}
	
	public void initialize(){
		
//		DEBUG
		System.out.println("**Initializing now**");
		
		ContentValues[] values = new ContentValues[3];
		
		for(int i = 0; i < values.length; i++){
			
			// Initialize each ContentValues object
			values[i] = new ContentValues(4);
		}
		
		// Populate the first item in the ContentValues
		values[0].put(OpenHelper.KEY_PRIORITY, Task.PRIORITY_HIGH);
		values[0].put(OpenHelper.KEY_DESCRIPTION_SHORT, "Take pic for Neeraj");
		values[0].put(OpenHelper.KEY_DESCRIPTION, "Take a pic of an aligator and send it to him");
		values[0].put(OpenHelper.KEY_DISTANCE, "3.2");
		
		// Insert the first item into the database
		writeDatabase.insert(OpenHelper.DICTIONARY_TABLE_NAME, null, values[0]);
		
		// Populate the second item in the ContentValues
		values[1].put(OpenHelper.KEY_PRIORITY, Task.PRIORITY_LOW);
		values[1].put(OpenHelper.KEY_DESCRIPTION_SHORT, "Visit John");
		values[1].put(OpenHelper.KEY_DESCRIPTION, "Pay a visit whenever possible");
		values[1].put(OpenHelper.KEY_DISTANCE, "1.3");
		
		// Insert the second item into the database
		writeDatabase.insert(OpenHelper.DICTIONARY_TABLE_NAME, null, values[1]);
		
		// Populate the second item in the ContentValues
		values[2].put(OpenHelper.KEY_PRIORITY, Task.PRIORITY_LOW);
		values[2].put(OpenHelper.KEY_DESCRIPTION_SHORT, "Visit Mary");
		values[2].put(OpenHelper.KEY_DESCRIPTION, "Give her some candy, fool!");
		values[2].put(OpenHelper.KEY_DISTANCE, "5.3");
		
		// Insert the second item into the database
		writeDatabase.insert(OpenHelper.DICTIONARY_TABLE_NAME, null, values[2]);
	}
}
