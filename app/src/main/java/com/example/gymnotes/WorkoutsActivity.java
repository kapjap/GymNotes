package com.example.gymnotes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
import java.util.List;

public class WorkoutsActivity extends AppCompatActivity {

    private RecyclerView recyclerWorkouts;
    private ImageView buttonBack;
    private ImageView buttonAddWorkout;
    private CardView bannerCard;

    private WorkoutAdapter workoutAdapter;
    private ArrayList<WorkoutEntity> workoutList;

    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workouts);

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

        recyclerWorkouts = findViewById(R.id.recyclerWorkouts);
        buttonBack = findViewById(R.id.buttonBack);
        buttonAddWorkout = findViewById(R.id.buttonAddWorkout);
        bannerCard = findViewById(R.id.bannerCard);

        recyclerWorkouts.setLayoutManager(new LinearLayoutManager(this));

        workoutList = new ArrayList<>();
        workoutAdapter = new WorkoutAdapter(workoutList, new WorkoutAdapter.OnWorkoutClickListener() {
            @Override
            public void onStartClick(WorkoutEntity workout) {
                Intent intent = new Intent(WorkoutsActivity.this, WorkoutSessionActivity.class);
                intent.putExtra("workout_id", workout.getId());
                intent.putExtra("workout_title", workout.getTitle());
                intent.putExtra("workout_time", workout.getEstimatedTime());
                startActivity(intent);
            }

            @Override
            public void onItemClick(WorkoutEntity workout) {
                Toast.makeText(WorkoutsActivity.this,
                        workout.getTitle(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(WorkoutEntity workout) {
                showDeleteDialog(workout);
            }
        });

        recyclerWorkouts.setAdapter(workoutAdapter);

        appDatabase = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "fitpulse_database"
        ).fallbackToDestructiveMigration().build();

        buttonBack.setOnClickListener(v -> finish());

        buttonAddWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutsActivity.this, CreateWorkoutActivity.class);
            startActivity(intent);
        });

        bannerCard.setOnClickListener(v ->
                Toast.makeText(this, "Готов к тренировке 💪", Toast.LENGTH_SHORT).show()
        );

        loadWorkouts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWorkouts();
    }

    private void loadWorkouts() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            workoutAdapter.updateList(new ArrayList<>());
            return;
        }

        String uid = currentUser.getUid();

        new Thread(() -> {
            List<WorkoutEntity> workouts = appDatabase.workoutDao().getAllWorkoutsByUser(uid);
            runOnUiThread(() -> workoutAdapter.updateList(new ArrayList<>(workouts)));
        }).start();
    }

    private void showDeleteDialog(WorkoutEntity workout) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление тренировки")
                .setMessage("Удалить тренировку \"" + workout.getTitle() + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteWorkout(workout))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteWorkout(WorkoutEntity workout) {
        new Thread(() -> {
            appDatabase.exerciseDao().deleteByWorkout(workout.getId());
            appDatabase.workoutDao().deleteWorkout(workout);

            runOnUiThread(() -> {
                Toast.makeText(
                        WorkoutsActivity.this,
                        "Тренировка удалена",
                        Toast.LENGTH_SHORT
                ).show();
                loadWorkouts();
            });
        }).start();
    }
}