package com.example.gymnotes;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WorkoutDao {

    @Insert
    long insertWorkout(WorkoutEntity workout);

    @Delete
    void deleteWorkout(WorkoutEntity workout);

    @Query("SELECT * FROM workouts WHERE user_id = :userId ORDER BY created_at DESC")
    List<WorkoutEntity> getAllWorkoutsByUser(String userId);

    @Query("SELECT * FROM workouts WHERE id = :workoutId LIMIT 1")
    WorkoutEntity getWorkoutById(int workoutId);

    @Query("SELECT COUNT(*) FROM workouts WHERE user_id = :userId")
    int getWorkoutCountByUser(String userId);

    @Query("SELECT COALESCE(SUM(estimatedTime), 0) FROM workouts WHERE user_id = :userId")
    int getTotalWorkoutMinutesByUser(String userId);

    @Query("SELECT * FROM workouts WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    List<WorkoutEntity> getLastWorkoutsByUser(String userId, int limit);

    @Query("DELETE FROM workouts WHERE user_id = :userId")
    void deleteAllWorkoutsByUser(String userId);
}