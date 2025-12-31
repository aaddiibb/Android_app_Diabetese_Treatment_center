package com.example.diabetestreatmentcenter;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diabetestreatmentcenter.adapters.DoctorAdapter;
import com.example.diabetestreatmentcenter.models.Doctor;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DoctorsListActivity extends AppCompatActivity {

    private static final String TAG = "DoctorsListActivity";
    private DoctorAdapter doctorAdapter;
    private FirebaseFirestore db;
    private ListenerRegistration doctorsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctors_list);

        db = FirebaseFirestore.getInstance();

        RecyclerView doctorsRecyclerView = findViewById(R.id.doctorsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        doctorsRecyclerView.setLayoutManager(layoutManager);

        // Enable smooth scrolling and optimizations
        doctorsRecyclerView.setHasFixedSize(true);
        doctorsRecyclerView.setItemViewCacheSize(20);
        doctorsRecyclerView.setDrawingCacheEnabled(true);
        doctorsRecyclerView.setDrawingCacheQuality(android.view.View.DRAWING_CACHE_QUALITY_HIGH);

        // Add spacing between items
        doctorsRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                     @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = 16; // Add 16dp spacing between items
            }
        });

        // Initialize adapter with click listener
        doctorAdapter = new DoctorAdapter(doctor -> {
            // Handle book appointment click
            Toast.makeText(DoctorsListActivity.this,
                    "Booking appointment with Dr. " + doctor.getName(),
                    Toast.LENGTH_SHORT).show();
            // TODO: Create BookAppointmentActivity
        });

        doctorsRecyclerView.setAdapter(doctorAdapter);

        loadDoctors();
    }

    private void loadDoctors() {
        Log.d(TAG, "Loading doctors from Firestore...");

        // Use a realtime listener to automatically update when new doctors are added
        doctorsListener = db.collection("doctors")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading doctors", error);
                        Toast.makeText(DoctorsListActivity.this,
                                "Error loading doctors: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        List<Doctor> doctors = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Doctor doctor = doc.toObject(Doctor.class);
                            doctor.setId(doc.getId());
                            doctors.add(doctor);
                            Log.d(TAG, "Loaded doctor: " + doctor.getName() + " - " + doctor.getSpecialty());
                        }

                        Log.d(TAG, "Total doctors loaded: " + doctors.size());
                        doctorAdapter.setDoctors(doctors);

                        if (doctors.isEmpty()) {
                            Toast.makeText(DoctorsListActivity.this,
                                    "No doctors available yet",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the listener when activity is destroyed
        if (doctorsListener != null) {
            doctorsListener.remove();
        }
    }
}
