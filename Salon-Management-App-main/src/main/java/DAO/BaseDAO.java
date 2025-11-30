package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BaseDAO {

    private static String DB_NAME = "salon_db";
    private static String DB_HOST = "localhost";
    private static String DB_USER = "root";
    private static String DB_PASS = ""; // kalau XAMPP-mu tanpa password

    public static Connection getCon() {
        Connection con = null;
        try {
            String url = "jdbc:mysql://" + DB_HOST + ":3306/" + DB_NAME
                    + "?useSSL=false&serverTimezone=UTC";
            con = DriverManager.getConnection(url, DB_USER, DB_PASS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }

    public static void closeCon(Connection con) {
        try {
            if (con != null) con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ⚠️ Tambahan untuk test
    public static void main(String[] args) {
        try (Connection con = getCon()) {
            if (con == null) {
                System.out.println("❌ KONEKSI GAGAL");
                return;
            }
            System.out.println("✅ KONEKSI BERHASIL");

            // coba baca data user
            String sql = "SELECT idUser, nama, email, role FROM users";
            try (PreparedStatement st = con.prepareStatement(sql);
                 ResultSet rs = st.executeQuery()) {

                System.out.println("Isi tabel users:");
                while (rs.next()) {
                    System.out.println(
                            rs.getString("idUser") + " | " +
                                    rs.getString("nama") + " | " +
                                    rs.getString("email") + " | " +
                                    rs.getString("role")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
