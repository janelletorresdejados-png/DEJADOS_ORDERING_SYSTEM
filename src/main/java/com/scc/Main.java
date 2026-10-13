package com.scc;

import com.scc.dao.UserDao;

import com.scc.model.Users; // Only one import statement needed here
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

                    Users currentUser = userdao.loginUser(loginUser, loginPass);

                    if (currentUser != null) {
                        System.out.println("Redirecting to the " + currentUser.role() + " dashboard...");
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
                    System.out.println(" Cashier");
                    System.out.println(" Kitchen Staff");
                    System.out.println(" Restaurant Manager");
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
                            System.out.println("Invalid number selection!");
                    }

                    if (role != null) {
                        userdao.registerUser(user, pass, role);
                    } else {
                        System.out.println("❌ Registration aborted due to invalid role selection.");
                    }
                    break;

                case "3":
                    System.out.println("\nExiting system. Goodbye!");
                    running = false;
                    break;

                default:
                    System.out.println("❌ Invalid option! Please choose 1, 2, or 3.");
            }
        }
    }

    private static void handleUserSession(Users user) {
        switch (user.role()) {
            case "Cashier":
                System.out.println("\n[Cashier Mode Enabled] - Ready to capture client order intake.");
                break;
            case "Kitchen Staff":
                System.out.println("\n[Kitchen Staff Mode Enabled] - Displaying ongoing active tickets.");
                break;
            case "Restaurant Manager":
                System.out.println("\n[Manager Mode Enabled] - Access granted to performance records.");
                break;
        }
    }
}
