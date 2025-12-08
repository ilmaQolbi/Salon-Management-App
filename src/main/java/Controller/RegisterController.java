package Controller;

import DAO.UserDAO;
import DAO.DatabaseManager;
import Model.User;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

public class RegisterController {

    @FXML
    private TextField tfEmail;
    @FXML
    private PasswordField tfPassword;
    @FXML
    private PasswordField tfConfirmPassword;
    @FXML
    private TextField tfNama;

    public void initialize() {
    }

    @FXML
    private void Registrasi(ActionEvent event) {
        try {
            String email = tfEmail.getText();
            String password = tfPassword.getText();
            String konfirmasiPassword = tfConfirmPassword.getText();
            String nama = tfNama.getText();


            // --- VALIDASI INPUT ---
            if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Semua field harus diisi!");
                return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(null, "Format email tidak valid!");
                return;
            }
            if (!password.equals(konfirmasiPassword)) {
                JOptionPane.showMessageDialog(null, "Password dan konfirmasi tidak cocok!");
                return;
            }
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(null, "Password minimal 6 karakter!");
                return;
            }

            // --- PERUBAHAN UTAMA DI SINI ---


            // Generate ID Berurutan (KRY001, dst)
            String userId = generateKaryawanId();

            // -------------------------------

            // Buat object User baru (Role otomatis 'Karyawan')
            User newUser = new User(userId, nama, email, password, "Karyawan");

            // Simpan ke database lewat DAO
            UserDAO.registrasiUser(newUser);

            // Tampilkan pesan sukses dengan info ID
            JOptionPane.showMessageDialog(null,
                    "Registrasi berhasil!\nID Anda:  \nSilakan login.");

            // Kembali ke halaman login
            kembaliLogin(event);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage());
        }
    }

    // --- METHOD GENERATOR ID BARU ---
    private String generateKaryawanId() {
        String prefix = "KRY";
        // Cari ID terakhir KRY...
        String query = "SELECT idUser FROM users WHERE idUser LIKE 'KRY%' ORDER BY idUser DESC LIMIT 1";

        ResultSet rs = DatabaseManager.executeQuery(query);
        int nextNumber = 1;

        try {
            if (rs != null && rs.next()) {
                String lastId = rs.getString("idUser");
                if (lastId.length() >= 3) {
                    String numberPart = lastId.substring(3);
                    nextNumber = Integer.parseInt(numberPart) + 1;
                }
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }

        return String.format("%s%03d", prefix, nextNumber);
    }

    @FXML
    private void kembaliLogin(ActionEvent event) throws IOException {
        URL loginUrl = getClass().getResource("/View/Login.fxml");
        if (loginUrl == null) {
            // Fallback manual jika getResource gagal
            loginUrl = new File("src/main/resources/View/Login.fxml").toURI().toURL();
        }

        Parent root = FXMLLoader.load(loginUrl);
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
}

