package hospital;

import java.sql.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;



public class Main {
    public static void main(String[] args) {
        try {

            // load my sql driver and establish connection
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hospital", "root", "harsimar");

            // create objects
            Patient_list plist = new Patient_list();
            Doctors_list dlist = new Doctors_list();
            Checkup_list clist = new Checkup_list();
            Scanner scanner = new Scanner(System.in);


            BillingManager billingManager = new BillingManager();

            // load doctors and patients from database
            loaddoctorsfromDatabase(con, dlist);
            loadPatientsfromDatabase(con, plist);

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
                        loginuser(con, scanner, plist, dlist, clist,billingManager);
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

  //  check if there already exists a admin account
    public static boolean adminExists(Connection con) {
        try {
            PreparedStatement stmt = con.prepareStatement("SELECT COUNT(*) FROM users WHERE role = 'Admin'");
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // it exists
            }
        } catch (Exception e) {
            System.out.println("Error checking admin existence: " + e);
        }
        return false;
    }

    // email validation
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";


        return email.matches(emailRegex);
    }

    // password validation
    public static boolean isValidPassword(String password) {
        return password.length() >= 8 && password.length() <= 20; // between 8 and 20 characters
    }

// Register a user
    public static void registerUser(Connection con, Scanner scanner) {
        try {
            System.out.println("Registration");
            System.out.println("Enter name, email, password, and role (Admin/Doctor/Nurse/Patient):");

            String name = scanner.nextLine().trim();
            String email = scanner.nextLine().trim();
            String pass = scanner.nextLine().trim();
            String role = scanner.nextLine().trim();

            // validate
            if (!isValidEmail(email)) {
                System.out.println("Invalid email format");
                return;
            }

           // validate
            if (!isValidPassword(pass)) {
                System.out.println("Password should be at least 8 characters");
                return;
            }

           // only one admin exists
            if (role.equalsIgnoreCase("Admin") && adminExists(con)) {
                System.out.println("Admin account already exists");
                return;
            }

            if (!role.matches("Admin|Doctor|Nurse|Patient")) {
                System.out.println("Invalid role! Please enter a valid role.");
                return;
            }

            // insert user details to db
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

    // main menu for different people
    public static void showmenu(Scanner scanner, String userRole, Patient_list plist, Doctors_list dlist, Checkup_list clist, BillingManager billingManager,Connection con) {
        while (true) {

            // if user is doctor
            if (userRole.equals("Doctor")) {
                showDoctorRestrictedMenu(con ,scanner,dlist); //  doctors restricted menu
                return;
            }

            System.out.println("Main Menu");

            //admin and nurse can access patient menu
            if (userRole.equals("Admin") || userRole.equals("Nurse")) {
                System.out.println("1 Patients");
            }

            // admin has access to doctor and billing
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
                        showPMenu(scanner, userRole, plist, dlist,con);
                    } else {
                        System.out.println("Access Denied");
                    }
                    break;

                case 2:
                    if (userRole.equals("Admin") ) {
                        showDMenu(scanner, userRole, dlist, plist,con);
                    } else {
                        System.out.println("Access Denied!");
                    }
                    break;

                case 3:
                    if (userRole.equals("Admin")) {
                        showBillingMenu(con ,scanner, userRole, billingManager, plist, dlist);
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

 // login class
    public static void loginuser(Connection con, Scanner sc, Patient_list plist, Doctors_list dlist, Checkup_list clist ,BillingManager billingManager) {
        try {
            System.out.println("Login");
            System.out.println("Enter email and password:");

            // get user input
            String email = sc.next().trim();
            String pass = sc.next().trim();


            if (!isValidEmail(email)) {
                System.out.println("Invalid email format");
                return;
            }
              // check user details in db
            PreparedStatement statement = con.prepareStatement("SELECT role FROM users WHERE email = ? AND password = ?");
            statement.setString(1, email);
            statement.setString(2, pass);
            ResultSet resultSet = statement.executeQuery();

            // if usser is , then show menu
            if (resultSet.next()) {
                String role = resultSet.getString("role");
                System.out.println("Welcome, " + role + "!");

                if (role.equals("Admin")) {
                    showmenu(sc, role, plist, dlist, clist ,billingManager,con);
                } else if (role.equals("Doctor")) {
                    showDoctorRestrictedMenu(con ,sc,dlist);
                } else if (role.equals("Nurse") || role.equals("Patient")) {
                    showPMenu(sc, role, plist, dlist,con);
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

    // load all doctors from the database into linkedlist
    public static void loaddoctorsfromDatabase(Connection con, Doctors_list dlist) {
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

    // load all patinets from the database into linkedlist
    public static void loadPatientsfromDatabase(Connection con, Patient_list plist) {
        try {
            String query = "SELECT * FROM patient";
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                // retrieve patient information from resultbset
                int id = rs.getInt("patient_id");
                String name = rs.getString("name");
                String contact = rs.getString("contact");

                Patient patient = new Patient(id, name, contact);
                plist.Insert(patient); //  Add patient to linked list
            }

        } catch (SQLException e) {
            System.out.println("Error loading patients: " + e.getMessage());
        }
    }

    // show all patients
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

 // display all doctors
    public static void showAllDoctors(Scanner scanner, Doctors_list dlist) {
        DNode temp = dlist.head;
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

 ///   ///  delete doctor from both db and linkedlist
    public static void deleteDoctor(Doctors_list dlist, Scanner scanner, Connection con) {
        System.out.println("Enter doctor ID to delete:");
        String doctorId = scanner.nextLine();

        // Delete from linked list
        if (dlist.delete(doctorId)) {
            System.out.println("Doctor deleted from list.");
        } else {
            System.out.println("Doctor not found in list.");
        }

        // Delete from db
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

    //delete patient from linkedlist and db
    public static void deletePatient(Patient_list plist, Scanner scanner, Connection con) {
        System.out.println("Enter ID to delete:");
        String patientId = scanner.nextLine();

        int patt = Integer.parseInt(patientId);  // id to integer
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

                loadPatientsfromDatabase(con, plist);

            } else {
                System.out.println(" Patient not found in database.");
            }
        } catch (SQLException e) {

        }
    }


    // display all doctors
    public static void showAllDoctors(Doctors_list dlist) {
        DNode temp = dlist.head; // Start from the oldest doctor
        System.out.println("--All Doctors--");
        while (temp != null) {
            System.out.println("ID: " + temp.doctor.getId() + ", Name: " + temp.doctor.getName() + ", Speciality: " + temp.doctor.getSpecialty());
            temp = temp.next;
        }
    }

    // method to assign a doctor to patient and store it in db
    public static void appointmentOfPatient(Connection con, Scanner scanner, Doctors_list dlist, Patient_list plist) {
        System.out.println("Enter doctor ID");
        int doctorId = Integer.parseInt(scanner.nextLine());  // read doctor id

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
   //           read priority level
            System.out.println("Enter priority 3 = Emergency, 2 = Intermediate, 1 = Normal");
            int priority = scanner.nextInt();
            scanner.nextLine();

            patient.setPriority(priority);
            doctor.addPatient(patient);    // assign patient to doctor in memory


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



// method to add new patient and add int the database
    public static void addPatients(Scanner scanner, Patient_list plist, Connection con) {
        System.out.println("Enter Patient ID:");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter Patient Name:");
        String name = scanner.nextLine();
        System.out.println("Enter Patient Contact:");
        String contact = scanner.nextLine();
        // patient object
        Patient newPatient = new Patient(id, name, contact);
        // insert patient data to sql
        plist.Insert(newPatient);
        System.out.println("Patient Added Successfully!");
        plist.displayPatients();
        addpatienttoDB(con, name, contact);
    }
    // method to add patient detail to db
    public static void addpatienttoDB(Connection con, String name, String contact) {
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

    // method to add doctor and store it in db
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

        // create doctor object and add it to linked list
        Doctor newDoctor = new Doctor(doctorId, name, speciality, fees);

        dlist.Insert(newDoctor);
        System.out.println("Doctor Added Successfully!");
        addDoctortoDB(con, doctorId, name, speciality, (double) fees);

    }


  // search doctor by speciality
    public static void searchDoctorBySpecility(Doctors_list dlist, Scanner scanner) {
        System.out.println("Enter Speciality to Search:");
        String speciality = scanner.nextLine();

        Doctor foundDoctor = dlist.searchBySpeciality(speciality);
        if (foundDoctor != null) {
            System.out.println(" Doctor Found: " + foundDoctor.getName());
        } else {
            System.out.println("Doctor Not Found!");
        }
    }

       // search doctor by ID
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

// search patient by id
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

    // view assigned patients sorted by priority
    public static void viewAssignedmentOfPatients(Connection con, Scanner scanner , Doctors_list dlist) {
        System.out.println("Enter your Doctor ID:");
        int doctorId = Integer.parseInt(scanner.nextLine()); // Assuming doctor IDs are integers


        String query = "SELECT p.patient_id, p.name, p.contact, dp.priority " +
                "FROM patient p " +
                "INNER JOIN doctor_patient dp ON p.patient_id = dp.patient_id " +
                "WHERE dp.doctor_id = ? " +
                "ORDER BY dp.priority DESC"; // Sort by priority automatically

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
                System.out.println("No patients assigned to this doctor.");
            }
        } catch (SQLException e) {

        }
    }


    // to find patient id from list
    public static Patient findPatientById(Patient_list plist, int patientId) {
        return plist.findPatientById(patientId);
    }
    // to find doctor id from list
    public static Doctor findDoctorById(Doctors_list dlist, int doctorId) {
        return dlist.findDoctorById(doctorId);
    }

   // method to add bill in database
    public static void addBill(Connection con, Scanner scanner, BillingManager billingManager) {
        System.out.println("Enter Patient ID:");
        int patientId = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Enter Doctor ID:");
        int doctorId = scanner.nextInt();
        scanner.nextLine();

       // check if patient exists in DB
        try (PreparedStatement stmt = con.prepareStatement("SELECT name FROM patient WHERE patient_id = ?")) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                System.out.println("Patient ID " + patientId + " not found in the database.");
                return;
            }
        } catch (SQLException e) {
            return;
        }
        // fetch cdoctors fees
        double consultationFee = 0.0;
        try (PreparedStatement stmt = con.prepareStatement("SELECT consultation_fee FROM doctor WHERE doctor_id = ?")) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                consultationFee = rs.getDouble("consultation_fee");
            } else {
                System.out.println("Doctor ID " + doctorId + " not found in the database.");
                return;
            }
        } catch (SQLException e) {
            return;
        }

        System.out.println("Enter disease:");
        String disease = scanner.nextLine();

        System.out.println("Enter medication:");
        String medication = scanner.nextLine();

        // insert bill details to db
        String insertQuery = "INSERT INTO bill (patient_id, doctor_id, disease, medication, consultation_fee) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(insertQuery)) {
            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, disease);
            stmt.setString(4, medication);
            stmt.setDouble(5, consultationFee);
            stmt.executeUpdate();
            System.out.println(" Bill successfully added for  patient ID " + patientId);
        } catch (SQLException e) {
        }
    }

// method to generate bill for id
    public static void generateBill(Connection con, Scanner scanner) {
        System.out.println("Enter Bill ID:");
        int billId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // fetch bill detail from db
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
                System.out.println(" No bill found with ID: " + billId);
            }
        } catch (SQLException e) {

        }
    }

    // add new doctor to db
    public static void addDoctortoDB(Connection con, int id, String name, String speciality, double fee) {
        try {
            String query = "INSERT INTO doctor (doctor_id, name, specialty, consultation_fee) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setString(3, speciality);
            statement.setDouble(4, fee);
            statement.executeUpdate();
            System.out.println("Doctor added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding doctor: " + e.getMessage());
        }
    }

// PATIENT MENU
    public static void showPMenu(Scanner scanner, String userRole, Patient_list plist, Doctors_list dlist,Connection con) {
        while (true) {
            System.out.println("Patient Menu");

            // admin and nurse can view patien
            if (userRole.equals("Admin") || userRole.equals("Nurse")) {
                System.out.println("1. Search for Patient by ID");
                System.out.println("2. Show All Patients");
            }

            // admin can modify details of patient
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
                    return;  // return to main menu
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }

    // DOCTOR MENU
    public static void showDMenu(Scanner scanner, String userRole, Doctors_list dlist, Patient_list plist,Connection con) {
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
                    if (userRole.equals("Admin")) searchDoctorBySpecility(dlist, scanner);
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
                    if (userRole.equals("Admin")) appointmentOfPatient(con ,scanner, dlist, plist);
                    else System.out.println("Access Denied!");
                    break;
                case 7:
                    if (userRole.equals("Doctor")) viewAssignedmentOfPatients(con,scanner,dlist);
                    else if(userRole.equals("Admin")) viewAssignedmentOfPatients(con,scanner,dlist);
                    else System.out.println("Access Denied!");
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }

    //DOCTOR MENU(only seen by them onlt
    public static void showDoctorRestrictedMenu(Connection con,Scanner scanner,Doctors_list dlist) {
        System.out.println("----Doctor Menu-----");
        System.out.println("1. View Assigned Patients");
        System.out.println("2. Logout");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                viewAssignedmentOfPatients(con,scanner,dlist);
                break;
            case 2:
                System.out.println("Logging Out...");
                return;
            default:
                System.out.println("Invalid choice");
        }
    }

    //declare gloabal patient and doctor list
    Patient_list plist = new Patient_list();
    Doctors_list dlist = new Doctors_list();

    // BILLING MENU
    public static void showBillingMenu(Connection con,Scanner scanner, String userRole, BillingManager billingManager , Patient_list plist, Doctors_list dlist ) {
        if (!userRole.equals("Admin")) {
            System.out.println("Access Denied! Only Admins can access billing.");
            return;
        }

        while (true) {
            System.out.println("----Billing & Accounting Menu-----");
            System.out.println("1. Add Bill (Auto Fetch Fees)");
            System.out.println("2. Generate Bill");
//            System.out.println("3. Show Billing History");
            System.out.println("3. Back to Main Menu");
            System.out.println("Enter your choice:");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addBill(con,scanner, billingManager);
                    break;
                case 2:
                    generateBill(con,scanner);
                    break;

                case 3:
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



