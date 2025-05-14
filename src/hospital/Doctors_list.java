package hospital;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;



//Node class
class DNode {
    Doctor doctor;
    DNode next;

    public DNode(Doctor doctor) {
        this.doctor = doctor;
        this.next = null;
    }
}

// doctor class
public class Doctors_list {
    DNode head, tail;

    public Doctors_list() {
        head = null;
        tail = null;
    }

    // insert to list//
    public void insertDoctor(Doctor doctor) {
        DNode node = new DNode(doctor);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
    }



    // converting data to sorted arraylist//
    private ArrayList<Doctor> getSortedDoctors() {
        ArrayList<Doctor> doctorList = new ArrayList<>();
        DNode temp = head;

        while (temp != null) {
            doctorList.add(temp.doctor);
            temp = temp.next;
        }

        int n = doctorList.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (doctorList.get(j).getId() > doctorList.get(j + 1).getId()) {

                    Doctor tempDoctor = doctorList.get(j);
                    doctorList.set(j, doctorList.get(j + 1));
                    doctorList.set(j + 1, tempDoctor);
                }
            }
        }

        return doctorList;
    }

    // search for doctor
    public Doctor findDoctorById(int doctorId) {
        ArrayList<Doctor> sortedDoctors = getSortedDoctors();
        int left = 0, right = sortedDoctors.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            Doctor midDoctor = sortedDoctors.get(mid);

            if (midDoctor.getId() == doctorId) {
                return midDoctor;
            } else if (midDoctor.getId() < doctorId) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return null;
    }

    // assign a patient to a doctor
    public void assignPatientToDoctor(Connection con, Patient_list plist, int doctorId, int patientId, int priority) {
        Doctor doctor = findDoctorById(doctorId);
        if (doctor == null) {
            System.out.println("doctor not found.");
            return;
        }

        Patient patient = plist.findPatientById(patientId);
        if (patient == null) {
            System.out.println("invalid patient ID.");
            return;
        }

        patient.setPriority(priority);
        doctor.addPatient(patient);

        String insertQuery = "INSERT INTO doctor_patient (doctor_id, patient_id, priority) VALUES (?, ?, ?)";
        try (PreparedStatement statement = con.prepareStatement(insertQuery)) {
            statement.setInt(1, doctorId);
            statement.setInt(2, patientId);
            statement.setInt(3, priority);
            statement.executeUpdate();
            System.out.println("patient ID " + patientId + "assigned to Dr." + doctor.getName() + "with priority " + priority);
        } catch (SQLException e) {

        }
    }

    //display assigned patient sorted by priority
    public void viewAssignedPatients(Connection con, int doctorId) {
        String query = "SELECT p.patient_id, p.name, p.contact, dp.priority " +
                "FROM patient p " +
                "INNER JOIN doctor_patient dp ON p.patient_id = dp.patient_id " +
                "WHERE dp.doctor_id = ? " +
                "ORDER BY dp.priority DESC";

        try (PreparedStatement statement = con.prepareStatement(query)) {
            statement.setInt(1, doctorId);
            ResultSet rs = statement.executeQuery();

            boolean foundPatients = false;

            while (rs.next()) {
                System.out.println("Name: " + rs.getString("name") +
                        ", Contact: " + rs.getString("contact") +
                        ", Priority: " + rs.getInt("priority"));
                foundPatients = true;
            }

            if (!foundPatients) {
                System.out.println("no patients assigned to this doctor.");
            }
        } catch (SQLException e) {
            System.err.println("error retrieving assigned patients");
        }
    }
    // then binary search for find doctor by their speciality
    public Doctor searchBySpeciality(String speciality) {
        ArrayList<Doctor> sortedDoctors = getSortedDoctors();
        int left = 0, right = sortedDoctors.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            Doctor midDoctor = sortedDoctors.get(mid);

            int comparison = midDoctor.getSpecialty().trim().compareToIgnoreCase(speciality.trim());
            if (comparison == 0) {
                return midDoctor;
            } else if (comparison < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return null;
    }

    // delete doctor
    public boolean deleteDoctor(int doctorId, Connection con) {
        DNode current = head, prev = null;

        while (current != null) {
            if (current.doctor.getId() == doctorId) {
                if (prev == null) {
                    head = current.next;
                } else {
                    prev.next = current.next;
                }
                if (current == tail) {
                    tail = prev;
                }
                System.out.println("doctor deleted from list.");
                break;
            }
            prev = current;
            current = current.next;
        }

        // deleting from database
        String deleteQuery = "DELETE FROM doctor WHERE doctor_id = ?";
        try (PreparedStatement statement = con.prepareStatement(deleteQuery)) {
            statement.setInt(1, doctorId);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting doctor from database: " + e.getMessage());
            return false;
        }
    }

    public void addDoctor(Doctor doctor) {
        DNode node = new DNode(doctor);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
    }

    // load doctors  data from database //
    public void loadDoctorsFromDatabase(Connection con) {
        try {
            String query = "SELECT * FROM doctor";
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("doctor_id");
                String name = rs.getString("name");
                String specialty = rs.getString("specialty");
                double fee = rs.getDouble("consultation_fee");

                addDoctor(new Doctor(id, name, specialty, fee));
            }
        } catch (SQLException e) {
            System.err.println("Error loading doctors from database: ");
        }
    }

    // store new doctor in database
    public void addDoctorToDB(Connection con, Doctor doctor) {
        String insertQuery = "INSERT INTO doctor (doctor_id, name, specialty, consultation_fee) VALUES (?, ?, ?,  ?)";
        try (PreparedStatement statement = con.prepareStatement(insertQuery)) {
            statement.setInt(1, doctor.getId());
            statement.setString(2, doctor.getName());
            statement.setString(3, doctor.getSpecialty());

            statement.setDouble(4, doctor.getConsultationFee());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                addDoctor(doctor);
                System.out.println("doctor added ");
            } else {
                System.out.println("failed to add doctor.");
            }
        } catch (SQLException e) {
            System.err.println("Error adding doctor: " );
        }
    }

    public void displayDoctors() {
        DNode temp = head;
        System.out.println("-- All Doctors --");

        if (temp == null) {
            System.out.println("No doctors found.");
            return;
        }

        while (temp != null) {
            System.out.println("ID: " + temp.doctor.getId() +
                    ", Name: " + temp.doctor.getName() +
                    ", Specialty: " + temp.doctor.getSpecialty() +
                    ", Fees: " + temp.doctor.getConsultationFee());
            temp = temp.next;
        }

}
}
