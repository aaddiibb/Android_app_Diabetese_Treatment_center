package com.example.diabetestreatmentcenter.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diabetestreatmentcenter.R;
import com.example.diabetestreatmentcenter.models.PredictionHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PredictionHistoryAdapter extends RecyclerView.Adapter<PredictionHistoryAdapter.PredictionHistoryViewHolder> {

    private List<PredictionHistory> historyList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

    public void setHistory(List<PredictionHistory> history) {
        this.historyList = history;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PredictionHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prediction_history, parent, false);
        return new PredictionHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PredictionHistoryViewHolder holder, int position) {
        PredictionHistory history = historyList.get(position);
        holder.bind(history, dateFormat);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class PredictionHistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView dateText;
        private final TextView riskPercentageText;
        private final TextView riskLevelText;
        private final TextView glucoseText;
        private final TextView bmiText;
        private final TextView bloodPressureText;
        private final TextView insulinText;
        private final TextView ageText;
        private final TextView dpfText;
        private final TextView pregnanciesText;
        private final TextView skinThicknessText;

        public PredictionHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            riskPercentageText = itemView.findViewById(R.id.riskPercentageText);
            riskLevelText = itemView.findViewById(R.id.riskLevelText);
            glucoseText = itemView.findViewById(R.id.glucoseText);
            bmiText = itemView.findViewById(R.id.bmiText);
            bloodPressureText = itemView.findViewById(R.id.bloodPressureText);
            insulinText = itemView.findViewById(R.id.insulinText);
            ageText = itemView.findViewById(R.id.ageText);
            dpfText = itemView.findViewById(R.id.dpfText);
            pregnanciesText = itemView.findViewById(R.id.pregnanciesText);
            skinThicknessText = itemView.findViewById(R.id.skinThicknessText);
        }

        public void bind(PredictionHistory history, SimpleDateFormat dateFormat) {
            // Set date
            if (history.getPredictionDate() != null) {
                Date date = history.getPredictionDate().toDate();
                dateText.setText(dateFormat.format(date));
            } else {
                dateText.setText("Unknown date");
            }

            // Set risk percentage
            riskPercentageText.setText(String.format(Locale.getDefault(), "%.1f%%", history.getRiskPercentage()));

            // Set risk level with color
            String riskLevel = history.getRiskLevel() != null ? history.getRiskLevel() : "UNKNOWN";
            riskLevelText.setText(riskLevel);

            // Color code based on risk level
            if (riskLevel.equalsIgnoreCase("HIGH")) {
                riskPercentageText.setTextColor(0xFFF44336); // Red
                riskLevelText.setTextColor(0xFFF44336);
            } else if (riskLevel.equalsIgnoreCase("LOW")) {
                riskPercentageText.setTextColor(0xFF4CAF50); // Green
                riskLevelText.setTextColor(0xFF4CAF50);
            } else {
                riskPercentageText.setTextColor(0xFFFFA726); // Orange
                riskLevelText.setTextColor(0xFFFFA726);
            }

            // Set all health metrics
            glucoseText.setText(String.format(Locale.getDefault(), "Glucose: %.1f mg/dL", history.getGlucose()));
            bmiText.setText(String.format(Locale.getDefault(), "BMI: %.1f", history.getBmi()));
            bloodPressureText.setText(String.format(Locale.getDefault(), "Blood Pressure: %.1f mmHg", history.getBloodPressure()));
            insulinText.setText(String.format(Locale.getDefault(), "Insulin: %.1f μU/mL", history.getInsulin()));
            ageText.setText(String.format(Locale.getDefault(), "Age: %d years", history.getAge()));
            dpfText.setText(String.format(Locale.getDefault(), "Diabetes Pedigree: %.3f", history.getDpf()));
            skinThicknessText.setText(String.format(Locale.getDefault(), "Skin Thickness: %.1f mm", history.getSkinThickness()));

            if (history.getPregnancies() > 0) {
                pregnanciesText.setText(String.format(Locale.getDefault(), "Pregnancies: %d", history.getPregnancies()));
                pregnanciesText.setVisibility(View.VISIBLE);
            } else {
                pregnanciesText.setVisibility(View.GONE);
            }
        }
    }
}

