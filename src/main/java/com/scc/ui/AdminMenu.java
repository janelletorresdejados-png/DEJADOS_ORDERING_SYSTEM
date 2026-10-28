package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner sc;
    private User currentAdmin;

    public AdminMenu(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.sc = sc;
    }

    public void displayMenu() {
        User fallbackAdmin = new User(-1, "Admin", "Restaurant Manager", "Approved");
        displayMenu(fallbackAdmin);
    }

    public void displayMenu(User adminSession) {
        this.currentAdmin = adminSession;
        boolean running = true;

        while (running) {
            System.out.println("\n======================================");
            System.out.println("      RESTAURANT MANAGER ADMIN MENU   ");
            System.out.println("======================================");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role and Status");
            System.out.println("5. Return to Main Dashboard");
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
                    handleUserUpdateFeature();
                    break;
                case "5":
                    running = false;
                    break;
                default:
                    System.out.println("❌ Invalid option. Please choose between 1 and 5.");
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

        int userId = readIntInput("\nEnter User ID to approve (or enter 0 to cancel): ");
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
    }

    private void handleUserUpdateFeature() {
        List<User> users = userDao.getAllUsers();
        printUserTable("CURRENT USER DIRECTORY", users);

        // 1. Display and prompt for user ID via wrapper [Image]
        int targetUserId = readIntInput("Enter target User ID to update (or enter 0 to cancel): ");
        if (targetUserId == 0) {
            System.out.println("↩️ Action canceled.");
            return;
        }

        // 2. Self-Edit Restriction verification [Image]
        if (currentAdmin != null && targetUserId == currentAdmin.id()) {
            System.out.println("DISPLAY:WARN Modifying the currently active admin account is restricted.");
            return;
        }

        // 3. Prompt for Role via input wrapper [Image]
        String newRole = readValidRole();
        if (newRole.equals("0")) {
            System.out.println("↩️ Action canceled.");
            return;
        }

        // 4. Prompt for Status via input wrapper [Image]
        String newStatus = readValidStatus();
        if (newStatus.equals("0")) {
            System.out.println("↩️ Action canceled.");
            return;
        }

        // Run persistence execution [Image]
        userDao.updateUserRoleAndStatus(targetUserId, newRole, newStatus);
    }

    private void printUserTable(String title, List<User> users) {
        System.out.println("\n--- " + title + " ---");
        if (users.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        String format = "| %-6s | %-18s | %-22s | %-12s |%n";
        String border = "+--------+--------------------+------------------------+--------------+";

        System.out.println(border);
        System.out.printf(format, "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(border);

        for (User u : users) {
            System.out.printf(format, u.id(), u.username(), u.role(), u.status());
        }
        System.out.println(border);
    }

    // --- Input Validation Wrappers ---

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Non-numeric inputs detected. Please provide a valid numerical ID.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.println("\nAvailable System Roles:");
            System.out.println(" - CASHIER");
            System.out.println(" - KITCHEN STAFF");
            System.out.println(" - RESTAURANT MANAGER");
            System.out.print("Enter target role [or enter 0 to cancel]: ");
            String input = sc.nextLine().trim().toUpperCase();

            if (input.equals("0")) {
                return "0";
            }

            if (input.equals("CASHIER") || input.equals("KITCHEN STAFF") || input.equals("RESTAURANT MANAGER")) {
                return input;
            }

            System.out.println("❌ Validation error: Accepted roles are restricted to CASHIER, KITCHEN STAFF, or RESTAURANT MANAGER.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("\nEnter target status (APPROVED, PENDING) [or enter 0 to cancel]: ");
            String input = sc.nextLine().trim().toUpperCase();

            if (input.equals("0")) {
                return "0";
            }

            if (input.equals("APPROVED") || input.equals("PENDING")) {
                return input;
            }

            System.out.println("❌ Validation error: Accepted statuses are restricted to APPROVED or PENDING.");
        }
    }
}
