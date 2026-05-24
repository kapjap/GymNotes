package com.example.gymnotes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecentProgressAdapter extends RecyclerView.Adapter<RecentProgressAdapter.ViewHolder> {

    private final List<WorkoutEntity> workoutList;

    public RecentProgressAdapter(List<WorkoutEntity> workoutList) {
        this.workoutList = workoutList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_progress_workout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutEntity workout = workoutList.get(position);

        holder.textWorkoutTitle.setText(workout.getTitle());

        String durationText = workout.getEstimatedTime() + " мин";
        String weightText = workout.getExerciseCount() + " упр.";

        holder.textWorkoutInfo.setText(durationText + " • " + weightText);
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textWorkoutTitle, textWorkoutInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textWorkoutTitle = itemView.findViewById(R.id.textWorkoutTitle);
            textWorkoutInfo = itemView.findViewById(R.id.textWorkoutInfo);
        }
    }
}