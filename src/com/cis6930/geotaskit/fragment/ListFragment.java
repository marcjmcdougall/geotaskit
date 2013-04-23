package com.cis6930.geotaskit.fragment;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.cis6930.geotaskit.EditorActivity;
import com.cis6930.geotaskit.R;
import com.cis6930.geotaskit.Task;
import com.cis6930.geotaskit.adapter.TaskAdapter;
import com.cis6930.geotaskit.backends.DatabaseInterface;

public class ListFragment extends Fragment {

  private ArrayList<Task> items;

  private DatabaseInterface db;
  private TaskAdapter adapter;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    // TODO: Not adding new components to ListView for some reason (they are in
    // DB)
    View view = inflater.inflate(R.layout.fragment_list, container, false);
    System.out.println("**List Fragment onCreateView() called**");

    System.out.println("Adapter Contents: ");
    System.out.println("Size: " + items.size());

    for (Task task : items) {

      System.out.println("[ " + task.lattitude + ", " + task.longitude + " ]");
    }

    adapter = new TaskAdapter(getActivity(), R.layout.item_task, items);

    ListView list = (ListView) view.findViewById(R.id.fragment_list_list);
    list.setAdapter(adapter);

    // When single tap, task will be edited with EditorActivity and extras
    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
			editTask(index);
		}
	});
    
    list.setOnItemLongClickListener(new OnItemLongClickListener() {

    	@Override
    	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int index, long arg3) {
    		
    		// build the dialog to select an action (edit, delete, or dismiss dialog)
    		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    		//title (title of the task)
    		builder.setTitle(items.get(index).name);
    		// message
    		builder.setMessage(R.string.list_dialog_task_message);
    		// edit
    		builder.setPositiveButton(R.string.list_dialog_task_edit, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					editTask(index);
				}
			});
    		// delete
    		builder.setNegativeButton(R.string.list_dialog_task_delete, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeTask(index);
				}
			});
    		// edit
    		builder.setNeutralButton(R.string.list_dialog_task_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

    		builder.create().show();
    		
    		return true;
    	}
    });

    return view;
  }
  
  private void editTask(int index){
		// Get task to be edited
		Task task = items.get(index);
		
		// Pass data of the task as extras
		Intent intent = new Intent(getActivity(), EditorActivity.class);
		intent.putExtra(EditorActivity.INTENT_EDITING, true);
		intent.putExtra(EditorActivity.INTENT_TASK_ID, task.getId());
		intent.putExtra(EditorActivity.INTENT_TASK_PRIORITY, task.color_priority);
		intent.putExtra(EditorActivity.INTENT_TASK_NAME, task.name);
		intent.putExtra(EditorActivity.INTENT_TASK_DESCRIPTION, task.description);
		intent.putExtra(EditorActivity.INTENT_TASK_LATITUDE, task.lattitude);
		intent.putExtra(EditorActivity.INTENT_TASK_LONGITUD, task.longitude);
		
		// Start activity
		startActivity(intent);
  }
  
  private void removeTask(int index){
	  // Get task to be edited
	  Task task = items.get(index);
	  db.removeTask(task);
	  this.onResume();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {

    items = new ArrayList<Task>();
    db = new DatabaseInterface(getActivity().getApplicationContext());

    items = db.getTasks();
    // adapter.notifyDataSetChanged();

    // items.add(new Task(Task.PRIORITY_HIGH, "Take pic for Neeraj",
    // "Take a pic of an aligator and send it to him", "3.2", 58.2942f,
    // 54.405f));
    // items.add(new Task(Task.PRIORITY_LOW, "Visit John",
    // "Pay a visit whenever possible", "1.3", 58.2942f, 54.405f));
    // items.add(new Task(Task.PRIORITY_NORMAL, "Groceries",
    // "Buy them at Publix first week every mo.", "4.7", 58.2942f, 54.405f));
    // items.add(new Task(Task.PRIORITY_NORMAL, "Pay the utility bill",
    // "Every 15 days", "2.3", 58.2942f, 54.405f));
    // items.add(new Task(Task.PRIORITY_LOW, "Go to the movies",
    // "...just if really bored", "0.3", 58.2942f, 54.405f));
    // items.add(new Task(Task.PRIORITY_LOW, "Jane's House", "Damn Jane...",
    // "0.5", 58.2942f, 54.405f));
    // items.add(new Task(Task.PRIORITY_HIGH, "Take pic for Neeraj",
    // "Take a pic of an aligator and send it to him", "3.2", 58.2942f,
    // 54.405f));
    // items.add(new Task(Task.PRIORITY_LOW, "Visit John",
    // "Pay a visit whenever possible", "1.3", 58.2942f, 54.405f));
    // items.add(new Task(Task.PRIORITY_NORMAL, "Groceries",
    // "Buy them at Publix first week every mo.", "4.7", 58.2942f, 54.405f));

    super.onCreate(savedInstanceState);
  }

  @Override
  public void onResume() {

    System.out.println("**List Fragment onResume() called**");

    items.clear();
    items.addAll(db.getTasks());
    
    adapter.notifyDataSetChanged();

    super.onResume();
  }
}