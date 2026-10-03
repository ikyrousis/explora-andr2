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

import com.bumptech.glide.Glide;
import com.example.andr2project.R;
import com.example.andr2project.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private int blueTeamCounter;
    private int redTeamCounter;
    private int yellowTeamCounter;
    private int noTeamCounter;

    public HomeFragment(){
        // require a empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvblueCounter = view.findViewById(R.id.tvBlueTeamCounter);
        TextView tvredCounter = view.findViewById(R.id.tvRedTeamCounter);
        TextView tvyellowCounter = view.findViewById(R.id.tvYellowTeamCounter);

        firebaseAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();

        db.collection("Waypoints").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Log.e(TAG, "onEvent: Listen failed", error);
                    return;
                }
                db.collection("Waypoints").get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){

                                     int blueTeamCounter = 0;
                                     int redTeamCounter = 0;
                                     int yellowTeamCounter = 0;
                                     int noTeamCounter = 0;
                                    for(QueryDocumentSnapshot document : task.getResult()) {
                                        String owner = document.get("owner", String.class);
                                        if (owner.equals("BLUE")) {
                                            blueTeamCounter++;
                                        }
                                        if (owner.equals("RED")) {
                                            redTeamCounter++;
                                        }
                                        if (owner.equals("YELLOW")) {
                                            yellowTeamCounter++;
                                        }
                                        if (owner.equals("NONE")) {
                                            noTeamCounter++;
                                        }
                                    }
                                    tvblueCounter.setText(Integer.toString(blueTeamCounter));
                                    tvredCounter.setText(Integer.toString(redTeamCounter));
                                    tvyellowCounter.setText(Integer.toString(yellowTeamCounter));

                            }
                                else{
                                }
                            }
                        });
            }
        });




        };
}
