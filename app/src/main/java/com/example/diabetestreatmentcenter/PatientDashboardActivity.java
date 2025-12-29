package com.example.diabetestreatmentcenter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diabetestreatmentcenter.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class PatientDashboardActivity extends AppCompatActivity {

    private Button bookAppointmentButton, myAppointmentsButton, predictionButton,
                   healthHistoryButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);

        bookAppointmentButton = findViewById(R.id.bookAppointmentButton);
        myAppointmentsButton = findViewById(R.id.myAppointmentsButton);
        predictionButton = findViewById(R.id.predictionButton);
        healthHistoryButton = findViewById(R.id.healthHistoryButton);
        logoutButton = findViewById(R.id.logoutButton);

        bookAppointmentButton.setOnClickListener(v -> {
            startActivity(new Intent(PatientDashboardActivity.this, DoctorsListActivity.class));
        });

        myAppointmentsButton.setOnClickListener(v -> {
            startActivity(new Intent(PatientDashboardActivity.this, PatientAppointmentsActivity.class));
        });

        predictionButton.setOnClickListener(v -> {
            startActivity(new Intent(PatientDashboardActivity.this, PredictionActivity.class));
        });

        healthHistoryButton.setOnClickListener(v -> {
            startActivity(new Intent(PatientDashboardActivity.this, HealthHistoryActivity.class));
        });

        logoutButton.setOnClickListener(v -> handleLogout());
    }

    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        SessionManager.getInstance().clear();
        Intent intent = new Intent(PatientDashboardActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
