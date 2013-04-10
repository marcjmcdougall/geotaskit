package com.cis6930.geotaskit.fragment;
/*
 * Models the information pertaining to the task so that it can be displayed on the map 
 */


public class TaskInfo {
  private String taskDescription;
  
  public TaskInfo(String desc){
    this.setTaskDescription(desc); 
  }

  public String getTaskDescription() {
    return taskDescription;
  }

  public void setTaskDescription(String taskDescription) {
    this.taskDescription = taskDescription;
  }
}
