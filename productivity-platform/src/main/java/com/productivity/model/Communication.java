package com.productivity.model;

import java.time.LocalDateTime;

public class Communication {
    private int id;
    private int fromEmployeeId;
    private int toEmployeeId;
    private String channel; // email, chat
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    private String fromName;
    private String toName;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getFromEmployeeId() { return fromEmployeeId; }
    public void setFromEmployeeId(int fromEmployeeId) { this.fromEmployeeId = fromEmployeeId; }
    public int getToEmployeeId() { return toEmployeeId; }
    public void setToEmployeeId(int toEmployeeId) { this.toEmployeeId = toEmployeeId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }
    public String getToName() { return toName; }
    public void setToName(String toName) { this.toName = toName; }
}
