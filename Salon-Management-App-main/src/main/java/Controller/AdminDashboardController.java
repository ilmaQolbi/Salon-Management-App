package Controller;

import DAO.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node; // Penting untuk mengambil stage dari event
import java.io.IOException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardController {

    // --- 1. UI ELEMENTS (FXML) ---

    // Navigasi Sidebar
    @FXML private VBox viewKaryawan;
    @FXML private VBox viewLayanan;
    @FXML private VBox viewLaporan;

    // --- BAGIAN KARYAWAN ---
    @FXML private TextField txtNamaKaryawan;
    @FXML private ComboBox<String> cbRole;
    @FXML private ComboBox<String> cbStatus;

    @FXML private TableView<UserModel> tableKaryawan;
    @FXML private TableColumn<UserModel, String> colIdKaryawan;
    @FXML private TableColumn<UserModel, String> colNamaKaryawan;
    @FXML private TableColumn<UserModel, String> colRole;
    @FXML private TableColumn<UserModel, String> colStatus;

    // --- BAGIAN LAYANAN (INI YANG MENYEBABKAN ERROR SEBELUMNYA) ---
    @FXML private TextField txtNamaLayanan;
    @FXML private TextField txtHarga;
    @FXML private TextField txtDurasi;

    @FXML private TableView<LayananModel> tableLayanan;
    @FXML private TableColumn<LayananModel, Integer> colIdLayanan;
    @FXML private TableColumn<LayananModel, String> colNamaLayanan;
    @FXML private TableColumn<LayananModel, Double> colHarga;
    @FXML private TableColumn<LayananModel, Integer> colDurasi;

    // --- DATA LIST ---
    private ObservableList<UserModel> listKaryawan = FXCollections.observableArrayList();
    private ObservableList<LayananModel> listLayanan = FXCollections.observableArrayList();

    // --- 2. INITIALIZE (JALAN SAAT APLIKASI DIBUKA) ---
    @FXML
    public void initialize() {
        // A. Setup Karyawan
        cbRole.setItems(FXCollections.observableArrayList("Admin", "Kasir", "Karyawan"));
        cbStatus.setItems(FXCollections.observableArrayList("Aktif", "Nonaktif"));

        colIdKaryawan.setCellValueFactory(new PropertyValueFactory<>("idUser"));
        colNamaKaryawan.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableKaryawan.setItems(listKaryawan);

        // B. Setup Layanan
        colIdLayanan.setCellValueFactory(new PropertyValueFactory<>("idLayanan"));
        colNamaLayanan.setCellValueFactory(new PropertyValueFactory<>("namaLayanan"));
        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        colDurasi.setCellValueFactory(new PropertyValueFactory<>("durasi"));
        tableLayanan.setItems(listLayanan);

        // C. Load Semua Data dari Database
        loadDataKaryawan();
        loadDataLayanan();

        // D. Tampilkan View Default
        handleShowKaryawan(null);
    }

    // ===============================================================
    // BAGIAN LOGIKA KARYAWAN (USERS)
    // ===============================================================

    private void loadDataKaryawan() {
        listKaryawan.clear();
        String query = "SELECT * FROM users";
        ResultSet rs = DatabaseManager.executeQuery(query);
        try {
            while (rs != null && rs.next()) {
                listKaryawan.add(new UserModel(
                        rs.getString("idUser"),
                        rs.getString("nama"),
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String generateNextId(String role) {
        String prefix;
        if (role.equalsIgnoreCase("Admin")) prefix = "ADM";
        else if (role.equalsIgnoreCase("Kasir")) prefix = "KSR";
        else prefix = "KRY";

        String query = "SELECT idUser FROM users WHERE idUser LIKE '" + prefix + "%' ORDER BY idUser DESC LIMIT 1";
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
    void aksiTambahKaryawan(ActionEvent event) {
        String nama = txtNamaKaryawan.getText();
        String role = cbRole.getValue();
        String status = cbStatus.getValue();

        if (nama != null && !nama.isEmpty() && role != null) {
            String idUserBaru = generateNextId(role);
            String emailDummy = nama.replaceAll("\\s+","").toLowerCase() + "@salon.com";
            String passDefault = role.toLowerCase() + "123";
            if (status == null) status = "Aktif";

            String query = "INSERT INTO users (idUser, nama, email, password, role, status) VALUES (?, ?, ?, ?, ?, ?)";
            int result = DatabaseManager.executeUpdate(query, idUserBaru, nama, emailDummy, passDefault, role, status);

            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "User ID: " + idUserBaru + " berhasil ditambahkan.");
                loadDataKaryawan();
                txtNamaKaryawan.clear();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Nama dan Role harus diisi!");
        }
    }

    @FXML
    void aksiHapusKaryawan(ActionEvent event) {
        UserModel selected = tableKaryawan.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "DELETE FROM users WHERE idUser = ?";
            if (DatabaseManager.executeUpdate(query, selected.getIdUser()) > 0) {
                listKaryawan.remove(selected);
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data dihapus.");
            }
        }
    }

    // ===============================================================
    // BAGIAN LOGIKA LAYANAN (SERVICES) - INI YANG HILANG SEBELUMNYA
    // ===============================================================

    private void loadDataLayanan() {
        listLayanan.clear();
        String query = "SELECT * FROM layanan";
        ResultSet rs = DatabaseManager.executeQuery(query);
        try {
            while (rs != null && rs.next()) {
                listLayanan.add(new LayananModel(
                        rs.getInt("id_layanan"),
                        rs.getString("nama_layanan"),
                        rs.getDouble("harga"),
                        rs.getInt("durasi")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void aksiTambahLayanan(ActionEvent event) {
        try {
            String nama = txtNamaLayanan.getText();
            String hargaStr = txtHarga.getText();
            String durasiStr = txtDurasi.getText();

            if (nama.isEmpty() || hargaStr.isEmpty() || durasiStr.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua data layanan harus diisi!");
                return;
            }

            double harga = Double.parseDouble(hargaStr);
            int durasi = Integer.parseInt(durasiStr);

            String query = "INSERT INTO layanan (nama_layanan, harga, durasi) VALUES (?, ?, ?)";
            int result = DatabaseManager.executeUpdate(query, nama, harga, durasi);

            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Layanan berhasil ditambahkan.");
                loadDataLayanan();
                txtNamaLayanan.clear();
                txtHarga.clear();
                txtDurasi.clear();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format Salah", "Harga dan Durasi harus berupa angka.");
        }
    }

    // Opsional: Tambahkan logika hapus layanan jika tombolnya ada
    @FXML
    void aksiHapusLayanan(ActionEvent event) {
        LayananModel selected = tableLayanan.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "DELETE FROM layanan WHERE id_layanan = ?";
            if(DatabaseManager.executeUpdate(query, selected.getIdLayanan()) > 0) {
                listLayanan.remove(selected);
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Layanan dihapus.");
            }
        }
    }

    // ===============================================================
    // HELPER & MODEL CLASSES
    // ===============================================================

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML void handleShowKaryawan(ActionEvent event) { switchView(viewKaryawan); }
    @FXML void handleShowLayanan(ActionEvent event) { switchView(viewLayanan); }
    @FXML void handleShowLaporan(ActionEvent event) { switchView(viewLaporan); }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfirmasi Logout");
            alert.setHeaderText(null);
            alert.setContentText("Apakah Anda yakin ingin keluar?");

            // Jika user klik OK, baru proses logout
            if (alert.showAndWait().get() == ButtonType.OK) {

                // 1. Tentukan lokasi file Login.fxml
                // PERHATIKAN: Path ini harus sesuai dengan struktur folder kamu.
                // Jika Login.fxml ada di folder View, gunakan "/View/Login.fxml"
                // Jika Login.fxml ada di root resources, gunakan "/Login.fxml"
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Login.fxml"));
                Parent root = loader.load();

                // 2. Ambil Stage (Jendela) saat ini dari tombol yang diklik
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // 3. Set Scene baru ke Login
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Login Salon");
                stage.centerOnScreen();
                stage.show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Navigasi", "Gagal memuat halaman Login.\nCek apakah file Login.fxml ada di folder '/View/'");
        }
    }

    private void switchView(VBox view) {
        if(viewKaryawan != null) viewKaryawan.setVisible(false);
        if(viewLayanan != null) viewLayanan.setVisible(false);
        if(viewLaporan != null) viewLaporan.setVisible(false);
        if(view != null) view.setVisible(true);
    }

    public static class UserModel {
        private String idUser, nama, role, status;
        public UserModel(String idUser, String nama, String role, String status) {
            this.idUser = idUser; this.nama = nama; this.role = role; this.status = status;
        }
        public String getIdUser() { return idUser; }
        public String getNama() { return nama; }
        public String getRole() { return role; }
        public String getStatus() { return status; }
    }

    public static class LayananModel {
        private int idLayanan;
        private String namaLayanan;
        private double harga;
        private int durasi;
        public LayananModel(int id, String nama, double harga, int durasi) {
            this.idLayanan = id; this.namaLayanan = nama; this.harga = harga; this.durasi = durasi;
        }
        public int getIdLayanan() { return idLayanan; }
        public String getNamaLayanan() { return namaLayanan; }
        public Double getHarga() { return harga; }
        public Integer getDurasi() { return durasi; }
    }
}