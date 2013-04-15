package com.cis6930.geotaskit.fragment;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.cis6930.geotaskit.R;
import com.cis6930.geotaskit.Task;
import com.cis6930.geotaskit.adapter.TaskAdapter;

public class ListFragment extends Fragment{
	
	ArrayList<Task> items;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_list, container, false);

		ListView list = (ListView) view.findViewById(R.id.fragment_list_list);
		list.setAdapter(new TaskAdapter(getActivity(), R.layout.item_task, items));
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//just for when the fragment is created, fill the arraylist with false Task items for demonstrative purposes
		items = new ArrayList<Task>();
		
		items.add(new Task(Task.PRIORITY_HIGH, "Take pic for Neeraj", "Take a pic of an aligator and send it to him", "3.2"));
		items.add(new Task(Task.PRIORITY_LOW, "Visit John", "Pay a visit whenever possible", "1.3"));
		items.add(new Task(Task.PRIORITY_NORMAL, "Groceries", "Buy them at Publix first week every mo.", "4.7"));
		items.add(new Task(Task.PRIORITY_NORMAL, "Pay the utility bill", "Every 15 days", "2.3"));
		items.add(new Task(Task.PRIORITY_LOW, "Go to the movies", "...just if really bored", "0.3"));
		items.add(new Task(Task.PRIORITY_LOW, "Jane's House", "Damn Jane...", "0.5"));
		items.add(new Task(Task.PRIORITY_HIGH, "Take pic for Neeraj", "Take a pic of an aligator and send it to him", "3.2"));
		items.add(new Task(Task.PRIORITY_LOW, "Visit John", "Pay a visit whenever possible", "1.3"));
		items.add(new Task(Task.PRIORITY_NORMAL, "Groceries", "Buy them at Publix first week every mo.", "4.7"));
		
		super.onCreate(savedInstanceState);
	}
	
}