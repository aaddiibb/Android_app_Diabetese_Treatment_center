package com.example.diabetestreatmentcenter;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diabetestreatmentcenter.adapters.PredictionHistoryAdapter;
import com.example.diabetestreatmentcenter.models.PredictionHistory;
import com.example.diabetestreatmentcenter.utils.SessionManager;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PredictionHistoryActivity extends AppCompatActivity {

    private static final String TAG = "PredictionHistory";
    private PredictionHistoryAdapter historyAdapter;
    private FirebaseFirestore db;
    private ListenerRegistration historyListener;
    private TextView emptyStateText, startDateText, endDateText, filterStatusText;
    private RecyclerView recyclerView;
    private Button selectStartDateButton, selectEndDateButton, applyFilterButton, clearFilterButton;

    private Calendar startDate = null;
    private Calendar endDate = null;
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction_history);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupButtons();

        // Initialize adapter
        historyAdapter = new PredictionHistoryAdapter();
        recyclerView.setAdapter(historyAdapter);

        loadPredictionHistory(null, null);
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.predictionHistoryRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        startDateText = findViewById(R.id.startDateText);
        endDateText = findViewById(R.id.endDateText);
        filterStatusText = findViewById(R.id.filterStatusText);
        selectStartDateButton = findViewById(R.id.selectStartDateButton);
        selectEndDateButton = findViewById(R.id.selectEndDateButton);
        applyFilterButton = findViewById(R.id.applyFilterButton);
        clearFilterButton = findViewById(R.id.clearFilterButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }

    private void setupButtons() {
        selectStartDateButton.setOnClickListener(v -> showStartDatePicker());
        selectEndDateButton.setOnClickListener(v -> showEndDatePicker());
        applyFilterButton.setOnClickListener(v -> applyDateFilter());
        clearFilterButton.setOnClickListener(v -> clearFilter());
    }

    private void showStartDatePicker() {
        Calendar calendar = startDate != null ? startDate : Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                startDate = Calendar.getInstance();
                startDate.set(year, month, dayOfMonth, 0, 0, 0);
                startDate.set(Calendar.MILLISECOND, 0);
                startDateText.setText(displayDateFormat.format(startDate.getTime()));
                startDateText.setVisibility(View.VISIBLE);
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void showEndDatePicker() {
        Calendar calendar = endDate != null ? endDate : Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                endDate = Calendar.getInstance();
                endDate.set(year, month, dayOfMonth, 23, 59, 59);
                endDate.set(Calendar.MILLISECOND, 999);
                endDateText.setText(displayDateFormat.format(endDate.getTime()));
                endDateText.setVisibility(View.VISIBLE);
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void applyDateFilter() {
        if (startDate == null || endDate == null) {
            Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startDate.after(endDate)) {
            Toast.makeText(this, "Start date must be before end date", Toast.LENGTH_SHORT).show();
            return;
        }

        loadPredictionHistory(startDate.getTime(), endDate.getTime());

        String filterText = "Showing: " + displayDateFormat.format(startDate.getTime()) +
                           " to " + displayDateFormat.format(endDate.getTime());
        filterStatusText.setText(filterText);
        filterStatusText.setVisibility(View.VISIBLE);
    }

    private void clearFilter() {
        startDate = null;
        endDate = null;
        startDateText.setVisibility(View.GONE);
        endDateText.setVisibility(View.GONE);
        filterStatusText.setVisibility(View.GONE);
        loadPredictionHistory(null, null);
    }

    private void loadPredictionHistory(Date startDateFilter, Date endDateFilter) {
        // Get current user ID
        String currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId == null) {
            currentUserId = FirebaseAuth.getInstance().getUid();
        }

        if (currentUserId == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot load prediction history - user ID is null");
            showEmptyState(true);
            return;
        }

        Log.d(TAG, "Loading prediction history for user: " + currentUserId);

        final String userId = currentUserId;

        // Remove previous listener if exists
        if (historyListener != null) {
            historyListener.remove();
        }

        // Query prediction history
        historyListener = db.collection("prediction_history")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading prediction history", error);

                        String errorMsg = error.getMessage();

                        if (errorMsg != null && errorMsg.toLowerCase().contains("permission")) {
                            Toast.makeText(PredictionHistoryActivity.this,
                                    "Permission Denied: Please update Firestore security rules.",
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG, "PERMISSION DENIED! Update Firestore security rules.");
                        } else {
                            Toast.makeText(PredictionHistoryActivity.this,
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

                            // Apply date filter if specified
                            if (startDateFilter != null && endDateFilter != null && history.getPredictionDate() != null) {
                                Date predDate = history.getPredictionDate().toDate();
                                if (predDate.before(startDateFilter) || predDate.after(endDateFilter)) {
                                    continue; // Skip this entry
                                }
                            }

                            historyList.add(history);
                            Log.d(TAG, "Loaded prediction - Risk: " + history.getRiskPercentage() +
                                    "% - Date: " + (history.getPredictionDate() != null ?
                                    history.getPredictionDate().toDate() : "null"));
                        }

                        Log.d(TAG, "Total predictions loaded: " + historyList.size());

                        // Sort in memory by date (newest first)
                        historyList.sort((h1, h2) -> {
                            if (h1.getPredictionDate() != null && h2.getPredictionDate() != null) {
                                return h2.getPredictionDate().compareTo(h1.getPredictionDate());
                            }
                            return 0;
                        });

                        historyAdapter.setHistory(historyList);
                        showEmptyState(historyList.isEmpty());
                    }
                });
    }

    private void showEmptyState(boolean show) {
        if (show) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (historyListener != null) {
            historyListener.remove();
        }
    }
}

