package com.example.gymnotes;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;

import com.yandex.mapkit.MapKitFactory;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ThemeManager.applySavedTheme(this);
        registerGlobalThemeCallback();

        MapKitFactory.setApiKey("a9aa08a8-98ea-4050-8671-0bbc66115483");
        MapKitFactory.initialize(this);
    }

    private void registerGlobalThemeCallback() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                applyThemeWhenLayoutIsReady(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                applyThemeWhenLayoutIsReady(activity);
            }

            @Override
            public void onActivityResumed(Activity activity) {
                applyThemeWhenLayoutIsReady(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    private void applyThemeWhenLayoutIsReady(Activity activity) {
        View root = activity.getWindow().getDecorView();
        root.post(() -> ThemeManager.applyThemeToActivity(activity));
    }
}
