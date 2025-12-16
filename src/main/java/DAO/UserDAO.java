package DAO;

import static DAO.DatabaseManager.koneksi;
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

            con = koneksi();
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
        int result = DatabaseManager.eksekusiUpdate(query,
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
            con = koneksi();
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
        ResultSet rs = DatabaseManager.eksekusiQuery(query);
        try {
            while (rs != null && rs.next()) {
                list.add(new String[] { rs.getString("idUser"), rs.getString("nama") });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Mengambil semua data user dari database
     * 
     * @return List semua User
     */
    public static java.util.List<User> getAllUsers() {
        java.util.List<User> list = new java.util.ArrayList<>();
        String query = "SELECT * FROM users";
        ResultSet rs = DatabaseManager.eksekusiQuery(query);
        try {
            while (rs != null && rs.next()) {
                list.add(new User(
                        rs.getString("idUser"),
                        rs.getString("nama"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tambah user baru ke database
     * 
     * @return true jika berhasil
     */
    public static boolean tambahUser(User user) {
        String query = "INSERT INTO users (idUser, nama, email, password, role, status) VALUES (?, ?, ?, ?, ?, ?)";
        int result = DatabaseManager.eksekusiUpdate(query,
                user.getIdUser(), user.getNama(), user.getEmail(),
                user.getPassword(), user.getRole(), user.getStatus());
        return result > 0;
    }

    /**
     * Update data user
     * 
     * @return true jika berhasil
     */
    public static boolean updateUser(String idUser, String nama, String email, String password, String role,
            String status) {
        String query = "UPDATE users SET nama = ?, email = ?, password = ?, role = ?, status = ? WHERE idUser = ?";
        int result = DatabaseManager.eksekusiUpdate(query, nama, email, password, role, status, idUser);
        return result > 0;
    }

    /**
     * Hapus user berdasarkan ID
     * 
     * @return true jika berhasil
     */
    public static boolean hapusUser(String idUser) {
        String query = "DELETE FROM users WHERE idUser = ?";
        int result = DatabaseManager.eksekusiUpdate(query, idUser);
        return result > 0;
    }

    /**
     * Mengambil ID user pertama (fallback untuk foreign key)
     * 
     * @return ID user pertama atau "USR-001" jika tidak ada
     */
    public static String ambilUserIdPertama() {
        String query = "SELECT idUser FROM users LIMIT 1";
        ResultSet rs = DatabaseManager.eksekusiQuery(query);
        try {
            if (rs != null && rs.next()) {
                return rs.getString("idUser");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "USR-001";
    }
}
