package Controller;

import DAO.UserDAO;
import Model.User;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

public class RegisterController implements Initializable {

    @FXML
    private TextField tfEmail;

    @FXML
    private PasswordField tfPassword;

    @FXML
    private PasswordField tfConfirmPassword;

    @FXML
    private Button btnRegister;

    @FXML
    private Hyperlink linkBackToLogin;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialization code if needed
    }

    @FXML
    private void onRegister(ActionEvent event) {
        try {
            String email = tfEmail.getText();
            String password = tfPassword.getText();
            String confirmPassword = tfConfirmPassword.getText();

            // Validasi input kosong
            if (email == null || email.isEmpty() 
                    || password == null || password.isEmpty() 
                    || confirmPassword == null || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(null, 
                        "Semua field harus diisi!");
                return;
            }

            // Validasi email format
            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(null, 
                        "Format email tidak valid!");
                return;
            }

            // Validasi password match
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(null, 
                        "Password dan konfirmasi password tidak cocok!");
                return;
            }

            // Validasi panjang password
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(null, 
                        "Password minimal 6 karakter!");
                return;
            }

            // Generate ID unik untuk user
            String userId = UUID.randomUUID().toString();

            // Buat object User baru
            User newUser = new User(userId, email, password);

            // Simpan ke database
            UserDAO.registerUser(newUser);

            // Tampilkan pesan sukses
            JOptionPane.showMessageDialog(null, 
                    "Registrasi berhasil! Silakan login.");

            // Kembali ke halaman login
            goToLogin(event);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                    "Terjadi kesalahan saat registrasi: " + e.getMessage());
        }
    }

    @FXML
    private void onBackToLogin(ActionEvent event) throws IOException {
        goToLogin(event);
    }

    private void goToLogin(ActionEvent event) throws IOException {
        URL loginUrl = new File("src/main/java/View/Login.fxml")
                .toURI().toURL();
        Parent root = FXMLLoader.load(loginUrl);
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                .getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
}
