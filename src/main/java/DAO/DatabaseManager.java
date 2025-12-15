package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    // nama database 'salon_db'
    private static final String URL = "jdbc:mysql://localhost:3306/salon_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

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

    // Method untuk memastikan kolom ada di tabel antrian_Pelanggan
    public static void ensureStatusColumn() {
        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {

            // Coba tambahkan kolom status jika belum ada
            String alterQuery = "ALTER TABLE antrian_Pelanggan ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'Menunggu'";
            stmt.executeUpdate(alterQuery);

            // Tambahkan kolom id_karyawan untuk menyimpan ID karyawan yang ditugaskan
            String alterQuery2 = "ALTER TABLE antrian_Pelanggan ADD COLUMN IF NOT EXISTS id_karyawan VARCHAR(20) DEFAULT NULL";
            stmt.executeUpdate(alterQuery2);

            System.out.println("Kolom 'status' dan 'id_karyawan' sudah dipastikan ada di tabel antrian_Pelanggan.");

        } catch (SQLException e) {
            // Kolom mungkin sudah ada atau syntax tidak didukung, abaikan error
            System.out.println("Catatan: " + e.getMessage());
        }
    }

}