package com.example.diabetestreatmentcenter;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PatientAppointmentsActivity extends AppCompatActivity {

    private RecyclerView appointmentsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_appointments);

        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView);
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // TODO: Load patient appointments from Firebase
        Toast.makeText(this, "My Appointments - To be implemented with Firebase", Toast.LENGTH_SHORT).show();
    }
}

