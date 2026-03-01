package com.productivity.model;

import java.time.LocalDateTime;

public class Task {
    public enum Status { pending, in_progress, completed }

    private int id;
    private int employeeId;
    private String title;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Integer estimatedMins;
    private String employeeName; // for display

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public Integer getEstimatedMins() { return estimatedMins; }
    public void setEstimatedMins(Integer estimatedMins) { this.estimatedMins = estimatedMins; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
}
