package com.example.gymnotes;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "fitpulse_settings";

    private static final String KEY_AUTO_REST = "auto_rest";
    private static final String KEY_REST_SOUND = "rest_sound";
    private static final String KEY_VIBRATION = "vibration";
    private static final String KEY_REST_DURATION = "rest_duration";
    private static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
    private static final String KEY_LOCATION = "location";

    private ImageView buttonBackSettings;
    private Switch switchAutoRest;
    private Switch switchRestSound;
    private Switch switchVibration;
    private Switch switchKeepScreenOn;
    private Switch switchLightTheme;
    private Switch switchLocation;
    private LinearLayout layoutRestDuration;
    private TextView textRestDurationValue;
    private Button buttonSaveSettings;

    private SharedPreferences preferences;
    private int selectedRestDuration = 60;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fineGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                boolean coarseGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                if (fineGranted || coarseGranted) {
                    checkLocationSettingsAndEnable();
                } else {
                    switchLocation.setChecked(false);
                    Toast.makeText(this, "Доступ к геолокации не предоставлен", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<IntentSenderRequest> locationSettingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    switchLocation.setChecked(true);
                    saveLocationOnly(true);
                    Toast.makeText(this, "Геолокация включена", Toast.LENGTH_SHORT).show();
                } else {
                    switchLocation.setChecked(false);
                    saveLocationOnly(false);
                    Toast.makeText(this, "Геолокация не включена", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        initViews();
        loadSettings();
        setupListeners();
        applyKeepScreenOn();
        ThemeManager.applyThemeToActivity(this);
    }

    private void initViews() {
        buttonBackSettings = findViewById(R.id.buttonBackSettings);
        switchAutoRest = findViewById(R.id.switchAutoRest);
        switchRestSound = findViewById(R.id.switchRestSound);
        switchVibration = findViewById(R.id.switchVibration);
        switchKeepScreenOn = findViewById(R.id.switchKeepScreenOn);
        switchLightTheme = findViewById(R.id.switchLightTheme);
        switchLocation = findViewById(R.id.switchLocation);
        layoutRestDuration = findViewById(R.id.layoutRestDuration);
        textRestDurationValue = findViewById(R.id.textRestDurationValue);
        buttonSaveSettings = findViewById(R.id.buttonSaveSettings);
    }

    private void setupListeners() {
        buttonBackSettings.setOnClickListener(v -> finish());

        layoutRestDuration.setOnClickListener(v -> showRestDurationDialog());

        switchKeepScreenOn.setOnCheckedChangeListener((buttonView, isChecked) -> applyKeepScreenOn());

        switchLightTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }

            ThemeManager.setLightThemeEnabled(this, isChecked);
            Toast.makeText(
                    this,
                    isChecked ? "Светлая тема включена" : "Тёмная тема включена",
                    Toast.LENGTH_SHORT
            ).show();
        });

        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }

            if (isChecked) {
                requestLocationPermissionIfNeeded();
            } else {
                saveLocationOnly(false);
                Toast.makeText(this, "Геолокация в приложении отключена", Toast.LENGTH_SHORT).show();
            }
        });

        buttonSaveSettings.setOnClickListener(v -> {
            if (switchLocation.isChecked()) {
                if (!hasLocationPermission()) {
                    switchLocation.setChecked(false);
                    saveLocationOnly(false);
                    Toast.makeText(this, "Сначала разреши доступ к геолокации", Toast.LENGTH_SHORT).show();
                    return;
                }

                checkLocationSettingsAndEnableBeforeSave();
                return;
            }

            saveSettings();
            Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadSettings() {
        boolean autoRest = preferences.getBoolean(getUserKey(KEY_AUTO_REST), false);
        boolean restSound = preferences.getBoolean(getUserKey(KEY_REST_SOUND), false);
        boolean vibration = preferences.getBoolean(getUserKey(KEY_VIBRATION), false);
        boolean keepScreenOn = preferences.getBoolean(getUserKey(KEY_KEEP_SCREEN_ON), false);
        boolean location = preferences.getBoolean(getUserKey(KEY_LOCATION), false);
        selectedRestDuration = preferences.getInt(getUserKey(KEY_REST_DURATION), 60);

        switchAutoRest.setChecked(autoRest);
        switchRestSound.setChecked(restSound);
        switchVibration.setChecked(vibration);
        switchKeepScreenOn.setChecked(keepScreenOn);
        switchLightTheme.setChecked(ThemeManager.isLightThemeEnabled(this));
        switchLocation.setChecked(location && hasLocationPermission());

        updateRestDurationText();
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(getUserKey(KEY_AUTO_REST), switchAutoRest.isChecked());
        editor.putBoolean(getUserKey(KEY_REST_SOUND), switchRestSound.isChecked());
        editor.putBoolean(getUserKey(KEY_VIBRATION), switchVibration.isChecked());
        editor.putInt(getUserKey(KEY_REST_DURATION), selectedRestDuration);
        editor.putBoolean(getUserKey(KEY_KEEP_SCREEN_ON), switchKeepScreenOn.isChecked());
        editor.putBoolean(getUserKey(KEY_LOCATION), switchLocation.isChecked() && hasLocationPermission());
        editor.apply();

        applyKeepScreenOn();
    }

    private void saveLocationOnly(boolean enabled) {
        preferences.edit()
                .putBoolean(getUserKey(KEY_LOCATION), enabled && hasLocationPermission())
                .apply();
    }

    private void showRestDurationDialog() {
        final String[] options = {"30 сек.", "45 сек.", "60 сек.", "90 сек.", "120 сек."};
        final int[] values = {30, 45, 60, 90, 120};

        int checkedItem = 2;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == selectedRestDuration) {
                checkedItem = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выбери длительность отдыха");
        builder.setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
            selectedRestDuration = values[which];
            updateRestDurationText();
            dialog.dismiss();
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void updateRestDurationText() {
        textRestDurationValue.setText(selectedRestDuration + " сек.");
    }

    private void applyKeepScreenOn() {
        if (switchKeepScreenOn != null && switchKeepScreenOn.isChecked()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void requestLocationPermissionIfNeeded() {
        if (hasLocationPermission()) {
            checkLocationSettingsAndEnable();
            return;
        }

        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void checkLocationSettingsAndEnable() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000
        ).build();

        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build();

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<com.google.android.gms.location.LocationSettingsResponse> task =
                client.checkLocationSettings(request);

        task.addOnSuccessListener(response -> {
            switchLocation.setChecked(true);
            saveLocationOnly(true);
            Toast.makeText(this, "Геолокация включена", Toast.LENGTH_SHORT).show();
        });

        task.addOnFailureListener(exception -> {
            if (exception instanceof ResolvableApiException) {
                IntentSenderRequest intentSenderRequest =
                        new IntentSenderRequest.Builder(
                                ((ResolvableApiException) exception).getResolution()
                        ).build();

                locationSettingsLauncher.launch(intentSenderRequest);
            } else {
                switchLocation.setChecked(false);
                saveLocationOnly(false);
                Toast.makeText(this, "Невозможно включить геолокацию", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLocationSettingsAndEnableBeforeSave() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000
        ).build();

        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build();

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<com.google.android.gms.location.LocationSettingsResponse> task =
                client.checkLocationSettings(request);

        task.addOnSuccessListener(response -> {
            saveSettings();
            Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show();
        });

        task.addOnFailureListener(exception -> {
            if (exception instanceof ResolvableApiException) {
                IntentSenderRequest intentSenderRequest =
                        new IntentSenderRequest.Builder(
                                ((ResolvableApiException) exception).getResolution()
                        ).build();

                locationSettingsLauncher.launch(intentSenderRequest);
            } else {
                switchLocation.setChecked(false);
                saveLocationOnly(false);
                Toast.makeText(this, "Невозможно включить геолокацию", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private String getUserKey(String baseKey) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "guest";
        return baseKey + "_" + uid;
    }
}
