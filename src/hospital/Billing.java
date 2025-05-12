package hospital;

// intializing all variables
public class Billing {
    private static int billCounter = 0;
    private int billId;
    private Patient patient;
    private Doctor doctor;
    private String disease;
    private String medication;
    private double amount;
    private String paymentStatus;


    // parameterised constructor
    public Billing(Patient patient, Doctor doctor, String disease, String medication, double amount, String paymentStatus) {
        this.billId = ++billCounter;
        this.patient = patient;
        this.doctor = doctor;
        this.disease = disease;
        this.medication = medication;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
    }

    // Getters
    public int getBillId() { return billId; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public String getDisease() { return disease; }
    public String getMedication() { return medication; }
    public double getAmount() { return amount; }
    public String getPaymentStatus() { return paymentStatus; }


    public void setPaymentStatus(String status) { this.paymentStatus = status; }

    public void displayBill() {
        System.out.println("------Generated Bill------");
        System.out.println("Bill ID: " + billId);
        System.out.println("Patient: " + patient.getName());
        System.out.println("Doctor: " + doctor.getName());
        System.out.println("Disease: " + disease);
        System.out.println("Medication: " + medication);
        System.out.println("Amount: $" + amount);
        System.out.println("Payment Status: " + paymentStatus);
    }
}

