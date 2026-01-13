package com.example.diabetestreatmentcenter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diabetestreatmentcenter.adapters.HealthHistoryAdapter;
import com.example.diabetestreatmentcenter.models.PredictionHistory;
import com.example.diabetestreatmentcenter.models.User;
import com.example.diabetestreatmentcenter.utils.ChartGenerator;
import com.example.diabetestreatmentcenter.utils.PDFReportGenerator;
import com.example.diabetestreatmentcenter.utils.SessionManager;
import com.github.mikephil.charting.charts.LineChart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HealthHistoryActivity extends AppCompatActivity {

    private static final String TAG = "HealthHistory";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private HealthHistoryAdapter historyAdapter;
    private FirebaseFirestore db;
    private ListenerRegistration historyListener;
    private TextView emptyStateText;
    private RecyclerView recyclerView;
    private Button toggleViewButton;
    private Button predictionHistoryButton;
    private Button downloadPdfButton;
    private LinearLayout chartContainer;
    private LinearLayout listContainer;
    private Spinner chartTypeSpinner;
    private LineChart lineChart;
    private boolean isChartView = false;
    private List<PredictionHistory> currentHistory = new ArrayList<>();
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_history);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupChartTypeSpinner();

        // Initialize adapter
        historyAdapter = new HealthHistoryAdapter();
        recyclerView.setAdapter(historyAdapter);

        toggleViewButton.setOnClickListener(v -> toggleView());
        predictionHistoryButton.setOnClickListener(v -> openPredictionHistory());
        downloadPdfButton.setOnClickListener(v -> downloadPdfReport());

        loadCurrentUser();
        loadHealthHistory();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.healthHistoryRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        toggleViewButton = findViewById(R.id.toggleViewButton);
        predictionHistoryButton = findViewById(R.id.predictionHistoryButton);
        downloadPdfButton = findViewById(R.id.downloadPdfButton);
        chartContainer = findViewById(R.id.chartContainer);
        listContainer = findViewById(R.id.listContainer);
        chartTypeSpinner = findViewById(R.id.chartTypeSpinner);
        lineChart = findViewById(R.id.lineChart);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }

    private void setupChartTypeSpinner() {
        String[] chartTypes = {
            "Diabetes Risk Trend",
            "Glucose Level Trend",
            "BMI Trend",
            "Blood Pressure Trend",
            "Insulin Level Trend"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            chartTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chartTypeSpinner.setAdapter(adapter);

        chartTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isChartView && !currentHistory.isEmpty()) {
                    updateChart(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void toggleView() {
        isChartView = !isChartView;

        if (isChartView) {
            if (currentHistory.isEmpty()) {
                Toast.makeText(this, "No data available for charts", Toast.LENGTH_SHORT).show();
                isChartView = false;
                return;
            }

            // Show chart view
            listContainer.setVisibility(View.GONE);
            chartContainer.setVisibility(View.VISIBLE);
            toggleViewButton.setText("Show List");
            updateChart(chartTypeSpinner.getSelectedItemPosition());
        } else {
            // Show list view
            chartContainer.setVisibility(View.GONE);
            listContainer.setVisibility(View.VISIBLE);
            toggleViewButton.setText("Show Charts");
        }
    }

    private void updateChart(int chartType) {
        if (currentHistory.isEmpty()) {
            return;
        }

        switch (chartType) {
            case 0: // Risk Trend
                ChartGenerator.setupRiskTrendChart(lineChart, currentHistory);
                break;
            case 1: // Glucose Trend
                ChartGenerator.setupGlucoseTrendChart(lineChart, currentHistory);
                break;
            case 2: // BMI Trend
                ChartGenerator.setupBMITrendChart(lineChart, currentHistory);
                break;
            case 3: // Blood Pressure Trend
                ChartGenerator.setupBloodPressureTrendChart(lineChart, currentHistory);
                break;
            case 4: // Insulin Trend
                ChartGenerator.setupInsulinTrendChart(lineChart, currentHistory);
                break;
        }
    }

    private void openPredictionHistory() {
        Intent intent = new Intent(HealthHistoryActivity.this, PredictionHistoryActivity.class);
        startActivity(intent);
    }

    private void loadHealthHistory() {
        // Get current user ID
        String currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId == null) {
            currentUserId = FirebaseAuth.getInstance().getUid();
        }

        if (currentUserId == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot load health history - user ID is null");
            showEmptyState(true);
            return;
        }

        Log.d(TAG, "Loading health history for user: " + currentUserId);

        // Query prediction history where userId matches current user
        final String userId = currentUserId;

        // Try query with ordering first, fallback to simple query if index not available
        historyListener = db.collection("prediction_history")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading health history", error);

                        String errorMsg = error.getMessage();

                        // Check for specific error types
                        if (errorMsg != null && errorMsg.toLowerCase().contains("permission")) {
                            Toast.makeText(HealthHistoryActivity.this,
                                    "Permission Denied: Please update Firestore security rules.\nSee FIRESTORE_PERMISSION_FIX.md",
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG, "PERMISSION DENIED! Update Firestore security rules to allow read access to prediction_history collection.");
                        } else if (errorMsg != null && errorMsg.contains("index")) {
                            Toast.makeText(HealthHistoryActivity.this,
                                    "Note: History will be shown in upload order (no sorting)",
                                    Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Firestore index not created yet. Data shown without ordering.");
                        } else {
                            Toast.makeText(HealthHistoryActivity.this,
                                    "Error loading history: " + errorMsg,
                                    Toast.LENGTH_SHORT).show();
                        }
                        showEmptyState(true);
                        return;
                    }

                    if (value != null) {
                        List<PredictionHistory> historyList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            PredictionHistory history = doc.toObject(PredictionHistory.class);
                            history.setId(doc.getId());
                            historyList.add(history);
                            Log.d(TAG, "Loaded history entry - Risk: " + history.getRiskPercentage() +
                                    "% - Level: " + history.getRiskLevel());
                        }

                        Log.d(TAG, "Total history entries loaded: " + historyList.size());

                        // Sort in memory by date (newest first) if timestamps are available
                        historyList.sort((h1, h2) -> {
                            if (h1.getPredictionDate() != null && h2.getPredictionDate() != null) {
                                return h2.getPredictionDate().compareTo(h1.getPredictionDate());
                            }
                            return 0;
                        });

                        currentHistory = historyList;
                        historyAdapter.setHistory(historyList);
                        showEmptyState(historyList.isEmpty());

                        // Enable toggle button only if we have data
                        toggleViewButton.setEnabled(!historyList.isEmpty());
                    }
                });
    }

    private void showEmptyState(boolean show) {
        if (show) {
            listContainer.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            toggleViewButton.setEnabled(false);
            downloadPdfButton.setEnabled(false);
        } else {
            if (!isChartView) {
                listContainer.setVisibility(View.VISIBLE);
            }
            emptyStateText.setVisibility(View.GONE);
            toggleViewButton.setEnabled(true);
            downloadPdfButton.setEnabled(true);
        }
    }

    private void loadCurrentUser() {
        String currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId == null) {
            currentUserId = FirebaseAuth.getInstance().getUid();
        }

        if (currentUserId == null) {
            Log.e(TAG, "Cannot load user - user ID is null");
            return;
        }

        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            currentUser.setId(documentSnapshot.getId());
                            Log.d(TAG, "User loaded: " + currentUser.getName());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user", e);
                });
    }

    private void downloadPdfReport() {
        if (currentHistory == null || currentHistory.isEmpty()) {
            Toast.makeText(this, "No health data available to generate report", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for storage permission on Android 6.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
                return;
            }
        }

        generateAndSharePdf();
    }

    private void generateAndSharePdf() {
        Toast.makeText(this, "Generating PDF report...", Toast.LENGTH_SHORT).show();

        // Generate PDF in background
        new Thread(() -> {
            try {
                File pdfFile = PDFReportGenerator.generateHealthReport(
                        HealthHistoryActivity.this,
                        currentUser,
                        currentHistory
                );

                runOnUiThread(() -> {
                    if (pdfFile != null && pdfFile.exists()) {
                        Toast.makeText(HealthHistoryActivity.this,
                                "Report generated successfully!", Toast.LENGTH_SHORT).show();
                        openPdfFile(pdfFile);
                    } else {
                        Toast.makeText(HealthHistoryActivity.this,
                                "Failed to generate PDF report", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error generating PDF", e);
                runOnUiThread(() -> Toast.makeText(HealthHistoryActivity.this,
                        "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void openPdfFile(File pdfFile) {
        try {
            Uri pdfUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    pdfFile
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            Intent chooser = Intent.createChooser(intent, "Open PDF with");
            startActivity(chooser);
        } catch (Exception e) {
            Log.e(TAG, "Error opening PDF", e);
            Toast.makeText(this, "PDF saved to: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateAndSharePdf();
            } else {
                Toast.makeText(this, "Storage permission is required to save PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the listener when activity is destroyed
        if (historyListener != null) {
            historyListener.remove();
        }
    }
}

