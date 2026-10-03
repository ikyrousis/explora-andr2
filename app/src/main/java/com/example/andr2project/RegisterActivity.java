package com.example.andr2project;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView goback;
    private Button registerUser;
    private EditText nameEt, emailEt, passwordEt;
    private CircleImageView img;
    Uri selectedImageUri;
    private FirebaseAuth mAuth;
    ProgressDialog loader;
    private CollectionReference db;
    private StorageReference mStorageRef;

    double longitude;
    double latitude;
    private LocationRequest locationRequest;

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance().collection("Users");
        mStorageRef = FirebaseStorage.getInstance().getReference("UserPictures");

        loader = new ProgressDialog(RegisterActivity.this);
        loader.setTitle("Register");
        loader.setMessage("Registering user! Please wait...");
        loader.setCanceledOnTouchOutside(false);


        goback = findViewById(R.id.gobacklogin);
        goback.setOnClickListener(this);

        registerUser = findViewById(R.id.registeruserbtn);
        registerUser.setOnClickListener(this);

        nameEt = findViewById(R.id.nameId);
        emailEt = findViewById(R.id.emailId);
        passwordEt = findViewById(R.id.passwordId);
        img = findViewById(R.id.imgId);

        img.setOnClickListener(v -> {
            imageChooser();
        });
    }

    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        launchSomeActivity.launch(i);
    }

    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // do your operation from here....
                    if (data != null && data.getData() != null) {
                        selectedImageUri = data.getData();
                        Bitmap selectedImageBitmap;
                        try {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                            img.setImageBitmap(selectedImageBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.gobacklogin:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.registeruserbtn:
                registerUser();
                break;
        }
    }

    private void registerUser() {

        String email = emailEt.getText().toString().trim();
        String name = nameEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();

        if (!isGPSEnabled()) {
            Toast.makeText(RegisterActivity.this, "Please enable GPS!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            nameEt.setError("Name is required");
            nameEt.requestFocus();
        } else if (email.isEmpty()) {
            emailEt.setError("Email is required");
            emailEt.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.setError("Please provide valid email");
            emailEt.requestFocus();
        } else if (password.isEmpty()) {
            passwordEt.setError("Password required!");
            passwordEt.requestFocus();
        } else if (password.length() < 6) {
            passwordEt.setError("Minimum password length is 6");
            passwordEt.requestFocus();
        } else {
            loader.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        GeoPoint location = new GeoPoint(latitude, longitude);

                        try {
                            if (selectedImageUri == null) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("name", name);
                                map.put("email", email);
                                map.put("team", Team.NONE.toString());
                                map.put("imgUrl", "");
                                map.put("location", location);
                                map.put("isLoggedIn", true);

                                db.document(mAuth.getCurrentUser().getUid())
                                        .set(map).addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                loader.dismiss();
                                                Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(RegisterActivity.this, ProfileActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                loader.dismiss();
                                                Toast.makeText(RegisterActivity.this, "Failed to register user! " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                StorageReference fileReference = mStorageRef.child(mAuth.getCurrentUser().getUid() + "." + getFileExtension(selectedImageUri));
                                fileReference.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                    task.addOnSuccessListener(uri -> {
                                        String img = uri.toString();
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("name", name);
                                        map.put("email", email);
                                        map.put("team", Team.NONE.toString());
                                        map.put("imgUrl", img);
                                        map.put("location", location);
                                        map.put("isLoggedIn", true);

                                        db.document(mAuth.getCurrentUser().getUid())
                                                .set(map).addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        loader.dismiss();
                                                        Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(RegisterActivity.this, ProfileActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        loader.dismiss();
                                                        Toast.makeText(RegisterActivity.this, "Failed to register user! " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    });
                                }).addOnFailureListener(e -> {
                                    loader.dismiss();
                                    Log.e(TAG, "registerUser: " + e.getMessage());
                                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        } catch (Exception e) {
                            loader.dismiss();
                            Log.e(TAG, "registerUser: " + e.getMessage());
                            Toast.makeText(this, "Failed to register user!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        loader.dismiss();
                        Log.e(TAG, "registerUser: " + e.getMessage());
                        Toast.makeText(this, "Failed to register user!", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    getCurrentLocation();
                } else {
                    turnOnGPS();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                getCurrentLocation();
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (isGPSEnabled()) {
                LocationServices.getFusedLocationProviderClient(RegisterActivity.this)
                        .requestLocationUpdates(locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);

                                LocationServices.getFusedLocationProviderClient(RegisterActivity.this)
                                        .removeLocationUpdates(this);

                                if (locationResult.getLocations().size() > 0) {

                                    int index = locationResult.getLocations().size() - 1;

                                    longitude = locationResult.getLocations().get(index).getLongitude();
                                    latitude = locationResult.getLocations().get(index).getLatitude();

                                    Log.d(TAG, "onLocationResult: " + longitude + "_" + latitude);
                                }
                            }
                        }, Looper.getMainLooper());
            } else {
                turnOnGPS();
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(RegisterActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(RegisterActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager;
        boolean isEnabled;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }
}