package com.cis6930.geotaskit.adapter;

import java.util.ArrayList;
import java.util.List;

import com.cis6930.geotaskit.R;
import com.cis6930.geotaskit.Task;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TaskAdapter extends ArrayAdapter<Task> {

  private List<Task> items = null;
  private int resource;
  private Context context;

  // get the context, the resource the item will be inflated with, and the list
  // of objects
  public TaskAdapter(Context context, int textViewResourceId, List<Task> objects) {
    super(context, textViewResourceId, objects);
    this.context = context;
    this.resource = textViewResourceId;
    this.items = objects;
  }

  // return the item of the list with an specific index
  @Override
  public Task getItem(int position) {
    return items.get(position);
  }

  // get id of an item, in this case is not needed, and just return same
  // position
  @Override
  public long getItemId(int position) {
    return position;
  }

  // get the view whenever the adapter needs to redraw an item
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = convertView;

    // just draw the view ONlY if it is not recycled (which means the view is
    // null)
    // when the iteam was already drawn the adapter recycles its inflated layout
    // for a better memory management and just needs to re-set the view children
    // contents
    if (view == null) {
      LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = vi.inflate(resource, null);
    }

    // if the list has items then set the value of children components
    if (items != null && !items.isEmpty()) {
      Task task = getItem(position);

      // get the views that will hold the information of the task
      View view_priority = view.findViewById(R.id.item_task_view_priority);
      TextView text_name = (TextView) view.findViewById(R.id.item_task_text_name);
      TextView text_description = (TextView) view.findViewById(R.id.item_task_text_description);
      TextView text_miles = (TextView) view.findViewById(R.id.item_task_text_miles);

      // set priority color and data
      view_priority.setBackgroundColor(task.color_priority);
      text_name.setText(task.name);
      text_description.setText(task.description);
      text_miles.setText(task.miles_left + " mi");
    }

    return view;
  }

  @Override
  public int getCount() {
    return items.size();
  }
}
