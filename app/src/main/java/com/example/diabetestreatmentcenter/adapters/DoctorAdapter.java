package com.example.diabetestreatmentcenter.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diabetestreatmentcenter.R;
import com.example.diabetestreatmentcenter.models.Doctor;

import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private List<Doctor> doctorList = new ArrayList<>();
    private OnDoctorClickListener listener;

    public interface OnDoctorClickListener {
        void onBookAppointmentClick(Doctor doctor);
    }

    public DoctorAdapter(OnDoctorClickListener listener) {
        this.listener = listener;
    }

    public void setDoctors(List<Doctor> doctors) {
        this.doctorList = doctors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);
        holder.bind(doctor);
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    class DoctorViewHolder extends RecyclerView.ViewHolder {
        private TextView doctorNameText, specialtyText, clinicText, availableDaysText;
        private Button bookButton;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorNameText = itemView.findViewById(R.id.doctorNameText);
            specialtyText = itemView.findViewById(R.id.specialtyText);
            clinicText = itemView.findViewById(R.id.clinicText);
            availableDaysText = itemView.findViewById(R.id.availableDaysText);
            bookButton = itemView.findViewById(R.id.bookButton);
        }

        public void bind(Doctor doctor) {
            doctorNameText.setText("Dr. " + doctor.getName());
            specialtyText.setText("Specialty: " + (doctor.getSpecialty() != null ? doctor.getSpecialty() : "Not specified"));

            if (doctor.getClinicAddress() != null && !doctor.getClinicAddress().isEmpty()) {
                clinicText.setText("Clinic: " + doctor.getClinicAddress());
                clinicText.setVisibility(View.VISIBLE);
            } else {
                clinicText.setVisibility(View.GONE);
            }

            if (doctor.getAvailableDays() != null && !doctor.getAvailableDays().isEmpty()) {
                availableDaysText.setText("Available: " + doctor.getAvailableDays());
                availableDaysText.setVisibility(View.VISIBLE);
            } else {
                availableDaysText.setVisibility(View.GONE);
            }

            bookButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookAppointmentClick(doctor);
                }
            });
        }
    }
}

