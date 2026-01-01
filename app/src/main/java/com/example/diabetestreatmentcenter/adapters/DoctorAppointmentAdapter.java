package com.example.diabetestreatmentcenter.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class DoctorAppointmentAdapter extends RecyclerView.Adapter<DoctorAppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointmentList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
    private OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onApprove(Appointment appointment);
        void onDecline(Appointment appointment);
    }

    public DoctorAppointmentAdapter(OnAppointmentActionListener listener) {
        this.listener = listener;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointmentList = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor_appointment, parent, false);
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
        private final TextView patientNameText;
        private final TextView dateTimeText;
        private final TextView statusText;
        private final TextView noteText;
        private final Button approveButton;
        private final Button declineButton;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            patientNameText = itemView.findViewById(R.id.patientNameText);
            dateTimeText = itemView.findViewById(R.id.dateTimeText);
            statusText = itemView.findViewById(R.id.statusText);
            noteText = itemView.findViewById(R.id.noteText);
            approveButton = itemView.findViewById(R.id.approveButton);
            declineButton = itemView.findViewById(R.id.declineButton);
        }

        public void bind(Appointment appointment) {
            // Set patient name
            if (appointment.getPatientName() != null && !appointment.getPatientName().isEmpty()) {
                patientNameText.setText("Patient: " + appointment.getPatientName());
            } else {
                patientNameText.setText("Patient");
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
                    approveButton.setVisibility(View.GONE);
                    declineButton.setVisibility(View.GONE);
                    break;
                case "COMPLETED":
                    statusText.setTextColor(0xFF2196F3); // Blue
                    approveButton.setVisibility(View.GONE);
                    declineButton.setVisibility(View.GONE);
                    break;
                case "CANCELLED":
                    statusText.setTextColor(0xFFF44336); // Red
                    approveButton.setVisibility(View.GONE);
                    declineButton.setVisibility(View.GONE);
                    break;
                case "REQUESTED":
                default:
                    statusText.setTextColor(0xFFFFA726); // Orange
                    approveButton.setVisibility(View.VISIBLE);
                    declineButton.setVisibility(View.VISIBLE);
                    break;
            }

            // Set patient note
            if (appointment.getPatientNote() != null && !appointment.getPatientNote().isEmpty()) {
                noteText.setText("Note: " + appointment.getPatientNote());
                noteText.setVisibility(View.VISIBLE);
            } else {
                noteText.setVisibility(View.GONE);
            }

            // Set button listeners
            approveButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApprove(appointment);
                }
            });

            declineButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDecline(appointment);
                }
            });
        }
    }
}

