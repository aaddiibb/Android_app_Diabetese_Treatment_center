package com.example.diabetestreatmentcenter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diabetestreatmentcenter.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class DoctorDashboardActivity extends AppCompatActivity {

    private Button appointmentsButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        appointmentsButton = findViewById(R.id.appointmentsButton);
        logoutButton = findViewById(R.id.logoutButton);

        appointmentsButton.setOnClickListener(v -> {
            startActivity(new Intent(DoctorDashboardActivity.this, DoctorAppointmentsActivity.class));
        });

        logoutButton.setOnClickListener(v -> handleLogout());
    }

    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        SessionManager.getInstance().clear();
        Intent intent = new Intent(DoctorDashboardActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

