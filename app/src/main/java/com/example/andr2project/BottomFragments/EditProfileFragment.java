package com.example.andr2project.BottomFragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.andr2project.R;
import com.example.andr2project.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment {

    ImageView backBtn;
    TextInputEditText nameEt, emailEt, passwordEt;
    CircleImageView imageProfileEdit;
    Spinner teamEdit;
    MaterialButton btnUpdate;
    FirebaseAuth firebaseAuth;
    private CollectionReference db;
    private StorageReference mStorageRef;
    Uri selectedImageUri;
    ProgressDialog loader;
    String dbEmail;
    String selectedTeam;
    private static final String TAG = "EditProfileFragment";

    public EditProfileFragment() {
        // require a empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance().collection("Users");
        mStorageRef = FirebaseStorage.getInstance().getReference("UserPictures");

        loader = new ProgressDialog(getActivity());
        loader.setTitle("Updating");
        loader.setMessage("Updating your credentials! Please wait...");
        loader.setCanceledOnTouchOutside(false);

        nameEt = view.findViewById(R.id.nameEditId);
        emailEt = view.findViewById(R.id.emailEditId);
        passwordEt = view.findViewById(R.id.passwordEditId);
        teamEdit = view.findViewById(R.id.teamEditId);
        btnUpdate = view.findViewById(R.id.updateEditId);
        imageProfileEdit = view.findViewById(R.id.imageProfileEditId);

        backBtn = view.findViewById(R.id.backBtnId);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });

        db.document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    try {
                        User user = task.getResult().toObject(User.class);
                        if (user != null) {
                            nameEt.setText(user.getName());
                            emailEt.setText(user.getEmail());
                            Glide.with(getActivity()).load(user.getImgUrl())
                                    .placeholder(R.drawable.ic_baseline_image_24)
                                    .into(imageProfileEdit);

                            ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getActivity(),
                                    R.array.teams, android.R.layout.simple_list_item_activated_1);
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
                            teamEdit.setAdapter(arrayAdapter);
                            teamEdit.setSelection(arrayAdapter.getPosition(user.getTeam().name()));
                            teamEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedTeam = parent.getItemAtPosition(position).toString();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });

                            btnUpdate.setEnabled(true);
                            dbEmail = user.getEmail();
                        } else {
                            Toast.makeText(getActivity(), "Please check your availability in database!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: " + e.getMessage());
                        Toast.makeText(getActivity(), "Please check your availability in database!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Please check your availability in database!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageProfileEdit.setOnClickListener(v -> {
            imageChooser();
        });

        btnUpdate.setOnClickListener(v -> {
            loader.show();

            String newEmail = emailEt.getText().toString().trim();
            String newPassword = passwordEt.getText().toString();
            boolean emailChanged = dbEmail != null && !dbEmail.equals(newEmail);
            boolean passwordChanged = !newPassword.isEmpty();

            if (passwordChanged && newPassword.length() < 6) {
                loader.dismiss();
                passwordEt.setError("Minimum password length is 6");
                return;
            }

            if (emailChanged || passwordChanged) {
                // Changing email or password requires Firebase re-authentication first.
                // Passwords are never stored in Firestore, so the current password
                // must be provided by the user.
                loader.dismiss();
                promptForCurrentPassword(currentPassword ->
                        reauthenticateAndUpdate(currentPassword, newEmail, newPassword, emailChanged, passwordChanged));
            } else {
                updateProfileDocument(newEmail);
            }
        });
    }

    private interface PasswordCallback {
        void onPasswordEntered(String currentPassword);
    }

    private void promptForCurrentPassword(PasswordCallback callback) {
        TextInputEditText input = new TextInputEditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Current password");

        FrameLayout wrapper = new FrameLayout(getActivity());
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        wrapper.setPadding(padding, padding, padding, 0);
        wrapper.addView(input);

        new AlertDialog.Builder(getActivity())
                .setTitle("Confirm your identity")
                .setMessage("Enter your current password to continue.")
                .setView(wrapper)
                .setPositiveButton("Confirm", (dialog, which) ->
                        callback.onPasswordEntered(input.getText().toString()))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void reauthenticateAndUpdate(String currentPassword, String newEmail,
                                         String newPassword, boolean emailChanged, boolean passwordChanged) {
        loader.show();
        AuthCredential credential = EmailAuthProvider.getCredential(dbEmail, currentPassword);
        firebaseAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        loader.dismiss();
                        Toast.makeText(getActivity(), "Current password is incorrect!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (passwordChanged) {
                        firebaseAuth.getCurrentUser().updatePassword(newPassword)
                                .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Password updated!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to update password!", Toast.LENGTH_SHORT).show());
                    }
                    if (emailChanged) {
                        firebaseAuth.getCurrentUser().updateEmail(newEmail)
                                .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Email updated!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to update email!", Toast.LENGTH_SHORT).show());
                    }
                    updateProfileDocument(newEmail);
                });
    }

    private void updateProfileDocument(String newEmail) {
        if (selectedImageUri != null) {
            StorageReference fileReference = mStorageRef.child(firebaseAuth.getCurrentUser().getUid() + "." + getFileExtension(selectedImageUri));
            fileReference.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                task.addOnSuccessListener(uri -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", nameEt.getText().toString());
                    map.put("email", newEmail);
                    map.put("team", selectedTeam);
                    map.put("imgUrl", uri.toString());
                    updateDocument(map);
                });
            }).addOnFailureListener(e -> {
                loader.dismiss();
                Log.e(TAG, "editProfileFragment: " + e.getMessage());
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("name", nameEt.getText().toString());
            map.put("email", newEmail);
            map.put("team", selectedTeam);
            updateDocument(map);
        }
    }

    private void updateDocument(Map<String, Object> map) {
        db.document(firebaseAuth.getCurrentUser().getUid()).update(map)
                .addOnCompleteListener(task -> {
                    loader.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Updated Successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to update user! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
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
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                            imageProfileEdit.setImageBitmap(selectedImageBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}
