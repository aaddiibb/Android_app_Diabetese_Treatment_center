package com.example.diabetestreatmentcenter;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diabetestreatmentcenter.adapters.AppointmentAdapter;
import com.example.diabetestreatmentcenter.models.Appointment;
import com.example.diabetestreatmentcenter.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PatientAppointmentsActivity extends AppCompatActivity {

    private static final String TAG = "PatientAppointments";
    private AppointmentAdapter appointmentAdapter;
    private FirebaseFirestore db;
    private ListenerRegistration appointmentsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_appointments);

        db = FirebaseFirestore.getInstance();

        RecyclerView appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView);
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentsRecyclerView.setHasFixedSize(true);

        // Initialize adapter
        appointmentAdapter = new AppointmentAdapter();
        appointmentsRecyclerView.setAdapter(appointmentAdapter);

        loadAppointments();
    }

    private void loadAppointments() {
        // Get current user ID
        String currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId == null) {
            currentUserId = FirebaseAuth.getInstance().getUid();
        }

        if (currentUserId == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot load appointments - user ID is null");
            return;
        }

        Log.d(TAG, "Loading appointments for user: " + currentUserId);

        // Query appointments where patientUserId matches current user
        final String userId = currentUserId;
        appointmentsListener = db.collection("appointments")
                .whereEqualTo("patientUserId", userId)
                .orderBy("scheduledAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading appointments", error);
                        Toast.makeText(PatientAppointmentsActivity.this,
                                "Error loading appointments: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        List<Appointment> appointments = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Appointment appointment = doc.toObject(Appointment.class);
                            appointment.setId(doc.getId());
                            appointments.add(appointment);
                            Log.d(TAG, "Loaded appointment with Dr. " + appointment.getDoctorName() +
                                    " - Status: " + appointment.getStatus());
                        }

                        Log.d(TAG, "Total appointments loaded: " + appointments.size());
                        appointmentAdapter.setAppointments(appointments);

                        if (appointments.isEmpty()) {
                            Toast.makeText(PatientAppointmentsActivity.this,
                                    "No appointments yet. Book one from the dashboard!",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the listener when activity is destroyed
        if (appointmentsListener != null) {
            appointmentsListener.remove();
        }
    }
}

