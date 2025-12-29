package com.example.diabetestreatmentcenter;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DoctorAppointmentsActivity extends AppCompatActivity {

    private RecyclerView appointmentsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_appointments);

        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView);
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // TODO: Load doctor appointments from Firebase
        Toast.makeText(this, "Doctor Appointments - To be implemented with Firebase", Toast.LENGTH_SHORT).show();
    }
}

