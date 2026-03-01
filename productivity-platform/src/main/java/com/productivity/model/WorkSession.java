package com.productivity.model;

import java.time.LocalDateTime;

public class WorkSession {
    private int id;
    private int employeeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int activityTypeId;
    private String appName;
    private String notes;
    private LocalDateTime createdAt;
    private String activityTypeName; // joined for display

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public int getActivityTypeId() { return activityTypeId; }
    public void setActivityTypeId(int activityTypeId) { this.activityTypeId = activityTypeId; }
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getActivityTypeName() { return activityTypeName; }
    public void setActivityTypeName(String activityTypeName) { this.activityTypeName = activityTypeName; }
}
