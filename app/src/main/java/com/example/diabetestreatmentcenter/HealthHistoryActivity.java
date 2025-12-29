package com.example.diabetestreatmentcenter;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HealthHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_history);

        // TODO: Implement health history with charts
        Toast.makeText(this, "Health History - To be implemented with charts", Toast.LENGTH_SHORT).show();
    }
}

