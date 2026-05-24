package com.example.gymnotes;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                WorkoutEntity.class,
                ExerciseEntity.class,
                CompletedWorkoutEntity.class,
                CompletedExerciseEntity.class
        },
        version = 6
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WorkoutDao workoutDao();
    public abstract ExerciseDao exerciseDao();
    public abstract CompletedWorkoutDao completedWorkoutDao();
    public abstract CompletedExerciseDao completedExerciseDao();
}