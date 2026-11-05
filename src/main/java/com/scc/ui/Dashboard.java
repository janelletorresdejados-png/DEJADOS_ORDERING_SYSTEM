package com.scc.ui;

import com.scc.model.User;
import com.scc.dao.DatabaseConnection;
import com.scc.dao.ActivityLogDao;
import com.scc.engine.NotificationLogEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Dashboard {
    private final Scanner sc = new Scanner(System.in);
    private final NotificationLogEngine logEngine = NotificationLogEngine.getInstance();
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final Authentication auth;
    private User currentUser;

    public Dashboard(Authentication auth) {
        this.auth = auth;
    }

    public void displayDashboard(User user) {
        this.currentUser = user;
        boolean sessionActive = true;
        String role = user.role();

        while (sessionActive) {
            System.out.println("\n=================================");
            System.out.format("  %s DASHBOARD CONTROL CENTRE\n", role.toUpperCase());
            System.out.format("  Active Operator: %s [ID: %d]\n", user.username(), user.id());
            System.out.println("=================================");

            switch (role.toUpperCase()) {
                case "CASHIER":
                    sessionActive = showCashierMenu();
                    break;
                case "KITCHEN STAFF":
                    sessionActive = showKitchenMenu();
                    break;
                case "RESTAURANT MANAGER":
                    sessionActive = showManagerMenu();
                    break;
                default:
                    System.out.println("⚠️ Unknown permission clearance profile detected. Forcing termination.");
                    sessionActive = false;
            }
        }
    }

    private boolean showCashierMenu() {
        System.out.println("1. Take New Order Intake & Print Receipt (Triggers Booking Log)");
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
                auth.logout(currentUser);
                System.out.println("\n Securely terminating cashier operator terminal token.");
                return false;
            default:
                System.out.println(" Invalid terminal operational code.");
        }
        return true;
    }

    private boolean showKitchenMenu() {
        System.out.println("1. View Active Food Tickets & Kitchen Queue");
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
                auth.logout(currentUser);
                System.out.println("\n Securely terminating kitchen terminal token.");
                return false;
            default:
                System.out.println(" Invalid terminal operational code.");
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
                auth.logout(currentUser);
                System.out.println("\n Securely terminating master administration session token.");
                return false;
            default:
                System.out.println(" Invalid terminal operational code.");
        }
        return true;
    }

    private void processNewOrder() {
        System.out.println("\n--- NEW ORDER WIZARD & ITEM SELECTION ---");
        System.out.print("Enter Customer Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Enter Contact Channel (SMS / Email / Both): ");
        String channel = sc.nextLine().trim();

        List<MenuItemSelection> cart = new ArrayList<>();
        boolean ordering = true;

        while (ordering) {
            System.out.println("\n--- MENU CATALOG ---");
            String catalogSql = "SELECT * FROM Menu_Catalog";
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(catalogSql)) {
                while (rs.next()) {
                    System.out.printf("[%d] %s (%s) - $%.2f\n",
                            rs.getInt("menu_item_id"),
                            rs.getString("title"),
                            rs.getString("category"),
                            rs.getDouble("item_cost"));
                }
            } catch (SQLException e) {
                System.out.println(" Error loading catalog: " + e.getMessage());
                return;
            }

            System.out.print("\nEnter Menu Item ID to add to order (or 0 to finish cart): ");
            int itemId = 0;
            try { itemId = Integer.parseInt(sc.nextLine().trim()); } catch(Exception e) { continue; }

            if (itemId == 0) {
                if (cart.isEmpty()) {
                    System.out.println("⚠️ Cart is empty. Add at least one item.");
                    continue;
                }
                break;
            }

            System.out.print("Enter Quantity: ");
            int qty = 1;
            try { qty = Integer.parseInt(sc.nextLine().trim()); } catch(Exception e) { qty = 1; }

            String fetchItem = "SELECT title, item_cost, category FROM Menu_Catalog WHERE menu_item_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(fetchItem)) {
                pstmt.setInt(1, itemId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String title = rs.getString("title");
                        double cost = rs.getDouble("item_cost");
                        String category = rs.getString("category");
                        cart.add(new MenuItemSelection(itemId, title, category, cost, qty));
                        System.out.println(" Added " + qty + "x " + title + " to order.");
                    } else {
                        System.out.println(" Invalid Item ID.");
                    }
                }
            } catch (SQLException e) {
                System.out.println(" Database error: " + e.getMessage());
            }
        }

        double totalBill = cart.stream().mapToDouble(item -> item.cost * item.quantity).sum();
        String primaryCategory = cart.get(0).category;

        double cashTendered = 0.0;
        while (true) {
            System.out.printf("\n Total Bill Amount: $%.2f\n", totalBill);
            System.out.print("Enter Cash Tendered by Customer: $");
            try {
                cashTendered = Double.parseDouble(sc.nextLine().trim());
                if (cashTendered < totalBill) {
                    System.out.println(" Insufficient funds tendered. Cash must be at least $" + totalBill);
                    continue;
                }
                break;
            } catch (Exception e) {
                System.out.println(" Invalid amount format.");
            }
        }
        double change = cashTendered - totalBill;

        String insertCustomer = "INSERT INTO Customers(customer_name, contact_channel) VALUES(?, ?)";
        String insertOrder = "INSERT INTO Orders(customer_id, meal_category, base_cost, total_bill, prep_time_minutes) VALUES(?, ?, ?, ?, 20)";
        String insertDetail = "INSERT INTO Order_Details(order_id, menu_item_id, quantity, subtotal_cost) VALUES(?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int customerId;
            try (PreparedStatement psCust = conn.prepareStatement(insertCustomer, Statement.RETURN_GENERATED_KEYS)) {
                psCust.setString(1, name);
                psCust.setString(2, channel);
                psCust.executeUpdate();
                try (ResultSet rs = psCust.getGeneratedKeys()) {
                    if (rs.next()) customerId = rs.getInt(1);
                    else throw new SQLException("Failed to generate customer ID.");
                }
            }

            int orderId;
            try (PreparedStatement psOrd = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                psOrd.setInt(1, customerId);
                psOrd.setString(2, primaryCategory);
                psOrd.setDouble(3, totalBill);
                psOrd.setDouble(4, totalBill);
                psOrd.executeUpdate();
                try (ResultSet rs = psOrd.getGeneratedKeys()) {
                    if (rs.next()) orderId = rs.getInt(1);
                    else throw new SQLException("Failed to generate order ID.");
                }
            }

            try (PreparedStatement psDet = conn.prepareStatement(insertDetail)) {
                for (MenuItemSelection item : cart) {
                    psDet.setInt(1, orderId);
                    psDet.setInt(2, item.itemId);
                    psDet.setInt(3, item.quantity);
                    psDet.setDouble(4, item.cost * item.quantity);
                    psDet.addBatch();
                }
                psDet.executeBatch();
            }

            conn.commit();
            System.out.println("💾 Transaction committed successfully into SQLite schema core.");

            int currentUserId = currentUser != null ? currentUser.id() : -1;

            logDao.logAction(
                    currentUserId,
                    "BILL_PROCESSED",
                    "Processed sale for Customer " + name + " - Total: P" + String.format("%.2f", totalBill)
            );

            printReceipt(name, cart, totalBill, cashTendered, change);

            logEngine.logDispatch(
                    "ORDER_BOOKED",
                    name,
                    channel,
                    "Success! Your order has been booked. Total: $" + String.format("%.2f", totalBill)
            );

        } catch (SQLException e) {
            System.out.println("❌ Order Booking SQL Error: " + e.getMessage());
        }
    }

    private void printReceipt(String customerName, List<MenuItemSelection> cart, double total, double tendered, double change) {
        System.out.println("\n==================================================");
        System.out.println("             🍽️ RESTAURANT OFFICIAL RECEIPT        ");
        System.out.println("==================================================");
        System.out.println(" Customer Name : " + customerName);
        System.out.println(" --------------------------------------------------");
        System.out.format(" %-22s | %-5s | %-8s | %-8s\n", "ITEM", "QTY", "PRICE", "SUBTOTAL");
        System.out.println(" --------------------------------------------------");
        for (MenuItemSelection item : cart) {
            System.out.format(" %-22s | %-5d | $%-7.2f | $%-7.2f\n",
                    item.title.length() > 22 ? item.title.substring(0, 19) + "..." : item.title,
                    item.quantity,
                    item.cost,
                    (item.cost * item.quantity));
        }
        System.out.println(" --------------------------------------------------");
        System.out.format(" TOTAL BILL    : $%.2f\n", total);
        System.out.format(" CASH TENDERED : $%.2f\n", tendered);
        System.out.format(" CHANGE DUE    : $%.2f\n", change);
        System.out.println("==================================================");
        System.out.println("           🙏 THANK YOU FOR YOUR ORDER!           ");
        System.out.println("==================================================\n");
    }

    private void processKitchenTransition() {
        System.out.println("\n--- UPDATE PRODUCTION STATE ---");
        displayActiveTickets();
        System.out.print("\nEnter Order Target ID to dispatch updates for: ");
        int orderId = 0;
        try { orderId = Integer.parseInt(sc.nextLine().trim()); } catch(Exception e) { return; }

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

        String query = "SELECT c.customer_name, c.contact_channel FROM Orders o JOIN Customers c ON o.customer_id = c.customer_id WHERE o.order_id = ?";

        String customerName = null;
        String contactChannel = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    customerName = rs.getString("customer_name");
                    contactChannel = rs.getString("contact_channel");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ State Change SQL Error: " + e.getMessage());
            return;
        }

        if (customerName != null && contactChannel != null) {
            int currentUserId = currentUser != null ? currentUser.id() : -1;

            logDao.logAction(
                    currentUserId,
                    "STATUS_UPDATE",
                    "Updated Ticket #" + orderId + " status from PENDING to " + state
            );

            logEngine.logDispatch(
                    "KITCHEN_STATE_TRANSITION",
                    customerName,
                    contactChannel,
                    "Production Update Alert: Your meal tracking status has advanced to [" + state + "]."
            );
            System.out.println("✅ Kitchen transition notification dispatched successfully.");
        } else {
            System.out.println("❌ Order reference ID record missing.");
        }
    }

    private void displayCustomers() {
        String sql = "SELECT * FROM Customers";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- CUSTOMER REPOSITORY DIRECTORY ---");
            String format = "| %-6s | %-20s | %-30s |%n";
            String border = "+--------+----------------------+--------------------------------+";

            System.out.println(border);
            System.out.printf(format, "ID", "CUSTOMER NAME", "NOTIFICATION PREFERENCES");
            System.out.println(border);

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.printf(format,
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("contact_channel"));
            }

            if (!hasData) {
                System.out.println("| No customer records found.                                              |");
            }
            System.out.println(border);

        } catch (SQLException e) {
            System.out.println("❌ Query error: " + e.getMessage());
        }
    }

    private void displayActiveTickets() {
        String sql = "SELECT o.order_id, c.customer_name, o.meal_category, o.total_bill FROM Orders o JOIN Customers c ON o.customer_id = c.customer_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- ACTIVE PRODUCTION QUEUE TICKETS ---");
            while (rs.next()) {
                System.out.format("Order ID: %d | Customer: %s | Category: %s | Bill: $%.2f\n",
                        rs.getInt("order_id"), rs.getString("customer_name"), rs.getString("meal_category"), rs.getDouble("total_bill"));
            }
        } catch (SQLException e) { System.out.println("❌ Query error: " + e.getMessage()); }
    }

    private static class MenuItemSelection {
        int itemId;
        String title;
        String category;
        double cost;
        int quantity;

        public MenuItemSelection(int itemId, String title, String category, double cost, int quantity) {
            this.itemId = itemId;
            this.title = title;
            this.category = category;
            this.cost = cost;
            this.quantity = quantity;
        }
    }
}