package com.example.diabetestreatmentcenter.models;

public class Doctor {
    private String id;
    private String userId;
    private String name;
    private String email;
    private String specialty;
    private String clinicAddress;
    private String availableDays;

    public Doctor() {
        // Required empty constructor for Firebase
    }

    public Doctor(String id, String userId, String name, String email, String specialty, String clinicAddress, String availableDays) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.specialty = specialty;
        this.clinicAddress = clinicAddress;
        this.availableDays = availableDays;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getClinicAddress() { return clinicAddress; }
    public void setClinicAddress(String clinicAddress) { this.clinicAddress = clinicAddress; }

    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }
}

