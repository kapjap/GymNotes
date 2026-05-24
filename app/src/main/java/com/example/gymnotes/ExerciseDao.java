package com.example.gymnotes;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ExerciseDao {

    @Insert
    void insert(ExerciseEntity exercise);

    @Query("SELECT * FROM ExerciseEntity WHERE workoutId = :workoutId")
    List<ExerciseEntity> getExercisesByWorkout(int workoutId);

    @Query("DELETE FROM ExerciseEntity WHERE workoutId = :workoutId")
    void deleteByWorkout(int workoutId);

    @Query("SELECT COALESCE(SUM(weight * reps * sets), 0) FROM ExerciseEntity")
    double getTotalWeightLifted();

    @Query("SELECT COALESCE(SUM(e.weight * e.reps * e.sets), 0) " +
            "FROM ExerciseEntity e " +
            "INNER JOIN workouts w ON e.workoutId = w.id " +
            "WHERE w.user_id = :userId")
    double getTotalWeightLiftedByUser(String userId);

    @Query("SELECT COALESCE(SUM(sets * reps), 0) FROM ExerciseEntity")
    int getTotalReps();

    @Query("SELECT COUNT(*) FROM ExerciseEntity")
    int getTotalExercisesCount();

    @Query("SELECT COALESCE(SUM(weight * reps * sets), 0) FROM ExerciseEntity WHERE workoutId = :workoutId")
    double getTotalWeightByWorkout(int workoutId);

    @Query("SELECT * FROM ExerciseEntity " +
            "WHERE workoutId IN (SELECT id FROM workouts WHERE user_id = :userId) " +
            "ORDER BY weight DESC, reps DESC LIMIT 1")
    ExerciseEntity getBestExerciseByUser(String userId);

    @Query("DELETE FROM ExerciseEntity " +
            "WHERE workoutId IN (SELECT id FROM workouts WHERE user_id = :userId)")
    void deleteByUserId(String userId);
}