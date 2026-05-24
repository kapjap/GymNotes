package com.example.gymnotes;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ExerciseEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int workoutId;
    public String name;
    public int sets;
    public int reps;
    public double weight;

    public ExerciseEntity() {
    }

    public ExerciseEntity(int workoutId, String name, int sets, int reps, double weight) {
        this.workoutId = workoutId;
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
    }
}