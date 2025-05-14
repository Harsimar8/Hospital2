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
            Scanner scanner = new Scanner(System.in);


            BillingManager billingManager = new BillingManager();

            // load doctors and patients from database
            dlist.loadDoctorsFromDatabase(con);
            plist.loadPatientsFromDatabase(con);

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
                        loginuser(con, scanner, plist, dlist,billingManager);
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
                return true;
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

// reegister a user
    public static void registerUser(Connection con, Scanner scanner) {
        try {
            System.out.println("Registration");
            System.out.println("Enter name, email, password, and role (Admin/Doctor/Nurse/Patient):");

            String name = scanner.nextLine().trim();
            String email = scanner.nextLine().trim();
            String pass = scanner.nextLine().trim();
            String role = scanner.nextLine().trim();

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
//MENU
    public static void showMenu(Scanner scanner, String userRole, Patient_list plist, Doctors_list dlist, BillingManager billingManager, Connection con) {
        while (true) {

            if (userRole.equals("Doctor")) {
                showDoctorRestrictedMenu(con, scanner, dlist);
                return;
            }

            System.out.println("\nMain Menu");

            if (userRole.equals("Admin") || userRole.equals("Nurse")) {
                System.out.println("1. Patients");
            }

            if (userRole.equals("Admin")) {
                System.out.println("2. Doctors");
                System.out.println("3. Billing & Accounting");
            }

            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    if (userRole.equals("Admin") || userRole.equals("Nurse")) {
                        showPatientMenu(scanner, plist, dlist, con);
                    } else {
                        System.out.println("Access Denied.");
                    }
                    break;

                case 2:
                    if (userRole.equals("Admin")) {
                        showDMenu(scanner, userRole, dlist, plist, con);
                    } else {
                        System.out.println("Access Denied!");
                    }
                    break;

                case 3:
                    if (userRole.equals("Admin")) {
                        showBillingMenu(con, scanner,userRole, billingManager, plist, dlist);
                    } else {
                        System.out.println("Access Denied.");
                    }
                    break;

                case 4:
                    System.out.println("Logging out...");
                    return;

                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }
//logina user
    public static void loginuser(Connection con, Scanner sc, Patient_list plist, Doctors_list dlist,BillingManager billingManager) {
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
                    showMenu(sc, role, plist, dlist,billingManager,con);
                } else if (role.equals("Doctor")) {
                    showDoctorRestrictedMenu(con ,sc,dlist);
                } else if (role.equals("Nurse") || role.equals("Patient")) {
                    showPatientMenu(sc,  plist, dlist,con);
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
    //patient menu
    public static void showPatientMenu(Scanner scanner, Patient_list plist, Doctors_list dlist, Connection con) {
        while (true) {
            System.out.println("\nPatient Menu");
            System.out.println("1. Search Patient by ID");
            System.out.println("2. Show All Patients");
            System.out.println("3. Add Patient");
            System.out.println("4. Delete Patient");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter patient ID: ");
                    int patientId = scanner.nextInt();
                    scanner.nextLine();

                    Patient foundPatient = plist.findPatientById(patientId);

                    if (foundPatient != null) {
                        System.out.println("patient Found:");
                        System.out.println("ID: " + foundPatient.getId() +
                                ", Name: " + foundPatient.getName() +
                                ", Contact: " + foundPatient.getContact());
                    } else {
                        System.out.println("patient not found.");
                    }
                    break;
                case 2:
                    plist.displayPatients();
                    break;
                case 3:
                    System.out.print("Enter Patient ID,    Name,    Contact: ");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    String name = scanner.nextLine();
                    String contact = scanner.nextLine();
                    plist.addPatientToDB(con, new Patient(id, name, contact));
                    break;
                case 4:
                    System.out.print("Enter Patient ID to delete: ");
                    int deleteId = scanner.nextInt();
                    scanner.nextLine();
                    plist.deletePatient(deleteId, con);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    //doctor menu
    public static void showDMenu(Scanner scanner, String userRole, Doctors_list dlist, Patient_list plist, Connection con) {
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
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    if (userRole.equals("Admin")) {
                        System.out.print("Enter Doctor ID,     Name,    Specialty,    Fees: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        String name = scanner.nextLine();
                        String specialty = scanner.nextLine();
                        double fee = scanner.nextDouble();
                        scanner.nextLine();

                        Doctor newDoctor = new Doctor(id, name, specialty, fee);
                        dlist.addDoctorToDB(con, newDoctor);
                    } else {
                        System.out.println("Access Denied!");
                    }
                    break;

                case 2:
                    if (userRole.equals("Admin")) {
                        System.out.print("Enter Specialty: ");
                        String specialtySearch = scanner.nextLine();
                        Doctor foundDoctor = dlist.searchBySpeciality(specialtySearch);
                        if (foundDoctor != null) {
                            System.out.println(foundDoctor);
                        } else {
                            System.out.println("Doctor not found.");
                        }
                    } else {
                        System.out.println("Access Denied!");
                    }
                    break;

                case 3:
                    if (userRole.equals("Admin")) {
                        System.out.print("Enter Doctor ID: ");
                        int doctorId = scanner.nextInt();
                        scanner.nextLine();
                        Doctor doctor = dlist.findDoctorById(doctorId);
                        if (doctor != null) {
                            System.out.println(doctor);
                        } else {
                            System.out.println("Doctor not found.");
                        }
                    } else {
                        System.out.println("Access Denied!");
                    }
                    break;

                case 4:
                    if (userRole.equals("Admin")) {
                        System.out.print("Enter Doctor ID to delete: ");
                        int doctorId = scanner.nextInt();
                        scanner.nextLine();
                        dlist.deleteDoctor(doctorId, con);
                    } else {
                        System.out.println("Access Denied!");
                    }
                    break;

                case 5:
                    if (userRole.equals("Admin")) {
                        dlist.displayDoctors();
                    } else {
                        System.out.println("Access Denied!");
                    }
                    break;

                case 6:
                    if (userRole.equals("Admin")) {
                        System.out.print("Enter Doctor ID: ");
                        int doctorId = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Enter Patient ID: ");
                        int patientId = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Enter Priority (3 = Emergency, 2 = Intermediate, 1 = Normal): ");
                        int priority = scanner.nextInt();
                        scanner.nextLine();

                        dlist.assignPatientToDoctor(con, plist, doctorId, patientId, priority);
                    } else {
                        System.out.println("Access Denied!");
                    }
                    break;

                case 7:
                    if (userRole.equals("Doctor") || userRole.equals("Admin")) {
                        System.out.print("Enter Doctor ID: ");
                        int doctorId = scanner.nextInt();
                        scanner.nextLine();
                        dlist.viewAssignedPatients(con, doctorId);
                    } else {
                        System.out.println("Access Denied!");
                    }
                    break;

                case 8:
                    return;

                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }
// menu show when only doctor is login
    public static void showDoctorRestrictedMenu(Connection con, Scanner scanner, Doctors_list dlist) {
        System.out.println("----Doctor Menu-----");
        System.out.println("1. View Assigned Patients");
        System.out.println("2. Logout");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                System.out.print("Enter Your Doctor ID: ");
                int doctorId = scanner.nextInt();
                scanner.nextLine();
                dlist.viewAssignedPatients(con, doctorId);
                break;

            case 2:
                System.out.println("logging Out.");
                return;

            default:
                System.out.println("Invalid choice,");
        }
    }

    public static void showBillingMenu(Connection con, Scanner scanner, String userRole, BillingManager billingManager, Patient_list plist, Doctors_list dlist) {
        if (!userRole.equals("Admin")) {
            System.out.println("Access Denied! Only Admins can access billing.");
            return;
        }

        while (true) {
            System.out.println("-Billing & Accounting Menu-");
            System.out.println("1. Add Bill ");
            System.out.println("2. Generate Bill");
            System.out.println("3. Back to Main Menu");
            System.out.print("Enter your choice: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input! P");
                scanner.nextLine();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter Patient ID: ");
                    int patientId = scanner.nextInt();
                    scanner.nextLine();

                    System.out.print("Enter Doctor ID: ");
                    int doctorId = scanner.nextInt();
                    scanner.nextLine();

                    System.out.print("Enter Disease: ");
                    String disease = scanner.nextLine();

                    System.out.print("Enter Medication: ");
                    String medication = scanner.nextLine();

                    billingManager.addBill(con, patientId, doctorId, disease, medication); // ✅ Calls `BillingManager` method
                    break;

                case 2:
                    System.out.print("Enter Bill ID: ");
                    int billId = scanner.nextInt();
                    scanner.nextLine();
                    billingManager.generateBill(con, billId);
                    break;

                case 3:
                    return; // Back to main menu

                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }

}



