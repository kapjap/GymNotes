package com.example.gymnotes;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "completed_exercises")
public class CompletedExerciseEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "completed_workout_id")
    private int completedWorkoutId;

    private String name;
    private int sets;
    private int reps;
    private double weight;

    public CompletedExerciseEntity(int completedWorkoutId, String name, int sets, int reps, double weight) {
        this.completedWorkoutId = completedWorkoutId;
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public int getCompletedWorkoutId() {
        return completedWorkoutId;
    }

    public String getName() {
        return name;
    }

    public int getSets() {
        return sets;
    }

    public int getReps() {
        return reps;
    }

    public double getWeight() {
        return weight;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCompletedWorkoutId(int completedWorkoutId) {
        this.completedWorkoutId = completedWorkoutId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}