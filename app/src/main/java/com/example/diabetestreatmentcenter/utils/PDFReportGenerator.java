package com.example.diabetestreatmentcenter.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;

import com.example.diabetestreatmentcenter.models.PredictionHistory;
import com.example.diabetestreatmentcenter.models.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PDFReportGenerator {

    private static final String TAG = "PDFReportGenerator";
    private static final int PAGE_WIDTH = 595; // A4 width in points
    private static final int PAGE_HEIGHT = 842; // A4 height in points
    private static final int MARGIN = 40;

    public static File generateHealthReport(Context context, User user, List<PredictionHistory> historyList) {
        if (historyList == null || historyList.isEmpty()) {
            Log.e(TAG, "No history data to generate report");
            return null;
        }

        // Create PDF document
        PdfDocument document = new PdfDocument();

        try {
            // Create first page
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            int yPosition = MARGIN;

            // Draw header
            yPosition = drawHeader(canvas, yPosition);

            // Draw patient information
            yPosition = drawPatientInfo(canvas, user, yPosition);

            // Draw summary statistics
            yPosition = drawSummaryStats(canvas, historyList, yPosition);

            // Draw latest prediction
            yPosition = drawLatestPrediction(canvas, historyList, yPosition);

            // Draw history table header
            yPosition = drawHistoryTableHeader(canvas, yPosition);

            // Draw history entries
            yPosition = drawHistoryEntries(canvas, historyList, yPosition, document);

            // Draw recommendations
            if (yPosition < PAGE_HEIGHT - 150) {
                drawRecommendations(canvas, historyList.get(0), yPosition);
            }

            document.finishPage(page);

            // Save the document
            return saveDocument(context, document);

        } catch (Exception e) {
            Log.e(TAG, "Error generating PDF", e);
            return null;
        } finally {
            document.close();
        }
    }

    private static int drawHeader(Canvas canvas, int yPosition) {
        Paint paint = new Paint();

        // Title background
        paint.setColor(Color.rgb(13, 115, 119)); // #0D7377
        canvas.drawRect(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition + 60, paint);

        // Title text
        paint.setColor(Color.WHITE);
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText("HEALTH METRICS REPORT", PAGE_WIDTH / 2f, yPosition + 40, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(12);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        canvas.drawText("Generated: " + sdf.format(new Date()), PAGE_WIDTH / 2f, yPosition + 55, paint);

        return yPosition + 80;
    }

    private static int drawPatientInfo(Canvas canvas, User user, int yPosition) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);
        paint.setFakeBoldText(true);

        canvas.drawText("Patient Information", MARGIN, yPosition, paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(12);

        yPosition += 25;
        if (user != null) {
            canvas.drawText("Name: " + (user.getName() != null ? user.getName() : "N/A"), MARGIN, yPosition, paint);
            yPosition += 20;
            canvas.drawText("Email: " + (user.getEmail() != null ? user.getEmail() : "N/A"), MARGIN, yPosition, paint);
            yPosition += 20;
            canvas.drawText("Phone: " + (user.getPhone() != null ? user.getPhone() : "N/A"), MARGIN, yPosition, paint);
        }

        return yPosition + 30;
    }

    private static int drawSummaryStats(Canvas canvas, List<PredictionHistory> historyList, int yPosition) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);
        paint.setFakeBoldText(true);

        canvas.drawText("Summary Statistics", MARGIN, yPosition, paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(12);

        yPosition += 25;

        // Calculate statistics
        int totalPredictions = historyList.size();
        double avgRisk = historyList.stream().mapToDouble(PredictionHistory::getRiskPercentage).average().orElse(0);
        double avgGlucose = historyList.stream().mapToDouble(PredictionHistory::getGlucose).average().orElse(0);
        double avgBMI = historyList.stream().mapToDouble(PredictionHistory::getBmi).average().orElse(0);

        canvas.drawText("Total Predictions: " + totalPredictions, MARGIN, yPosition, paint);
        yPosition += 20;
        canvas.drawText(String.format(Locale.getDefault(), "Average Risk: %.1f%%", avgRisk), MARGIN, yPosition, paint);
        yPosition += 20;
        canvas.drawText(String.format(Locale.getDefault(), "Average Glucose: %.1f mg/dL", avgGlucose), MARGIN, yPosition, paint);
        yPosition += 20;
        canvas.drawText(String.format(Locale.getDefault(), "Average BMI: %.1f", avgBMI), MARGIN, yPosition, paint);

        return yPosition + 30;
    }

    private static int drawLatestPrediction(Canvas canvas, List<PredictionHistory> historyList, int yPosition) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);
        paint.setFakeBoldText(true);

        canvas.drawText("Latest Prediction", MARGIN, yPosition, paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(12);

        yPosition += 25;

        PredictionHistory latest = historyList.get(0);

        // Risk level with color
        paint.setFakeBoldText(true);
        paint.setTextSize(16);
        if (latest.getRiskLevel() != null && latest.getRiskLevel().equalsIgnoreCase("HIGH")) {
            paint.setColor(Color.rgb(244, 67, 54)); // Red
        } else {
            paint.setColor(Color.rgb(76, 175, 80)); // Green
        }
        canvas.drawText(String.format(Locale.getDefault(), "Risk Level: %s (%.1f%%)",
            latest.getRiskLevel(), latest.getRiskPercentage()), MARGIN, yPosition, paint);

        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(false);
        paint.setTextSize(12);
        yPosition += 25;

        // Date
        if (latest.getPredictionDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            canvas.drawText("Date: " + sdf.format(latest.getPredictionDate().toDate()), MARGIN, yPosition, paint);
            yPosition += 20;
        }

        // Metrics in two columns
        int col1X = MARGIN;
        int col2X = PAGE_WIDTH / 2;

        canvas.drawText(String.format(Locale.getDefault(), "Glucose: %.1f mg/dL", latest.getGlucose()), col1X, yPosition, paint);
        canvas.drawText(String.format(Locale.getDefault(), "BMI: %.1f", latest.getBmi()), col2X, yPosition, paint);
        yPosition += 20;

        canvas.drawText(String.format(Locale.getDefault(), "Blood Pressure: %.1f mmHg", latest.getBloodPressure()), col1X, yPosition, paint);
        canvas.drawText(String.format(Locale.getDefault(), "Insulin: %.1f μU/mL", latest.getInsulin()), col2X, yPosition, paint);
        yPosition += 20;

        canvas.drawText(String.format(Locale.getDefault(), "Age: %d years", latest.getAge()), col1X, yPosition, paint);
        canvas.drawText(String.format(Locale.getDefault(), "Pregnancies: %d", latest.getPregnancies()), col2X, yPosition, paint);

        return yPosition + 30;
    }

    private static int drawHistoryTableHeader(Canvas canvas, int yPosition) {
        Paint paint = new Paint();
        paint.setColor(Color.rgb(13, 115, 119));
        canvas.drawRect(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition + 25, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(10);
        paint.setFakeBoldText(true);

        canvas.drawText("Date", MARGIN + 5, yPosition + 18, paint);
        canvas.drawText("Risk", MARGIN + 100, yPosition + 18, paint);
        canvas.drawText("Glucose", MARGIN + 170, yPosition + 18, paint);
        canvas.drawText("BP", MARGIN + 250, yPosition + 18, paint);
        canvas.drawText("BMI", MARGIN + 310, yPosition + 18, paint);
        canvas.drawText("Insulin", MARGIN + 370, yPosition + 18, paint);
        canvas.drawText("Age", MARGIN + 450, yPosition + 18, paint);

        return yPosition + 25;
    }

    @SuppressWarnings("unused")
    private static int drawHistoryEntries(Canvas canvas, List<PredictionHistory> historyList,
                                          int yPosition, PdfDocument document) {
        Paint paint = new Paint();
        paint.setTextSize(9);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());

        int maxEntries = Math.min(15, historyList.size());

        for (int i = 0; i < maxEntries; i++) {
            PredictionHistory history = historyList.get(i);

            // Alternate row colors
            if (i % 2 == 0) {
                paint.setColor(Color.rgb(245, 245, 245));
                canvas.drawRect(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition + 20, paint);
            }

            paint.setColor(Color.BLACK);

            String date = history.getPredictionDate() != null ?
                sdf.format(history.getPredictionDate().toDate()) : "N/A";
            canvas.drawText(date, MARGIN + 5, yPosition + 15, paint);

            canvas.drawText(String.format(Locale.getDefault(), "%.1f%%", history.getRiskPercentage()),
                MARGIN + 100, yPosition + 15, paint);
            canvas.drawText(String.format(Locale.getDefault(), "%.0f", history.getGlucose()),
                MARGIN + 170, yPosition + 15, paint);
            canvas.drawText(String.format(Locale.getDefault(), "%.0f", history.getBloodPressure()),
                MARGIN + 250, yPosition + 15, paint);
            canvas.drawText(String.format(Locale.getDefault(), "%.1f", history.getBmi()),
                MARGIN + 310, yPosition + 15, paint);
            canvas.drawText(String.format(Locale.getDefault(), "%.0f", history.getInsulin()),
                MARGIN + 370, yPosition + 15, paint);
            canvas.drawText(String.valueOf(history.getAge()),
                MARGIN + 450, yPosition + 15, paint);

            yPosition += 20;
        }

        if (historyList.size() > maxEntries) {
            paint.setTextSize(10);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(String.format(Locale.getDefault(),
                "... and %d more entries", historyList.size() - maxEntries),
                MARGIN, yPosition + 15, paint);
            yPosition += 25;
        }

        return yPosition + 10;
    }

    private static void drawRecommendations(Canvas canvas, PredictionHistory latest, int yPosition) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);
        paint.setFakeBoldText(true);

        canvas.drawText("Recommendations", MARGIN, yPosition, paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(11);

        yPosition += 25;

        if (latest.getRiskLevel() != null && latest.getRiskLevel().equalsIgnoreCase("HIGH")) {
            canvas.drawText("• Consult with your healthcare provider immediately", MARGIN + 10, yPosition, paint);
            yPosition += 18;
            canvas.drawText("• Monitor blood glucose levels regularly", MARGIN + 10, yPosition, paint);
            yPosition += 18;
            canvas.drawText("• Follow a balanced diet and exercise routine", MARGIN + 10, yPosition, paint);
            yPosition += 18;
            canvas.drawText("• Take prescribed medications as directed", MARGIN + 10, yPosition, paint);
        } else {
            canvas.drawText("• Maintain healthy lifestyle habits", MARGIN + 10, yPosition, paint);
            yPosition += 18;
            canvas.drawText("• Continue regular health check-ups", MARGIN + 10, yPosition, paint);
            yPosition += 18;
            canvas.drawText("• Stay physically active", MARGIN + 10, yPosition, paint);
            yPosition += 18;
            canvas.drawText("• Monitor your health metrics periodically", MARGIN + 10, yPosition, paint);
        }
    }

    private static File saveDocument(Context context, PdfDocument document) throws IOException {
        // Create directory for health reports
        File reportsDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "HealthReports");
        if (!reportsDir.exists()) {
            boolean created = reportsDir.mkdirs();
            if (!created) {
                Log.w(TAG, "Failed to create reports directory");
            }
        }

        // Create file with timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String fileName = "Health_Report_" + sdf.format(new Date()) + ".pdf";
        File file = new File(reportsDir, fileName);

        // Write document to file
        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
        }

        Log.d(TAG, "PDF saved to: " + file.getAbsolutePath());
        return file;
    }
}

