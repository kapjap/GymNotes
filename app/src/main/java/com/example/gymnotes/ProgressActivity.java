package com.example.gymnotes;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProgressActivity extends AppCompatActivity {

    private ImageView buttonBackStats;

    private TextView textStatsWorkoutCount;
    private TextView textStatsDuration;
    private TextView textStatsLastActivity;

    private TextView textSummaryLastWorkout;
    private TextView textSummaryTotalWeight;
    private TextView textSummaryTotalExercises;
    private TextView textSummaryAverageExercises;

    private TextView textTopExercise1Name;
    private TextView textTopExercise1Weight;
    private TextView textTopExercise2Name;
    private TextView textTopExercise2Weight;
    private TextView textTopExercise3Name;
    private TextView textTopExercise3Weight;

    private ProgressBar progressTop1;
    private ProgressBar progressTop2;
    private ProgressBar progressTop3;

    private AppDatabase appDatabase;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        initViews();

        appDatabase = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "fitpulse_database"
        ).fallbackToDestructiveMigration().build();

        executorService = Executors.newSingleThreadExecutor();

        buttonBackStats.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private void initViews() {
        buttonBackStats = findViewById(R.id.buttonBackStats);

        textStatsWorkoutCount = findViewById(R.id.textStatsWorkoutCount);
        textStatsDuration = findViewById(R.id.textStatsDuration);
        textStatsLastActivity = findViewById(R.id.textStatsLastActivity);

        textSummaryLastWorkout = findViewById(R.id.textSummaryLastWorkout);
        textSummaryTotalWeight = findViewById(R.id.textSummaryTotalWeight);
        textSummaryTotalExercises = findViewById(R.id.textSummaryTotalExercises);
        textSummaryAverageExercises = findViewById(R.id.textSummaryAverageExercises);

        textTopExercise1Name = findViewById(R.id.textTopExercise1Name);
        textTopExercise1Weight = findViewById(R.id.textTopExercise1Weight);
        textTopExercise2Name = findViewById(R.id.textTopExercise2Name);
        textTopExercise2Weight = findViewById(R.id.textTopExercise2Weight);
        textTopExercise3Name = findViewById(R.id.textTopExercise3Name);
        textTopExercise3Weight = findViewById(R.id.textTopExercise3Weight);

        progressTop1 = findViewById(R.id.progressTop1);
        progressTop2 = findViewById(R.id.progressTop2);
        progressTop3 = findViewById(R.id.progressTop3);
    }

    private void loadStatistics() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            showDefaultState();
            return;
        }

        String uid = currentUser.getUid();

        executorService.execute(() -> {
            List<CompletedWorkoutEntity> completedWorkouts;
            try {
                completedWorkouts = appDatabase.completedWorkoutDao().getAllCompletedWorkoutsByUser(uid);
                if (completedWorkouts == null) {
                    completedWorkouts = new ArrayList<>();
                }
            } catch (Exception e) {
                e.printStackTrace();
                completedWorkouts = new ArrayList<>();
            }

            int workoutCount = completedWorkouts.size();
            int totalMinutes = calculateTotalMinutes(completedWorkouts);
            String lastActivity = getLastActivityText(completedWorkouts);
            String lastWorkoutTitle = getLastWorkoutTitle(completedWorkouts);
            double totalWeight = calculateTotalWeight(completedWorkouts);
            int totalExercises = calculateTotalExercises(completedWorkouts);
            int averageExercises = workoutCount == 0 ? 0 : totalExercises / workoutCount;

            List<CompletedExerciseEntity> completedExercises;
            try {
                completedExercises = appDatabase.completedExerciseDao().getAllCompletedExercisesByUser(uid);
                if (completedExercises == null) {
                    completedExercises = new ArrayList<>();
                }
            } catch (Exception e) {
                e.printStackTrace();
                completedExercises = new ArrayList<>();
            }

            List<TopExerciseItem> topExercises = buildTopExercises(completedExercises);

            runOnUiThread(() -> {
                textStatsWorkoutCount.setText(String.valueOf(workoutCount));
                textStatsDuration.setText(formatMinutesShort(totalMinutes));
                textStatsLastActivity.setText(lastActivity);

                textSummaryLastWorkout.setText(lastWorkoutTitle);
                textSummaryTotalWeight.setText(formatWeight(totalWeight) + " кг");
                textSummaryTotalExercises.setText(String.valueOf(totalExercises));
                textSummaryAverageExercises.setText(String.valueOf(averageExercises));

                bindTopExercises(topExercises);
            });
        });
    }

    private void showDefaultState() {
        textStatsWorkoutCount.setText("0");
        textStatsDuration.setText("0м");
        textStatsLastActivity.setText("—");

        textSummaryLastWorkout.setText("Нет данных");
        textSummaryTotalWeight.setText("0 кг");
        textSummaryTotalExercises.setText("0");
        textSummaryAverageExercises.setText("0");

        textTopExercise1Name.setText("Нет данных");
        textTopExercise1Weight.setText("0 кг");
        textTopExercise2Name.setText("Нет данных");
        textTopExercise2Weight.setText("0 кг");
        textTopExercise3Name.setText("Нет данных");
        textTopExercise3Weight.setText("0 кг");

        progressTop1.setProgress(0);
        progressTop2.setProgress(0);
        progressTop3.setProgress(0);
    }

    private int calculateTotalMinutes(List<CompletedWorkoutEntity> workouts) {
        int total = 0;
        for (CompletedWorkoutEntity workout : workouts) {
            total += workout.getDurationMinutes();
        }
        return total;
    }

    private double calculateTotalWeight(List<CompletedWorkoutEntity> workouts) {
        double total = 0;
        for (CompletedWorkoutEntity workout : workouts) {
            total += workout.getTotalWeight();
        }
        return total;
    }

    private int calculateTotalExercises(List<CompletedWorkoutEntity> workouts) {
        int total = 0;
        for (CompletedWorkoutEntity workout : workouts) {
            total += workout.getExerciseCount();
        }
        return total;
    }

    private String getLastWorkoutTitle(List<CompletedWorkoutEntity> workouts) {
        if (workouts == null || workouts.isEmpty()) {
            return "Нет данных";
        }
        return workouts.get(0).getTitle();
    }

    private String getLastActivityText(List<CompletedWorkoutEntity> workouts) {
        if (workouts == null || workouts.isEmpty()) {
            return "—";
        }

        long completedAt = workouts.get(0).getCompletedAt();

        Calendar now = Calendar.getInstance();
        Calendar last = Calendar.getInstance();
        last.setTimeInMillis(completedAt);

        boolean sameYear = now.get(Calendar.YEAR) == last.get(Calendar.YEAR);
        int diffDays = now.get(Calendar.DAY_OF_YEAR) - last.get(Calendar.DAY_OF_YEAR);

        if (sameYear && diffDays == 0) {
            return "сегодня";
        }
        if (sameYear && diffDays == 1) {
            return "вчера";
        }

        long diffMillis = now.getTimeInMillis() - completedAt;
        long days = diffMillis / (24L * 60L * 60L * 1000L);

        if (days <= 0) {
            return "сегодня";
        }
        if (days == 1) {
            return "1д";
        }
        return days + "д";
    }

    private List<TopExerciseItem> buildTopExercises(List<CompletedExerciseEntity> exercises) {
        List<TopExerciseItem> result = new ArrayList<>();

        if (exercises == null || exercises.isEmpty()) {
            return result;
        }

        List<TopExerciseItem> merged = new ArrayList<>();

        for (CompletedExerciseEntity exercise : exercises) {
            if (exercise == null || exercise.getName() == null) {
                continue;
            }

            String name = exercise.getName().trim();
            if (name.isEmpty()) {
                continue;
            }

            boolean found = false;
            for (TopExerciseItem item : merged) {
                if (item.name.equalsIgnoreCase(name)) {
                    if (exercise.getWeight() > item.maxWeight) {
                        item.maxWeight = exercise.getWeight();
                    }
                    found = true;
                    break;
                }
            }

            if (!found) {
                merged.add(new TopExerciseItem(name, exercise.getWeight()));
            }
        }

        merged.sort((a, b) -> Double.compare(b.maxWeight, a.maxWeight));

        int limit = Math.min(3, merged.size());
        for (int i = 0; i < limit; i++) {
            result.add(merged.get(i));
        }

        return result;
    }

    private void bindTopExercises(List<TopExerciseItem> topExercises) {
        double maxWeight = 1.0;

        for (TopExerciseItem item : topExercises) {
            if (item.maxWeight > maxWeight) {
                maxWeight = item.maxWeight;
            }
        }

        bindTopExerciseItem(
                topExercises.size() > 0 ? topExercises.get(0) : null,
                textTopExercise1Name,
                textTopExercise1Weight,
                progressTop1,
                maxWeight
        );

        bindTopExerciseItem(
                topExercises.size() > 1 ? topExercises.get(1) : null,
                textTopExercise2Name,
                textTopExercise2Weight,
                progressTop2,
                maxWeight
        );

        bindTopExerciseItem(
                topExercises.size() > 2 ? topExercises.get(2) : null,
                textTopExercise3Name,
                textTopExercise3Weight,
                progressTop3,
                maxWeight
        );
    }

    private void bindTopExerciseItem(TopExerciseItem item,
                                     TextView nameView,
                                     TextView weightView,
                                     ProgressBar progressBar,
                                     double maxWeight) {
        if (item == null) {
            nameView.setText("Нет данных");
            weightView.setText("0 кг");
            progressBar.setProgress(0);
            return;
        }

        nameView.setText(item.name);
        weightView.setText(formatWeight(item.maxWeight) + " кг");

        int progress = maxWeight <= 0 ? 0 : (int) Math.round((item.maxWeight / maxWeight) * 100.0);
        progressBar.setProgress(progress);
    }

    private String formatMinutesShort(int totalMinutes) {
        if (totalMinutes <= 0) return "0м";
        if (totalMinutes < 60) return totalMinutes + "м";

        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        if (minutes == 0) {
            return hours + "ч";
        }

        return String.format(Locale.getDefault(), "%dч %dм", hours, minutes);
    }

    private String formatWeight(double value) {
        NumberFormat format = NumberFormat.getInstance(new Locale("ru", "RU"));
        format.setMaximumFractionDigits(1);

        if (value == Math.floor(value)) {
            format.setMaximumFractionDigits(0);
        }

        return format.format(value);
    }

    private static class TopExerciseItem {
        String name;
        double maxWeight;

        TopExerciseItem(String name, double maxWeight) {
            this.name = name;
            this.maxWeight = maxWeight;
        }
    }
}