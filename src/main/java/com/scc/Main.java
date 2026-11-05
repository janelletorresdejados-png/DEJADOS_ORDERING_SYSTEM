package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.AdminMenu;
import com.scc.ui.Authentication;
import com.scc.ui.Dashboard;
import com.scc.model.User;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Initialize SQLite Database Tables
        DatabaseInitializer.initialize();

        Scanner sc = new Scanner(System.in);
        UserDao userDao = new UserDao();
        Authentication auth = new Authentication(userDao, sc);
        Dashboard dashboard = new Dashboard(auth);
        AdminMenu adminMenu = new AdminMenu(userDao, sc);

        boolean running = true;
        while (running) {
            System.out.println("\n=================================");
            System.out.println("  RESTAURANT ORDERING MANAGEMENT  ");
            System.out.println("=================================");
            System.out.println("1. Log-in");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    User loggedInUser = auth.login();
                    if (loggedInUser != null) {
                        if (loggedInUser.role().equalsIgnoreCase("RESTAURANT MANAGER")) {
                            adminMenu.displayMenu(loggedInUser);
                            auth.logout(loggedInUser);
                        } else {
                            dashboard.displayDashboard(loggedInUser);
                        }
                    }
                    break;
                case "2":
                    auth.register();
                    break;
                case "3":
                    System.out.println("\nExiting system. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("❌ Invalid option. Please select 1, 2, or 3.");
            }
        }
        sc.close();
    }
}