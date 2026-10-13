package com.scc.ui;

import com.scc.model.User;
import java.util.Scanner;

public class Dashboard {
    private final Scanner sc = new Scanner(System.in);

    /**
     * Entry control station that routes the logged-in profile to their specific access views.
     */
    public void displayDashboard(User user) {
        boolean sessionActive = true;

        while (sessionActive) {
            System.out.println("\n=================================");
            System.out.format("  %s DASHBOARD CONTROL CENTRE\n", user.role().toUpperCase());
            System.out.format("  Active Operator: %s [ID: %d]\n", user.username(), user.id());
            System.out.println("=================================");

            // Dynamically structuralize selections based strictly on the credential metadata
            switch (user.role()) {
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
        System.out.println("1. Take New Order Intake");
        System.out.println("2. Lookup Customer Information Record");
        System.out.println("3. Log-out / Return to Base Core");
        System.out.print("Choose action: ");
        String selection = sc.nextLine();

        switch (selection) {
            case "1":
                System.out.println("\n[Action Initiated]: Launching order wizard workflow...");
                break;
            case "2":
                System.out.println("\n[Action Initiated]: Initializing customer data indexing console...");
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
        System.out.println("2. Update Order Production Status");
        System.out.println("3. Log-out / Return to Base Core");
        System.out.print("Choose action: ");
        String selection = sc.nextLine();

        switch (selection) {
            case "1":
                System.out.println("\n[Action Initiated]: Gathering pending ticket queue...");
                break;
            case "2":
                System.out.println("\n[Action Initiated]: Pulling meal assembly tracker module...");
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
        String selection = sc.nextLine();

        switch (selection) {
            case "1":
                System.out.println("\n[Action Initiated]: Compiling operational analytics pipeline...");
                break;
            case "2":
                System.out.println("\n[Action Initiated]: Opening user profile override panels...");
                break;
            case "3":
                System.out.println("\n[Action Initiated]: Loading item production budget matrix...");
                break;
            case "4":
                System.out.println("\n🔒 Securely terminating master administration session token.");
                return false;
            default:
                System.out.println("❌ Invalid terminal operational code.");
        }
        return true;
    }
}
