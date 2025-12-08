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

    // LOGIN VALIDATION
    public static User validasiLogin(String email, String password, String role) {
        User u = null;
        try {
            con = connect();
            String query = "SELECT idUser, nama, email, password, role FROM users WHERE email = ? AND password = ? AND role = ?";

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
                        rs.getString("role")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return u;
    }

    // REGISTER USER
    public static void registrasiUser(User user) throws SQLException {

        String query = "INSERT INTO users (idUser, email, password, role, status, nama) VALUES (?, ?, ?, ?, ?, ?)";

        // 2. Tentukan Data Default
        String defaultStatus = "Aktif";

        // Ambil nama dari email (sebelum @) sebagai nama sementara,
        // atau set string kosong jika DB mengizinkan.
        String tempName = user.getEmail().split("@")[0];

        // 3. Eksekusi menggunakan DatabaseManager
        // Pastikan urutan parameter sesuai dengan tanda tanya (?) di query
        int result = DatabaseManager.executeUpdate(query,
                user.getIdUser(),
                user.getEmail(),
                user.getPass(),
                user.getRole(),
                defaultStatus,
                tempName
        );

        // 4. Cek apakah data benar-benar masuk
        if (result == 0) {
            throw new SQLException("Gagal menyimpan data ke database (0 baris terpengaruh).");
        }
    }
}
