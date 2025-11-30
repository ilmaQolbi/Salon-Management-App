package Controller;

import DAO.UserDAO;
import Model.User;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javax.swing.JOptionPane;

public class LoginController implements Initializable {

    public static User user;

    @FXML
    private TextField tfEmail;

    @FXML
    private PasswordField tfPassword;

    @FXML
    private ComboBox<String> cbRole;


    @FXML
    private Button btnLogin;

    @FXML
    private Hyperlink linkRegister;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        user = null;

        // isi pilihan role kalau perlu
        cbRole.getItems().addAll("Admin", "Kasir", "Karyawan");
    }

    @FXML
    private void onLogin(ActionEvent event) {
        try {
            String email = tfEmail.getText();
            String password = tfPassword.getText();
            String role = cbRole.getValue();

            if (email == null || email.isEmpty()
                    || password == null || password.isEmpty()
                    || role == null || role.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Email, password, dan role harus diisi");
                return;
            }

            // validate dengan email, password, dan role
            user = UserDAO.validate(email, password, role);

            if (user != null) {
                Parent root;

                // cek role user dari database
                if ("Admin".equalsIgnoreCase(user.getRole())) {
                    // masuk ke AdminDashboard.fxml
                    root = FXMLLoader.load(
                            getClass().getResource("/View/AdminDashboard.fxml"));
                } else {
                    // sementara: role lain masuk ke dashboard biasa
                    // (sesuaikan kalau nanti punya FXML khusus Kasir/Karyawan)
                    root = FXMLLoader.load(
                            getClass().getResource("/View/dashboard.fxml"));
                }

                Stage stage = (Stage) btnLogin.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();

            } else {
                JOptionPane.showMessageDialog(null,
                        "INVALID USERNAME / PASSWORD!!!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Terjadi kesalahan: " + e.getMessage());
        }
    }


    @FXML
    private void onOpenRegister(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/View/Register.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                .getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    }
