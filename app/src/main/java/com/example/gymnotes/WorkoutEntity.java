package com.example.gymnotes;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workouts")
public class WorkoutEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private int exerciseCount;
    private int estimatedTime;
    private int restSeconds;

    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public WorkoutEntity(String title, int exerciseCount, int estimatedTime, int restSeconds, String userId) {
        this.title = title;
        this.exerciseCount = exerciseCount;
        this.estimatedTime = estimatedTime;
        this.restSeconds = restSeconds;
        this.userId = userId;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public int getExerciseCount() {
        return exerciseCount;
    }

    public int getEstimatedTime() {
        return estimatedTime;
    }

    public int getRestSeconds() {
        return restSeconds;
    }

    public String getUserId() {
        return userId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setExerciseCount(int exerciseCount) {
        this.exerciseCount = exerciseCount;
    }

    public void setEstimatedTime(int estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public void setRestSeconds(int restSeconds) {
        this.restSeconds = restSeconds;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}