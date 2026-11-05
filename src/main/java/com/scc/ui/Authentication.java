package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {
    private final UserDao userDao;
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final Scanner sc;

    public Authentication(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.sc = sc;
    }

    public User login() {
        System.out.println("\n--- USER LOGIN ---");
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String password = sc.nextLine().trim();

        User user = userDao.loginUser(username, password);
        if (user != null) {
            logDao.logAction(user.id(), "USER_LOGIN",
                    "User " + user.username() + " (ID: " + user.id() + ") logged in.");
            System.out.println("\nAuthentication successful!");
            return user;
        } else {
            System.out.println("\n Login failed: Account is pending approval, inactive, or credentials are invalid.");
            return null;
        }
    }

    public void logout(User user) {
        if (user != null) {
            logDao.logAction(user.id(), "USER_LOGOUT",
                    "User " + user.username() + " (ID: " + user.id() + ") logged out.");
        }
    }

    public void register() {
        System.out.println("\n--- USER REGISTRATION ---");
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String password = sc.nextLine().trim();

        System.out.println("Select Role:");
        System.out.println("1. Cashier");
        System.out.println("2. Kitchen Staff");
        System.out.println("3. Restaurant Manager");
        System.out.print("Choose role option: ");
        String roleChoice = sc.nextLine().trim();

        String role = switch (roleChoice) {
            case "1" -> "CASHIER";
            case "2" -> "KITCHEN STAFF";
            case "3" -> "RESTAURANT MANAGER";
            default -> "CASHIER";
        };

        boolean success = userDao.registerUser(username, password, role);
        if (success) {
            System.out.println(" Registration submitted successfully! Awaiting manager approval.");
        } else {
            System.out.println(" Registration failed. Username may already exist.");
        }
    }
}