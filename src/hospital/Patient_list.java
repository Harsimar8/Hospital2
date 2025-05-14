package hospital;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


//nodeclass
class PNode {
    Patient patient;
    PNode next;

    public PNode(Patient patient) {
        this.patient = patient;
        this.next = null;
    }
}

//patient list class
public class Patient_list {
    PNode head, tail;

    public Patient_list() {
        head = null;
        tail = null;
    }

    // insert patient into list//
    public void insertPatient(Patient patient) {
        PNode node = new PNode(patient);

        if (head == null || head.patient.getId() > patient.getId()) {
            node.next = head;
            head = node;
            if (tail == null) tail = node;
            return;
        }

        PNode temp = head;
        while (temp.next != null && temp.next.patient.getId() < patient.getId()) {
            temp = temp.next;
        }

        node.next = temp.next;
        temp.next = node;
        if (node.next == null) tail = node;
    }

    //converting linked list into sorted arraylist
    private ArrayList<Patient> getSortedPatients() {
        ArrayList<Patient> patientList = new ArrayList<>();
        PNode temp = head;

        while (temp != null) {
            patientList.add(temp.patient);
            temp = temp.next;
        }


        int n = patientList.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (patientList.get(j).getId() > patientList.get(j + 1).getId()) {

                    Patient tempPatient = patientList.get(j);
                    patientList.set(j, patientList.get(j + 1));
                    patientList.set(j + 1, tempPatient);
                }
            }
        }

        return patientList;
    }


    //serach patient by id
    public Patient findPatientById(int patientId) {
        ArrayList<Patient> sortedPatients = getSortedPatients();

        if (sortedPatients.isEmpty()) {
            System.out.println("patient list is empty");
            return null;
        }


        int left = 0, right = sortedPatients.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            Patient midPatient = sortedPatients.get(mid);

            if (midPatient.getId() == patientId) {
                return midPatient;
            } else if (midPatient.getId() < patientId) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        System.out.println("Patient ID " + patientId + " not found.");
        return null;
    }


    //delete patient list
    public boolean deletePatient(int patientId, Connection con) {
        PNode current = head, prev = null;

        while (current != null) {
            if (current.patient.getId() == patientId) {
                if (prev == null) {
                    head = current.next;
                } else {
                    prev.next = current.next;
                }
                if (current == tail) tail = prev;

                System.out.println("patient ID " + patientId + " deleted from list.");
                break;
            }
            prev = current;
            current = current.next;
        }


        String deleteQuery = "DELETE FROM patient WHERE patient_id = ?";
        try (PreparedStatement statement = con.prepareStatement(deleteQuery)) {
            statement.setInt(1, patientId);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // load patient from db to linked lit
    public void loadPatientsFromDatabase(Connection con) {
        try {
            head = tail = null;
            String query = "SELECT * FROM patient";
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("patient_id");
                String name = rs.getString("name");
                String contact = rs.getString("contact");

                insertPatient(new Patient(id, name, contact));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error loading patients from database: " + e.getMessage());
        }
    }

    //add new patient in dbs
    public void addPatientToDB(Connection con, Patient patient) {
        String insertQuery = "INSERT INTO patient (patient_id, name, contact) VALUES (?, ?, ?)";
        try (PreparedStatement statement = con.prepareStatement(insertQuery)) {
            statement.setInt(1, patient.getId());
            statement.setString(2, patient.getName());
            statement.setString(3, patient.getContact());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                insertPatient(patient);
                System.out.println("patient added ");
            } else {
                System.out.println("failed to add patient.");
            }
        } catch (SQLException e) {
            System.err.println("Error adding patient: ");
        }
    }

    // displY ALL patients
    public void displayPatients() {
        PNode temp = head;
        System.out.println(" All Patients ");

        if (temp == null) {
            System.out.println("no patients found.");
            return;
        }

        while (temp != null) {
            System.out.println("ID: " + temp.patient.getId() +
                    ", Name: " + temp.patient.getName() +
                    ", Contact: " + temp.patient.getContact());
            temp = temp.next;
        }
    }
}
