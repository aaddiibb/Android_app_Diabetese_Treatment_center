package com.example.diabetestreatmentcenter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diabetestreatmentcenter.models.User;
import com.example.diabetestreatmentcenter.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private String role;
    private TextView titleLabel;
    private EditText emailField, passwordField;
    private Button loginButton, backButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        role = getIntent().getStringExtra("ROLE");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        titleLabel = findViewById(R.id.titleLabel);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);
        backButton = findViewById(R.id.backButton);

        titleLabel.setText(role.equals("PATIENT") ? "Patient Login" : "Doctor Login");

        loginButton.setOnClickListener(v -> handleLogin());
        backButton.setOnClickListener(v -> finish());
    }

    private void handleLogin() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setText("Logging in...");
        loginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Fetch user data from Firestore
                            db.collection("users").document(firebaseUser.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            User user = documentSnapshot.toObject(User.class);
                                            if (user != null) {
                                                user.setId(firebaseUser.getUid());

                                                // Check role match
                                                if (!role.equals(user.getRole())) {
                                                    Toast.makeText(LoginActivity.this,
                                                            "You selected " + role + " but this account is " + user.getRole(),
                                                            Toast.LENGTH_LONG).show();
                                                    mAuth.signOut();
                                                    loginButton.setText("Login");
                                                    loginButton.setEnabled(true);
                                                    return;
                                                }

                                                // Save to session
                                                SessionManager.getInstance().setCurrentUser(user);
                                                SessionManager.getInstance().setCurrentUserId(firebaseUser.getUid());

                                                // Navigate to dashboard
                                                if ("PATIENT".equals(user.getRole())) {
                                                    startActivity(new Intent(LoginActivity.this, PatientDashboardActivity.class));
                                                } else {
                                                    startActivity(new Intent(LoginActivity.this, DoctorDashboardActivity.class));
                                                }
                                                finish();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                                                loginButton.setText("Login");
                                                loginButton.setEnabled(true);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        loginButton.setText("Login");
                                        loginButton.setEnabled(true);
                                    });
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        loginButton.setText("Login");
                        loginButton.setEnabled(true);
                    }
                });
    }
}
