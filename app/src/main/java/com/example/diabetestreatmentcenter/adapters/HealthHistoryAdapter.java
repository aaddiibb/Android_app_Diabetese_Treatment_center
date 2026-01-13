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

public class HealthHistoryAdapter extends RecyclerView.Adapter<HealthHistoryAdapter.HealthHistoryViewHolder> {

    private List<PredictionHistory> historyList = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

    public void setHistory(List<PredictionHistory> history) {
        this.historyList = history;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HealthHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_health_history, parent, false);
        return new HealthHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HealthHistoryViewHolder holder, int position) {
        PredictionHistory history = historyList.get(position);
        holder.bind(history);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    class HealthHistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView dateText, riskPercentageText, riskLevelText, detailsText;

        public HealthHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            riskPercentageText = itemView.findViewById(R.id.riskPercentageText);
            riskLevelText = itemView.findViewById(R.id.riskLevelText);
            detailsText = itemView.findViewById(R.id.detailsText);
        }

        public void bind(PredictionHistory history) {
            // Set date
            if (history.getPredictionDate() != null) {
                Date date = history.getPredictionDate().toDate();
                dateText.setText(dateFormat.format(date));
            } else {
                dateText.setText("Unknown date");
            }

            // Set risk percentage
            riskPercentageText.setText(String.format("Risk: %.1f%%", history.getRiskPercentage()));

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

            // Set details
            StringBuilder details = new StringBuilder();
            details.append("Age: ").append(history.getAge())
                   .append(" | Glucose: ").append(String.format("%.1f", history.getGlucose()))
                   .append("\nBMI: ").append(String.format("%.1f", history.getBmi()))
                   .append(" | BP: ").append(String.format("%.1f", history.getBloodPressure()));

            if (history.getPregnancies() > 0) {
                details.append("\nPregnancies: ").append(history.getPregnancies());
            }

            detailsText.setText(details.toString());
        }
    }
}

