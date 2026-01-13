package com.example.diabetestreatmentcenter;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
        appointmentAdapter.setCancelListener(appointment -> showCancelDialog(appointment));
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
        // Removed orderBy to avoid Firestore index requirement - will sort in memory
        final String userId = currentUserId;
        appointmentsListener = db.collection("appointments")
                .whereEqualTo("patientUserId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading appointments", error);

                        String errorMsg = error.getMessage();

                        // Check for specific error types
                        if (errorMsg != null && errorMsg.toLowerCase().contains("permission")) {
                            Toast.makeText(PatientAppointmentsActivity.this,
                                    "Permission Denied: Please update Firestore security rules.\nSee FIRESTORE_PERMISSION_FIX.md",
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG, "PERMISSION DENIED! Update Firestore security rules to allow read access to appointments collection.");
                        } else if (errorMsg != null && errorMsg.contains("index")) {
                            Toast.makeText(PatientAppointmentsActivity.this,
                                    "Note: Appointments shown in upload order",
                                    Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Firestore index not created. Showing appointments without ordering.");
                        } else {
                            Toast.makeText(PatientAppointmentsActivity.this,
                                    "Error loading appointments: " + errorMsg,
                                    Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (value != null) {
                        List<Appointment> appointments = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Appointment appointment = doc.toObject(Appointment.class);
                            appointment.setId(doc.getId());
                            appointments.add(appointment);
                            Log.d(TAG, "Loaded appointment with Dr. " + appointment.getDoctorName() +
                                    " - Status: " + appointment.getStatus() +
                                    " - PatientUserId: " + appointment.getPatientUserId());
                        }

                        // Sort in memory by scheduled date (newest first)
                        appointments.sort((a1, a2) -> {
                            if (a1.getScheduledAt() != null && a2.getScheduledAt() != null) {
                                return a2.getScheduledAt().compareTo(a1.getScheduledAt());
                            }
                            return 0;
                        });

                        Log.d(TAG, "Total appointments loaded: " + appointments.size());
                        appointmentAdapter.setAppointments(appointments);

                        if (appointments.isEmpty()) {
                            Toast.makeText(PatientAppointmentsActivity.this,
                                    "No appointments yet. Book one from the dashboard!",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.w(TAG, "Appointments query returned null");
                    }
                });
    }

    private void showCancelDialog(Appointment appointment) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel your appointment with Dr. " + appointment.getDoctorName() + "?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelAppointment(appointment))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelAppointment(Appointment appointment) {
        if (appointment.getId() == null) {
            Toast.makeText(this, "Error: Invalid appointment", Toast.LENGTH_SHORT).show();
            return;
        }

        // Delete the appointment from Firestore
        db.collection("appointments")
                .document(appointment.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Appointment cancelled and deleted: " + appointment.getId());
                    Toast.makeText(PatientAppointmentsActivity.this,
                            "Appointment cancelled successfully",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cancelling appointment", e);
                    Toast.makeText(PatientAppointmentsActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
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

