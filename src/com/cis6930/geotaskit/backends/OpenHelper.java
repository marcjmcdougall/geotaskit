package com.cis6930.geotaskit.backends;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class OpenHelper extends SQLiteOpenHelper {

	// The version of the database (used for some queries)
    private static final int DATABASE_VERSION = 2;
    
    // The name of the database
    public static final String DICTIONARY_TABLE_NAME = "tasks2";
    
    // Public variable that keeps track of the total number of columns
    public static final int COLUMN_TOTAL = 6;
    
    // The keys for the values that will be stored
    public static final String KEY_PRIORITY = "priority";
    public static final String KEY_DESCRIPTION_SHORT = "short_description";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_LATTITUDE = "lattitude";
    public static final String KEY_LONGITUDE = "longitude";
    
    // Create the table only if it does not already exist
    private static final String DICTIONARY_TABLE_CREATE =
            
    		"CREATE TABLE IF NOT EXISTS " + DICTIONARY_TABLE_NAME + " (" +
            KEY_PRIORITY + " INT13, " +
            KEY_DESCRIPTION_SHORT + " TEXT, " +
            KEY_DESCRIPTION + " TEXT, " +
            KEY_DISTANCE + " TEXT, " + 
            KEY_LATTITUDE + " FLOAT, " +
            KEY_LONGITUDE + " FLOAT);";
    
    private static final String DICTIONARY_TABLE_DROP =
    		
    		"DROP TABLE IF EXISTS " +
    		DICTIONARY_TABLE_NAME;
	
    public OpenHelper(Context context){
    	
    	super(context, DICTIONARY_TABLE_NAME, null, DATABASE_VERSION);
    }
    
    /**
     * Simple constructor - does nothing in this implementation.
     * 
     * @param context The context of the current Activity/Fragment.
     * @param name The name of the database.
     * @param factory The abstract factory.
     * @param version The version of the database.
     */
	public OpenHelper(Context context, String name, CursorFactory factory, int version) {
		
		// Do nothing here, just call the super constructor
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		// Execute the SQL command to drop the table (if it does not already exist)
		db.execSQL(DICTIONARY_TABLE_DROP);
		
		// Execute the SQL command to create the table (if it does not already exist)
		db.execSQL(DICTIONARY_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		// Currently, do nothing
	}
}
