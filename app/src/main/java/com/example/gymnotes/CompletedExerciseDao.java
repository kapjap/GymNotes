package com.example.gymnotes;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CompletedExerciseDao {

    @Insert
    void insertAll(List<CompletedExerciseEntity> exercises);

    @Query("SELECT * FROM completed_exercises WHERE completed_workout_id = :completedWorkoutId")
    List<CompletedExerciseEntity> getExercisesByCompletedWorkout(int completedWorkoutId);

    @Query("SELECT ce.* FROM completed_exercises ce " +
            "INNER JOIN completed_workouts cw ON ce.completed_workout_id = cw.id " +
            "WHERE cw.user_id = :userId")
    List<CompletedExerciseEntity> getAllCompletedExercisesByUser(String userId);

    @Query("DELETE FROM completed_exercises WHERE completed_workout_id IN " +
            "(SELECT id FROM completed_workouts WHERE user_id = :userId)")
    void deleteByUserId(String userId);
}