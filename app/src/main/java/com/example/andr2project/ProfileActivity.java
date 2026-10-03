package com.example.andr2project;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.andr2project.BottomFragments.HomeFragment;
import com.example.andr2project.BottomFragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        NavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView bottomNavigationView;
    DrawerLayout drawer;
    NavigationView navigationView;
    FirebaseAuth firebaseAuth;
    private CollectionReference db;
    ProgressDialog loader;
    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        loader = new ProgressDialog(ProfileActivity.this);
        loader.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance().collection("Users");

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        bottomNavigationView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

        CircleImageView imageAppBarMain = navigationView.getHeaderView(0).findViewById(R.id.imageAppBarMainId);
        TextView name = navigationView.getHeaderView(0).findViewById(R.id.nameAppBarMainId);
        TextView email = navigationView.getHeaderView(0).findViewById(R.id.emailAppBarMainId);

        db.document(firebaseAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(TAG, "Listen failed", error);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        User user = snapshot.toObject(User.class);
                        if (user != null) {
                            name.setText(user.getName());
                            email.setText(user.getEmail());
                            Glide.with(ProfileActivity.this).load(user.getImgUrl())
                                    .placeholder(R.drawable.ic_baseline_image_24)
                                    .into(imageAppBarMain);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: " + e.getMessage());
                    }
                } else {
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.menu_homeBB);
    }

    HomeFragment homeFragment = new HomeFragment();
    ProfileFragment profileFragment = new ProfileFragment();

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.menu_homeBB:
                    replaceFragment(homeFragment, Constants.Home_Fragment);
                    return true;

                case R.id.menu_mapBB:
                    Intent intent2 = new Intent(ProfileActivity.this, MapsActivity.class);
                    startActivity(intent2);
                    return true;

                case R.id.menu_profileBB:
                    addFragment(profileFragment, Constants.Profile_Fragment);
                    return true;

                case R.id.menu_hamburgerBB:
                    navOpenFun();
                    return true;

                case R.id.logoutId:
                    loader.setMessage("Logging out...");
                    Map<String, Object> map = new HashMap<>();
                    map.put("isLoggedIn", false);

                    db.document(firebaseAuth.getCurrentUser().getUid())
                            .update(map).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    loader.dismiss();
                                    firebaseAuth.signOut();
                                    drawer.closeDrawer(Gravity.RIGHT);
                                    Toast.makeText(this, "Logout Successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    loader.dismiss();
                                    Toast.makeText(ProfileActivity.this, "Failed to Logged out!" + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(Gravity.RIGHT)) {
            drawer.closeDrawer(Gravity.RIGHT);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null && item.getItemId() == android.R.id.home) {
            if (drawer.isDrawerOpen(Gravity.RIGHT)) {
                drawer.closeDrawer(Gravity.RIGHT);
            } else {
                drawer.openDrawer(Gravity.RIGHT);
            }
        }
        return false;
    }

    public void navOpenFun() {
        drawer.openDrawer(Gravity.RIGHT);
    }

    public void addFragment(Fragment fragment, String tag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.flFragment, fragment, tag);
        ft.addToBackStack("New_State");
        ft.commit();
    }

    public void replaceFragment(Fragment fragment, String tag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.flFragment, fragment, tag);
        getSupportFragmentManager().popBackStackImmediate();
        ft.commit();
    }
}