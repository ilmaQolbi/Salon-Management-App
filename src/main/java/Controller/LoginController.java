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
    private TextField tfUsername;

    @FXML
    private PasswordField tfPassword;

    @FXML
    private ComboBox<String> cbRole;

    // optional, kalau nanti mau dipakai
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
            String username = tfUsername.getText();
            String password = tfPassword.getText();
            String role     = cbRole.getValue();   // kalau mau dipakai

            if (username == null || username.isEmpty()
                    || password == null || password.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Username dan password harus diisi");
                return;
            }

            // kalau validate() kamu cuma butuh username + password:
            user = UserDAO.validate(username, password);

            // kalau ingin ikut cek role:
            // user = UserDAO.validate(username, password, role);

            if (user != null) {
                // TODO: ganti ke file FXML setelah login, misal dashboard.fxml
                URL dashboardUrl = new File("src/main/java/View/dashboard.fxml")
                        .toURI().toURL();
                Parent root = FXMLLoader.load(dashboardUrl);
                Stage stage = (Stage) btnLogin.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
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
        URL registerUrl = new File("src/main/java/View/Register.fxml")
                .toURI().toURL();
        Parent root = FXMLLoader.load(registerUrl);
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                .getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
}