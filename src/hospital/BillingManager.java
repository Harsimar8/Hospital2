package hospital;

import java.util.ArrayList;
import java.util.List;

public class BillingManager {
    private List<Billing> bills;

    public BillingManager() {
        this.bills = new ArrayList<>();
    }


    public void generateBill(Patient patient, Doctor doctor, String disease, String medication, double amount) {
        if (doctor == null || patient == null) {
            System.out.println("Invalid Patient or Doctor ID.");
            return;
        }




        for (Billing existingBill : bills) {
            if (existingBill.getPatient().getPatientId() == patient.getPatientId() &&
                    existingBill.getDoctor().getDoctorId() == doctor.getDoctorId() &&
                    existingBill.getPaymentStatus().equals("Pending")) {

                System.out.println("A pending bill already exists for this patient-doctor combination.");
                return;
            }
        }


        Billing bill = new Billing(patient, doctor, disease, medication, amount, "Pending");
        bills.add(bill);
        patient.addBill(bill);
        System.out.println("Bill generated successfully for " + patient.getName());
    }


    public void processPayment(int billId) {
        for (Billing bill : bills) {
            if (bill.getBillId() == billId) {
                if (bill.getPaymentStatus().equals("Paid")) {
                    System.out.println("Payment already processed for Bill ID " + billId);
                    return;
                }
                bill.setPaymentStatus("Paid");
                System.out.println("Payment processed successfully for Bill ID " + billId);
                return;
            }
        }
        System.out.println("Bill not found!");
    }


    public void showBillingHistory(Patient patient) {
        if (patient == null) {
            System.out.println("Invalid Patient ID.");
            return;
        }

        System.out.println("---- Billing History for " + patient.getName() + " ----");
        boolean hasBills = false;
        for (Billing bill : bills) {
            if (bill.getPatient().getPatientId() == patient.getPatientId()) {
                bill.displayBill();
                hasBills = true;
            }
        }
        if (!hasBills) {
            System.out.println("No billing records found for this patient.");
        }
    }

    public List<Billing> getAllBills() {
        return new ArrayList<>(bills);
    }
}
