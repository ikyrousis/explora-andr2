package com.example.andr2project.BottomFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.andr2project.Constants;
import com.example.andr2project.ProfileActivity;
import com.example.andr2project.R;
import com.example.andr2project.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    TextView nameTv, emailTv, teamTv;
    CircleImageView imageProfileView;
    FirebaseAuth firebaseAuth;
    MaterialButton btnProfileView;
    private CollectionReference db;
    private static final String TAG = "ProfileFragment";

    public ProfileFragment() {
        // require a empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nameTv = view.findViewById(R.id.nameProfileViewId);
        emailTv = view.findViewById(R.id.emailProfileViewId);
        teamTv = view.findViewById(R.id.teamProfileViewId);
        btnProfileView = view.findViewById(R.id.btnProfileViewId);
        imageProfileView = view.findViewById(R.id.imageProfileViewId);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance().collection("Users");

        db.document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    try {
                        User user = task.getResult().toObject(User.class);
                        if (user != null) {
                            nameTv.setText(user.getName());
                            emailTv.setText(user.getEmail());
                            teamTv.setText(user.getTeam().name());
                            Glide.with(getActivity()).load(user.getImgUrl())
                                    .placeholder(R.drawable.ic_baseline_image_24)
                                    .into(imageProfileView);
                        } else {
                            Toast.makeText(getActivity(), "Please check your availability in database!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: " + e.getMessage());
                    }
                } else {
                    Toast.makeText(getActivity(), "Please check your availability in database!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.flFragment, new EditProfileFragment(), Constants.Edit_Profile_Fragment);
                ft.addToBackStack("New_State");
                ft.commit();
            }
        });
    }
}
