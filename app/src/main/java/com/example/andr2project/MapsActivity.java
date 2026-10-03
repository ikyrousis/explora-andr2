package com.example.andr2project;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.andr2project.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, SensorEventListener {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Marker m;
    private FirebaseFirestore db;
    private LatLng currentLocation = new LatLng(51.4368, 5.5147);
    private FirebaseAuth firebaseAuth;
    private String currentUser;
    private String teamCurrentUser;
    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;
    public List<String> emails = new ArrayList<>();


    private List<MarkerOptions> waypoints = new ArrayList<>();
    Button button;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private double movementThreshold = 1;

    private boolean moving;

    private Map<MarkerOptions, String> markerIDs = new HashMap<>();

    private String emailCurrentUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);


        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Grab current location and set marker of current location of user
        m = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Your location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        //Get User UID
        currentUser = firebaseAuth.getCurrentUser().getUid();
        db.collection("Users").document(currentUser).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            teamCurrentUser = task.getResult().get("team", String.class);
                            emailCurrentUser = task.getResult().get("email", String.class);
                        }
                    }
                });

        //get all users
        getUsers();

        //get all waypoints
        getWaypoints();

        button = (Button)findViewById(R.id.buttongobackmap);
        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, ProfileActivity.class);
                startActivity(intent);

            }
        });
    }

    private void getUsers() {
        db.collection("Users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "Users listener failed", e);
                            return;
                        }

                        db.collection("Users").get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {

                                            List<MarkerOptions> nearbyMarkers = new ArrayList<MarkerOptions>();
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                GeoPoint usersGeoLocation = document.get("location", GeoPoint.class);
                                                if (usersGeoLocation != null) {
                                                    LatLng usersLocation = new LatLng(usersGeoLocation.getLatitude(), usersGeoLocation.getLongitude());
                                                    String usersName = document.get("name", String.class);
                                                    String usersEmail = document.get("email", String.class);
                                                    //Call vib and notification when user is nearby and only if not in local list of emails
                                                    if (calculateDistanceInKilometer(currentLocation.latitude, currentLocation.longitude, usersLocation.latitude, usersLocation.longitude)) {
                                                            if (emailCurrentUser != null && !emailCurrentUser.equals(usersEmail)) {
                                                                if(!emails.contains(usersEmail)){
                                                                    emails.add(usersEmail);
                                                                    makeNotification(usersLocation.latitude, usersLocation.longitude, usersName);
                                                                }
                                                            }
                                                    }
                                                    else{
                                                        emails.remove(usersEmail);
                                                    }
                                                    //Put marker on map
                                                    if (!document.getId().equals(currentUser) && Boolean.TRUE.equals(document.get("isLoggedIn", Boolean.class))) {
                                                        MarkerOptions markerOptions = new MarkerOptions().position(usersLocation).title(usersName);
                                                        nearbyMarkers.add(markerOptions);
                                                    }
                                                }

                                            }
                                            mMap.clear();
                                            for(MarkerOptions a : waypoints){
                                               mMap.addMarker(a);
                                            }
                                            MarkerOptions markerOptionsCurrentUser = new MarkerOptions().position(currentLocation).title("Your location");
                                            mMap.addMarker(markerOptionsCurrentUser);
                                            for (MarkerOptions a : nearbyMarkers) {
                                                mMap.addMarker(a);
                                            }

                                        } else {
                                            Log.e(TAG, "Failed to load users");
                                        }
                                    }
                                });
                    }
                });
    }

    private void getWaypoints() {
        db.collection("Waypoints")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "Waypoints listener failed", e);
                            return;
                        }

                        db.collection("Waypoints")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            waypoints.clear();
                                            markerIDs.clear();
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                GeoPoint point = document.get("location", GeoPoint.class);
                                                if (point != null) {
                                                    LatLng location = new LatLng(point.getLatitude(), point.getLongitude());
                                                    String team = document.getString("owner");
                                                    String title = document.getString("name");
                                                    String id = document.getId();

                                                    if (team == null) {
                                                        MarkerOptions marker = new MarkerOptions().position(location).title(title)
                                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                                        waypoints.add(marker);
                                                        markerIDs.put(marker, id);
                                                    } else if (team.equals("BLUE")) {
                                                        MarkerOptions marker = new MarkerOptions().position(location).title(title)
                                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                                        waypoints.add(marker);
                                                        markerIDs.put(marker, id);
                                                    } else if (team.equals("RED")) {
                                                        MarkerOptions marker = new MarkerOptions().position(location).title(title)
                                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                                        waypoints.add(marker);
                                                        markerIDs.put(marker, id);
                                                    } else if (team.equals("YELLOW")) {
                                                        MarkerOptions marker = new MarkerOptions().position(location).title(title)
                                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                                        waypoints.add(marker);
                                                        markerIDs.put(marker, id);
                                                    } else {
                                                        MarkerOptions marker = new MarkerOptions().position(location).title(title)
                                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                                        waypoints.add(marker);
                                                        markerIDs.put(marker, id);
                                                    }
                                                }
                                            }
                                            for(MarkerOptions m : waypoints){
                                                mMap.addMarker(m);
                                            }
                                        } else {
                                            Log.e(TAG, "Failed to load waypoints");
                                        }
                                    }
                                });
                    }
                });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (mMap != null) {
            m.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            //Push current location to database of user  THIS NEEDS TO BE LATER CHANGED TO USING UID OF USER TO UPDATE LOC
            DocumentReference docRef = db.collection("Users").document(currentUser);
            GeoPoint gp = new GeoPoint(location.getLatitude(), location.getLongitude());
            docRef.update("location", gp);
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        }

        for (MarkerOptions marker : waypoints) {
            if (calculateDistanceInKilometer(currentLocation.latitude, currentLocation.longitude, marker.getPosition().latitude, marker.getPosition().longitude)) {
                if (!moving) {
                    //Capture waypoint
                    captureWaypoint(marker);
                }
            }
        }
    }

    private void captureWaypoint(MarkerOptions marker) {
        DocumentReference waypoint = db.collection("Waypoints").document(Objects.requireNonNull(markerIDs.get(marker)));
        waypoint.update("owner", teamCurrentUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "DocumentSnapshot successfully updated!");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firebase", "Error updating document", e);
                    }
                });
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }


    public Boolean calculateDistanceInKilometer(double currentUserLat, double currentUserLng,
                                                double otherUserLat, double otherUserLng) {

        double latDistance = Math.toRadians(currentUserLat - otherUserLat);
        double lngDistance = Math.toRadians(currentUserLng - otherUserLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(currentUserLat)) * Math.cos(Math.toRadians(otherUserLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = (double) (AVERAGE_RADIUS_OF_EARTH_KM * c);
        //if distance is less than 10 meters return true otherwise false.
        if (distance <= 0.01) {
            return true;
        }
        return false;
    }

    public void makeNotification(double latitude, double longitude, String username) {
        //make notification with location at said latitude and longitude, and display the user
        String KEY = "com.example.andr2project.NOTIFICATIONS";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "userNotification")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Found " + username)
                .setContentText("You ran into " + username + " at: " + latitude + ";" + longitude)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup(KEY)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationID = new Random().nextInt(256);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationID, builder.build());

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v.hasVibrator())
            v.vibrate(400);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("userNotification", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Getting the magnitude from the vector read in the accelerometer
        double speed = Math.sqrt(Math.pow(event.values[0], 2) +
                Math.pow(event.values[1], 2) +
                Math.pow(event.values[2], 2));
        if (m != null) {
            if (speed < movementThreshold) {
                m.setAlpha(1f);
                moving = false;
                return;
            }
            m.setAlpha(0.5f);
            moving = true;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

}