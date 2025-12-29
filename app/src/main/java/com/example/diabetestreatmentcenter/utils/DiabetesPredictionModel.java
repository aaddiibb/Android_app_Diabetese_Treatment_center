package com.example.diabetestreatmentcenter.utils;

public class DiabetesPredictionModel {

    /**
     * Simple diabetes risk prediction model based on medical parameters
     * Returns a risk score between 0-100%
     */
    public PredictionResult predict(int pregnancies, double glucose, double bloodPressure,
                                   double skinThickness, double insulin, double bmi,
                                   double dpf, int age) {

        double riskScore = 0.0;

        // Glucose Level (most important factor) - 40% weight
        if (glucose >= 200) riskScore += 40;
        else if (glucose >= 140) riskScore += 30;
        else if (glucose >= 110) riskScore += 20;
        else if (glucose >= 100) riskScore += 10;

        // BMI - 20% weight
        if (bmi >= 35) riskScore += 20;
        else if (bmi >= 30) riskScore += 15;
        else if (bmi >= 25) riskScore += 10;
        else if (bmi >= 18.5) riskScore += 5;

        // Age - 15% weight
        if (age >= 65) riskScore += 15;
        else if (age >= 45) riskScore += 10;
        else if (age >= 35) riskScore += 5;

        // Diabetes Pedigree Function (family history) - 10% weight
        if (dpf >= 1.0) riskScore += 10;
        else if (dpf >= 0.5) riskScore += 7;
        else if (dpf >= 0.3) riskScore += 4;

        // Blood Pressure - 7% weight
        if (bloodPressure >= 140) riskScore += 7;
        else if (bloodPressure >= 120) riskScore += 5;
        else if (bloodPressure >= 90) riskScore += 3;

        // Insulin - 5% weight
        if (insulin >= 200) riskScore += 5;
        else if (insulin >= 100) riskScore += 3;

        // Pregnancies - 3% weight
        if (pregnancies >= 5) riskScore += 3;
        else if (pregnancies >= 3) riskScore += 2;

        // Cap at 100%
        riskScore = Math.min(riskScore, 100);

        // Determine if diabetic (threshold at 50%)
        boolean isDiabetic = riskScore >= 50;

        // Risk level
        String riskLevel;
        if (riskScore >= 70) riskLevel = "VERY HIGH";
        else if (riskScore >= 50) riskLevel = "HIGH";
        else if (riskScore >= 30) riskLevel = "MODERATE";
        else riskLevel = "LOW";

        // Recommendation
        String recommendation = getRecommendation(riskScore, glucose, bmi, bloodPressure);

        // Details string
        String details = String.format(
            "Pregnancies: %d\nGlucose: %.1f mg/dL\nBlood Pressure: %.1f mmHg\n" +
            "Skin Thickness: %.1f mm\nInsulin: %.1f μU/ml\nBMI: %.1f kg/m²\n" +
            "Diabetes Pedigree: %.3f\nAge: %d years",
            pregnancies, glucose, bloodPressure, skinThickness, insulin, bmi, dpf, age
        );

        return new PredictionResult(riskScore, isDiabetic, riskLevel, recommendation, details);
    }

    private String getRecommendation(double riskScore, double glucose, double bmi, double bloodPressure) {
        if (riskScore >= 70) {
            return "⚠️ URGENT: Your risk is very high. Please consult an endocrinologist immediately. " +
                   "Start monitoring blood glucose daily and make immediate lifestyle changes.";
        } else if (riskScore >= 50) {
            return "⚠️ HIGH RISK: Schedule an appointment with a doctor soon. Monitor your blood glucose, " +
                   "maintain a healthy diet, exercise regularly, and manage stress.";
        } else if (riskScore >= 30) {
            return "⚡ MODERATE RISK: Take preventive measures now. Adopt a balanced diet, exercise 30 minutes daily, " +
                   "monitor weight, and get regular check-ups.";
        } else {
            return "✅ LOW RISK: Continue maintaining a healthy lifestyle. Eat balanced meals, stay active, " +
                   "and get annual health screenings.";
        }
    }
}

