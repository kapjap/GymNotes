package com.example.gymnotes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<ExerciseEntity> exercises;

    public ExerciseAdapter(List<ExerciseEntity> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseEntity exercise = exercises.get(position);
        holder.nameText.setText(exercise.name);

        String info;
        if (exercise.weight > 0) {
            info = exercise.sets + " подхода × " + exercise.reps + " повторений × " +
                    String.format(Locale.US, "%.0f", exercise.weight) + " кг";
        } else {
            info = exercise.sets + " подхода × " + exercise.reps + " повторений";
        }

        holder.infoText.setText(info);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, infoText;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textExerciseName1);
            infoText = itemView.findViewById(R.id.textExerciseInfo1);
        }
    }
}