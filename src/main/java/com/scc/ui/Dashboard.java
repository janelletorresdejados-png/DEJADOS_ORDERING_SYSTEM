package com.scc.ui;

import com.scc.model.User;
import com.scc.dao.DatabaseConnection;
import com.scc.engine.NotificationLogEngine;

import java.sql.*;
import java.util.Scanner;

public class Dashboard {
    private final Scanner sc = new Scanner(System.in);
    private final NotificationLogEngine logEngine = NotificationLogEngine.getInstance();

    public void displayDashboard(User user) {
        boolean sessionActive = true;
        String role = user.role();

        while (sessionActive) {
            System.out.println("\n=================================");
            System.out.format("  %s DASHBOARD CONTROL CENTRE\n", role.toUpperCase());
            System.out.format("  Active Operator: %s [ID: %d]\n", user.username(), user.id());
            System.out.println("=================================");

            switch (role) {
                case "Cashier":
                    sessionActive = showCashierMenu();
                    break;
                case "Kitchen Staff":
                    sessionActive = showKitchenMenu();
                    break;
                case "Restaurant Manager":
                    sessionActive = showManagerMenu();
                    break;
                default:
                    System.out.println("⚠️ Unknown permission clearance profile detected. Forcing termination.");
                    sessionActive = false;
            }
        }
    }

    private boolean showCashierMenu() {
        System.out.println("1. Take New Order Intake (Triggers Booking Log)");
        System.out.println("2. Lookup Customer Information Record");
        System.out.println("3. Log-out / Return to Base Core");
        System.out.print("Choose action: ");
        String selection = sc.nextLine().trim();

        switch (selection) {
            case "1":
                processNewOrder();
                break;
            case "2":
                displayCustomers();
                break;
            case "3":
                System.out.println("\n🔒 Securely terminating cashier operator terminal token.");
                return false;
            default:
                System.out.println("❌ Invalid terminal operational code.");
        }
        return true;
    }

    private boolean showKitchenMenu() {
        System.out.println("1. View Active Food Tickets");
        System.out.println("2. Update Order Production Status (Triggers Transition Log)");
        System.out.println("3. Log-out / Return to Base Core");
        System.out.print("Choose action: ");
        String selection = sc.nextLine().trim();

        switch (selection) {
            case "1":
                displayActiveTickets();
                break;
            case "2":
                processKitchenTransition();
                break;
            case "3":
                System.out.println("\n🔒 Securely terminating kitchen terminal token.");
                return false;
            default:
                System.out.println("❌ Invalid terminal operational code.");
        }
        return true;
    }

    private boolean showManagerMenu() {
        System.out.println("1. Run Daily Financial Revenue Logs");
        System.out.println("2. Manage Active System User Accounts");
        System.out.println("3. Audit Menu Catalog Unit Costs");
        System.out.println("4. Log-out / Return to Base Core");
        System.out.print("Choose action: ");
        String selection = sc.nextLine().trim();

        switch (selection) {
            case "1":
                System.out.println("\n[Analytics]: Compiling operational analytics pipeline...");
                break;
            case "2":
                System.out.println("\n[Admin]: Opening user profile override panels...");
                break;
            case "3":
                System.out.println("\n[Catalog]: Loading item production budget matrix...");
                break;
            case "4":
                System.out.println("\n🔒 Securely terminating master administration session token.");
                return false;
            default:
                System.out.println("❌ Invalid terminal operational code.");
        }
        return true;
    }

    private void processNewOrder() {
        System.out.println("\n--- NEW ORDER WIZARD ---");
        System.out.print("Enter Customer Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Enter Contact Channel (SMS / Email / Both): ");
        String channel = sc.nextLine().trim();
        System.out.print("Enter Meal Category: ");
        String category = sc.nextLine().trim();
        System.out.print("Enter Total Bill Cost: ");
        double bill = 0.0;
        try { bill = Double.parseDouble(sc.nextLine().trim()); } catch(Exception e) { bill = 25.0; }

        String insertCustomer = "INSERT INTO Customers(customer_name, contact_channel) VALUES(?, ?)";
        String insertOrder = "INSERT INTO Orders(customer_id, meal_category, base_cost, total_bill, prep_time_minutes) VALUES(?, ?, ?, ?, 20)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psCust = conn.prepareStatement(insertCustomer, Statement.RETURN_GENERATED_KEYS)) {

            psCust.setString(1, name);
            psCust.setString(2, channel);
            psCust.executeUpdate();

            try (ResultSet rs = psCust.getGeneratedKeys()) {
                if (rs.next()) {
                    int customerId = rs.getInt(1);
                    try (PreparedStatement psOrd = conn.prepareStatement(insertOrder)) {
                        psOrd.setInt(1, customerId);
                        psOrd.setString(2, category);
                        psOrd.setDouble(3, bill);
                        psOrd.setDouble(4, bill);
                        psOrd.executeUpdate();
                    }
                }
            }
            System.out.println("💾 Transaction committed smoothly into SQLite schema core.");

            logEngine.logDispatch(
                    "ORDER_BOOKED",
                    name,
                    channel,
                    "Success! Your order for " + category + " has been booked. Total: $" + bill
            );

        } catch (SQLException e) {
            System.out.println("❌ Order Booking SQL Error: " + e.getMessage());
        }
    }

    private void processKitchenTransition() {
        System.out.println("\n--- UPDATE PRODUCTION STATE ---");
        displayActiveTickets();
        System.out.print("\nEnter Customer Target ID to dispatch updates for: ");
        int inputId = 0;
        try { inputId = Integer.parseInt(sc.nextLine().trim()); } catch(Exception e) { return; }

        System.out.println("Select New Kitchen Progress Stage:");
        System.out.println("1. PREPARING | 2. COOKING | 3. READY FOR PICKUP");
        System.out.print("Select choice: ");
        String stageChoice = sc.nextLine().trim();
        String state = switch (stageChoice) {
            case "1" -> "PREPARING";
            case "2" -> "COOKING";
            case "3" -> "READY FOR PICKUP";
            default -> "PROCESSING";
        };

        String query = "SELECT customer_name, contact_channel FROM Customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, inputId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("customer_name");
                    String channel = rs.getString("contact_channel");

                    logEngine.logDispatch(
                            "KITCHEN_STATE_TRANSITION",
                            name,
                            channel,
                            "Production Update Alert: Your meal tracking status has advanced to [" + state + "]."
                    );
                } else {
                    System.out.println("❌ Customer reference ID record missing.");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ State Change SQL Error: " + e.getMessage());
        }
    }

    private void displayCustomers() {
        String sql = "SELECT * FROM Customers";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- CUSTOMER REPOSITORY DIRECTORY ---");
            while (rs.next()) {
                System.out.format("ID: %d | Name: %s | Notification Preferences: %s\n",
                        rs.getInt("customer_id"), rs.getString("customer_name"), rs.getString("contact_channel"));
            }
        } catch (SQLException e) { System.out.println("❌ Query error: " + e.getMessage()); }
    }

    private void displayActiveTickets() {
        String sql = "SELECT c.customer_id, c.customer_name, o.meal_category, o.total_bill FROM Customers c JOIN Orders o ON c.customer_id = o.customer_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- ACTIVE PRODUCTION QUEUE TICKETS ---");
            while (rs.next()) {
                System.out.format("Cust ID: %d | Customer: %s | Category: %s | Bill: $%.2f\n",
                        rs.getInt("customer_id"), rs.getString("customer_name"), rs.getString("meal_category"), rs.getDouble("total_bill"));
            }
        } catch (SQLException e) { System.out.println("❌ Query error: " + e.getMessage()); }
    }
}