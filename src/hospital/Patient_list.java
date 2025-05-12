package hospital;

class PNode {
    Patient patient;
    PNode next, prev;

    public PNode(Patient patient) {
        this.patient = patient;
        this.next = null;
        this.prev = null;
    }
}

public class Patient_list {
    PNode head, tail;

    public Patient_list() {
        head = null;
        tail = null;
    }


    public void Insert(Patient patient) {
        PNode node = new PNode(patient);
        if (head == null) {
            head = node;
            tail = node; // First node becomes both head & tail
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node; // Move tail to the new node
        }
    }

    public Patient searchById(String Id) {
        PNode temp = tail;
        while (temp != null) {
            if (temp.patient.getId()== Integer.parseInt(Id)) {
                return temp.patient;
            }
            temp = temp.prev;
        }
        return null;
    }
    public Patient findPatientById(int patientId) {
        PNode temp = head;
        while (temp != null) {
            if (temp.patient.getPatientId() == patientId) {
                return temp.patient;
            }
            temp = temp.next;
        }
        System.out.println("❌ Patient ID " + patientId + " not found in list.");
        return null; // ✅ Allows caller to handle missing patient gracefully
    }







    public boolean delete(String patientId) {
        PNode current = head, prev = null;

        while (current != null) {
            if (current.patient.getId() == Integer.parseInt(patientId)) { // Match found
                if (prev == null) {
                    head = current.next;
                } else {
                    prev.next = current.next;
                }
                return true;
            }
            prev = current;
            current = current.next;
        }
        return false;
    }

    public int size() {
        PNode temp = head;
        int count = 0;
        while (temp != null) {
            count++;
            temp = temp.next;
        }
        return count;
    }

    public void PrintData() {
        PNode temp = head;
        int count = 0;
        while (temp != null) {
            count++;
            System.out.println(count + "  " + temp.patient.toString());
            temp = temp.next;
        }
    }


    public void displayPatients() {
        PNode temp = head;
        while (temp != null) {
            System.out.println("ID: " + temp.patient.getId() +
                    ", Name: " + temp.patient.getName() +
                    ", Contact: " + temp.patient.getContact());
            temp = temp.next;
        }
    }
}

