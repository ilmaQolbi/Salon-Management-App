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
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField tfEmail;
    @FXML
    private PasswordField tfPassword;
    @FXML
    private ComboBox<String> cbRole;
    @FXML
    private VBox brandingSection;
    @FXML
    private StackPane formSection;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Isi pilihan Role
        cbRole.setItems(FXCollections.observableArrayList("Admin", "Kasir", "Karyawan"));

        // Animation: Fade In Branding Section
        FadeTransition fadeBranding = new FadeTransition(Duration.seconds(1), brandingSection);
        fadeBranding.setFromValue(0);
        fadeBranding.setToValue(1);
        fadeBranding.play();

        // Animation: Slide In Branding (Up)
        TranslateTransition slideBranding = new TranslateTransition(Duration.seconds(1), brandingSection);
        slideBranding.setFromY(50);
        slideBranding.setToY(0);
        slideBranding.play();

        // Animation: Fade In Form Section
        FadeTransition fadeForm = new FadeTransition(Duration.seconds(1), formSection);
        fadeForm.setFromValue(0);
        fadeForm.setToValue(1);
        fadeForm.play();

        // Animation: Slide In Form (Right to Left)
        TranslateTransition slideForm = new TranslateTransition(Duration.seconds(1), formSection);
        slideForm.setFromX(50);
        slideForm.setToX(0);
        slideForm.play();
    }

    @FXML
    private void KeLogin(ActionEvent event) {
        String email = tfEmail.getText();
        String password = tfPassword.getText();
        String role = cbRole.getValue();

        // 1. Validasi Input Kosong
        if (email.isEmpty() || password.isEmpty() || role == null) {
            tampilkanAlert(Alert.AlertType.WARNING, "Validasi", "Semua field harus diisi!");
            return;
        }

        try {
            // 2. Cek Login ke Database via DAO
            // Pastikan method UserDAO.loginUser mengembalikan object User
            User user = UserDAO.validasiLogin(email, password, role);

            if (user != null) {

                // Cek Status Approval
                String status = user.getStatus();
                if (status != null && status.equalsIgnoreCase("Menunggu Persetujuan")) {
                    tampilkanAlert(Alert.AlertType.WARNING, "Menunggu Persetujuan",
                            "Akun Anda sedang menunggu persetujuan Admin.\nSilakan hubungi Admin.");
                    return;
                } else if (status != null && status.equalsIgnoreCase("Ditolak")) {
                    tampilkanAlert(Alert.AlertType.ERROR, "Ditolak", "Maaf, pendaftaran akun Anda ditolak oleh Admin.");
                    return;
                } else if (status == null || status.equalsIgnoreCase("Aktif")) {
                    // LOGIN SUKSES
                    tampilkanAlert(Alert.AlertType.INFORMATION, "Login Berhasil", "Selamat datang " + user.getNama());

                    // 3. Arahkan User sesuai Role
                    // Masuk ke Admin Dashboard
                    if (role.equalsIgnoreCase("Admin")) {
                        keDashboard(event, "/View/AdminDashboard.fxml", "Admin Dashboard", user);
                        // Masuk ke Kasir Dashboard
                    } else if (role.equalsIgnoreCase("Kasir")) {
                        keDashboard(event, "/View/KasirDashboard.fxml", "Kasir Salon", user);
                    } else if (role.equalsIgnoreCase("Karyawan")) {
                        keDashboard(event, "/View/KaryawanDashboard.fxml", "Karyawan Dashboard", user);
                    }
                } else {
                    tampilkanAlert(Alert.AlertType.ERROR, "Error", "Status akun tidak valid: " + status);
                }

            } else {
                // LOGIN GAGAL
                tampilkanAlert(Alert.AlertType.ERROR, "Login Gagal", "Email, Password, atau Role salah.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            tampilkanAlert(Alert.AlertType.ERROR, "Error", "Terjadi kesalahan : " + e.getMessage());
        }
    }

    @FXML
    // Pindah ke halaman Register
    private void keHalamanRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Simpan status maximized sebelum ganti scene
            boolean wasMaximized = stage.isMaximized();

            // Buat scene dengan ukuran yang sama
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);
            stage.setTitle("Register Karyawan");

            // Restore maximized state
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            tampilkanAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka halaman Register.\n" + e.toString());
        }
    }

    // --- PINDAH SCENE ---
    private void keDashboard(ActionEvent aksi, String lokasiFxml, String judul, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(lokasiFxml));
            Parent root = loader.load();

            // Kirim data User ke Controller tujuan
            Object controller = loader.getController();
            if (controller instanceof Controller.KasirController) {
                ((Controller.KasirController) controller).setKasirData(user.getIdUser(), user.getNama());
            } else if (controller instanceof Controller.KaryawanController) {
                ((Controller.KaryawanController) controller).setUserData(user.getIdUser(), user.getNama());
            } else if (controller instanceof Controller.AdminController) {
                // ((Controller.AdminController) controller).setUserData(user); // Jika ada
            }

            Stage stage = (Stage) ((Node) aksi.getSource()).getScene().getWindow();

            // Simpan status maximized sebelum ganti scene
            boolean wasMaximized = stage.isMaximized();

            // Ganti scene ke dashboard dengan ukuran yang sama
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);
            stage.setTitle(judul);

            // Restore maximized state
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            tampilkanAlert(Alert.AlertType.ERROR, "Error",
                    "Gagal memuat halaman: " + lokasiFxml + "\nError: " + e.getMessage());
        }
    }

    // Helper method untuk menampilkan Alert JavaFX
    private void tampilkanAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}