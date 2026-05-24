package com.example.gymnotes;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class HomeMenuActivity extends AppCompatActivity {

    private LinearLayout profileContainer;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_menu);

        auth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FrameLayout cardWorkouts = findViewById(R.id.cardWorkouts);
        cardWorkouts.setOnClickListener(v -> {
            Intent intent = new Intent(HomeMenuActivity.this, WorkoutsActivity.class);
            startActivity(intent);
        });

        LinearLayout cardNotes = findViewById(R.id.cardNotes);
        cardNotes.setOnClickListener(v -> {
            Intent intent = new Intent(HomeMenuActivity.this, NotesMain.class);
            startActivity(intent);
        });

        LinearLayout cardNews = findViewById(R.id.cardNews);
        cardNews.setOnClickListener(v -> {
            Intent intent = new Intent(HomeMenuActivity.this, News.class);
            startActivity(intent);
        });


        LinearLayout cardSettings = findViewById(R.id.cardSettings);
        cardSettings.setOnClickListener(v -> {
            Intent intent = new Intent(HomeMenuActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        LinearLayout cardProgress = findViewById(R.id.cardProgress);

        cardProgress.setOnClickListener(v -> {
            startActivity(new Intent(HomeMenuActivity.this, ProgressActivity.class));
        });
        ImageView buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> {
            auth.signOut();

            Intent intent = new Intent(HomeMenuActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        FrameLayout cardGyms = findViewById(R.id.cardGyms);

        cardGyms.setOnClickListener(v -> {
            Intent intent = new Intent(HomeMenuActivity.this, GymsNearbyActivity.class);
            startActivity(intent);
        });
        profileContainer = findViewById(R.id.profileContainer);


        profileContainer.setOnClickListener(v -> {

            Intent intent = new Intent(HomeMenuActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }


}