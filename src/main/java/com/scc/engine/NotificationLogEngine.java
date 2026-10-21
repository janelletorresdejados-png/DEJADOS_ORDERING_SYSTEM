package com.scc.engine;

import com.scc.dao.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationLogEngine {

    private static NotificationLogEngine instance;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private NotificationLogEngine() {}

    public static synchronized NotificationLogEngine getInstance() {
        if (instance == null) {
            instance = new NotificationLogEngine();
        }
        return instance;
    }

    public synchronized void logDispatch(String eventType, String customerName, String contactChannel, String payloadDetails) {
        String timestamp = LocalDateTime.now().format(dtf);

        // 1. Existing Console Output
        System.out.println("\n📡 [DISPATCHED NOTIFICATION LOG ENGINE] | " + timestamp);
        System.out.println("   🔹 EVENT TYPE : " + eventType);
        System.out.println("   🔹 CUSTOMER   : " + customerName);
        System.out.println("   🔹 CHANNEL    : " + contactChannel.toUpperCase());

        if (contactChannel.equalsIgnoreCase("SMS") || contactChannel.equalsIgnoreCase("Both")) {
            System.out.println("   📱 [SMS Payload Outbound] -> To: " + customerName + " | Contents: \"" + payloadDetails + "\"");
        }
        if (contactChannel.equalsIgnoreCase("Email") || contactChannel.equalsIgnoreCase("Both")) {
            System.out.println("   ✉️ [Email Payload Outbound] -> To: " + customerName.toLowerCase().replaceAll("\\s+", "") + "@restaurant-connect.com | Subject: Order Update | Contents: \"" + payloadDetails + "\"");
        }
        System.out.println("   ─────────────────────────────────────────────────────────────────");

        // 2. NEW: Save permanently to SQLite database table
        String sql = "INSERT INTO Notification_Logs (event_type, customer_name, contact_channel, message_payload) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, eventType);
            pstmt.setString(2, customerName);
            pstmt.setString(3, contactChannel);
            pstmt.setString(4, payloadDetails);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Failed to commit notification log record to SQLite database: " + e.getMessage());
        }
    }
}
