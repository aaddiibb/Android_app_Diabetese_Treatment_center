package com.example.diabetestreatmentcenter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button patientButton, doctorButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        patientButton = findViewById(R.id.patientButton);
        doctorButton = findViewById(R.id.doctorButton);

        patientButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AuthChoiceActivity.class);
            intent.putExtra("ROLE", "PATIENT");
            startActivity(intent);
        });

        doctorButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AuthChoiceActivity.class);
            intent.putExtra("ROLE", "DOCTOR");
            startActivity(intent);
        });
    }
}