package com.example.diabetestreatmentcenter.models;

public class User {
    private String id;
    private String role; // PATIENT / DOCTOR
    private String name;
    private String email;
    private String phone;

    // Required empty constructor for Firebase
    public User() {
    }

    public User(String id, String role, String name, String email, String phone) {
        this.id = id;
        this.role = role;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
