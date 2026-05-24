package com.example.gymnotes;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutSessionActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "fitpulse_settings";
    private static final String KEY_AUTO_REST = "auto_rest";
    private static final String KEY_REST_SOUND = "rest_sound";
    private static final String KEY_VIBRATION = "vibration";
    private static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
    private static final String KEY_REST_DURATION = "rest_duration";

    private View rootView;

    private ImageView buttonBack;
    private ImageView buttonPause;
    private TextView textWorkoutTitle;
    private TextView textWorkoutTimer;
    private RecyclerView recyclerWorkoutExercises;
    private TextView textRestTimer;
    private TextView textRestTotal;
    private TextView buttonStartRest;
    private TextView buttonSkipRest;
    private TextView buttonFinishWorkout;

    private WorkoutProgressAdapter adapter;
    private ArrayList<WorkoutProgressModel> exerciseList;

    private AppDatabase appDatabase;

    private int workoutId;
    private String workoutTitle;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private int workoutSeconds = 0;
    private int totalRestSeconds = 60;
    private int restSeconds = 0;

    private boolean isPaused = false;
    private boolean isRestRunning = false;
    private boolean isFinishingWorkout = false;

    private boolean autoRestEnabled = false;
    private boolean vibrationEnabled = false;
    private boolean restSoundEnabled = false;
    private boolean keepScreenOnEnabled = false;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isPaused) {
                workoutSeconds++;

                if (isRestRunning && restSeconds > 0) {
                    restSeconds--;

                    if (restSeconds <= 0) {
                        restSeconds = 0;
                        isRestRunning = false;

                        runOnUiThread(() -> {
                            updateTimers();
                            updateRestButtons();
                            onRestFinished();
                        });
                    }
                }

                updateTimers();
                updateRestButtons();
            }

            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout_session);

        rootView = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    systemBars.top,
                    v.getPaddingRight(),
                    systemBars.bottom
            );
            return insets;
        });

        buttonBack = findViewById(R.id.buttonBack);
        buttonPause = findViewById(R.id.buttonPause);
        textWorkoutTitle = findViewById(R.id.textWorkoutTitle);
        textWorkoutTimer = findViewById(R.id.textWorkoutTimer);
        recyclerWorkoutExercises = findViewById(R.id.recyclerWorkoutExercises);
        textRestTimer = findViewById(R.id.textRestTimer);
        textRestTotal = findViewById(R.id.textRestTotal);
        buttonStartRest = findViewById(R.id.buttonStartRest);
        buttonSkipRest = findViewById(R.id.buttonSkipRest);
        buttonFinishWorkout = findViewById(R.id.buttonFinishWorkout);

        appDatabase = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "fitpulse_database"
        ).fallbackToDestructiveMigration().build();

        workoutId = getIntent().getIntExtra("workout_id", -1);
        workoutTitle = getIntent().getStringExtra("workout_title");

        if (workoutTitle == null || workoutTitle.trim().isEmpty()) {
            workoutTitle = "Тренировка";
        }

        textWorkoutTitle.setText(workoutTitle);

        loadUserSettings();
        applyKeepScreenOn();

        exerciseList = new ArrayList<>();
        adapter = new WorkoutProgressAdapter(exerciseList, (position, setIndex) -> {
            WorkoutProgressModel item = exerciseList.get(position);
            int oldCompletedSets = item.getCompletedSets();

            int newCompletedSets;
            if (oldCompletedSets == setIndex + 1) {
                newCompletedSets = setIndex;
            } else {
                newCompletedSets = setIndex + 1;
            }

            item.setCompletedSets(newCompletedSets);
            adapter.notifyItemChanged(position);

            boolean setWasMarkedCompleted = newCompletedSets > oldCompletedSets;

            if (setWasMarkedCompleted && autoRestEnabled && !isPaused) {
                startRestAutomatically();
            }
        });

        recyclerWorkoutExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerWorkoutExercises.setAdapter(adapter);

        loadWorkoutRest();
        loadExercises();

        updateTimers();
        updateRestButtons();
        handler.post(timerRunnable);

        buttonBack.setOnClickListener(v -> finish());

        buttonPause.setOnClickListener(v -> {
            isPaused = !isPaused;

            if (isPaused) {
                buttonPause.setImageResource(R.drawable.baseline_play_arrow_24);
                Toast.makeText(this, "Тренировка на паузе", Toast.LENGTH_SHORT).show();
            } else {
                buttonPause.setImageResource(R.drawable.baseline_pause_24);
                Toast.makeText(this, "Тренировка продолжена", Toast.LENGTH_SHORT).show();
            }
        });

        buttonStartRest.setOnClickListener(v -> {
            if (isPaused) {
                Toast.makeText(this, "Сначала снимите тренировку с паузы", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isRestRunning) {
                Toast.makeText(this, "Отдых уже идёт", Toast.LENGTH_SHORT).show();
                return;
            }

            startRest();
            Toast.makeText(this, "Отдых начался", Toast.LENGTH_SHORT).show();
        });

        buttonSkipRest.setOnClickListener(v -> {
            if (!isRestRunning) {
                Toast.makeText(this, "Сейчас отдых не запущен", Toast.LENGTH_SHORT).show();
                return;
            }

            restSeconds = 0;
            isRestRunning = false;
            updateTimers();
            updateRestButtons();
            Toast.makeText(this, "Отдых пропущен", Toast.LENGTH_SHORT).show();
        });

        buttonFinishWorkout.setOnClickListener(v -> finishWorkoutAndSave());
    }

    private void finishWorkoutAndSave() {
        if (isFinishingWorkout) {
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        isFinishingWorkout = true;
        buttonFinishWorkout.setEnabled(false);
        buttonFinishWorkout.setAlpha(0.6f);

        new Thread(() -> {
            try {
                List<ExerciseEntity> exercises = appDatabase.exerciseDao().getExercisesByWorkout(workoutId);

                if (exercises == null) {
                    exercises = new ArrayList<>();
                }

                double totalWeight = 0;
                for (ExerciseEntity ex : exercises) {
                    totalWeight += ex.weight * ex.reps * ex.sets;
                }

                int completedMinutes = Math.max(1, (workoutSeconds + 59) / 60);

                CompletedWorkoutEntity completedWorkout = new CompletedWorkoutEntity(
                        workoutId,
                        currentUser.getUid(),
                        workoutTitle,
                        completedMinutes,
                        totalWeight,
                        exercises.size(),
                        System.currentTimeMillis()
                );

                long completedWorkoutId = appDatabase.completedWorkoutDao()
                        .insertCompletedWorkout(completedWorkout);

                List<CompletedExerciseEntity> completedExercises = new ArrayList<>();
                for (ExerciseEntity ex : exercises) {
                    completedExercises.add(new CompletedExerciseEntity(
                            (int) completedWorkoutId,
                            ex.name,
                            ex.sets,
                            ex.reps,
                            ex.weight
                    ));
                }

                if (!completedExercises.isEmpty()) {
                    appDatabase.completedExerciseDao().insertAll(completedExercises);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Тренировка завершена", Toast.LENGTH_SHORT).show();
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    isFinishingWorkout = false;
                    buttonFinishWorkout.setEnabled(true);
                    buttonFinishWorkout.setAlpha(1f);
                    Toast.makeText(this, "Ошибка сохранения завершённой тренировки", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadUserSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        autoRestEnabled = prefs.getBoolean(getUserKey(KEY_AUTO_REST), false);
        vibrationEnabled = prefs.getBoolean(getUserKey(KEY_VIBRATION), false);
        restSoundEnabled = prefs.getBoolean(getUserKey(KEY_REST_SOUND), false);
        keepScreenOnEnabled = prefs.getBoolean(getUserKey(KEY_KEEP_SCREEN_ON), false);

        int settingsRestDuration = prefs.getInt(getUserKey(KEY_REST_DURATION), 60);

        if (totalRestSeconds <= 0) {
            totalRestSeconds = settingsRestDuration;
        }
    }

    private String getUserKey(String baseKey) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "guest";
        return baseKey + "_" + uid;
    }

    private void applyKeepScreenOn() {
        if (keepScreenOnEnabled) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            if (rootView != null) {
                rootView.setKeepScreenOn(true);
            }
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            if (rootView != null) {
                rootView.setKeepScreenOn(false);
            }
        }
    }

    private void startRestAutomatically() {
        if (isRestRunning) {
            return;
        }

        startRest();
        Toast.makeText(this, "Автоотдых начался", Toast.LENGTH_SHORT).show();
    }

    private void startRest() {
        restSeconds = totalRestSeconds;
        isRestRunning = true;
        updateTimers();
        updateRestButtons();
    }

    private void onRestFinished() {
        playRestSoundIfNeeded();
        vibrateIfNeeded();
        Toast.makeText(this, "Отдых завершён", Toast.LENGTH_SHORT).show();
    }

    private void playRestSoundIfNeeded() {
        if (!restSoundEnabled) return;

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (notification == null) {
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }

            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
            if (ringtone != null) {
                ringtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void vibrateIfNeeded() {
        if (!vibrationEnabled) return;

        try {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator == null) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(400);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadWorkoutRest() {
        new Thread(() -> {
            WorkoutEntity workout = appDatabase.workoutDao().getWorkoutById(workoutId);

            if (workout != null) {
                int workoutRest = workout.getRestSeconds();

                if (workoutRest > 0) {
                    totalRestSeconds = workoutRest;
                } else {
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    totalRestSeconds = prefs.getInt(getUserKey(KEY_REST_DURATION), 60);
                }
            } else {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                totalRestSeconds = prefs.getInt(getUserKey(KEY_REST_DURATION), 60);
            }

            runOnUiThread(this::updateTimers);
        }).start();
    }

    private void loadExercises() {
        new Thread(() -> {
            List<ExerciseEntity> exercises = appDatabase.exerciseDao().getExercisesByWorkout(workoutId);
            ArrayList<WorkoutProgressModel> loadedList = new ArrayList<>();

            for (ExerciseEntity ex : exercises) {
                String info = ex.sets + " подхода × " + ex.reps + " повторений";
                loadedList.add(new WorkoutProgressModel(
                        ex.id,
                        ex.name,
                        info,
                        0,
                        ex.sets
                ));
            }

            runOnUiThread(() -> {
                exerciseList.clear();
                exerciseList.addAll(loadedList);
                adapter.notifyDataSetChanged();

                if (exerciseList.isEmpty()) {
                    Toast.makeText(this, "В этой тренировке пока нет упражнений", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void updateTimers() {
        textWorkoutTimer.setText(formatTime(workoutSeconds));
        textRestTimer.setText(formatTime(restSeconds));
        textRestTotal.setText("из " + totalRestSeconds + " сек");
    }

    private void updateRestButtons() {
        if (isRestRunning) {
            buttonStartRest.setAlpha(0.5f);
            buttonSkipRest.setAlpha(1f);
        } else {
            buttonStartRest.setAlpha(1f);
            buttonSkipRest.setAlpha(0.5f);
        }
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserSettings();
        applyKeepScreenOn();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rootView != null) {
            rootView.setKeepScreenOn(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
    }
}