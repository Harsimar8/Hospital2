package hospital;

import java.sql.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;



public class Main {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hospital", "root", "harsimar");


            Patient_list plist = new Patient_list();
            Doctors_list dlist = new Doctors_list();
            Checkup_list clist = new Checkup_list();
            Scanner scanner = new Scanner(System.in);


            BillingManager billingManager = new BillingManager();

            loadDoctorsFromDatabase(con, dlist);
            loadPatientsFromDatabase(con, plist);

            while (true) {
                System.out.println("----Menu-----");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.println("Enter your choice:");


                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        registerUser(con, scanner);
                        break;
                    case 2:
                        loginUser(con, scanner, plist, dlist, clist,billingManager);
                        break;
                    case 3:
                        System.out.println("Exiting..");
                        return;
                    default:
                        System.out.println("Invalid choice");
                }
            }
        } catch (Exception exception) {
            System.out.println(exception);
        }
    }


    public static boolean adminExists(Connection con) {
        try {
            PreparedStatement stmt = con.prepareStatement("SELECT COUNT(*) FROM users WHERE role = 'Admin'");
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // Admin already exists
            }
        } catch (Exception e) {
            System.out.println("Error checking admin existence: " + e);
        }
        return false;
    }

    // Email validation method
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";


        return email.matches(emailRegex);
    }

    // Password validation method
    public static boolean isValidPassword(String password) {
        return password.length() >= 8 && password.length() <= 20; // Ensures between 8 and 20 characters
    }


    public static void registerUser(Connection con, Scanner scanner) {
        try {
            System.out.println("-Registration--");
            System.out.println("Enter name, email, password, and role (Admin/Doctor/Nurse/Patient):");

            String name = scanner.nextLine().trim();
            String email = scanner.nextLine().trim();
            String pass = scanner.nextLine().trim();
            String role = scanner.nextLine().trim();

            // Ensure email format is correct
            if (!isValidEmail(email)) {
                System.out.println("Invalid email format");
                return;
            }


            if (!isValidPassword(pass)) {
                System.out.println("Password should be at least 8 characters");
                return;
            }


            if (role.equalsIgnoreCase("Admin") && adminExists(con)) {
                System.out.println("Admin account already exists");
                return;
            }

            if (!role.matches("Admin|Doctor|Nurse|Patient")) {
                System.out.println("Invalid role! Please enter a valid role.");
                return;
            }

            PreparedStatement statement =
                    con.prepareStatement("INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)");

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, pass);
            statement.setString(4, role);

            int row = statement.executeUpdate();
            if (row == 1) {
                System.out.println(role + " registration successful!");
            }
        } catch (Exception e) {

        }
    }


    public static void showmenu(Scanner scanner, String userRole, Patient_list plist, Doctors_list dlist, Checkup_list clist, BillingManager billingManager,Connection con) {
        while (true) {
            if (userRole.equals("Doctor")) {
                showDoctorRestrictedMenu(con ,scanner,dlist); //  doctors restricted menu
                return;
            }

            System.out.println("Main Menu");

            if (userRole.equals("Admin") || userRole.equals("Nurse")) {
                System.out.println("1 Patients");
            }

            if (userRole.equals("Admin")) {
                System.out.println("2 Doctors");
                System.out.println("3 Billing & Accounting");
            }

            System.out.println("4. Logout");
            System.out.println("Enter your choice:");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    if (userRole.equals("Doctor")) {
                        showDoctorRestrictedMenu(con ,scanner,dlist);
                    }
                    else if(userRole.equals("Admin") || userRole.equals("Nurse")) {
                        showPatientMenu(scanner, userRole, plist, dlist,con);
                    } else {
                        System.out.println("Access Denied");
                    }
                    break;

                case 2:
                    if (userRole.equals("Admin") ) {
                        showDoctorMenu(scanner, userRole, dlist, plist,con);
                    } else {
                        System.out.println("Access Denied!");
                    }
                    break;

                case 3:
                    if (userRole.equals("Admin")) {
                        showBillingMenu(scanner, userRole, billingManager, plist, dlist);
                    } else {
                        System.out.println("Access Denied");
                    }
                    break;

                case 4:
                    System.out.println("Logging Out...");
                    return;

                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }


    public static void loginUser(Connection con, Scanner sc, Patient_list plist, Doctors_list dlist, Checkup_list clist ,BillingManager billingManager) {
        try {
            System.out.println("Login");
            System.out.println("Enter email and password:");
            String email = sc.next().trim();
            String pass = sc.next().trim();


            if (!isValidEmail(email)) {
                System.out.println("Invalid email format");
                return;
            }

            PreparedStatement statement = con.prepareStatement("SELECT role FROM users WHERE email = ? AND password = ?");
            statement.setString(1, email);
            statement.setString(2, pass);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String role = resultSet.getString("role");
                System.out.println("Welcome, " + role + "!");

                if (role.equals("Admin")) {
                    showmenu(sc, role, plist, dlist, clist ,billingManager,con);
                } else if (role.equals("Doctor")) {
                    showDoctorRestrictedMenu(con ,sc,dlist);
                } else if (role.equals("Nurse") || role.equals("Patient")) {
                    showPatientMenu(sc, role, plist, dlist,con);
                } else {
                    System.out.println("Access Denied.");
                }
            } else {
                System.out.println("Wrong email or password.");
            }
        } catch (Exception ex) {
            System.out.println("Login Error: " + ex);
        }
    }


    public static void loadDoctorsFromDatabase(Connection con, Doctors_list dlist) {
        try {
            String query = "SELECT * FROM doctor";
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                int doctorId = rs.getInt("doctor_id");
                String name = rs.getString("name");
                String specialty = rs.getString("specialty");
                double consultationFee = rs.getDouble("consultation_fee");

                Doctor doctor = new Doctor(doctorId, name, specialty, consultationFee);
                dlist.Insert(doctor);
            }

        } catch (SQLException e) {

        }
    }

    public static void loadPatientsFromDatabase(Connection con, Patient_list plist) {
        try {
            String query = "SELECT * FROM patient";
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("patient_id");
                String name = rs.getString("name");
                String contact = rs.getString("contact");

                Patient patient = new Patient(id, name, contact);
                plist.Insert(patient); // ✅ Add patient to linked list
            }

        } catch (SQLException e) {
            System.out.println("Error loading patients: " + e.getMessage());
        }
    }

    public static void showAllPatients(Patient_list plist) {
        PNode temp = plist.head;
        System.out.println("- All Patients -");

        if (temp == null) {
            System.out.println("No patients found.");
            return;
        }

        StringBuilder result = new StringBuilder();

        while (temp != null) {
            result.append("ID: ").append(temp.patient.getId())
                    .append(", Name: ").append(temp.patient.getName())
                    .append(", Contact: ").append(temp.patient.getContact())
                    .append("\n"); // Append a newline for formatting
            temp = temp.next;
        }

        System.out.println(result.toString());
    }


    public static void showAllDoctors(Scanner scanner, Doctors_list dlist) {
        DNode temp = dlist.tail;
        System.out.println("-All Doctors-");

        if (temp == null) {
            System.out.println("No doctor found.");
            return;
        }

        while (temp != null) {
            System.out.println("ID: " + temp.doctor.getId() +
                    ", Name: " + temp.doctor.getName() +
                    ", Speciality: " + temp.doctor.getSpecialty() +
                    ", Contact: " + temp.doctor.getContact() +
                    ", Fees: " + temp.doctor.getConsultationFee());
            temp = temp.prev;
        }

    }


    public static void deleteDoctor(Doctors_list dlist, Scanner scanner, Connection con) {
        System.out.println("Enter doctor ID to delete:");
        String doctorId = scanner.nextLine();

        // Delete from linked list
        if (dlist.delete(doctorId)) {
            System.out.println("Doctor deleted from list.");
        } else {
            System.out.println("Doctor not found in list.");
        }

        // Delete from database
        String deleteQuery = "DELETE FROM doctor WHERE doctor_id = ?";
        try (PreparedStatement statement = con.prepareStatement(deleteQuery)) {
            statement.setString(1, doctorId);
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Doctor deleted from database!");
            } else {
                System.out.println("ID not found in database.");
            }
        } catch (SQLException e) {

        }
    }

    public static void deletePatient(Patient_list plist, Scanner scanner, Connection con) {
        System.out.println("Enter ID to delete:");
        String patientId = scanner.nextLine();

        int patt = Integer.parseInt(patientId);
        // Delete from linked list
        if (plist.delete(patientId)) {
            System.out.println("Patient deleted from list.");
        } else {
            System.out.println("Patient not found in list.");
        }

        // Delete from database
        String deleteQuery = "DELETE FROM patient WHERE patient_id = ?";
        try (PreparedStatement statement = con.prepareStatement(deleteQuery)) {
            statement.setInt(1, patt);
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {

                loadPatientsFromDatabase(con, plist);

            } else {
                System.out.println(" Patient not found in database.");
            }
        } catch (SQLException e) {

        }
    }


    public static void showCheckupRecommendations(Checkup_list clist) {
        System.out.println("Checkup Recommendation");
        clist.print();
    }


    public static void showAllDoctors(Doctors_list dlist) {
        DNode temp = dlist.tail; // Start from the oldest doctor
        System.out.println("--All Doctors--");
        while (temp != null) {
            System.out.println("ID: " + temp.doctor.getId() + ", Name: " + temp.doctor.getName() + ", Speciality: " + temp.doctor.getSpecialty());
            temp = temp.prev;
        }
    }

    public static void assignPatientToDoctor(Connection con, Scanner scanner, Doctors_list dlist, Patient_list plist) {
        System.out.println("Enter doctor ID");
        int doctorId = Integer.parseInt(scanner.nextLine());

        Doctor doctor = dlist.getDoctorById(String.valueOf(doctorId));
        if (doctor == null) {
            System.out.println("Doctor not found.");
            return;
        }

        while (true) {
            System.out.println("Enter patient ID");
            int patientId = Integer.parseInt(scanner.nextLine());

            Patient patient = plist.searchById(String.valueOf(patientId));
            if (patient == null) {
                System.out.println("Invalid patient ID");
                continue;
            }

            System.out.println("Enter priority 3 = Emergency, 2 = Intermediate, 1 = Normal");
            int priority = scanner.nextInt();
            scanner.nextLine();

            patient.setPriority(priority);
            doctor.addPatient(patient);


            String insertQuery = "INSERT INTO doctor_patient (doctor_id, patient_id, priority) VALUES (?, ?, ?)";
            try (PreparedStatement statement = con.prepareStatement(insertQuery)) {
                statement.setInt(1, doctorId);
                statement.setInt(2, patientId);
                statement.setInt(3, priority);
                statement.executeUpdate();
                System.out.println(" Patient ID " + patientId + " assigned to Dr. " + doctor.getName() + " in database with level " + priority);
            } catch (SQLException e) {
                System.out.println("Not");
            }

            System.out.println("Do you want to assign another patient? (yes/no):");
            String choice = scanner.nextLine();
            if (!choice.equalsIgnoreCase("yes")) {
                break;
            }
        }
    }




    public static void addPatients(Scanner scanner, Patient_list plist, Connection con) {
        System.out.println("Enter Patient ID:");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter Patient Name:");
        String name = scanner.nextLine();
        System.out.println("Enter Patient Contact:");
        String contact = scanner.nextLine();
        Patient newPatient = new Patient(id, name, contact);
        plist.Insert(newPatient);
        System.out.println("Patient Added Successfully!");
        plist.displayPatients();
        addPatientToDatabase(con, name, contact);
    }
    public static void addPatientToDatabase(Connection con, String name, String contact) {
        String insertQuery = "INSERT INTO patient (name, contact) VALUES (?, ?)";
        try (PreparedStatement statement = con.prepareStatement(insertQuery)) {
            statement.setString(1, name);
            statement.setString(2, contact);
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Patient added to database ");
            } else {
                System.out.println("Failed to add patient ");
            }
        } catch (SQLException e) {
            System.out.println("Error adding patiennt" + e.getMessage());

        }
    }

    public static void addDoctors(Scanner scanner,Doctors_list dlist, Connection con) {
        // ✅ Ensure correct method name
        System.out.println("Enter Doctor ID:");
        int doctorId = Integer.parseInt(scanner.nextLine());

        System.out.println("Enter Doctor Name:");
        String name = scanner.nextLine();



        System.out.println("Enter Doctor Speciality:");
        String speciality = scanner.nextLine();

        System.out.println("Enter Doctor Fees:");
        int fees = scanner.nextInt();
        scanner.nextLine();

        Doctor newDoctor = new Doctor(doctorId, name, speciality, fees);

        dlist.Insert(newDoctor);
        System.out.println("Doctor Added Successfully!");
        addDoctorToDatabase(con, doctorId, name, speciality, (double) fees);

    }

    public static void addCheckups(Scanner scanner,Checkup_list clist, Doctors_list dlist, Patient_list plist) {  // ✅ Ensure correct method name
        System.out.println("Adding Checkups...");

    }

    public static void searchDoctorBySpeciality(Doctors_list dlist, Scanner scanner) {
        System.out.println("Enter Speciality to Search:");
        String speciality = scanner.nextLine();

        Doctor foundDoctor = dlist.searchBySpeciality(speciality);
        if (foundDoctor != null) {
            System.out.println(" Doctor Found: " + foundDoctor.getName());
        } else {
            System.out.println("Doctor Not Found!");
        }
    }


    public static void searchDoctorById(Doctors_list dlist, Scanner scanner) {
        System.out.println("Enter Doctor ID to Search:");
        String id = scanner.nextLine();

        Doctor foundDoctor = dlist.searchById(id);
        if (foundDoctor != null) {
            System.out.println("Doctor Found: " + foundDoctor.getName());
        } else {
            System.out.println("Doctor Not Found!");
        }
    }





    public static void searchPatientById(Patient_list plist, Scanner scanner) {
        System.out.println("Enter Patient ID to search:");
        String id = scanner.nextLine();

        Patient foundPatient = plist.searchById(id);
        if (foundPatient != null) {
            System.out.println("Patient Found: " + foundPatient.getName());
        } else {
            System.out.println("Patient Not Found!");
        }
    }

    public static void searchPatientByContact(Patient_list plist, Scanner scanner) {
        System.out.println("Enter Patient Contact to search:");
        String contact = scanner.nextLine();

        Patient foundPatient = plist.searchById(contact);
        if (foundPatient != null) {
            System.out.println("Patient Found: " + foundPatient.getContact());
        } else {
            System.out.println("Patient Not Found!");
        }
    }

    public static void viewAssignedPatients(Connection con, Scanner scanner , Doctors_list dlist) {
        System.out.println("Enter your Doctor ID:");
        int doctorId = Integer.parseInt(scanner.nextLine()); // Assuming doctor IDs are integers

        // ✅ Query assigned patients from the database
        String query = "SELECT p.patient_id, p.name, p.contact, dp.priority " +
                "FROM patient p " +
                "INNER JOIN doctor_patient dp ON p.patient_id = dp.patient_id " +
                "WHERE dp.doctor_id = ? " +
                "ORDER BY dp.priority DESC"; // Sort by priority automatically

        try (PreparedStatement statement = con.prepareStatement(query)) {
            statement.setInt(1, doctorId);
            ResultSet rs = statement.executeQuery();

            boolean foundPatients = false;
            System.out.println("Patients for Doctor ID " + doctorId + " (sorted by priority):");
            while (rs.next()) {
                System.out.println("Name: " + rs.getString("name") +
                        ", Contact: " + rs.getString("contact") +
                        ", Priority: " + rs.getInt("priority"));
                foundPatients = true;
            }

            if (!foundPatients) {
                System.out.println("⚠️ No patients assigned to this doctor.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving assigned patients: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public static Patient findPatientById(Patient_list plist, int patientId) {
        return plist.findPatientById(patientId);
    }

    public static Doctor findDoctorById(Doctors_list dlist, int doctorId) {
        return dlist.findDoctorById(doctorId);
    }


    public static void addBill(Scanner scanner, BillingManager billingManager, Patient_list plist, Doctors_list dlist) {


        System.out.println("Enter Patient ID:");
        int patientId = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Enter Doctor ID:");
        int doctorId = scanner.nextInt();
        scanner.nextLine();

        Patient patient = findPatientById(plist, patientId);
        Doctor doctor = findDoctorById(dlist, doctorId);

        if (patient == null) {
            System.out.println("Error: Patient ID " + patientId + " not found in Patient_list.");
            return;
        }

        if (doctor == null) {
            System.out.println("Error: Doctor ID " + doctorId + " not found in Doctors_list.");
            return;
        }
        if (patient == null || doctor == null) {
            System.out.println("Invalid Patient or Doctor ID.");
            return;
        }

        double amount = doctor.getConsultationFee();

        System.out.println("Enter Disease:");
        String disease = scanner.nextLine();

        System.out.println("Enter Medication:");
        String medication = scanner.nextLine();

        billingManager.generateBill(patient, doctor, disease, medication, amount);

        System.out.println("Bill successfully added for patient: " + patient.getName());
    }


    public static void generateBill(Scanner scanner, BillingManager billingManager) {
        System.out.println("Enter Patient ID:");
        int patientId = scanner.nextInt();
        scanner.nextLine();

        for (Billing bill : billingManager.getAllBills()) {
            if (bill.getPatient().getPatientId() == patientId) {
                bill.displayBill();
                return;
            }
        }
        System.out.println("No bill found for this patient.");
    }

    public static void showBillingHistory(Scanner scanner, BillingManager billingManager) {
        System.out.println("Enter Patient ID:");
        int patientId = scanner.nextInt();
        scanner.nextLine();


    }


    public static void addDoctorToDatabase(Connection con, int id, String name, String speciality, double fee) {
        try {
            String query = "INSERT INTO doctor (doctor_id, name, specialty, consultation_fee) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setString(3, speciality);
            statement.setDouble(4, fee);
            statement.executeUpdate();
            System.out.println("✅ Doctor added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding doctor: " + e.getMessage());
        }
    }




    public static void showPatientMenu(Scanner scanner, String userRole, Patient_list plist, Doctors_list dlist,Connection con) {
        while (true) {
            System.out.println("Patient Menu");

            if (userRole.equals("Admin") || userRole.equals("Nurse")) {
                System.out.println("1. Search for Patient by ID");
                System.out.println("2. Show All Patients");
            }

            if (userRole.equals("Admin")) {
                System.out.println("3. Add Patient");
                System.out.println("4. Delete Patient");
            }

            System.out.println("5. Back to Main Menu");
            System.out.println("Enter your choice:");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    searchPatientById(plist, scanner);
                    break;
                case 2:
                    if (userRole.equals("Admin") || userRole.equals("Nurse")) showAllPatients(plist);
                    else System.out.println("Access Denied!");
                    break;
                case 3:
                    if (userRole.equals("Admin")) addPatients(scanner, plist,con);
                    else System.out.println("Access Denied!");
                    break;
                case 4:
                    if (userRole.equals("Admin")) deletePatient(plist, scanner,con);
                    else System.out.println("Access Denied!");
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }

    public static void showDoctorMenu(Scanner scanner, String userRole, Doctors_list dlist, Patient_list plist,Connection con) {
        while (true) {
            System.out.println("Doctor Menu");
            System.out.println("1. Add Doctor");
            System.out.println("2. Search Doctor by Specialty");
            System.out.println("3. Search Doctor by ID");
            System.out.println("4. Delete Doctor");
            System.out.println("5. Show All Doctors");
            System.out.println("6. Assign Patient to Doctor");
            System.out.println("7. Show Assigned Patients");
            System.out.println("8. Back to Main Menu");
            System.out.println("Enter your choice:");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    if (userRole.equals("Admin")) addDoctors(scanner, dlist,con);
                    else System.out.println("Access Denied!");
                    break;
                case 2:
                    if (userRole.equals("Admin")) searchDoctorBySpeciality(dlist, scanner);
                    else System.out.println("Access Denied!");
                    break;
                case 3:
                    if (userRole.equals("Admin")) searchDoctorById(dlist, scanner);
                    else System.out.println("Access Denied!");
                    break;
                case 4:
                    if (userRole.equals("Admin")) deleteDoctor(dlist, scanner,con);
                    else System.out.println("Access Denied!");
                    break;
                case 5:
                    if (userRole.equals("Admin")) showAllDoctors(dlist);
                    else System.out.println("Access Denied!");
                    break;
                case 6:
                    if (userRole.equals("Admin")) assignPatientToDoctor(con ,scanner, dlist, plist);
                    else System.out.println("Access Denied!");
                    break;
                case 7:
                    if (userRole.equals("Doctor")) viewAssignedPatients(con,scanner,dlist);
                    else if(userRole.equals("Admin")) viewAssignedPatients(con,scanner,dlist);
                    else System.out.println("Access Denied!");
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }

    public static void showDoctorRestrictedMenu(Connection con,Scanner scanner,Doctors_list dlist) {
        System.out.println("----Doctor Menu-----");
        System.out.println("1. View Assigned Patients");
        System.out.println("2. Logout");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                viewAssignedPatients(con,scanner,dlist);
                break;
            case 2:
                System.out.println("Logging Out...");
                return;
            default:
                System.out.println("Invalid choice");
        }
    }

    Patient_list plist = new Patient_list();
    Doctors_list dlist = new Doctors_list();

    public static void showBillingMenu(Scanner scanner, String userRole, BillingManager billingManager , Patient_list plist, Doctors_list dlist ) {
        if (!userRole.equals("Admin")) {
            System.out.println("Access Denied! Only Admins can access billing.");
            return;
        }

        while (true) {
            System.out.println("----Billing & Accounting Menu-----");
            System.out.println("1. Add Bill (Auto Fetch Fees)");
            System.out.println("2. Generate Bill");
            System.out.println("3. Show Billing History");
            System.out.println("4. Back to Main Menu");
            System.out.println("Enter your choice:");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addBill(scanner, billingManager, plist, dlist);
                    break;
                case 2:
                    generateBill(scanner, billingManager);
                    break;
                case 3:
                    showBillingHistory(scanner, billingManager);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();
            } else {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();

            }
        }
    }


}



