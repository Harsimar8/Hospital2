package  hospital;
import java.util.*;

public class Patient {
 // intializing all variables
    private String  name, contact;
    private int id;
    private int priority;
    private int patientId;
    private List<Billing> billingHistory;


    // default constructor
    public Patient() {
        this.id = 0;
        this.name = "";
        this.contact = "";
        this.priority = 1;
        this.billingHistory = new ArrayList<>();
    }


    // Parameterised constructor
    public Patient(int id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.priority = 1;
        this.billingHistory = new ArrayList<>();
    }

      // GETTERS AND SETTERS
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


    public List<Billing> getBillingHistory() {
        return billingHistory;
    }

    @Override
    public String toString() {
        return "{Patient: Id=" + id + ", Name=" + name + ", Contact=" + contact + "}";
    }
}

