package com.example.gymnotes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WorkoutProgressAdapter extends RecyclerView.Adapter<WorkoutProgressAdapter.ProgressViewHolder> {

    public interface OnSetClickListener {
        void onSetClicked(int position, int setIndex);
    }

    private final List<WorkoutProgressModel> items;
    private final OnSetClickListener listener;

    public WorkoutProgressAdapter(List<WorkoutProgressModel> items, OnSetClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_progress, parent, false);
        return new ProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressViewHolder holder, int position) {
        WorkoutProgressModel item = items.get(position);

        holder.textExerciseName.setText(item.getName());
        holder.textExerciseInfo.setText(item.getInfo());

        ImageView[] checks = {
                holder.check1,
                holder.check2,
                holder.check3,
                holder.check4,
                holder.check5
        };

        for (int i = 0; i < checks.length; i++) {
            final int setIndex = i;

            if (i < item.getTotalSets()) {
                checks[i].setVisibility(View.VISIBLE);

                if (i < item.getCompletedSets()) {
                    checks[i].setImageResource(R.drawable.bg_set_done);
                } else {
                    checks[i].setImageResource(R.drawable.bg_set_empty);
                }

                checks[i].setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onSetClicked(position, setIndex);
                    }
                });
            } else {
                checks[i].setVisibility(View.GONE);
                checks[i].setOnClickListener(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {

        TextView textExerciseName, textExerciseInfo;
        ImageView check1, check2, check3, check4, check5;

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);

            textExerciseName = itemView.findViewById(R.id.textExerciseName);
            textExerciseInfo = itemView.findViewById(R.id.textExerciseInfo);
            check1 = itemView.findViewById(R.id.check1);
            check2 = itemView.findViewById(R.id.check2);
            check3 = itemView.findViewById(R.id.check3);
            check4 = itemView.findViewById(R.id.check4);
            check5 = itemView.findViewById(R.id.check5);
        }
    }
}