package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    // Pastikan nama database 'salon_db'
    private static final String URL = "jdbc:mysql://localhost:3306/salon_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Kosongkan jika pakai XAMPP default

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Method untuk mengambil data (SELECT)
    public static ResultSet executeQuery(String query) {
        try {
            Connection conn = connect();
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method untuk tambah/hapus/edit (INSERT, UPDATE, DELETE)
    public static int executeUpdate(String query, Object... params) {
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}