package com.example.gymnotes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

public class ThemeManager {

    private static final String PREFS_NAME = "fitpulse_settings";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String OLD_KEY_LIGHT_THEME = "light_theme";

    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    private static final int DARK_BACKGROUND = Color.parseColor("#000000");
    private static final int DARK_BACKGROUND_2 = Color.parseColor("#050505");
    private static final int DARK_BACKGROUND_3 = Color.parseColor("#0D0D0D");
    private static final int DARK_SURFACE = Color.parseColor("#09101C");
    private static final int DARK_SURFACE_2 = Color.parseColor("#071B2E");
    private static final int DARK_SURFACE_3 = Color.parseColor("#131313");
    private static final int DARK_SURFACE_4 = Color.parseColor("#1E1E1E");
    private static final int DARK_SURFACE_5 = Color.parseColor("#001F2A");
    private static final int DARK_INPUT = Color.parseColor("#171717");
    private static final int DARK_INPUT_2 = Color.parseColor("#1A1A1A");
    private static final int DARK_DIVIDER = Color.parseColor("#1A2233");

    private static final int LIGHT_BACKGROUND = Color.parseColor("#F4F7FB");
    private static final int LIGHT_SURFACE = Color.parseColor("#FFFFFF");
    private static final int LIGHT_SURFACE_ALT = Color.parseColor("#EAF1FB");
    private static final int LIGHT_DIVIDER = Color.parseColor("#D9E2F2");
    private static final int LIGHT_PRIMARY_TEXT = Color.parseColor("#111827");
    private static final int LIGHT_SECONDARY_TEXT = Color.parseColor("#667085");
    private static final int LIGHT_ICON = Color.parseColor("#6B7280");
    private static final int ACCENT = Color.parseColor("#5272B4");
    private static final int ACCENT_BRIGHT = Color.parseColor("#3B82F6");

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
                .remove(OLD_KEY_LIGHT_THEME)
                .apply();

        applySavedTheme(context);
    }

    public static String getThemeMode(Context context) {
        SharedPreferences preferences = getPreferences(context);
        if (preferences.contains(KEY_THEME_MODE)) {
            return preferences.getString(KEY_THEME_MODE, THEME_SYSTEM);
        }
        return THEME_SYSTEM;
    }

    public static boolean isLightThemeEnabled(Context context) {
        return THEME_LIGHT.equals(getThemeMode(context));
    }

    public static void setLightThemeEnabled(Context context, boolean enabled) {
        setThemeMode(context, enabled ? THEME_LIGHT : THEME_DARK);
    }

    public static boolean shouldUseLightTheme(Context context) {
        String mode = getThemeMode(context);
        if (THEME_LIGHT.equals(mode)) {
            return true;
        }
        if (THEME_DARK.equals(mode)) {
            return false;
        }

        int uiMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return uiMode != Configuration.UI_MODE_NIGHT_YES;
    }

    public static void applyThemeToActivity(Activity activity) {
        if (!shouldUseLightTheme(activity)) {
            return;
        }

        View root = activity.getWindow().getDecorView();
        root.postDelayed(() -> applyLightThemeToView(root), 60);
    }

    private static void applyLightThemeToView(View view) {
        if (view == null) {
            return;
        }

        applyLightBackground(view);

        if (view instanceof CardView) {
            ((CardView) view).setCardBackgroundColor(LIGHT_SURFACE);
        }

        if (view instanceof TextView) {
            applyLightTextColor((TextView) view);
        }

        if (view instanceof Button) {
            ((Button) view).setTextColor(Color.WHITE);
        }

        if (view instanceof ImageView) {
            applyLightIconTint((ImageView) view);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyLightThemeToView(group.getChildAt(i));
            }
        }
    }

    private static void applyLightBackground(View view) {
        Drawable background = view.getBackground();
        if (!(background instanceof ColorDrawable)) {
            return;
        }

        int color = ((ColorDrawable) background).getColor();

        if (matches(color, DARK_BACKGROUND, DARK_BACKGROUND_2, DARK_BACKGROUND_3)) {
            view.setBackgroundColor(LIGHT_BACKGROUND);
        } else if (matches(color, DARK_SURFACE, DARK_SURFACE_2, DARK_SURFACE_3, DARK_SURFACE_4, DARK_SURFACE_5, DARK_INPUT, DARK_INPUT_2)) {
            view.setBackgroundColor(LIGHT_SURFACE);
        } else if (matches(color, DARK_DIVIDER)) {
            view.setBackgroundColor(LIGHT_DIVIDER);
        }
    }

    private static void applyLightTextColor(TextView textView) {
        int color = normalizeColor(textView.getCurrentTextColor());

        if (isSameColor(color, Color.WHITE) || isVeryLight(color)) {
            textView.setTextColor(LIGHT_PRIMARY_TEXT);
        } else if (isGrayText(color)) {
            textView.setTextColor(LIGHT_SECONDARY_TEXT);
        }
    }

    private static void applyLightIconTint(ImageView imageView) {
        ColorStateList tintList = imageView.getImageTintList();
        if (tintList == null) {
            return;
        }

        int tint = normalizeColor(tintList.getDefaultColor());
        if (isGrayText(tint)) {
            imageView.setImageTintList(ColorStateList.valueOf(LIGHT_ICON));
        } else if (isSameColor(tint, ACCENT) || isSameColor(tint, ACCENT_BRIGHT)) {
            imageView.setImageTintList(ColorStateList.valueOf(ACCENT));
        }
    }

    private static boolean matches(int color, int... colors) {
        for (int candidate : colors) {
            if (isSameColor(color, candidate)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isVeryLight(int color) {
        return Color.red(color) > 210 && Color.green(color) > 210 && Color.blue(color) > 210;
    }

    private static boolean isGrayText(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Math.abs(r - g) < 22 && Math.abs(g - b) < 22 && r > 80 && r < 210;
    }

    private static int normalizeColor(int color) {
        return Color.rgb(Color.red(color), Color.green(color), Color.blue(color));
    }

    private static boolean isSameColor(int firstColor, int secondColor) {
        return normalizeColor(firstColor) == normalizeColor(secondColor);
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
