package hospital;


class DNode {
    Doctor doctor;
    DNode next, prev;

    public DNode(Doctor doctor) {
        this.doctor = doctor;
        this.next = null;
        this.prev = null;
    }
}

public class Doctors_list {
    DNode head, tail;

    public Doctors_list(){
        head = null;
        tail = null;
    }

    public void Insert(Doctor doctor) {
        DNode node = new DNode(doctor);

        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
    }


    public double getDoctorFeeById(String doctorId) {
        Doctor doctor = searchById(doctorId);
        return (doctor != null) ? doctor.getConsultationFee() : 0;
    }



    public Doctor searchById(String Id) {
        DNode temp = head;
        while (temp != null) {
            if (temp.doctor.getId()== Integer.parseInt(Id)) {
                return temp.doctor;
            }
            temp = temp.next;
        }
        return null;
    }



    public Doctor searchBySpeciality(String speciality) {
        DNode temp = tail;
        while (temp != null) {
            if (temp.doctor.getSpecialty().trim().equalsIgnoreCase(speciality.trim())) {
                return temp.doctor;
            }
            temp = temp.prev;
        }
        return null;
    }
    public Doctor findDoctorById(int doctorId) {
        DNode temp = head;
        while (temp != null) {
            if (temp.doctor.getDoctorId() == doctorId) {
                return temp.doctor;
            }
            temp = temp.next;
        }
        return null;
    }


    public Doctor getDoctorById(String doctorId) {
        DNode temp = head;
        while (temp != null) {
            if (temp.doctor.getId() == Integer.parseInt(doctorId)) {
                return temp.doctor;
            }
            temp = temp.next;
        }
        return null;
    }

    public boolean delete(String id) {
        DNode temp = tail;

        while (temp != null) {
            if (temp.doctor.getId() == Integer.parseInt(id)) {

                if (temp == head && temp == tail) {
                    head = null;
                    tail = null;
                }

                else if (temp == head) {
                    head = temp.next;
                    if (head != null) {
                        head.prev = null;
                    }
                }

                else if (temp == tail) {
                    tail = temp.prev;
                    if (tail != null) {
                        tail.next = null;
                    }
                }

                else {
                    temp.prev.next = temp.next;
                    temp.next.prev = temp.prev;
                }

                return true;
            }
            temp = temp.prev;
        }

        return false;
    }


    public void allDoctor() {
        DNode temp = head;
        System.out.println("--All Doctors--");
        while (temp != null) {
            System.out.println("Doctor ID: " + temp.doctor.getId() + ", Specialty: " + temp.doctor.getSpecialty());
            temp = temp.next;
        }
    }
}

