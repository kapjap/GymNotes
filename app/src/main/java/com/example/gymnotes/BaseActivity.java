package com.example.gymnotes;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeManager.applySavedTheme(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ThemeManager.applyThemeToActivity(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        ThemeManager.applyThemeToActivity(this);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        ThemeManager.applyThemeToActivity(this);
    }
}
