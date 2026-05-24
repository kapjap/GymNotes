package com.example.gymnotes;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Geometry;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.TextStyle;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.Response;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.SearchType;
import com.yandex.mapkit.search.Session;
import com.yandex.runtime.Error;

import java.util.List;

public class GymsNearbyActivity extends AppCompatActivity implements Session.SearchListener {

    private static final String PREFS_NAME = "fitpulse_settings";
    private static final String KEY_LOCATION = "location";

    private static final Point DEFAULT_POINT = new Point(50.5954, 36.5879);

    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private SearchManager searchManager;
    private Session searchSession;
    private MapObjectCollection mapObjects;

    private Point currentUserPoint = DEFAULT_POINT;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadNearbyGyms();
                } else {
                    Toast.makeText(this, "Нет доступа к геолокации", Toast.LENGTH_SHORT).show();
                    currentUserPoint = DEFAULT_POINT;
                    moveCamera(currentUserPoint);
                    searchNearbyGyms(currentUserPoint, false);
                }
            });

    private final MapObjectTapListener gymTapListener = new MapObjectTapListener() {
        @Override
        public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {
            Object data = mapObject.getUserData();
            if (data instanceof GymItem) {
                showGymBottomSheet((GymItem) data);
                return true;
            }
            return false;
        }
    };

    private static class GymItem {
        String name;
        String address;

        GymItem(String name, String address) {
            this.name = name;
            this.address = address;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyms_nearby);

        mapView = findViewById(R.id.mapview);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        String style = "[{\"tags\":{\"any\":[\"poi\"]},\"elements\":[\"label\",\"label.icon\"],\"stylers\":{\"visibility\":\"off\"}}]";
        mapView.getMap().setMapStyle(style);

        SearchFactory.initialize(this);
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        ImageView back = findViewById(R.id.buttonBackGyms);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        startMapFlow();
    }

    private void startMapFlow() {
        if (!isLocationEnabledInApp()) {
            currentUserPoint = DEFAULT_POINT;
            moveCamera(currentUserPoint);
            searchNearbyGyms(currentUserPoint, false);
            Toast.makeText(this, "Геолокация отключена в настройках приложения", Toast.LENGTH_SHORT).show();
            return;
        }

        checkLocationPermissionAndStart();
    }

    private void checkLocationPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            loadNearbyGyms();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void loadNearbyGyms() {
        if (!isLocationEnabledInApp()) {
            currentUserPoint = DEFAULT_POINT;
            moveCamera(currentUserPoint);
            searchNearbyGyms(currentUserPoint, false);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            currentUserPoint = DEFAULT_POINT;
            moveCamera(currentUserPoint);
            searchNearbyGyms(currentUserPoint, false);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentUserPoint = new Point(location.getLatitude(), location.getLongitude());
                    } else {
                        currentUserPoint = DEFAULT_POINT;
                        Toast.makeText(this, "Не удалось получить геолокацию", Toast.LENGTH_SHORT).show();
                    }

                    moveCamera(currentUserPoint);
                    searchNearbyGyms(currentUserPoint, true);
                })
                .addOnFailureListener(e -> {
                    currentUserPoint = DEFAULT_POINT;
                    moveCamera(currentUserPoint);
                    searchNearbyGyms(currentUserPoint, false);
                    Toast.makeText(this, "Ошибка получения геолокации", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isLocationEnabledInApp() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(getUserKey(KEY_LOCATION), false);
    }

    private String getUserKey(String baseKey) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "guest";
        return baseKey + "_" + uid;
    }

    private void moveCamera(Point point) {
        mapView.getMap().move(
                new CameraPosition(point, 14.8f, 0, 0),
                new Animation(Animation.Type.SMOOTH, 1f),
                null
        );
    }

    private void addUserPlacemark(Point userPoint) {
        PlacemarkMapObject me = mapObjects.addPlacemark(userPoint);

        TextStyle textStyle = new TextStyle();
        textStyle.setSize(14f);
        textStyle.setOffset(4f);

        me.setText("Я", textStyle);
    }

    private void addGymPlacemark(Point gymPoint, String gymName, String gymAddress) {
        PlacemarkMapObject gym = mapObjects.addPlacemark(gymPoint);

        if (gymName != null && !gymName.trim().isEmpty()) {
            TextStyle textStyle = new TextStyle();
            textStyle.setSize(12f);
            textStyle.setOffset(5f);
            gym.setText(gymName, textStyle);
        }

        gym.setUserData(new GymItem(
                gymName != null && !gymName.trim().isEmpty() ? gymName : "Зал",
                gymAddress != null && !gymAddress.trim().isEmpty() ? gymAddress : "Адрес не указан"
        ));

        gym.addTapListener(gymTapListener);
    }

    private void showGymBottomSheet(GymItem gym) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_gym, null, false);

        TextView title = view.findViewById(R.id.textGymTitle);
        TextView address = view.findViewById(R.id.textGymAddress);

        title.setText(gym.name);
        address.setText(gym.address);

        dialog.setContentView(view);
        dialog.show();
    }

    private void searchNearbyGyms(Point centerPoint, boolean showUserPlacemark) {
        if (searchSession != null) {
            searchSession.cancel();
        }

        mapObjects.clear();

        if (showUserPlacemark) {
            addUserPlacemark(centerPoint);
        }

        double lat = centerPoint.getLatitude();
        double lon = centerPoint.getLongitude();

        BoundingBox box = new BoundingBox(
                new Point(lat - 0.04, lon - 0.04),
                new Point(lat + 0.04, lon + 0.04)
        );

        SearchOptions options = new SearchOptions();
        options.setSearchTypes(SearchType.BIZ.value);
        options.setResultPageSize(50);

        searchSession = searchManager.submit(
                "gym фитнес тренажерный зал спортзал фитнес-клуб",
                Geometry.fromBoundingBox(box),
                options,
                this
        );
    }

    @Override
    public void onSearchResponse(@NonNull Response response) {
        List<com.yandex.mapkit.GeoObjectCollection.Item> items =
                response.getCollection().getChildren();

        int count = 0;
        boolean showUserPlacemark = isLocationEnabledInApp();

        mapObjects.clear();
        if (showUserPlacemark) {
            addUserPlacemark(currentUserPoint);
        }

        for (com.yandex.mapkit.GeoObjectCollection.Item item : items) {
            if (item.getObj() == null || item.getObj().getGeometry().isEmpty()) {
                continue;
            }

            Point point = item.getObj().getGeometry().get(0).getPoint();
            if (point == null) {
                continue;
            }

            String gymName = item.getObj().getName();
            String gymAddress = "Адрес не указан";

            if (item.getObj().getDescriptionText() != null && !item.getObj().getDescriptionText().trim().isEmpty()) {
                gymAddress = item.getObj().getDescriptionText();
            }

            addGymPlacemark(point, gymName, gymAddress);
            count++;
        }

        Toast.makeText(this, "Залов найдено: " + count, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSearchError(@NonNull Error error) {
        mapObjects.clear();

        if (isLocationEnabledInApp()) {
            addUserPlacemark(currentUserPoint);
        }

        Toast.makeText(this, "Ошибка поиска залов", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mapView != null) {
            startMapFlow();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
}