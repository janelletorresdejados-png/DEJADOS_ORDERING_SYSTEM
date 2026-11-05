package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final ActivityLogDao logDao = new ActivityLogDao();
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
            System.out.println("3. Update User Role and Status");
            System.out.println("4. Delete User Account");
            System.out.println("5. View System Audit Logs");
            System.out.println("6. Return to Main Dashboard");
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
                    handleUserUpdateFeature();
                    break;
                case "4":
                    handleUserDeletionFeature();
                    break;
                case "5":
                    handleAuditLogsMenu();
                    break;
                case "6":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose between 1 and 6.");
            }
        }
    }

    private void handleAuditLogsMenu() {
        boolean inAuditMenu = true;
        while (inAuditMenu) {
            System.out.println("\n--- SYSTEM AUDIT LOGS MENU ---");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs Safely");
            System.out.println("4. Back to Admin Menu");
            System.out.print("Choose action: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    printAuditLogTable(logDao.getAllLogs());
                    break;
                case "2":
                    int targetId = readIntInput("Enter User ID to filter logs: ");
                    printAuditLogTable(logDao.getLogsByUserId(targetId));
                    break;
                case "3":
                    System.out.print("⚠️ Are you sure you want to clear all historical audit logs? (yes/no): ");
                    String confirm = sc.nextLine().trim();
                    if (confirm.equalsIgnoreCase("yes")) {
                        logDao.clearLogs();
                    } else {
                        System.out.println("↩️ Action canceled.");
                    }
                    break;
                case "4":
                    inAuditMenu = false;
                    break;
                default:
                    System.out.println(" Invalid choice.");
            }
        }
    }

    private void printAuditLogTable(List<ActivityLog> logs) {
        System.out.println("\n--- SYSTEM AUDIT LOGS ---");
        if (logs.isEmpty()) {
            System.out.println("No activity logs found.");
            return;
        }

        String format = "| %-5s | %-8s | %-18s | %-45s | %-20s |%n";
        String border = "+-------+----------+--------------------+-----------------------------------------------+----------------------+";

        System.out.println(border);
        System.out.printf(format, "ID", "USER ID", "ACTION TYPE", "DESCRIPTION", "TIMESTAMP");
        System.out.println(border);

        for (ActivityLog log : logs) {
            System.out.printf(format,
                    log.logId(),
                    log.userId(),
                    log.actionType(),
                    log.description(),
                    log.timestamp());
        }
        System.out.println(border);
    }

    private void viewAllUsers() {
        List<User> users = userDao.getAllUsers();
        printUserTable("ALL SYSTEM USERS", users);
    }

    private void viewPendingRegistrations() {
        List<User> users = userDao.getPendingUsers();
        printUserTable("PENDING REGISTRATIONS", users);
    }

    private void handleUserUpdateFeature() {
        List<User> users = userDao.getAllUsers();
        printUserTable("CURRENT USER DIRECTORY", users);

        int targetUserId = readIntInput("Enter target User ID to update (or enter 0 to cancel): ");
        if (targetUserId == 0) {
            System.out.println("↩️ Action canceled.");
            return;
        }

        if (currentAdmin != null && targetUserId == currentAdmin.id()) {
            System.out.println("⚠️ Modifying the currently active admin account is restricted.");
            return;
        }

        String newRole = readValidRole();
        if (newRole.equals("0")) {
            System.out.println("↩️ Action canceled.");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus.equals("0")) {
            System.out.println("↩️ Action canceled.");
            return;
        }

        int adminId = currentAdmin != null ? currentAdmin.id() : -1;
        userDao.updateUserRoleAndStatus(adminId, targetUserId, newRole, newStatus);
    }

    private void handleUserDeletionFeature() {
        List<User> users = userDao.getAllUsers();
        printUserTable("CURRENT USER DIRECTORY", users);

        int targetUserId = readIntInput("Enter target User ID to delete (or enter 0 to cancel): ");
        if (targetUserId == 0) {
            System.out.println("↩️ Action canceled.");
            return;
        }

        if (currentAdmin != null && targetUserId == currentAdmin.id()) {
            System.out.println("⚠️ Deleting the currently active admin account is strictly restricted.");
            return;
        }

        System.out.print("⚠️ Are you sure you want to permanently delete User ID " + targetUserId + "? (yes/no): ");
        String confirm = sc.nextLine().trim();
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("↩️ Deletion canceled.");
            return;
        }

        int adminId = currentAdmin != null ? currentAdmin.id() : -1;
        boolean success = userDao.deleteUser(adminId, targetUserId);
        if (success) {
            System.out.println("User ID " + targetUserId + " successfully deleted from the system.");
        } else {
            System.out.println(" Failed to delete user.");
        }
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

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Non-numeric inputs detected. Please provide a valid numerical ID.");
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

            if (input.equals("0")) return "0";
            if (input.equals("CASHIER") || input.equals("KITCHEN STAFF") || input.equals("RESTAURANT MANAGER")) {
                return input;
            }
            System.out.println(" Validation error: Accepted roles are restricted.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("\nEnter target status (APPROVED, PENDING) [or enter 0 to cancel]: ");
            String input = sc.nextLine().trim().toUpperCase();

            if (input.equals("0")) return "0";
            if (input.equals("APPROVED") || input.equals("PENDING")) {
                return input;
            }
            System.out.println(" Validation error: Accepted statuses are restricted to APPROVED or PENDING.");
        }
    }
}