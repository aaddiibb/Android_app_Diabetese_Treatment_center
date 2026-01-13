package com.example.diabetestreatmentcenter.utils;

import android.graphics.Color;

import com.example.diabetestreatmentcenter.models.PredictionHistory;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChartGenerator {

    private static final int CHART_TEXT_COLOR = Color.WHITE;
    private static final int CHART_GRID_COLOR = Color.parseColor("#444444");
    private static final int CHART_LINE_COLOR = Color.parseColor("#4CAF50");
    private static final int CHART_FILL_COLOR = Color.parseColor("#804CAF50");

    public static void setupRiskTrendChart(LineChart chart, List<PredictionHistory> history) {
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < history.size(); i++) {
            PredictionHistory pred = history.get(i);
            entries.add(new Entry(i, (float) pred.getRiskPercentage()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Diabetes Risk (%)");
        styleDataSet(dataSet, Color.parseColor("#F44336")); // Red for risk

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        setupChart(chart, "Diabetes Risk Trend Over Time", history);
        chart.getAxisLeft().setAxisMaximum(100f);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.invalidate();
    }

    public static void setupGlucoseTrendChart(LineChart chart, List<PredictionHistory> history) {
        List<Entry> entries = new ArrayList<>();
        float minGlucose = Float.MAX_VALUE;
        float maxGlucose = Float.MIN_VALUE;

        for (int i = 0; i < history.size(); i++) {
            PredictionHistory pred = history.get(i);
            float glucose = (float) pred.getGlucose();
            entries.add(new Entry(i, glucose));

            if (glucose < minGlucose) minGlucose = glucose;
            if (glucose > maxGlucose) maxGlucose = glucose;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Glucose (mg/dL)");
        styleDataSet(dataSet, Color.parseColor("#2196F3")); // Blue

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        setupChart(chart, "Glucose Level Trend", history);

        // Add padding to y-axis
        float padding = (maxGlucose - minGlucose) * 0.15f;
        chart.getAxisLeft().setAxisMaximum(Math.min(300f, maxGlucose + padding));
        chart.getAxisLeft().setAxisMinimum(Math.max(40f, minGlucose - padding));

        chart.invalidate();
    }

    public static void setupBMITrendChart(LineChart chart, List<PredictionHistory> history) {
        List<Entry> entries = new ArrayList<>();
        float minBmi = Float.MAX_VALUE;
        float maxBmi = Float.MIN_VALUE;

        for (int i = 0; i < history.size(); i++) {
            PredictionHistory pred = history.get(i);
            float bmi = (float) pred.getBmi();
            entries.add(new Entry(i, bmi));

            if (bmi < minBmi) minBmi = bmi;
            if (bmi > maxBmi) maxBmi = bmi;
        }

        LineDataSet dataSet = new LineDataSet(entries, "BMI (kg/m²)");
        styleDataSet(dataSet, Color.parseColor("#FF9800")); // Orange

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        setupChart(chart, "Body Mass Index (BMI) Trend", history);

        // Add padding to y-axis
        float padding = Math.max(2f, (maxBmi - minBmi) * 0.15f);
        chart.getAxisLeft().setAxisMaximum(Math.min(50f, maxBmi + padding));
        chart.getAxisLeft().setAxisMinimum(Math.max(10f, minBmi - padding));

        chart.invalidate();
    }

    public static void setupBloodPressureTrendChart(LineChart chart, List<PredictionHistory> history) {
        List<Entry> entries = new ArrayList<>();
        float minBp = Float.MAX_VALUE;
        float maxBp = Float.MIN_VALUE;

        for (int i = 0; i < history.size(); i++) {
            PredictionHistory pred = history.get(i);
            float bp = (float) pred.getBloodPressure();
            entries.add(new Entry(i, bp));

            if (bp < minBp) minBp = bp;
            if (bp > maxBp) maxBp = bp;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Blood Pressure (mmHg)");
        styleDataSet(dataSet, Color.parseColor("#9C27B0")); // Purple

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        setupChart(chart, "Blood Pressure Trend", history);

        // Add padding to y-axis
        float padding = Math.max(5f, (maxBp - minBp) * 0.15f);
        chart.getAxisLeft().setAxisMaximum(Math.min(200f, maxBp + padding));
        chart.getAxisLeft().setAxisMinimum(Math.max(40f, minBp - padding));

        chart.invalidate();
    }

    public static void setupInsulinTrendChart(LineChart chart, List<PredictionHistory> history) {
        List<Entry> entries = new ArrayList<>();
        float minInsulin = Float.MAX_VALUE;
        float maxInsulin = Float.MIN_VALUE;

        for (int i = 0; i < history.size(); i++) {
            PredictionHistory pred = history.get(i);
            float insulin = (float) pred.getInsulin();
            entries.add(new Entry(i, insulin));

            if (insulin < minInsulin) minInsulin = insulin;
            if (insulin > maxInsulin) maxInsulin = insulin;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Insulin (μU/ml)");
        styleDataSet(dataSet, Color.parseColor("#00BCD4")); // Cyan

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        setupChart(chart, "Insulin Level Trend", history);

        // Add padding to y-axis
        float padding = Math.max(5f, (maxInsulin - minInsulin) * 0.15f);
        chart.getAxisLeft().setAxisMaximum(maxInsulin + padding);
        chart.getAxisLeft().setAxisMinimum(Math.max(0f, minInsulin - padding));

        chart.invalidate();
    }

    private static void setupChart(LineChart chart, String description, List<PredictionHistory> history) {
        // Chart styling
        chart.getDescription().setText(description);
        chart.getDescription().setTextColor(CHART_TEXT_COLOR);
        chart.getDescription().setTextSize(12f);
        chart.setBackgroundColor(Color.parseColor("#1E1E1E"));
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawBorders(true);
        chart.setBorderColor(CHART_GRID_COLOR);
        chart.setBorderWidth(1f);

        // Legend styling
        chart.getLegend().setTextColor(CHART_TEXT_COLOR);
        chart.getLegend().setTextSize(12f);

        // X-axis styling
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(CHART_TEXT_COLOR);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(CHART_GRID_COLOR);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < history.size()) {
                    PredictionHistory pred = history.get(index);
                    if (pred.getPredictionDate() != null) {
                        Date date = pred.getPredictionDate().toDate();
                        return dateFormat.format(date);
                    }
                }
                return String.valueOf(index + 1);
            }
        });

        // Y-axis (left) styling
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(CHART_TEXT_COLOR);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(CHART_GRID_COLOR);
        leftAxis.setGranularityEnabled(true);

        // Y-axis (right) - disable
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private static void styleDataSet(LineDataSet dataSet, int color) {
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(CHART_TEXT_COLOR);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(80);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);
        dataSet.setDrawValues(false); // Don't show values on each point to avoid clutter
    }
}

