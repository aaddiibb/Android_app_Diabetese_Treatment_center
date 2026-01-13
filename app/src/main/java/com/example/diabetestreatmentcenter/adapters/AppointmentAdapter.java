package com.example.diabetestreatmentcenter.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diabetestreatmentcenter.R;
import com.example.diabetestreatmentcenter.models.Appointment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointmentList = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
    private OnAppointmentCancelListener cancelListener;

    public interface OnAppointmentCancelListener {
        void onCancel(Appointment appointment);
    }

    public void setCancelListener(OnAppointmentCancelListener listener) {
        this.cancelListener = listener;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointmentList = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);
        holder.bind(appointment);
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private TextView doctorNameText, dateTimeText, statusText, noteText;
        private android.widget.Button cancelButton;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorNameText = itemView.findViewById(R.id.doctorNameText);
            dateTimeText = itemView.findViewById(R.id.dateTimeText);
            statusText = itemView.findViewById(R.id.statusText);
            noteText = itemView.findViewById(R.id.noteText);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }

        public void bind(Appointment appointment) {
            // Set doctor name
            if (appointment.getDoctorName() != null && !appointment.getDoctorName().isEmpty()) {
                doctorNameText.setText("Dr. " + appointment.getDoctorName());
            } else {
                doctorNameText.setText("Doctor");
            }

            // Set date and time
            if (appointment.getScheduledAt() != null) {
                Date date = appointment.getScheduledAt().toDate();
                dateTimeText.setText(dateFormat.format(date));
                dateTimeText.setVisibility(View.VISIBLE);
            } else {
                dateTimeText.setText("Date not set");
                dateTimeText.setVisibility(View.VISIBLE);
            }

            // Set status with color coding
            String status = appointment.getStatus() != null ? appointment.getStatus() : "PENDING";
            statusText.setText("Status: " + status);

            // Color code status
            switch (status.toUpperCase()) {
                case "SCHEDULED":
                    statusText.setTextColor(0xFF4CAF50); // Green
                    break;
                case "COMPLETED":
                    statusText.setTextColor(0xFF2196F3); // Blue
                    break;
                case "CANCELLED":
                    statusText.setTextColor(0xFFF44336); // Red
                    break;
                case "REQUESTED":
                default:
                    statusText.setTextColor(0xFFFFA726); // Orange
                    break;
            }

            // Set patient note
            if (appointment.getPatientNote() != null && !appointment.getPatientNote().isEmpty()) {
                noteText.setText("Note: " + appointment.getPatientNote());
                noteText.setVisibility(View.VISIBLE);
            } else {
                noteText.setVisibility(View.GONE);
            }

            // Show cancel button only for REQUESTED or SCHEDULED appointments
            String currentStatus = appointment.getStatus() != null ? appointment.getStatus().toUpperCase() : "PENDING";
            if (currentStatus.equals("CANCELLED") || currentStatus.equals("COMPLETED")) {
                cancelButton.setVisibility(View.GONE);
            } else {
                cancelButton.setVisibility(View.VISIBLE);
                cancelButton.setOnClickListener(v -> {
                    if (cancelListener != null) {
                        cancelListener.onCancel(appointment);
                    }
                });
            }
        }
    }
}

