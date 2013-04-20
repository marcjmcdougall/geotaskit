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
import com.cis6930.geotaskit.backends.DatabaseInterface;

public class ListFragment extends Fragment{
	
	ArrayList<Task> items;
	
	private DatabaseInterface db;
	private TaskAdapter adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_list, container, false);

		adapter = new TaskAdapter(getActivity(), R.layout.item_task, items);
		
		ListView list = (ListView) view.findViewById(R.id.fragment_list_list);
		list.setAdapter(adapter);
		
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		//just for when the fragment is created, fill the ArrayList with false Task items for demonstrative purposes
		items = new ArrayList<Task>();
		db = new DatabaseInterface(getActivity().getApplicationContext());
		
		items = db.getTasks();
		
//		items.add(new Task(Task.PRIORITY_HIGH, "Take pic for Neeraj", "Take a pic of an aligator and send it to him", "3.2", 58.2942f, 54.405f));
//		items.add(new Task(Task.PRIORITY_LOW, "Visit John", "Pay a visit whenever possible", "1.3", 58.2942f, 54.405f));
//		items.add(new Task(Task.PRIORITY_NORMAL, "Groceries", "Buy them at Publix first week every mo.", "4.7", 58.2942f, 54.405f));
//		items.add(new Task(Task.PRIORITY_NORMAL, "Pay the utility bill", "Every 15 days", "2.3", 58.2942f, 54.405f));
//		items.add(new Task(Task.PRIORITY_LOW, "Go to the movies", "...just if really bored", "0.3", 58.2942f, 54.405f));
//		items.add(new Task(Task.PRIORITY_LOW, "Jane's House", "Damn Jane...", "0.5", 58.2942f, 54.405f));
//		items.add(new Task(Task.PRIORITY_HIGH, "Take pic for Neeraj", "Take a pic of an aligator and send it to him", "3.2", 58.2942f, 54.405f));
//		items.add(new Task(Task.PRIORITY_LOW, "Visit John", "Pay a visit whenever possible", "1.3", 58.2942f, 54.405f));
//		items.add(new Task(Task.PRIORITY_NORMAL, "Groceries", "Buy them at Publix first week every mo.", "4.7", 58.2942f, 54.405f));
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		
		System.out.println("**List Fragment onResume() called**");
		
		items = db.getTasks();
		adapter.notifyDataSetChanged();
		
		super.onResume();
	}
}