package com.scc.dao;

import com.scc.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    public void registerUser(String username, String password, String role) {
        String sql = "INSERT INTO users (username, password, role, status) VALUES (?, ?, ?, 'Active')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("\n🎉 User profile successfully registered!");
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("\n❌ Error: Username '" + username + "' is already taken.");
            } else {
                System.out.println("\n❌ Database Error: " + e.getMessage());
            }
        }
    }

    public User loginUser(String username, String password) {
        String sql = "SELECT user_id, username, role, status FROM users WHERE username = ? AND password = ? AND status = 'Active'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("user_id");
                    String user = rs.getString("username");
                    String role = rs.getString("role");
                    String status = rs.getString("status");

                    User loggedInUser = new User(id, user, role, status);
                    System.out.println("\n🔓 Login Successful! " + loggedInUser);
                    return loggedInUser;
                } else {
                    System.out.println("\n❌ Invalid username, password, or account is inactive.");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("\n❌ Database Login Error: " + e.getMessage());
            return null;
        }
    }
}
