package  hospital;
import java.util.*;

public class Patient {

    private String  name, contact;
    private int id;
    private int priority;
    private int patientId;
    private List<Billing> billingHistory;


    public Patient() {
        this.id = 0;
        this.name = "";
        this.contact = "";
        this.priority = 1;
        this.billingHistory = new ArrayList<>();
    }


    public Patient(int id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.priority = 1;
        this.billingHistory = new ArrayList<>();
    }


    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    // Billing Methods
    public void addBill(Billing bill) {
        billingHistory.add(bill);
    }

    public List<Billing> getBillingHistory() {
        return billingHistory;
    }

    public List<Billing> getUnpaidBills() {
        List<Billing> unpaidBills = new ArrayList<>();
        for (Billing bill : billingHistory) {
            if (bill.getPaymentStatus().equals("Pending")) {
                unpaidBills.add(bill);
            }
        }
        return unpaidBills;
    }

    @Override
    public String toString() {
        return "{Patient: Id=" + id + ", Name=" + name + ", Contact=" + contact + "}";
    }
}

