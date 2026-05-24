package com.example.gymnotes;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "profile_prefs";
    private static final String KEY_AVATAR_URI_PREFIX = "avatar_uri_";
    private static final String KEY_WEIGHT_PREFIX = "weight_";
    private static final String KEY_HEIGHT_PREFIX = "height_";
    private static final String KEY_AGE_PREFIX = "age_";

    private ImageView buttonBackProfile;
    private ImageView imageProfileAvatar;
    private Button buttonEditProfile;
    private Button buttonLogoutProfile;
    private Button buttonEditName;

    private TextView textProfileName;
    private TextView textProfileEmail;

    // Новый блок вместо статистики
    private TextView textWeightValue;
    private TextView textHeightValue;
    private TextView textAgeValue;

    private LinearLayout itemChangePassword;
    private LinearLayout itemWorkoutHistory;
    private LinearLayout itemExportProgress;
    private LinearLayout itemDeleteAccount;

    private FirebaseAuth auth;
    private AppDatabase db;
    private SharedPreferences preferences;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String[]> avatarPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {
                    }

                    saveAvatarUri(uri.toString());
                    imageProfileAvatar.setImageURI(uri);
                    Toast.makeText(this, "Аватар обновлён", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();

        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "fitpulse_database"
        ).fallbackToDestructiveMigration().build();

        loadUserInfo();
        loadSavedAvatar();
        loadPhysicalData(); // <-- вместо loadStatistics()
        initClicks();
    }

    private void initViews() {
        buttonBackProfile = findViewById(R.id.buttonBackProfile);
        imageProfileAvatar = findViewById(R.id.imageProfileAvatar);
        buttonEditProfile = findViewById(R.id.buttonEditProfile);
        buttonLogoutProfile = findViewById(R.id.buttonLogoutProfile);

        textProfileName = findViewById(R.id.textProfileName);
        textProfileEmail = findViewById(R.id.textProfileEmail);

        // Новые TextView
        textWeightValue = findViewById(R.id.textWeightValue);
        textHeightValue = findViewById(R.id.textHeightValue);
        textAgeValue = findViewById(R.id.textAgeValue);

        itemChangePassword = findViewById(R.id.itemChangePassword);
        itemWorkoutHistory = findViewById(R.id.itemWorkoutHistory);
        itemExportProgress = findViewById(R.id.itemExportProgress);
        itemDeleteAccount = findViewById(R.id.itemDeleteAccount);
        buttonEditName = findViewById(R.id.buttonEditName);
    }

    private void initClicks() {
        buttonBackProfile.setOnClickListener(v -> finish());

        imageProfileAvatar.setOnClickListener(v -> openAvatarPicker());

        // Можешь оставить редактирование имени
        // или заменить на showEditPhysicalDataDialog()
        buttonEditProfile.setOnClickListener(v -> showEditPhysicalDataDialog());

        buttonLogoutProfile.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(ProfileActivity.this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        buttonEditName.setOnClickListener(v -> showEditNameDialog());
        itemChangePassword.setOnClickListener(v -> sendPasswordReset());

        itemWorkoutHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, WorkoutsActivity.class);
            startActivity(intent);
        });

        itemExportProgress.setOnClickListener(v -> exportProgress());

        itemDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void loadUserInfo() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            textProfileName.setText("Пользователь");
            textProfileEmail.setText("Нет данных");
            return;
        }

        String displayName = user.getDisplayName();
        String email = user.getEmail();

        textProfileName.setText(getPrettyName(displayName, email));
        textProfileEmail.setText(email != null ? email : "Нет почты");
    }

    private String getPrettyName(String displayName, String email) {
        if (!TextUtils.isEmpty(displayName)) {
            return displayName.trim();
        }

        if (!TextUtils.isEmpty(email) && email.contains("@")) {
            String login = email.substring(0, email.indexOf("@")).trim();

            if (login.isEmpty()) {
                return "Пользователь";
            }

            login = login.replace(".", " ");
            login = login.replace("_", " ");
            login = login.replace("-", " ");

            String[] parts = login.split("\\s+");
            StringBuilder pretty = new StringBuilder();

            for (String part : parts) {
                if (!part.isEmpty()) {
                    pretty.append(part.substring(0, 1).toUpperCase(Locale.getDefault()))
                            .append(part.substring(1))
                            .append(" ");
                }
            }

            String result = pretty.toString().trim();
            return result.isEmpty() ? "Пользователь" : result;
        }

        return "Пользователь";
    }

    private void showEditNameDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменить имя");

        final EditText editText = new EditText(this);
        editText.setHint("Введите имя");
        editText.setText(textProfileName.getText().toString());
        editText.setPadding(40, 30, 40, 30);

        builder.setView(editText);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String newName = editText.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "Имя не может быть пустым", Toast.LENGTH_SHORT).show();
                return;
            }

            UserProfileChangeRequest profileUpdates =
                    new UserProfileChangeRequest.Builder()
                            .setDisplayName(newName)
                            .build();

            user.updateProfile(profileUpdates)
                    .addOnSuccessListener(unused -> {
                        textProfileName.setText(newName);
                        Toast.makeText(this, "Имя обновлено", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showEditPhysicalDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменить данные");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 10);

        final EditText editWeight = new EditText(this);
        editWeight.setHint("Вес (кг)");
        editWeight.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        editWeight.setText(getSavedWeight());

        final EditText editHeight = new EditText(this);
        editHeight.setHint("Рост (см)");
        editHeight.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        editHeight.setText(getSavedHeight());

        final EditText editAge = new EditText(this);
        editAge.setHint("Возраст");
        editAge.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        editAge.setText(getSavedAge());

        layout.addView(editWeight);
        layout.addView(editHeight);
        layout.addView(editAge);

        builder.setView(layout);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String weight = editWeight.getText().toString().trim();
            String height = editHeight.getText().toString().trim();
            String age = editAge.getText().toString().trim();

            savePhysicalData(weight, height, age);
            loadPhysicalData();

            Toast.makeText(this, "Данные обновлены", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void openAvatarPicker() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Сначала войдите в аккаунт", Toast.LENGTH_SHORT).show();
            return;
        }
        avatarPickerLauncher.launch(new String[]{"image/*"});
    }

    private String getCurrentUid() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    private String getAvatarKey() {
        String uid = getCurrentUid();
        if (uid == null) {
            return null;
        }
        return KEY_AVATAR_URI_PREFIX + uid;
    }

    private String getWeightKey() {
        String uid = getCurrentUid();
        return uid == null ? null : KEY_WEIGHT_PREFIX + uid;
    }

    private String getHeightKey() {
        String uid = getCurrentUid();
        return uid == null ? null : KEY_HEIGHT_PREFIX + uid;
    }

    private String getAgeKey() {
        String uid = getCurrentUid();
        return uid == null ? null : KEY_AGE_PREFIX + uid;
    }

    private void saveAvatarUri(String uri) {
        String avatarKey = getAvatarKey();
        if (avatarKey == null) return;
        preferences.edit().putString(avatarKey, uri).apply();
    }

    private void loadSavedAvatar() {
        String avatarKey = getAvatarKey();

        if (avatarKey == null) {
            imageProfileAvatar.setImageResource(R.drawable.ic_profile_placeholder);
            return;
        }

        String uriString = preferences.getString(avatarKey, null);

        if (uriString != null) {
            try {
                imageProfileAvatar.setImageURI(Uri.parse(uriString));
                return;
            } catch (Exception ignored) {
            }
        }

        imageProfileAvatar.setImageResource(R.drawable.ic_profile_placeholder);
    }

    private void savePhysicalData(String weight, String height, String age) {
        String weightKey = getWeightKey();
        String heightKey = getHeightKey();
        String ageKey = getAgeKey();

        if (weightKey == null || heightKey == null || ageKey == null) return;

        preferences.edit()
                .putString(weightKey, weight.isEmpty() ? "82" : weight)
                .putString(heightKey, height.isEmpty() ? "181" : height)
                .putString(ageKey, age.isEmpty() ? "24" : age)
                .apply();
    }

    private String getSavedWeight() {
        String key = getWeightKey();
        return key == null ? "82" : preferences.getString(key, "82");
    }

    private String getSavedHeight() {
        String key = getHeightKey();
        return key == null ? "181" : preferences.getString(key, "181");
    }

    private String getSavedAge() {
        String key = getAgeKey();
        return key == null ? "24" : preferences.getString(key, "24");
    }

    private void loadPhysicalData() {
        textWeightValue.setText(getSavedWeight());
        textHeightValue.setText(getSavedHeight());
        textAgeValue.setText(getSavedAge());
    }

    private void exportProgress() {
        String uid = getCurrentUid();

        if (uid == null) {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            int workoutCount = 0;
            int totalMinutes = 0;
            double totalWeight = 0;

            try {
                workoutCount = db.workoutDao().getWorkoutCountByUser(uid);
                totalMinutes = db.workoutDao().getTotalWorkoutMinutesByUser(uid);
                totalWeight = db.exerciseDao().getTotalWeightLiftedByUser(uid);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int finalWorkoutCount = workoutCount;
            int finalTotalMinutes = totalMinutes;
            double finalTotalWeight = totalWeight;

            runOnUiThread(() -> {
                String exportText =
                        "Мой прогресс в FitPulse 💪\n\n" +
                                "Тренировок: " + finalWorkoutCount + "\n" +
                                "Поднято: " + formatWeight(finalTotalWeight) + "\n" +
                                "Время в зале: " + formatDuration(finalTotalMinutes);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Прогресс FitPulse");
                shareIntent.putExtra(Intent.EXTRA_TEXT, exportText);

                startActivity(Intent.createChooser(shareIntent, "Экспорт прогресса"));
            });
        });
    }

    private String formatWeight(double weight) {
        NumberFormat format = NumberFormat.getInstance(new Locale("ru", "RU"));
        format.setMaximumFractionDigits(0);
        return format.format(weight) + " кг";
    }

    private String formatDuration(int totalMinutes) {
        if (totalMinutes <= 0) {
            return "0 мин";
        }

        if (totalMinutes < 60) {
            return totalMinutes + " мин";
        }

        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        if (minutes == 0) {
            return hours + " ч";
        }

        return hours + " ч " + minutes + " мин";
    }

    private void sendPasswordReset() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Почта не найдена", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(user.getEmail())
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Письмо для смены пароля отправлено", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Удалить аккаунт")
                .setMessage("Вы уверены, что хотите удалить аккаунт?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteAccount())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteAccount() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        String avatarKey = KEY_AVATAR_URI_PREFIX + uid;
        String weightKey = KEY_WEIGHT_PREFIX + uid;
        String heightKey = KEY_HEIGHT_PREFIX + uid;
        String ageKey = KEY_AGE_PREFIX + uid;

        user.delete()
                .addOnSuccessListener(unused -> executor.execute(() -> {
                    try {
                        db.exerciseDao().deleteByUserId(uid);
                        db.workoutDao().deleteAllWorkoutsByUser(uid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    preferences.edit()
                            .remove(avatarKey)
                            .remove(weightKey)
                            .remove(heightKey)
                            .remove(ageKey)
                            .apply();

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Аккаунт удалён", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Не удалось удалить аккаунт: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
        loadSavedAvatar();
        loadPhysicalData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}