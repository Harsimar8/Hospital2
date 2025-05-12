package hospital;

import java.util.*;

public class Doctor {
    private String name, contact, specialty;

    private double consultationFee;

    private int id;// Ensure unique doctor ID tracking
    private List<Patient> assignedPatients;


    public Doctor() {
        this.id = 0;
        this.name = "";
        this.specialty = "";
        this.consultationFee = 0.0;
        this.assignedPatients = new ArrayList<>();
    }


    public Doctor(int id, String name, String specialty, double consultationFee) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.consultationFee = consultationFee;
        this.assignedPatients = new ArrayList<>();
    }


    public int getId() { return id; }
    public void setId(int  id) { this.id = id; }

    public int getDoctorId() { return id; }
    public void setDoctorId(int id) { this.id = id; }


    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public List<Patient> getPatients() { return assignedPatients; }

    public void addPatient(Patient patient) {
        assignedPatients.add(patient);
    }


    @Override
    public String toString() {
        return "{Doctor: ID=" + id + ", Name=" + name + ", Contact=" + contact +
                ", Specialty=" + specialty + ", Fees=" + consultationFee + "}";
    }
}

