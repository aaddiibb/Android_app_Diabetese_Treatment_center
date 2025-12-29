package com.example.diabetestreatmentcenter.utils;

public class PredictionResult {
    private double riskPercentage;
    private boolean isDiabetic;
    private String riskLevel;
    private String recommendation;
    private String detailsString;

    public PredictionResult(double riskPercentage, boolean isDiabetic, String riskLevel,
                           String recommendation, String detailsString) {
        this.riskPercentage = riskPercentage;
        this.isDiabetic = isDiabetic;
        this.riskLevel = riskLevel;
        this.recommendation = recommendation;
        this.detailsString = detailsString;
    }

    public double getRiskPercentage() {
        return riskPercentage;
    }

    public boolean isDiabetic() {
        return isDiabetic;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public String getDetailsString() {
        return detailsString;
    }
}

