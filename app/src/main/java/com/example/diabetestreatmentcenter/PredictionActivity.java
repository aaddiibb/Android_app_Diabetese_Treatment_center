package com.example.diabetestreatmentcenter;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diabetestreatmentcenter.models.PredictionHistory;
import com.example.diabetestreatmentcenter.utils.DiabetesPredictionModel;
import com.example.diabetestreatmentcenter.utils.PredictionResult;
import com.example.diabetestreatmentcenter.utils.SessionManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

public class PredictionActivity extends AppCompatActivity {

    private EditText nameField, ageField, pregnanciesField, glucoseField, bloodPressureField,
                     skinThicknessField, insulinField, bmiField, dpfField;
    private Button predictBtn, clearBtn, backBtn;
    private TextView loadingLabel;
    private LinearLayout resultContainer;

    private DiabetesPredictionModel predictionModel;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        predictionModel = new DiabetesPredictionModel();
        db = FirebaseFirestore.getInstance();

        initializeViews();

        predictBtn.setOnClickListener(v -> handlePredict());
        clearBtn.setOnClickListener(v -> handleClear());
        backBtn.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        nameField = findViewById(R.id.nameField);
        ageField = findViewById(R.id.ageField);
        pregnanciesField = findViewById(R.id.pregnanciesField);
        glucoseField = findViewById(R.id.glucoseField);
        bloodPressureField = findViewById(R.id.bloodPressureField);
        skinThicknessField = findViewById(R.id.skinThicknessField);
        insulinField = findViewById(R.id.insulinField);
        bmiField = findViewById(R.id.bmiField);
        dpfField = findViewById(R.id.dpfField);
        predictBtn = findViewById(R.id.predictBtn);
        clearBtn = findViewById(R.id.clearBtn);
        backBtn = findViewById(R.id.backBtn);
        loadingLabel = findViewById(R.id.loadingLabel);
        resultContainer = findViewById(R.id.resultContainer);
    }

    private void handlePredict() {
        if (!validateInputs()) {
            return;
        }

        predictBtn.setEnabled(false);
        loadingLabel.setVisibility(View.VISIBLE);

        try {
            String name = nameField.getText().toString().trim();
            int age = Integer.parseInt(ageField.getText().toString().trim());
            int pregnancies = Integer.parseInt(pregnanciesField.getText().toString().trim());
            double glucose = Double.parseDouble(glucoseField.getText().toString().trim());
            double bloodPressure = Double.parseDouble(bloodPressureField.getText().toString().trim());
            double skinThickness = Double.parseDouble(skinThicknessField.getText().toString().trim());
            double insulin = Double.parseDouble(insulinField.getText().toString().trim());
            double bmi = Double.parseDouble(bmiField.getText().toString().trim());
            double dpf = Double.parseDouble(dpfField.getText().toString().trim());

            PredictionResult result = predictionModel.predict(
                    pregnancies, glucose, bloodPressure, skinThickness,
                    insulin, bmi, dpf, age
            );

            displayResults(name, result);
            savePredictionToFirebase(result, pregnancies, glucose, bloodPressure,
                                    skinThickness, insulin, bmi, dpf, age);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            predictBtn.setEnabled(true);
            loadingLabel.setVisibility(View.GONE);
        }
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(nameField.getText())) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int age = Integer.parseInt(ageField.getText().toString().trim());
            if (age < 1 || age > 120) {
                Toast.makeText(this, "Age must be between 1 and 120", Toast.LENGTH_SHORT).show();
                return false;
            }

            double glucose = Double.parseDouble(glucoseField.getText().toString().trim());
            if (glucose < 0 || glucose > 1000) {
                Toast.makeText(this, "Glucose must be between 0 and 1000", Toast.LENGTH_SHORT).show();
                return false;
            }

            double bmi = Double.parseDouble(bmiField.getText().toString().trim());
            if (bmi < 10 || bmi > 100) {
                Toast.makeText(this, "BMI must be between 10 and 100", Toast.LENGTH_SHORT).show();
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void displayResults(String name, PredictionResult result) {
        resultContainer.removeAllViews();
        resultContainer.setVisibility(View.VISIBLE);
        loadingLabel.setVisibility(View.GONE);
        predictBtn.setEnabled(true);

        // Title
        TextView titleView = new TextView(this);
        titleView.setText("Prediction Results for " + name);
        titleView.setTextSize(18);
        titleView.setTextColor(0xFFF0F0F0);
        titleView.setPadding(0, 0, 0, 20);
        resultContainer.addView(titleView);

        // Risk percentage
        TextView riskView = new TextView(this);
        riskView.setText(String.format("Diabetes Risk: %.1f%%", result.getRiskPercentage()));
        riskView.setTextSize(16);
        riskView.setTextColor(result.isDiabetic() ? 0xFFD32F2F : 0xFF4CAF50);
        resultContainer.addView(riskView);

        // Risk level
        TextView levelView = new TextView(this);
        levelView.setText("Risk Level: " + result.getRiskLevel());
        levelView.setTextSize(15);
        levelView.setTextColor(result.isDiabetic() ? 0xFFD32F2F : 0xFF4CAF50);
        levelView.setPadding(0, 10, 0, 20);
        resultContainer.addView(levelView);

        // Recommendation
        TextView recommendationView = new TextView(this);
        recommendationView.setText("Recommendation:\n" + result.getRecommendation());
        recommendationView.setTextSize(13);
        recommendationView.setTextColor(0xFFE0E0E0);
        recommendationView.setPadding(0, 0, 0, 20);
        resultContainer.addView(recommendationView);

        // Details
        TextView detailsView = new TextView(this);
        detailsView.setText("Input Summary:\n" + result.getDetailsString());
        detailsView.setTextSize(12);
        detailsView.setTextColor(0xFFBBBBBB);
        resultContainer.addView(detailsView);
    }

    private void savePredictionToFirebase(PredictionResult result, int pregnancies,
                                         double glucose, double bloodPressure, double skinThickness,
                                         double insulin, double bmi, double dpf, int age) {
        String userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == null) return;

        PredictionHistory history = new PredictionHistory();
        history.setUserId(userId);
        history.setPredictionDate(Timestamp.now());
        history.setRiskPercentage(result.getRiskPercentage());
        history.setRiskLevel(result.getRiskLevel());
        history.setPregnancies(pregnancies);
        history.setGlucose(glucose);
        history.setBloodPressure(bloodPressure);
        history.setSkinThickness(skinThickness);
        history.setInsulin(insulin);
        history.setBmi(bmi);
        history.setDpf(dpf);
        history.setAge(age);

        db.collection("prediction_history")
                .add(history)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(PredictionActivity.this, "Prediction saved to history", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Silent failure - not critical
                });
    }

    private void handleClear() {
        nameField.setText("");
        ageField.setText("");
        pregnanciesField.setText("");
        glucoseField.setText("");
        bloodPressureField.setText("");
        skinThicknessField.setText("");
        insulinField.setText("");
        bmiField.setText("");
        dpfField.setText("");
        resultContainer.removeAllViews();
        resultContainer.setVisibility(View.GONE);
        loadingLabel.setVisibility(View.GONE);
        predictBtn.setEnabled(true);
    }
}

