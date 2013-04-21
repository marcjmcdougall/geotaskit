package com.cis6930.geotaskit;

import java.io.Serializable;

import android.graphics.Color;

public class Task implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public static final String TAG = "task";
	
	public static final int PRIORITY_HIGH = Color.parseColor("#ff4444");
	public static final int PRIORITY_NORMAL = Color.parseColor("#ffbb33");
	public static final int PRIORITY_LOW = Color.parseColor("#99cc00");
	
	public int color_priority;
	
	public String name;
	public String description;
	
	public float lattitude;
	public float longitude;
	
	//add the actual object that handles the coordinates!
	//for the time being... for demo purposes:
	public String miles_left;
	
	public Task(int priority, String name, String description, String miles_left, float lattitude, float longitude) {
		
		this.color_priority = priority;
		this.name = name;
		this.description = description;
		this.miles_left = miles_left;
		
		this.lattitude = lattitude;
		this.longitude = longitude;
	}
	
}
