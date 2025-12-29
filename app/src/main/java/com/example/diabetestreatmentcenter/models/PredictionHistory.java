package com.example.diabetestreatmentcenter.models;

import com.google.firebase.Timestamp;

public class PredictionHistory {
    private String id;
    private String userId;
    private Timestamp predictionDate;
    private double riskPercentage;
    private String riskLevel; // LOW / HIGH
    private int pregnancies;
    private double glucose;
    private double bloodPressure;
    private double skinThickness;
    private double insulin;
    private double bmi;
    private double dpf;
    private int age;
    private String notes;

    public PredictionHistory() {
        // Required empty constructor for Firebase
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Timestamp getPredictionDate() { return predictionDate; }
    public void setPredictionDate(Timestamp predictionDate) { this.predictionDate = predictionDate; }

    public double getRiskPercentage() { return riskPercentage; }
    public void setRiskPercentage(double riskPercentage) { this.riskPercentage = riskPercentage; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public int getPregnancies() { return pregnancies; }
    public void setPregnancies(int pregnancies) { this.pregnancies = pregnancies; }

    public double getGlucose() { return glucose; }
    public void setGlucose(double glucose) { this.glucose = glucose; }

    public double getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(double bloodPressure) { this.bloodPressure = bloodPressure; }

    public double getSkinThickness() { return skinThickness; }
    public void setSkinThickness(double skinThickness) { this.skinThickness = skinThickness; }

    public double getInsulin() { return insulin; }
    public void setInsulin(double insulin) { this.insulin = insulin; }

    public double getBmi() { return bmi; }
    public void setBmi(double bmi) { this.bmi = bmi; }

    public double getDpf() { return dpf; }
    public void setDpf(double dpf) { this.dpf = dpf; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

