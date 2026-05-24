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
    private static final int DARK_CARD = Color.parseColor("#09101C");
    private static final int DARK_DIVIDER = Color.parseColor("#1A2233");
    private static final int DARK_PRIMARY_TEXT = Color.parseColor("#FFFFFF");
    private static final int DARK_SECONDARY_TEXT = Color.parseColor("#AAB4C8");
    private static final int DARK_BLUE_TEXT = Color.parseColor("#6F8FCF");
    private static final int DARK_ICON = Color.parseColor("#7E8AA5");

    private static final int LIGHT_BACKGROUND = Color.parseColor("#F4F7FB");
    private static final int LIGHT_CARD = Color.parseColor("#FFFFFF");
    private static final int LIGHT_DIVIDER = Color.parseColor("#D9E2F2");
    private static final int LIGHT_PRIMARY_TEXT = Color.parseColor("#111827");
    private static final int LIGHT_SECONDARY_TEXT = Color.parseColor("#5E6B7E");
    private static final int LIGHT_BLUE_TEXT = Color.parseColor("#2F69C7");
    private static final int LIGHT_ICON = Color.parseColor("#6B7280");
    private static final int ACCENT = Color.parseColor("#5272B4");

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
        View root = activity.getWindow().getDecorView();
        applyThemeToView(activity, root, isLightThemeEnabled(activity));
    }

    private static void applyThemeToView(Context context, View view, boolean lightTheme) {
        if (view == null) {
            return;
        }

        applyBackground(view, lightTheme);

        if (view instanceof CardView) {
            ((CardView) view).setCardBackgroundColor(lightTheme ? LIGHT_CARD : DARK_CARD);
        }

        if (view instanceof TextView) {
            applyTextColor((TextView) view, lightTheme);
        }

        if (view instanceof Button) {
            ((Button) view).setTextColor(Color.WHITE);
        }

        if (view instanceof ImageView) {
            applyIconTint((ImageView) view, lightTheme);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyThemeToView(context, group.getChildAt(i), lightTheme);
            }
        }
    }

    private static void applyBackground(View view, boolean lightTheme) {
        Drawable background = view.getBackground();
        if (!(background instanceof ColorDrawable)) {
            return;
        }

        int color = ((ColorDrawable) background).getColor();

        if (isSameColor(color, DARK_BACKGROUND) || isSameColor(color, LIGHT_BACKGROUND)) {
            view.setBackgroundColor(lightTheme ? LIGHT_BACKGROUND : DARK_BACKGROUND);
        } else if (isSameColor(color, DARK_CARD) || isSameColor(color, LIGHT_CARD)) {
            view.setBackgroundColor(lightTheme ? LIGHT_CARD : DARK_CARD);
        } else if (isSameColor(color, DARK_DIVIDER) || isSameColor(color, LIGHT_DIVIDER)) {
            view.setBackgroundColor(lightTheme ? LIGHT_DIVIDER : DARK_DIVIDER);
        }
    }

    private static void applyTextColor(TextView textView, boolean lightTheme) {
        int color = textView.getCurrentTextColor();

        if (isSameColor(color, DARK_PRIMARY_TEXT) || isSameColor(color, LIGHT_PRIMARY_TEXT)) {
            textView.setTextColor(lightTheme ? LIGHT_PRIMARY_TEXT : DARK_PRIMARY_TEXT);
        } else if (isSameColor(color, DARK_SECONDARY_TEXT) || isSameColor(color, LIGHT_SECONDARY_TEXT)) {
            textView.setTextColor(lightTheme ? LIGHT_SECONDARY_TEXT : DARK_SECONDARY_TEXT);
        } else if (isSameColor(color, DARK_BLUE_TEXT) || isSameColor(color, LIGHT_BLUE_TEXT)) {
            textView.setTextColor(lightTheme ? LIGHT_BLUE_TEXT : DARK_BLUE_TEXT);
        }
    }

    private static void applyIconTint(ImageView imageView, boolean lightTheme) {
        ColorStateList tintList = imageView.getImageTintList();
        if (tintList == null) {
            return;
        }

        int tint = tintList.getDefaultColor();
        if (isSameColor(tint, DARK_ICON) || isSameColor(tint, LIGHT_ICON)) {
            imageView.setImageTintList(ColorStateList.valueOf(lightTheme ? LIGHT_ICON : DARK_ICON));
        } else if (isSameColor(tint, ACCENT)) {
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
