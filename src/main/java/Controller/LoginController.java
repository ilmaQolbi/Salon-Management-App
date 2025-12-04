package Controller;

import DAO.UserDAO;
import Model.User;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.swing.JOptionPane; // Import JOptionPane
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField tfEmail;
    @FXML private PasswordField tfPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private Button btnLogin;
    @FXML private Hyperlink linkRegister;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Isi pilihan Role
        cbRole.setItems(FXCollections.observableArrayList("Admin", "Kasir", "Karyawan"));
    }

    @FXML
    private void KeLogin(ActionEvent event) {
        String email = tfEmail.getText();
        String password = tfPassword.getText();
        String role = cbRole.getValue();

        // 1. Validasi Input Kosong
        if (email.isEmpty() || password.isEmpty() || role == null) {
            JOptionPane.showMessageDialog(null, "Semua field harus diisi!");
            return;
        }

        try {
            // 2. Cek Login ke Database via DAO
            // Pastikan method UserDAO.loginUser mengembalikan object User
            User user = UserDAO.validate(email, password, role);

            if (user != null) {
                // LOGIN SUKSES
                JOptionPane.showMessageDialog(null, "Login Berhasil! Selamat datang " + user.getNama());

                // 3. Arahkan User sesuai Role
                // Masuk ke Admin Dashboard
                if (role.equalsIgnoreCase("Admin")) {
                    goToDashboard(event, "/View/AdminDashboard.fxml", "Admin Dashboard", user, false);
                    // Masuk ke Kasir Dashboard
                } else if (role.equalsIgnoreCase("Kasir")) {
                    goToDashboard(event, "/View/KasirDashboard.fxml", "Kasir Salon", user, true);
                }

            } else {
                // LOGIN GAGAL
                JOptionPane.showMessageDialog(null, "Login Gagal! Email, Password, atau Role salah.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan database: " + e.getMessage());
        }
    }

    @FXML
    private void onOpenRegister(ActionEvent event) {
        try {
            // Pindah ke halaman Register
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register Karyawan");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal membuka halaman Register.");
        }
    }

    // --- HELPER UNTUK PINDAH SCENE ---
    private void goToDashboard(ActionEvent event, String fxmlPath, String title, User user, boolean isCashier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Khusus Kasir: Kirim data user (Nama & ID) ke Controller Kasir
            if (isCashier) {
                KasirController cashierCtrl = loader.getController();
                // Pastikan di CashierController.java ada method setKasirData
                cashierCtrl.setKasirData(user.getIdUser(), user.getNama());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal memuat halaman: " + fxmlPath + "\nError: " + e.getMessage());
        }
    }
}