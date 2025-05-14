package hospital;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//billingmanager class
public class BillingManager {
    private List<Billing> bills;

    //contructor intializes
    public BillingManager() {
        this.bills = new ArrayList<>();
    }

    // add bill function
    public void addBill(Connection con, int patientId, int doctorId, String disease, String medication) {
        // Verify patient exists
        try (PreparedStatement stmt = con.prepareStatement("SELECT name FROM patient WHERE patient_id = ?")) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                System.out.println("patient ID " + patientId + " not found in db");
                return;
            }
        } catch (SQLException e) {
            System.err.println("error fetching patient data: ");
            return;
        }

        // fetch doctor's fee
        double consultationFee = 0.0;
        try (PreparedStatement stmt = con.prepareStatement("SELECT consultation_fee FROM doctor WHERE doctor_id = ?")) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                consultationFee = rs.getDouble("consultation_fee");
            } else {
                System.out.println("doctor ID " + doctorId + " not found in db");
                return;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor data: " + e.getMessage());
            return;
        }

        // Insert bill
        String insertQuery = "INSERT INTO bill (patient_id, doctor_id, disease, medication, consultation_fee) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(insertQuery)) {
            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, disease);
            stmt.setString(4, medication);
            stmt.setDouble(5, consultationFee);
            stmt.executeUpdate();
            System.out.println("bill successfully added for " + patientId);
        } catch (SQLException e) {
            System.err.println("Error");
        }
    }

    // display bill
    public void generateBill(Connection con, int billId) {
        String query = "SELECT b.bill_id, p.name AS patient_name, d.name AS doctor_name, b.disease, b.medication, b.consultation_fee, b.bill_date " +
                "FROM bill b " +
                "INNER JOIN patient p ON b.patient_id = p.patient_id " +
                "INNER JOIN doctor d ON b.doctor_id = d.doctor_id " +
                "WHERE b.bill_id = ?";

        try (PreparedStatement statement = con.prepareStatement(query)) {
            statement.setInt(1, billId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                System.out.println("|-------------------------------------------------|");
                System.out.println("|               HOSPITAL BILL RECEIPT            ");
                System.out.println("|-------------------------------------------------|");
                System.out.printf("| Bill ID          : %-26d \n", rs.getInt("bill_id"));
                System.out.println("|-------------------------------------------------|");
                System.out.printf("| Patient         : %-26s \n", rs.getString("patient_name"));
                System.out.printf("| Doctor          : Dr. %-23s \n", rs.getString("doctor_name"));
                System.out.println("|-------------------------------------------------|");
                System.out.printf("| Disease         : %-26s \n", rs.getString("disease"));
                System.out.println("|-------------------------------------------------|");
                System.out.printf("| Medication      : %-26s \n", rs.getString("medication"));
                System.out.println("|-------------------------------------------------|");
                System.out.printf("| Consultation Fee: ₹%-25.2f \n", rs.getDouble("consultation_fee"));
                System.out.println("|-------------------------------------------------|");
                System.out.printf("| Billing Date    : %-26s \n", rs.getTimestamp("bill_date"));
                System.out.println("+-------------------------------------------------|\n");
            } else {
                System.out.println("no bill found with ID: " + billId);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving bill: ");
        }
    }

}
