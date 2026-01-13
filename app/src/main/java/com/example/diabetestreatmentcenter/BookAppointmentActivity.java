package com.example.diabetestreatmentcenter;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diabetestreatmentcenter.models.Appointment;
import com.example.diabetestreatmentcenter.utils.SessionManager;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BookAppointmentActivity extends AppCompatActivity {

    private static final String TAG = "BookAppointment";

    private TextView doctorNameText, doctorSpecialtyText;
    private EditText patientNoteInput;
    private Button selectDateButton, selectTimeButton, bookButton;
    private String doctorId, doctorName, doctorSpecialty;
    private Calendar selectedDateTime;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        db = FirebaseFirestore.getInstance();
        selectedDateTime = Calendar.getInstance();

        // Get doctor info from intent
        doctorId = getIntent().getStringExtra("doctorId");
        doctorName = getIntent().getStringExtra("doctorName");
        doctorSpecialty = getIntent().getStringExtra("doctorSpecialty");

        if (doctorId == null || doctorName == null) {
            Toast.makeText(this, "Error: Doctor information missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        displayDoctorInfo();
        setupClickListeners();
    }

    private void initializeViews() {
        doctorNameText = findViewById(R.id.doctorNameText);
        doctorSpecialtyText = findViewById(R.id.doctorSpecialtyText);
        patientNoteInput = findViewById(R.id.patientNoteInput);
        selectDateButton = findViewById(R.id.selectDateButton);
        selectTimeButton = findViewById(R.id.selectTimeButton);
        bookButton = findViewById(R.id.bookButton);
    }

    private void displayDoctorInfo() {
        doctorNameText.setText("Dr. " + doctorName);
        if (doctorSpecialty != null && !doctorSpecialty.isEmpty()) {
            doctorSpecialtyText.setText("Specialty: " + doctorSpecialty);
        } else {
            doctorSpecialtyText.setText("Diabetes Specialist");
        }
    }

    private void setupClickListeners() {
        selectDateButton.setOnClickListener(v -> showDatePicker());
        selectTimeButton.setOnClickListener(v -> showTimePicker());
        bookButton.setOnClickListener(v -> bookAppointment());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    String dateText = String.format(Locale.getDefault(), "Date: %02d/%02d/%d",
                            dayOfMonth, month + 1, year);
                    selectDateButton.setText(dateText);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);

                    String timeText = String.format(Locale.getDefault(), "Time: %02d:%02d", hourOfDay, minute);
                    selectTimeButton.setText(timeText);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // 24-hour format
        );

        timePickerDialog.show();
    }

    private void bookAppointment() {
        // Validate inputs
        if (selectDateButton.getText().toString().equals("Select Date")) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectTimeButton.getText().toString().equals("Select Time")) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get patient info
        String tempUserId = SessionManager.getInstance().getCurrentUserId();
        if (tempUserId == null) {
            tempUserId = FirebaseAuth.getInstance().getUid();
        }

        if (tempUserId == null) {
            Toast.makeText(this, "Error: Not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Patient user ID is null!");
            return;
        }

        // Make final for lambda
        final String patientUserId = tempUserId;

        String patientName = SessionManager.getInstance().getCurrentUser() != null ?
                SessionManager.getInstance().getCurrentUser().getName() : "Patient";

        String patientNote = patientNoteInput.getText().toString().trim();

        // Create appointment object
        Appointment appointment = new Appointment();
        appointment.setPatientUserId(patientUserId);
        appointment.setPatientName(patientName);
        appointment.setDoctorUserId(doctorId);
        appointment.setDoctorName(doctorName);
        appointment.setScheduledAt(new Timestamp(new Date(selectedDateTime.getTimeInMillis())));
        appointment.setStatus("REQUESTED");
        appointment.setPatientNote(patientNote.isEmpty() ? null : patientNote);
        appointment.setDoctorNote(null);

        Log.d(TAG, "Booking appointment - Patient: " + patientName + " (ID: " + patientUserId + "), Doctor: " + doctorName + " (ID: " + doctorId + ")");

        // Save to Firestore
        bookButton.setEnabled(false);
        db.collection("appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Appointment booked successfully with ID: " + documentReference.getId());
                    Log.d(TAG, "Appointment details - PatientUserId: " + patientUserId + ", DoctorUserId: " + doctorId);
                    Toast.makeText(BookAppointmentActivity.this,
                            "Appointment requested successfully!",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error booking appointment", e);
                    Toast.makeText(BookAppointmentActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    bookButton.setEnabled(true);
                });
    }
}

