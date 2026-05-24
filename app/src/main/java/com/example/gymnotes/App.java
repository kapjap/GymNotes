package com.example.gymnotes;

import android.app.Application;
import com.yandex.mapkit.MapKitFactory;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        MapKitFactory.setApiKey("a9aa08a8-98ea-4050-8671-0bbc66115483");
        MapKitFactory.initialize(this);
    }
}