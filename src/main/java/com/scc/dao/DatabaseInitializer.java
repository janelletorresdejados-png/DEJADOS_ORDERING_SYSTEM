package com.scc.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void initialize() {
        String[] tables = {
                """
           CREATE TABLE IF NOT EXISTS User (
               user_id    INTEGER PRIMARY KEY AUTOINCREMENT,
               username   TEXT NOT NULL UNIQUE,
               password   TEXT NOT NULL,
               role       TEXT NOT NULL,
               status     TEXT NOT NULL DEFAULT 'Pending',
               created_at DATETIME DEFAULT CURRENT_TIMESTAMP
           );   
           """,
                """
           CREATE TABLE IF NOT EXISTS Customers (
               customer_id     INTEGER PRIMARY KEY AUTOINCREMENT,
               customer_name   TEXT NOT NULL,
               contact_channel TEXT NOT NULL
           );
           """,
                """
           CREATE TABLE IF NOT EXISTS Orders (
               order_id          INTEGER PRIMARY KEY AUTOINCREMENT,
               customer_id       INTEGER,
               meal_category     TEXT NOT NULL,
               base_cost         REAL NOT NULL,
               priority_fee      REAL DEFAULT 0.0,
               total_bill        REAL NOT NULL,
               prep_time_minutes INTEGER NOT NULL,
               FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
           );
           """,
                """
           CREATE TABLE IF NOT EXISTS Menu_Catalog (
               menu_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
               category     TEXT NOT NULL,
               title        TEXT NOT NULL UNIQUE,
               item_cost    REAL NOT NULL
           );
           """,
                """
           CREATE TABLE IF NOT EXISTS Order_Details (
               order_detail_id INTEGER PRIMARY KEY AUTOINCREMENT,
               order_id        INTEGER NOT NULL,
               menu_item_id    INTEGER NOT NULL,
               quantity        INTEGER NOT NULL,
               subtotal_cost   REAL NOT NULL,
               FOREIGN KEY (order_id) REFERENCES Orders(order_id),
               FOREIGN KEY (menu_item_id) REFERENCES Menu_Catalog(menu_item_id)
           );
           """,
                """
           CREATE TABLE IF NOT EXISTS Notification_Logs (
               log_id          INTEGER PRIMARY KEY AUTOINCREMENT,
               event_type      TEXT NOT NULL,
               customer_name   TEXT NOT NULL,
               contact_channel TEXT NOT NULL,
               message_payload TEXT NOT NULL,
               dispatched_at   DATETIME DEFAULT CURRENT_TIMESTAMP
           );
           """,
                """
           CREATE TABLE IF NOT EXISTS activity_logs (
               log_id      INTEGER PRIMARY KEY AUTOINCREMENT,
               user_id     INTEGER,
               action_type TEXT NOT NULL,
               description TEXT NOT NULL,
               timestamp   DATETIME DEFAULT CURRENT_TIMESTAMP
           );
           """
        };

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String tableSql : tables) {
                stmt.execute(tableSql);
            }
            seedDefaultMenu(stmt);
            System.out.println("-> Complete Database Architecture verified successfully.");

        } catch (SQLException e) {
            System.err.println("❌ Critical Error during database initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedDefaultMenu(Statement stmt) throws SQLException {
        stmt.execute("INSERT OR IGNORE INTO Menu_Catalog (category, title, item_cost) VALUES ('Main Course', 'Cheeseburger Combo', 12.50);");
        stmt.execute("INSERT OR IGNORE INTO Menu_Catalog (category, title, item_cost) VALUES ('Main Course', 'Crispy Chicken Sandwich', 10.00);");
        stmt.execute("INSERT OR IGNORE INTO Menu_Catalog (category, title, item_cost) VALUES ('Beverage', 'Craft Soda', 3.50);");
        stmt.execute("INSERT OR IGNORE INTO Menu_Catalog (category, title, item_cost) VALUES ('Beverage', 'Iced Lemon Tea', 3.00);");
        stmt.execute("INSERT OR IGNORE INTO Menu_Catalog (category, title, item_cost) VALUES ('Dessert', 'Chocolate Fudge Brownie', 5.00);");
        stmt.execute("INSERT OR IGNORE INTO User (user_id, username, password, role, status) VALUES (1, 'admin', 'admin', 'RESTAURANT MANAGER', 'Approved');");
    }
}