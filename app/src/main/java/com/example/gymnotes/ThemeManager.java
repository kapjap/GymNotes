package com.example.gymnotes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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
    private static final String KEY_LIGHT_THEME = "light_theme";

    private static final int DARK_BACKGROUND = Color.parseColor("#000000");
    private static final int DARK_BACKGROUND_2 = Color.parseColor("#050505");
    private static final int DARK_SURFACE = Color.parseColor("#09101C");
    private static final int DARK_SURFACE_2 = Color.parseColor("#1E1E1E");
    private static final int DARK_INPUT = Color.parseColor("#1A1A1A");
    private static final int DARK_DIVIDER = Color.parseColor("#1A2233");
    private static final int DARK_PRIMARY_TEXT = Color.parseColor("#FFFFFF");
    private static final int DARK_SECONDARY_TEXT = Color.parseColor("#AAB4C8");
    private static final int DARK_SECONDARY_TEXT_2 = Color.parseColor("#8E97AA");
    private static final int DARK_SECONDARY_TEXT_3 = Color.parseColor("#8FA6D8");
    private static final int DARK_ICON = Color.parseColor("#7E8AA5");

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
        AppCompatDelegate.setDefaultNightMode(
                isLightThemeEnabled(context)
                        ? AppCompatDelegate.MODE_NIGHT_NO
                        : AppCompatDelegate.MODE_NIGHT_YES
        );
    }

    public static void setLightThemeEnabled(Context context, boolean enabled) {
        getPreferences(context)
                .edit()
                .putBoolean(KEY_LIGHT_THEME, enabled)
                .apply();

        AppCompatDelegate.setDefaultNightMode(
                enabled
                        ? AppCompatDelegate.MODE_NIGHT_NO
                        : AppCompatDelegate.MODE_NIGHT_YES
        );
    }

    public static boolean isLightThemeEnabled(Context context) {
        return getPreferences(context).getBoolean(KEY_LIGHT_THEME, false);
    }

    public static void applyThemeToActivity(Activity activity) {
        if (!isLightThemeEnabled(activity)) {
            return;
        }

        View root = activity.getWindow().getDecorView();
        root.post(() -> applyLightThemeToView(root));
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

        if (isSameColor(color, DARK_BACKGROUND) || isSameColor(color, DARK_BACKGROUND_2)) {
            view.setBackgroundColor(LIGHT_BACKGROUND);
        } else if (isSameColor(color, DARK_SURFACE)
                || isSameColor(color, DARK_SURFACE_2)
                || isSameColor(color, DARK_INPUT)) {
            view.setBackgroundColor(LIGHT_SURFACE);
        } else if (isSameColor(color, DARK_DIVIDER)) {
            view.setBackgroundColor(LIGHT_DIVIDER);
        }
    }

    private static void applyLightTextColor(TextView textView) {
        int color = textView.getCurrentTextColor();

        if (isSameColor(color, DARK_PRIMARY_TEXT)) {
            textView.setTextColor(LIGHT_PRIMARY_TEXT);
        } else if (isSameColor(color, DARK_SECONDARY_TEXT)
                || isSameColor(color, DARK_SECONDARY_TEXT_2)
                || isSameColor(color, DARK_SECONDARY_TEXT_3)) {
            textView.setTextColor(LIGHT_SECONDARY_TEXT);
        }
    }

    private static void applyLightIconTint(ImageView imageView) {
        ColorStateList tintList = imageView.getImageTintList();
        if (tintList == null) {
            return;
        }

        int tint = tintList.getDefaultColor();
        if (isSameColor(tint, DARK_ICON)) {
            imageView.setImageTintList(ColorStateList.valueOf(LIGHT_ICON));
        } else if (isSameColor(tint, ACCENT) || isSameColor(tint, ACCENT_BRIGHT)) {
            imageView.setImageTintList(ColorStateList.valueOf(ACCENT));
        }
    }

    private static boolean isSameColor(int firstColor, int secondColor) {
        return Color.rgb(Color.red(firstColor), Color.green(firstColor), Color.blue(firstColor))
                == Color.rgb(Color.red(secondColor), Color.green(secondColor), Color.blue(secondColor));
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
