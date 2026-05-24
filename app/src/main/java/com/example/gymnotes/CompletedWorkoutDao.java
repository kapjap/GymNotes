package com.example.gymnotes;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CompletedWorkoutDao {

    @Insert
    long insertCompletedWorkout(CompletedWorkoutEntity completedWorkout);

    @Query("SELECT * FROM completed_workouts WHERE user_id = :userId ORDER BY completed_at DESC")
    List<CompletedWorkoutEntity> getAllCompletedWorkoutsByUser(String userId);

    @Query("SELECT * FROM completed_workouts WHERE user_id = :userId ORDER BY completed_at DESC LIMIT 1")
    CompletedWorkoutEntity getLastCompletedWorkoutByUser(String userId);

    @Query("SELECT COUNT(*) FROM completed_workouts WHERE user_id = :userId")
    int getCompletedWorkoutCountByUser(String userId);

    @Query("SELECT COALESCE(SUM(duration_minutes), 0) FROM completed_workouts WHERE user_id = :userId")
    int getTotalCompletedMinutesByUser(String userId);

    @Query("SELECT COALESCE(SUM(total_weight), 0) FROM completed_workouts WHERE user_id = :userId")
    double getTotalCompletedWeightByUser(String userId);

    @Query("SELECT COALESCE(SUM(exercise_count), 0) FROM completed_workouts WHERE user_id = :userId")
    int getTotalCompletedExerciseCountByUser(String userId);

    @Query("DELETE FROM completed_workouts WHERE user_id = :userId")
    void deleteByUserId(String userId);
}