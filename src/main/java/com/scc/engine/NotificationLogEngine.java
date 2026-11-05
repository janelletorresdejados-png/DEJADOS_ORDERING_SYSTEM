package com.scc.engine;

import com.scc.dao.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationLogEngine {
    private static NotificationLogEngine instance;

    private NotificationLogEngine() {}

    public static synchronized NotificationLogEngine getInstance() {
        if (instance == null) {
            instance = new NotificationLogEngine();
        }
        return instance;
    }

    public void logDispatch(String eventType, String recipientName, String channel, String messageBody) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println("\n[DISPATCHED NOTIFICATION LOG ENGINE] | " + timestamp);
        System.out.println("   ♦ EVENT TYPE : " + eventType);
        System.out.println("   ♦ CUSTOMER   : " + recipientName);
        System.out.println("   ♦ CHANNEL    : " + channel);
        System.out.println("   --------------------------------------------------");
        System.out.println("   " + messageBody);
        System.out.println("--------------------------------------------------\n");

        String sql = "INSERT INTO Notification_Logs (event_type, customer_name, contact_channel, message_payload) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, eventType);
            pstmt.setString(2, recipientName);
            pstmt.setString(3, channel);
            pstmt.setString(4, messageBody);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(" Failed to commit notification log record to SQLite database: " + e.getMessage());
        }
    }
}