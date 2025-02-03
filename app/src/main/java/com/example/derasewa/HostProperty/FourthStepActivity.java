package com.example.derasewa.HostProperty;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.derasewa.BuildConfig;
import com.example.derasewa.LoginActivity;
import com.example.derasewa.ProfileFragment;
import com.example.derasewa.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.events.MapEventsReceiver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FourthStepActivity extends AppCompatActivity implements MapEventsReceiver {

    private SharedPreferences sharedPreferences;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();
    private MapView mapView;

    private TextView coordinateView;
    private TextView errorMessage;
    private Button submitButton;

    private String title;
    private String type;
    private int numberOfRooms;
    private boolean hasBedroom;
    private boolean hasKitchen;
    private boolean hasBathroom;
    private String address;
    private String phoneNumber;
    private double price;
    private String description;
    private List<String> base64Images;
    private double selectedLatitude;
    private double selectedLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourth_step);

        coordinateView = findViewById(R.id.coordinate_view);
        errorMessage = findViewById(R.id.error_message);
        submitButton = findViewById(R.id.submit_button);

        // Initialize osmdroid configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, getSharedPreferences("osmdroid", MODE_PRIVATE));

        mapView = findViewById(R.id.map);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        // Set the initial location based on device location
        setInitialLocation();

        // Add MapEventsOverlay to handle map clicks
        MapEventsOverlay eventsOverlay = new MapEventsOverlay(this, this);
        mapView.getOverlays().add(eventsOverlay);

        // Retrieve the state from previous activities
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        type = intent.getStringExtra("type");
        numberOfRooms = intent.getIntExtra("numberOfRooms", 0);
        hasBedroom = intent.getBooleanExtra("hasBedroom", false);
        hasKitchen = intent.getBooleanExtra("hasKitchen", false);
        hasBathroom = intent.getBooleanExtra("hasBathroom", false);
        address = intent.getStringExtra("address");
        phoneNumber = intent.getStringExtra("phoneNumber");
        price = intent.getDoubleExtra("price", 0);
        description = intent.getStringExtra("description");
        base64Images = intent.getStringArrayListExtra("base64Images");

        submitButton.setOnClickListener(v -> validateAndSubmit());
    }

    private void setInitialLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                // If device location is available, set it
                GeoPoint startPoint = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                mapView.getController().setCenter(startPoint);
                mapView.getController().setZoom(15.0);
                addMarker(startPoint, "Current Location");

                // Set coordinates in coordinateView (initially using device's GPS)
                selectedLatitude = lastKnownLocation.getLatitude();
                selectedLongitude = lastKnownLocation.getLongitude();
                coordinateView.setText("Latitude: " + selectedLatitude + ", Longitude: " + selectedLongitude);
            } else {
                // If no GPS location is available, use a default location
                GeoPoint startPoint = new GeoPoint(27.7172, 85.3240); // Default location (e.g., Kathmandu)
                mapView.getController().setCenter(startPoint);
                addMarker(startPoint, "Default Location");

                // Set coordinates in coordinateView (using default location)
                selectedLatitude = 27.7172;
                selectedLongitude = 85.3240;
                coordinateView.setText("Latitude: 27.7172, Longitude: 85.3240");
            }
        }
    }

    private void addMarker(GeoPoint point, String title) {
        mapView.getOverlays().clear();
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(title);
        mapView.getOverlays().add(marker);
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        addMarker(p, "Selected Location");
        selectedLatitude = p.getLatitude();
        selectedLongitude = p.getLongitude();
        coordinateView.setText("Latitude: " + selectedLatitude + ", Longitude: " + selectedLongitude);
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }

    private void validateAndSubmit() {
        // Check if coordinates are selected
        if (selectedLatitude == 0 || selectedLongitude == 0) {
            errorMessage.setText("Please select a location on the map.");
            errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        // If validation succeeds, submit the data
        submitData();
    }

    private void submitData() {
        sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString("jwtToken", null);

        if (jwtToken == null) {
            errorMessage.setText("Authentication token not found.");
            errorMessage.setVisibility(View.VISIBLE);

            Intent intent = new Intent(FourthStepActivity.this, LoginActivity.class);
            startActivity(intent); // Start the LoginActivity
            finish(); // Finish the current activity
            return;
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("title", title);
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("numberOfRooms", numberOfRooms);
        jsonObject.addProperty("hasBedroom", hasBedroom);
        jsonObject.addProperty("hasKitchen", hasKitchen);
        jsonObject.addProperty("hasBathroom", hasBathroom);
        jsonObject.addProperty("address", address);
        jsonObject.addProperty("phoneNumber", phoneNumber);
        jsonObject.addProperty("price", price);
        jsonObject.addProperty("description", description);
        jsonObject.add("images", new Gson().toJsonTree(base64Images));

        // Coordinates
        JsonArray coordinates = new JsonArray();
        coordinates.add(selectedLatitude);
        coordinates.add(selectedLongitude);
        jsonObject.add("coordinates", coordinates);

        String jsonString = new Gson().toJson(jsonObject);
        RequestBody body = RequestBody.Companion.create(jsonString, JSON);
        Request request = new Request.Builder()
                .url(BuildConfig.SERVER_IP + "/submit-property") // Update with your server URL
                .addHeader("x-api-key", BuildConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + jwtToken)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    errorMessage.setText("Submission failed: " + e.getMessage());
                    errorMessage.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                try {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(FourthStepActivity.this, "Property successfully submitted.", Toast.LENGTH_SHORT).show();
                            errorMessage.setText(""); // Clear the error message
                            errorMessage.setVisibility(View.GONE);

                            // Navigate to ProfileFragment (or wherever you want)
                            Intent intent = new Intent(FourthStepActivity.this, ProfileFragment.class);
                            startActivity(intent);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            errorMessage.setText("Submission failed: " + responseBody);
                            errorMessage.setVisibility(View.VISIBLE);
                        });
                    }
                } catch (Exception e) {
                    Log.e("FourthStepActivity", "Error parsing response", e);
                    runOnUiThread(() -> {
                        errorMessage.setText("Error parsing server response.");
                        errorMessage.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }
}
