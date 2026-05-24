package com.example.gymnotes;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "completed_workouts")
public class CompletedWorkoutEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "workout_id")
    private int workoutId;

    @ColumnInfo(name = "user_id")
    private String userId;

    private String title;

    @ColumnInfo(name = "duration_minutes")
    private int durationMinutes;

    @ColumnInfo(name = "total_weight")
    private double totalWeight;

    @ColumnInfo(name = "exercise_count")
    private int exerciseCount;

    @ColumnInfo(name = "completed_at")
    private long completedAt;

    public CompletedWorkoutEntity(int workoutId,
                                  String userId,
                                  String title,
                                  int durationMinutes,
                                  double totalWeight,
                                  int exerciseCount,
                                  long completedAt) {
        this.workoutId = workoutId;
        this.userId = userId;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.totalWeight = totalWeight;
        this.exerciseCount = exerciseCount;
        this.completedAt = completedAt;
    }

    public int getId() {
        return id;
    }

    public int getWorkoutId() {
        return workoutId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public int getExerciseCount() {
        return exerciseCount;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setWorkoutId(int workoutId) {
        this.workoutId = workoutId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public void setExerciseCount(int exerciseCount) {
        this.exerciseCount = exerciseCount;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }
}