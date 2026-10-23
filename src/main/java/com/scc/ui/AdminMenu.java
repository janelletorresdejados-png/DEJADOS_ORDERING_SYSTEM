package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner sc;

    public AdminMenu(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.sc = sc;
    }

    public void displayMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n======================================");
            System.out.println("      RESTAURANT MANAGER ADMIN MENU   ");
            System.out.println("======================================");
            System.out.println("1. View All System Users[cite: 6]");
            System.out.println("2. View Pending Registrations[cite: 6]");
            System.out.println("3. Approve User Account[cite: 6]");
            System.out.println("4. Return to Main Dashboard");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    viewAllUsers();
                    break;
                case "2":
                    viewPendingRegistrations();
                    break;
                case "3":
                    approveUserAccount();
                    break;
                case "4":
                    running = false;
                    break;
                default:
                    System.out.println("❌ Invalid option. Please choose between 1 and 4.");
            }
        }
    }

    private void viewAllUsers() {
        List<User> users = userDao.getAllUsers();
        printUserTable("ALL SYSTEM USERS", users);
    }

    private void viewPendingRegistrations() {
        List<User> users = userDao.getPendingUsers();
        printUserTable("PENDING REGISTRATIONS", users);
    }

    private void approveUserAccount() {
        List<User> pendingUsers = userDao.getPendingUsers();
        if (pendingUsers.isEmpty()) {
            System.out.println("\nℹ️ No pending user registrations found.");
            return;
        }

        printUserTable("PENDING ACCOUNTS AWAITING APPROVAL", pendingUsers);
        System.out.print("\nEnter User ID to approve (or enter 0 to cancel): ");

        String input = sc.nextLine().trim();
        try {
            int userId = Integer.parseInt(input);
            if (userId == 0) {
                System.out.println("↩️ Approval action canceled.");
                return;
            }

            boolean exists = pendingUsers.stream().anyMatch(u -> u.id() == userId);
            if (!exists) {
                System.out.println("❌ Error: User ID " + userId + " not found in pending list.");
                return;
            }

            boolean success = userDao.approveUser(userId);
            if (success) {
                System.out.println("\n✅ Success! User ID " + userId + " status updated to 'Approved'.");
            } else {
                System.out.println("\n❌ Failed to update user status.");
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Please enter a valid numeric User ID.");
        }
    }

    private void printUserTable(String title, List<User> users) {
        System.out.println("\n--- " + title + " ---");
        if (users.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        String format = "| %-6s | %-18s | %-16s | %-12s |%n";
        String border = "+--------+--------------------+------------------+--------------+";

        System.out.println(border);
        System.out.printf(format, "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(border);

        for (User u : users) {
            System.out.printf(format, u.id(), u.username(), u.role(), u.status());
        }
        System.out.println(border);
    }
}