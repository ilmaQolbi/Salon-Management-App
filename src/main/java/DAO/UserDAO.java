package DAO;

import static DAO.DatabaseManager.connect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Model.User;

public class UserDAO {

    private static PreparedStatement st;
    private static Connection con;

    // Validasi LOGIN
    public static User validasiLogin(String email, String password, String role) {
        User u = null;
        try {

            con = connect();
            String query = "SELECT idUser, nama, email, password, role, status FROM users WHERE email = ? AND password = ? AND role = ?";

            st = con.prepareStatement(query);
            st.setString(1, email);
            st.setString(2, password);
            st.setString(3, role);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                u = new User(
                        rs.getString("idUser"),
                        rs.getString("nama"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("status"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return u;
    }

    // Registrasi USER
    public static void registrasiUser(User user) throws SQLException {

        String query = "INSERT INTO users (idUser, email, password, role, status, nama) VALUES (?, ?, ?, ?, ?, ?)";

        // 2. Tentukan Data Default
        String defaultStatus = "Menunggu Persetujuan";

        String tempName = user.getEmail().split("@")[0];

        // 3. Eksekusi menggunakan DatabaseManage
        int result = DatabaseManager.executeUpdate(query,
                user.getIdUser(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                defaultStatus,
                tempName);

        // 4. Cek apakah data benar-benar masuk
        if (result == 0) {
            throw new SQLException("Gagal menyimpan data ke database (0 baris terpengaruh).");
        }
    }

    // UPDATE STATUS USER (Approve/Reject)
    public static boolean updateStatusUser(String idUser, String newStatus) {
        String query = "UPDATE users SET status = ? WHERE idUser = ?";
        try {
            con = connect();
            st = con.prepareStatement(query);
            st.setString(1, newStatus);
            st.setString(2, idUser);
            int rows = st.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // AMBIL KARYAWAN AKTIF (Id & Nama)
    public static java.util.List<String[]> getKaryawanAktif() {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        String query = "SELECT idUser, nama FROM users WHERE role = 'Karyawan' AND status = 'Aktif' ORDER BY nama";
        ResultSet rs = DatabaseManager.executeQuery(query);
        try {
            while (rs != null && rs.next()) {
                list.add(new String[] { rs.getString("idUser"), rs.getString("nama") });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
