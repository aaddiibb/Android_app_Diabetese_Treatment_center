package com.example.diabetestreatmentcenter;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DoctorsListActivity extends AppCompatActivity {

    private RecyclerView doctorsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctors_list);

        doctorsRecyclerView = findViewById(R.id.doctorsRecyclerView);
        doctorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // TODO: Load doctors from Firebase
        Toast.makeText(this, "Doctors List - To be implemented with Firebase", Toast.LENGTH_SHORT).show();
    }
}
