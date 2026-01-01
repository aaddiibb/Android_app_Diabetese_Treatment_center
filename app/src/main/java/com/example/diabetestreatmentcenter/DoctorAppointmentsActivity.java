package com.example.diabetestreatmentcenter;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diabetestreatmentcenter.adapters.DoctorAppointmentAdapter;
import com.example.diabetestreatmentcenter.models.Appointment;
import com.example.diabetestreatmentcenter.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DoctorAppointmentsActivity extends AppCompatActivity {

    private static final String TAG = "DoctorAppointments";
    private DoctorAppointmentAdapter appointmentAdapter;
    private FirebaseFirestore db;
    private ListenerRegistration appointmentsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_appointments);

        db = FirebaseFirestore.getInstance();

        RecyclerView appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView);
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentsRecyclerView.setHasFixedSize(true);

        // Initialize adapter with action listener
        appointmentAdapter = new DoctorAppointmentAdapter(new DoctorAppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onApprove(Appointment appointment) {
                showApproveDialog(appointment);
            }

            @Override
            public void onDecline(Appointment appointment) {
                showDeclineDialog(appointment);
            }
        });

        appointmentsRecyclerView.setAdapter(appointmentAdapter);

        loadAppointments();
    }

    private void loadAppointments() {
        // Get current doctor user ID
        String currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId == null) {
            currentUserId = FirebaseAuth.getInstance().getUid();
        }

        if (currentUserId == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot load appointments - user ID is null");
            return;
        }

        Log.d(TAG, "Loading appointments for doctor: " + currentUserId);

        // Query appointments where doctorUserId matches current user
        final String userId = currentUserId;
        appointmentsListener = db.collection("appointments")
                .whereEqualTo("doctorUserId", userId)
                .orderBy("scheduledAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading appointments", error);
                        Toast.makeText(DoctorAppointmentsActivity.this,
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
                            Log.d(TAG, "Loaded appointment from " + appointment.getPatientName() +
                                    " - Status: " + appointment.getStatus());
                        }

                        Log.d(TAG, "Total appointments loaded: " + appointments.size());
                        appointmentAdapter.setAppointments(appointments);

                        if (appointments.isEmpty()) {
                            Toast.makeText(DoctorAppointmentsActivity.this,
                                    "No appointments yet",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showApproveDialog(Appointment appointment) {
        new AlertDialog.Builder(this)
                .setTitle("Approve Appointment")
                .setMessage("Approve appointment with " + appointment.getPatientName() + "?")
                .setPositiveButton("Approve", (dialog, which) -> approveAppointment(appointment))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeclineDialog(Appointment appointment) {
        new AlertDialog.Builder(this)
                .setTitle("Decline Appointment")
                .setMessage("Decline appointment with " + appointment.getPatientName() + "?")
                .setPositiveButton("Decline", (dialog, which) -> declineAppointment(appointment))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void approveAppointment(Appointment appointment) {
        if (appointment.getId() == null) {
            Toast.makeText(this, "Error: Invalid appointment", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("appointments")
                .document(appointment.getId())
                .update("status", "SCHEDULED")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Appointment approved: " + appointment.getId());
                    Toast.makeText(DoctorAppointmentsActivity.this,
                            "Appointment approved successfully",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error approving appointment", e);
                    Toast.makeText(DoctorAppointmentsActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void declineAppointment(Appointment appointment) {
        if (appointment.getId() == null) {
            Toast.makeText(this, "Error: Invalid appointment", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("appointments")
                .document(appointment.getId())
                .update("status", "CANCELLED")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Appointment declined: " + appointment.getId());
                    Toast.makeText(DoctorAppointmentsActivity.this,
                            "Appointment declined",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error declining appointment", e);
                    Toast.makeText(DoctorAppointmentsActivity.this,
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

