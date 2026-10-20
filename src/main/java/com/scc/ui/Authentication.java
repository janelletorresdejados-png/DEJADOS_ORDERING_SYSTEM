package com.scc.ui; // Updated to match your folder path

import com.scc.dao.UserDao;   // Updated import
import com.scc.model.User;     // Updated import
import java.util.Scanner;

public class Authentication {

    private final UserDao userdao;
    private final Scanner sc;

    public Authentication(UserDao userdao, Scanner sc) {
        this.userdao = userdao;
        this.sc = sc;
    }

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n=============================");
            System.out.println("RESTAURANT ORDERING MANAGEMENT");
            System.out.println("=============================");
            System.out.println("1. Log-in");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println("\n--- USER LOGIN ---");
                    System.out.print("Enter username: ");
                    String loginUser = sc.nextLine().trim();
                    System.out.print("Enter password: ");
                    String loginPass = sc.nextLine();

                    User currentUser = userdao.loginUser(loginUser, loginPass);

                    if (currentUser != null) {
                        handleUserSession(currentUser);
                    } else {
                        System.out.println("❌ Login failed. Invalid credentials or inactive profile.");
                    }
                    break;

                case "2":
                    System.out.println("\n--- CREATE ACCOUNT ---");
                    System.out.print("Enter new username: ");
                    String user = sc.nextLine().trim();
                    System.out.print("Enter password: ");
                    String pass = sc.nextLine();

                    if (user.isEmpty() || pass.isEmpty()) {
                        System.out.println("❌ Registration canceled: Username and password cannot be empty.");
                        break;
                    }

                    System.out.println("Select Role:");
                    System.out.println(" Cashier");
                    System.out.println(" Kitchen Staff");
                    System.out.println(" Restaurant Manager");
                    System.out.print("Enter choice (1-3): ");
                    String roleChoice = sc.nextLine().trim();
                    String role = null;

                    switch (roleChoice) {
                        case "1": role = "Cashier"; break;
                        case "2": role = "Kitchen Staff"; break;
                        case "3": role = "Restaurant Manager"; break;
                        default: System.out.println("❌ Invalid structural assignment choice.");
                    }

                    if (role != null) {
                        userdao.registerUser(user, pass, role);
                    } else {
                        System.out.println("❌ Registration cycle canceled due to verification failure.");
                    }
                    break;

                case "3":
                    System.out.println("\nExiting system terminal. Processing complete. Goodbye!");
                    running = false;
                    break;

                default:
                    System.out.println("❌ Operational fault: Please choose input code 1, 2, or 3.");
            }
        }
    }

    private void handleUserSession(User user) {
        System.out.println("\n=== Access Granted ===");
        System.out.println("Welcome, " + user.username() + " [" + user.role() + "]");

        Dashboard dashboard = new Dashboard();
        dashboard.displayDashboard(user);
    }
}
