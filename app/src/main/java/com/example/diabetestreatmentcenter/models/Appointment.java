package com.example.diabetestreatmentcenter.models;

import com.google.firebase.Timestamp;

public class Appointment {
    private String id;
    private String patientUserId;
    private String patientName;
    private String doctorUserId;
    private String doctorName;
    private Timestamp scheduledAt;
    private String status; // REQUESTED / SCHEDULED / COMPLETED / CANCELLED
    private String patientNote;
    private String doctorNote;

    public Appointment() {
        // Required empty constructor for Firebase
    }

    public Appointment(String id, String patientUserId, String patientName, String doctorUserId,
                      String doctorName, Timestamp scheduledAt, String status, String patientNote, String doctorNote) {
        this.id = id;
        this.patientUserId = patientUserId;
        this.patientName = patientName;
        this.doctorUserId = doctorUserId;
        this.doctorName = doctorName;
        this.scheduledAt = scheduledAt;
        this.status = status;
        this.patientNote = patientNote;
        this.doctorNote = doctorNote;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientUserId() { return patientUserId; }
    public void setPatientUserId(String patientUserId) { this.patientUserId = patientUserId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorUserId() { return doctorUserId; }
    public void setDoctorUserId(String doctorUserId) { this.doctorUserId = doctorUserId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public Timestamp getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Timestamp scheduledAt) { this.scheduledAt = scheduledAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPatientNote() { return patientNote; }
    public void setPatientNote(String patientNote) { this.patientNote = patientNote; }

    public String getDoctorNote() { return doctorNote; }
    public void setDoctorNote(String doctorNote) { this.doctorNote = doctorNote; }
}

