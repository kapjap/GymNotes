package com.example.gymnotes;

public class WorkoutProgressModel {

    private int exerciseId;
    private String name;
    private String info;
    private int completedSets;
    private int totalSets;

    public WorkoutProgressModel(int exerciseId, String name, String info, int completedSets, int totalSets) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.info = info;
        this.completedSets = completedSets;
        this.totalSets = totalSets;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public int getCompletedSets() {
        return completedSets;
    }

    public int getTotalSets() {
        return totalSets;
    }

    public void setCompletedSets(int completedSets) {
        this.completedSets = completedSets;
    }
}