package com.cis6930.geotaskit.fragment;

/*
 * Models the information pertaining to the task so that it can be displayed on the map 
 */

public class MyMapTaskInfo {
  private String taskTitle;
  private int taskPriority;
  private String taskDescription;

  public MyMapTaskInfo(String title, String desc, int priority) {
    this.setTaskTitle(title);
    this.setTaskDescription(desc);
    this.setTaskPriority(priority);
  }

  public String getTaskTitle() {
    return taskTitle;
  }

  public void setTaskTitle(String taskTitle) {
    this.taskTitle = taskTitle;
  }

  public String getTaskDescription() {
    return taskDescription;
  }

  public void setTaskDescription(String taskDescription) {
    this.taskDescription = taskDescription;
  }

  public int getTaskPriority() {
    return taskPriority;
  }

  public void setTaskPriority(int taskPriority) {
    this.taskPriority = taskPriority;
  }
}
