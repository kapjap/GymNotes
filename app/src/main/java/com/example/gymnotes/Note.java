package com.example.gymnotes;

public class Note {
    private String title;
    private String groupId;
    private String timestamp; // заменили Timestamp

    public Note() {}

    public Note(String title, String groupId, String  timestamp) {
        this.title = title;
        this.groupId = groupId;
        this.timestamp = timestamp;
    }

    public String getTitle() { return title; }
    public String getGroupId() { return groupId; }
    public String getTimestamp() { return timestamp; }

    public void setTitle(String title) { this.title = title; }
    public void setGroupId(String content) { this.groupId = groupId; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

