package com.productivity.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProductivitySnapshot {
    private int id;
    private int employeeId;
    private LocalDate snapshotDate;
    private double score;
    private int tasksDone;
    private int focusMinutes;
    private int collaborationCount;
    private String rawMetricsJson;
    private LocalDateTime createdAt;
    private String employeeName;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public LocalDate getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(LocalDate snapshotDate) { this.snapshotDate = snapshotDate; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public int getTasksDone() { return tasksDone; }
    public void setTasksDone(int tasksDone) { this.tasksDone = tasksDone; }
    public int getFocusMinutes() { return focusMinutes; }
    public void setFocusMinutes(int focusMinutes) { this.focusMinutes = focusMinutes; }
    public int getCollaborationCount() { return collaborationCount; }
    public void setCollaborationCount(int collaborationCount) { this.collaborationCount = collaborationCount; }
    public String getRawMetricsJson() { return rawMetricsJson; }
    public void setRawMetricsJson(String rawMetricsJson) { this.rawMetricsJson = rawMetricsJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
}
