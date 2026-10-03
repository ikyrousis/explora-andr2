package com.example.andr2project;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView register;
    private EditText emailEt, passwordEt;
    private Button signinbutton;
    private FirebaseAuth mAuth;
    private CollectionReference db;
    ProgressDialog loader;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        loader = new ProgressDialog(MainActivity.this);
        loader.setCanceledOnTouchOutside(false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance().collection("Users");

        emailEt = findViewById(R.id.emailId);
        passwordEt = findViewById(R.id.passwordId);

        register = findViewById(R.id.register);
        register.setOnClickListener(this);
        signinbutton = findViewById(R.id.loginbtn);
        signinbutton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loader.show();
        loader.setCancelable(false);
        loader.setMessage("Please wait...");
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("isLoggedIn", true);

            db.document(mAuth.getCurrentUser().getUid())
                    .update(map).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            loader.dismiss();
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            loader.dismiss();
                            Toast.makeText(MainActivity.this, "Failed to Login user!" + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                        }
                    });
        } else {
            loader.dismiss();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register:
                startActivity(new Intent(this, RegisterActivity.class));
                finish();
                break;
            case R.id.loginbtn:
                loginUser();
                break;
        }
    }

    private void loginUser() {
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();

        if (email.isEmpty()) {
            emailEt.setError("Email is required");
            emailEt.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.setError("Please enter a valid email");
        } else if (password.isEmpty()) {
            passwordEt.setError("Password required");
            passwordEt.requestFocus();
        } else {
            loader.show();
            loader.setTitle("Sign In");
            loader.setMessage("Signing In! Please wait...");
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("isLoggedIn", true);

                        db.document(mAuth.getCurrentUser().getUid())
                                .update(map).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        loader.dismiss();
                                        Toast.makeText(MainActivity.this, "User Login Successful!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        loader.dismiss();
                                        Toast.makeText(MainActivity.this, "Failed to Login user!" + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        loader.dismiss();
                        Toast.makeText(MainActivity.this, "Failed to Login user!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}