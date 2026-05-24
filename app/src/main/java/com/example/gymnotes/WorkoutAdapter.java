package com.example.gymnotes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    public interface OnWorkoutClickListener {
        void onStartClick(WorkoutEntity workout);
        void onItemClick(WorkoutEntity workout);
        void onItemLongClick(WorkoutEntity workout);
    }

    private final ArrayList<WorkoutEntity> workoutList;
    private final OnWorkoutClickListener listener;

    public WorkoutAdapter(ArrayList<WorkoutEntity> workoutList, OnWorkoutClickListener listener) {
        this.workoutList = workoutList;
        this.listener = listener;
    }

    public void updateList(ArrayList<WorkoutEntity> newList) {
        workoutList.clear();
        workoutList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        WorkoutEntity workout = workoutList.get(position);

        holder.textWorkoutTitle.setText(workout.getTitle());
        holder.textWorkoutInfo.setText(workout.getExerciseCount() + " упражнений");
        holder.textWorkoutTime.setText("~" + workout.getEstimatedTime() + " мин");

        holder.itemView.setOnClickListener(v -> listener.onItemClick(workout));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(workout);
            return true;
        });

        holder.buttonStart.setOnClickListener(v -> listener.onStartClick(workout));
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {

        TextView textWorkoutTitle;
        TextView textWorkoutInfo;
        TextView textWorkoutTime;
        TextView buttonStart;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            textWorkoutTitle = itemView.findViewById(R.id.textWorkoutTitle);
            textWorkoutInfo = itemView.findViewById(R.id.textWorkoutInfo);
            textWorkoutTime = itemView.findViewById(R.id.textWorkoutTime);
            buttonStart = itemView.findViewById(R.id.buttonStart);
        }
    }
}