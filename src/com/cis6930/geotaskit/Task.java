package com.cis6930.geotaskit;

import android.graphics.Color;

public class Task {

	public static final int PRIORITY_HIGH = Color.parseColor("#ff4444");
	public static final int PRIORITY_NORMAL = Color.parseColor("#ffbb33");
	public static final int PRIORITY_LOW = Color.parseColor("#99cc00");
	
	public int color_priority;
	
	public String name;
	public String description;
	
	//add the actual object that handles the coordinates!
	//for the time being... for demo purposes:
	public String miles_left;
	
	public Task(int priority, String name, String description, String miles_left) {
		this.color_priority = priority;
		this.name = name;
		this.description = description;
		this.miles_left = miles_left;
	}
	
}
