package com.example.gymnotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class CreateWorkoutActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "fitpulse_settings";
    private static final String KEY_REST_DURATION = "rest_duration";

    private ImageView buttonBack;
    private TextView buttonSaveWorkout;
    private EditText editWorkoutTitle;
    private EditText editWorkoutTime;
    private EditText editWorkoutRest;

    private CardView cardAddExercise;
    private RecyclerView recyclerExercises;

    private ArrayList<ExerciseEntity> exerciseList;
    private ExerciseAdapter adapter;

    private AppDatabase appDatabase;

    private static final int ADD_EXERCISE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_workout);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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
        buttonSaveWorkout = findViewById(R.id.buttonSaveWorkout);
        editWorkoutTitle = findViewById(R.id.editWorkoutTitle);
        editWorkoutTime = findViewById(R.id.editWorkoutTime);
        editWorkoutRest = findViewById(R.id.editWorkoutRest);
        cardAddExercise = findViewById(R.id.cardAddExercise);
        recyclerExercises = findViewById(R.id.recyclerExercises);

        appDatabase = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "fitpulse_database"
        ).fallbackToDestructiveMigration().build();

        exerciseList = new ArrayList<>();
        adapter = new ExerciseAdapter(exerciseList);

        recyclerExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerExercises.setAdapter(adapter);

        loadRestDurationFromSettings();

        buttonBack.setOnClickListener(v -> finish());

        cardAddExercise.setOnClickListener(v -> {
            Intent intent = new Intent(CreateWorkoutActivity.this, AddExerciseActivity.class);
            startActivityForResult(intent, ADD_EXERCISE_REQUEST);
        });

        buttonSaveWorkout.setOnClickListener(v -> saveWorkout());
    }

    private void loadRestDurationFromSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedRest = prefs.getInt(getUserKey(KEY_REST_DURATION), 60);
        editWorkoutRest.setText(String.valueOf(savedRest));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_EXERCISE_REQUEST && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("name");
            int sets = data.getIntExtra("sets", 0);
            int reps = data.getIntExtra("reps", 0);
            double weight = data.getDoubleExtra("weight", 0);

            if (name != null && !name.trim().isEmpty()) {
                ExerciseEntity exercise = new ExerciseEntity();
                exercise.name = name;
                exercise.sets = sets;
                exercise.reps = reps;
                exercise.weight = weight;

                exerciseList.add(exercise);
                adapter.notifyItemInserted(exerciseList.size() - 1);
            }
        }
    }

    private void saveWorkout() {
        String title = editWorkoutTitle.getText().toString().trim();
        String timeText = editWorkoutTime.getText().toString().trim();
        String restText = editWorkoutRest.getText().toString().trim();

        if (title.isEmpty()) {
            editWorkoutTitle.setError("Введите название тренировки");
            return;
        }

        if (timeText.isEmpty()) {
            editWorkoutTime.setError("Введите время тренировки");
            return;
        }

        if (restText.isEmpty()) {
            editWorkoutRest.setError("Введите отдых");
            return;
        }

        if (exerciseList.isEmpty()) {
            Toast.makeText(this, "Добавьте хотя бы одно упражнение", Toast.LENGTH_SHORT).show();
            return;
        }

        int estimatedTime;
        int restSeconds;

        try {
            estimatedTime = Integer.parseInt(timeText);
        } catch (NumberFormatException e) {
            editWorkoutTime.setError("Введите корректное число");
            return;
        }

        try {
            restSeconds = Integer.parseInt(restText);
        } catch (NumberFormatException e) {
            editWorkoutRest.setError("Введите корректное число");
            return;
        }

        if (restSeconds <= 0) {
            editWorkoutRest.setError("Отдых должен быть больше 0");
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Сначала войдите в аккаунт", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        int finalEstimatedTime = estimatedTime;
        int finalRestSeconds = restSeconds;

        new Thread(() -> {
            long workoutId = appDatabase.workoutDao().insertWorkout(
                    new WorkoutEntity(
                            title,
                            exerciseList.size(),
                            finalEstimatedTime,
                            finalRestSeconds,
                            uid
                    )
            );

            for (ExerciseEntity ex : exerciseList) {
                ex.workoutId = (int) workoutId;
                appDatabase.exerciseDao().insert(ex);
            }

            runOnUiThread(() -> {
                Toast.makeText(CreateWorkoutActivity.this, "Тренировка сохранена 💪", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private String getUserKey(String baseKey) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "guest";
        return baseKey + "_" + uid;
    }
}