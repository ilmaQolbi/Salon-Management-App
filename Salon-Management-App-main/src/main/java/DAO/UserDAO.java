package DAO;

import static DAO.BaseDAO.closeCon;
import static DAO.BaseDAO.getCon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Model.User;

public class UserDAO {

    private static PreparedStatement st;
    private static Connection con;

    // LOGIN VALIDATION
    public static User validate(String email, String password, String role) {
        User u = null;
        try {
            con = getCon();
            String query = "SELECT idUser, email, password, role FROM users WHERE email = ? AND password = ? AND role = ?";

            st = con.prepareStatement(query);
            st.setString(1, email);
            st.setString(2, password);
            st.setString(3, role);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                // sesuaikan dengan constructor User-mu
                u = new User(
                        rs.getString("idUser"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeCon(con);
        }
        return u;
    }
    // SEARCH USER BY ID (STRING)
    public static User searchByUid(String idUser) {
        User u = null;
        try {
            con = getCon();
            String query = "SELECT * FROM users WHERE idUser = ?";

            st = con.prepareStatement(query);
            st.setString(1, idUser);

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                u = new User(
                    rs.getString("idUser"),
                    rs.getString("email"),
                    rs.getString("password"),
                    "user" // default role
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeCon(con);
        }
        return u;
    }

    // REGISTER USER
    public static void registerUser(User user) throws SQLException {

        // 1. Siapkan Query
        // Kita perlu mengisi kolom 'nama' dan 'status' secara default
        // karena di form register belum ada input nama.
        String query = "INSERT INTO users (idUser, email, password, role, status, nama) VALUES (?, ?, ?, ?, ?, ?)";

        // 2. Tentukan Data Default
        String defaultStatus = "Aktif";

        // Ambil nama dari email (sebelum @) sebagai nama sementara,
        // atau set string kosong jika DB mengizinkan.
        String tempName = user.getEmail().split("@")[0];

        // 3. Eksekusi menggunakan DatabaseManager
        // Pastikan urutan parameter sesuai dengan tanda tanya (?) di query
        int result = DatabaseManager.executeUpdate(query,
                user.getIdUser(),   // 1. idUser (dari UUID di controller)
                user.getEmail(),    // 2. email
                user.getPass(), // 3. password
                user.getRole(),     // 4. role (biasanya "Karyawan")
                defaultStatus,      // 5. status
                tempName            // 6. nama (Wajib diisi supaya tidak error SQL)
        );

        // 4. Cek apakah data benar-benar masuk
        if (result == 0) {
            throw new SQLException("Gagal menyimpan data ke database (0 baris terpengaruh).");
        }
    }
}
