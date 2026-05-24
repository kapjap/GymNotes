package com.example.gymnotes;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    private static final String PREFS_NAME = "fitpulse_settings";
    private static final String KEY_THEME_MODE = "theme_mode";

    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    private ThemeManager() {
    }

    public static void applySavedTheme(Context context) {
        String mode = getThemeMode(context);

        if (THEME_LIGHT.equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (THEME_DARK.equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public static void setThemeMode(Context context, String mode) {
        getPreferences(context)
                .edit()
                .putString(KEY_THEME_MODE, mode)
                .apply();

        applySavedTheme(context);
    }

    public static String getThemeMode(Context context) {
        return getPreferences(context).getString(KEY_THEME_MODE, THEME_SYSTEM);
    }

    public static boolean isLightThemeEnabled(Context context) {
        return THEME_LIGHT.equals(getThemeMode(context));
    }

    public static void setLightThemeEnabled(Context context, boolean enabled) {
        setThemeMode(context, enabled ? THEME_LIGHT : THEME_DARK);
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
