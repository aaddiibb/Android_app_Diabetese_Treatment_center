package com.example.diabetestreatmentcenter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diabetestreatmentcenter.models.Doctor;
import com.example.diabetestreatmentcenter.models.User;
import com.example.diabetestreatmentcenter.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private String role;
    private TextView titleLabel, gmailCheckLabel, minLengthCheckLabel, capitalLetterCheckLabel;
    private EditText nameField, emailField, phoneField, passwordField, confirmPasswordField;
    private LinearLayout doctorFieldsBox;
    private EditText specialtyField, clinicField, availableDaysField;
    private Button registerButton, backButton;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        role = getIntent().getStringExtra("ROLE");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupValidation();

        titleLabel.setText(role.equals("PATIENT") ? "Create Patient Account" : "Create Doctor Account");
        doctorFieldsBox.setVisibility("DOCTOR".equals(role) ? View.VISIBLE : View.GONE);

        registerButton.setOnClickListener(v -> handleRegister());
        backButton.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        titleLabel = findViewById(R.id.titleLabel);
        nameField = findViewById(R.id.nameField);
        emailField = findViewById(R.id.emailField);
        phoneField = findViewById(R.id.phoneField);
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        gmailCheckLabel = findViewById(R.id.gmailCheckLabel);
        minLengthCheckLabel = findViewById(R.id.minLengthCheckLabel);
        capitalLetterCheckLabel = findViewById(R.id.capitalLetterCheckLabel);
        doctorFieldsBox = findViewById(R.id.doctorFieldsBox);
        specialtyField = findViewById(R.id.specialtyField);
        clinicField = findViewById(R.id.clinicField);
        availableDaysField = findViewById(R.id.availableDaysField);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupValidation() {
        emailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validateEmail(String email) {
        boolean isGmailValid = email.toLowerCase().contains("@gmail.com");
        
        if (isGmailValid) {
            gmailCheckLabel.setText("✓");
            gmailCheckLabel.setTextColor(0xFF4CAF50);
        } else {
            gmailCheckLabel.setText("✗");
            gmailCheckLabel.setTextColor(0xFFD32F2F);
        }
    }

    private void validatePassword(String password) {
        boolean hasMinLength = password.length() >= 8;
        boolean hasCapitalLetter = password.matches(".*[A-Z].*");
        
        if (hasMinLength) {
            minLengthCheckLabel.setText("✓");
            minLengthCheckLabel.setTextColor(0xFF4CAF50);
        } else {
            minLengthCheckLabel.setText("✗");
            minLengthCheckLabel.setTextColor(0xFFD32F2F);
        }
        
        if (hasCapitalLetter) {
            capitalLetterCheckLabel.setText("✓");
            capitalLetterCheckLabel.setTextColor(0xFF4CAF50);
        } else {
            capitalLetterCheckLabel.setText("✗");
            capitalLetterCheckLabel.setTextColor(0xFFD32F2F);
        }
    }

    private boolean isEmailValid(String email) {
        return email.toLowerCase().contains("@gmail.com");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8 && password.matches(".*[A-Z].*");
    }

    private void handleRegister() {
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || 
            TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmailValid(email)) {
            Toast.makeText(this, "Email must be a Gmail account", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPasswordValid(password)) {
            Toast.makeText(this, "Password must be at least 8 characters with one capital letter", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("DOCTOR".equals(role)) {
            String specialty = specialtyField.getText().toString().trim();
            if (TextUtils.isEmpty(specialty)) {
                Toast.makeText(this, "Doctor specialty is required", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        registerButton.setEnabled(false);
        registerButton.setText("Creating Account...");

        // Create Firebase Auth user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            
                            // Create User object
                            User user = new User(userId, role, name, email, phone);
                            
                            // Save to Firestore
                            db.collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        if ("DOCTOR".equals(role)) {
                                            saveDoctorProfile(userId, name, email);
                                        } else {
                                            completeRegistration(user, userId);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, "Error saving user: " + e.getMessage(), 
                                                Toast.LENGTH_SHORT).show();
                                        registerButton.setEnabled(true);
                                        registerButton.setText("Create Account");
                                    });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), 
                                Toast.LENGTH_LONG).show();
                        registerButton.setEnabled(true);
                        registerButton.setText("Create Account");
                    }
                });
    }

    private void saveDoctorProfile(String userId, String name, String email) {
        String specialty = specialtyField.getText().toString().trim();
        String clinic = clinicField.getText().toString().trim();
        String availableDays = availableDaysField.getText().toString().trim();
        
        Doctor doctor = new Doctor(null, userId, name, email, specialty, clinic, availableDays);
        
        android.util.Log.d("RegisterActivity", "Saving doctor profile - UserId: " + userId + ", Name: " + name + ", Specialty: " + specialty);

        db.collection("doctors").document(userId)
                .set(doctor)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("RegisterActivity", "Doctor profile saved successfully!");
                    Toast.makeText(RegisterActivity.this, "Doctor profile created successfully!", Toast.LENGTH_SHORT).show();
                    User user = new User(userId, role, name, email, phoneField.getText().toString().trim());
                    completeRegistration(user, userId);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("RegisterActivity", "Error saving doctor profile", e);
                    Toast.makeText(RegisterActivity.this, "Error saving doctor profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    registerButton.setEnabled(true);
                    registerButton.setText("Create Account");
                });
    }

    private void completeRegistration(User user, String userId) {
        SessionManager.getInstance().setCurrentUser(user);
        SessionManager.getInstance().setCurrentUserId(userId);
        
        if ("PATIENT".equals(role)) {
            startActivity(new Intent(RegisterActivity.this, PatientDashboardActivity.class));
        } else {
            startActivity(new Intent(RegisterActivity.this, DoctorDashboardActivity.class));
        }
        finish();
    }
}

