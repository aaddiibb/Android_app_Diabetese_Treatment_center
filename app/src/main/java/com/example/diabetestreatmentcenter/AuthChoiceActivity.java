package com.example.diabetestreatmentcenter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AuthChoiceActivity extends AppCompatActivity {

    private String role;
    private TextView titleLabel;
    private Button loginButton, registerButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_choice);

        role = getIntent().getStringExtra("ROLE");

        titleLabel = findViewById(R.id.titleLabel);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);

        titleLabel.setText("Continue as " + role);

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(AuthChoiceActivity.this, LoginActivity.class);
            intent.putExtra("ROLE", role);
            startActivity(intent);
        });

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(AuthChoiceActivity.this, RegisterActivity.class);
            intent.putExtra("ROLE", role);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> finish());
    }
}
