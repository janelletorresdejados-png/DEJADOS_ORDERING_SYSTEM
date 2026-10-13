package com.scc;

import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.Dashboard;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final UserDao userdao = new UserDao();

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            System.out.println("\n=============================");
            System.out.println("RESTAURANT ORDERING MANAGEMENT");
            System.out.println("=============================");
            System.out.println("1. Log-in");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("\n--- USER LOGIN ---");
                    System.out.print("Enter username: ");
                    String loginUser = sc.nextLine();
                    System.out.print("Enter password: ");
                    String loginPass = sc.nextLine();

                    User currentUser = userdao.loginUser(loginUser, loginPass);

                    if (currentUser != null) {
                        handleUserSession(currentUser);
                    }
                    break;

                case "2":
                    System.out.println("\n--- CREATE ACCOUNT ---");
                    System.out.print("Enter new username: ");
                    String user = sc.nextLine();
                    System.out.print("Enter password: ");
                    String pass = sc.nextLine();

                    System.out.println("Select Role:");
                    System.out.println(" [1] Cashier");
                    System.out.println(" [2] Kitchen Staff");
                    System.out.println(" [3] Restaurant Manager");
                    System.out.print("Enter choice (1-3): ");
                    String roleChoice = sc.nextLine();
                    String role = null;

                    switch (roleChoice) {
                        case "1":
                            role = "Cashier";
                            break;
                        case "2":
                            role = "Kitchen Staff";
                            break;
                        case "3":
                            role = "Restaurant Manager";
                            break;
                        default:
                            System.out.println("Invalid structural assignment index selected.");
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

    private static void handleUserSession(User user) {
        Dashboard dashboardEngine = new Dashboard();
        dashboardEngine.displayDashboard(user);
    }
}
