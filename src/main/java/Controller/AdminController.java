package Controller;

import DAO.DatabaseManager;
import Model.Layanan;
import Model.User;
import Model.LaporanModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.util.Callback;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.beans.property.SimpleObjectProperty;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class AdminController {

    // --- 1. UI ELEMENTS (FXML) ---

    // Navigasi Sidebar
    @FXML
    private VBox viewKaryawan;
    @FXML
    private VBox viewLayanan;
    @FXML
    private VBox viewLaporan;

    // --- BAGIAN KARYAWAN ---
    @FXML
    private TextField txtNamaKaryawan;
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private ComboBox cbRole;
    @FXML
    private ComboBox cbStatus;

    @FXML
    private TableView tableKaryawan;
    @FXML
    private TableColumn colIdKaryawan;
    @FXML
    private TableColumn colNamaKaryawan;
    @FXML
    private TableColumn colEmail;
    @FXML
    private TableColumn colPassword;
    @FXML
    private TableColumn colRole;
    @FXML
    private TableColumn colStatus;
    @FXML
    private TextField txtNamaLayanan;
    @FXML
    private TextField txtHarga;
    @FXML
    private TextField txtDurasi;
    @FXML
    private TableView tableLayanan;
    @FXML
    private TableColumn colIdLayanan;
    @FXML
    private TableColumn colNamaLayanan;
    @FXML
    private TableColumn colHarga;
    @FXML
    private TableColumn colDurasi;

    // --- BAGIAN LAPORAN ---
    @FXML
    private TableView<LaporanModel> tableLaporan;
    @FXML
    private TableColumn<LaporanModel, Integer> colLaporId;
    @FXML
    private TableColumn<LaporanModel, String> colLaporUser;
    @FXML
    private TableColumn<LaporanModel, String> colLaporIsi;
    @FXML
    private TableColumn<LaporanModel, String> colLaporTanggal;
    @FXML
    private TableColumn<LaporanModel, String> colLaporStatus;
    @FXML
    private TableColumn<LaporanModel, Button> colLaporAksi;

    // --- DATA LIST ---
    private ObservableList DaftarKaryawan = FXCollections.observableArrayList();
    private ObservableList DaftarLayanan = FXCollections.observableArrayList();
    private ObservableList<LaporanModel> DaftarLaporan = FXCollections.observableArrayList();

    // --- 2. INISIALISASI (JALAN SAAT APLIKASI DIBUKA) ---
    @FXML
    public void initialize() {
        // A. Setup Karyawan
        cbRole.setItems(FXCollections.observableArrayList("Admin", "Kasir", "Karyawan"));
        cbStatus.setItems(FXCollections.observableArrayList("Aktif", "Nonaktif"));

        colIdKaryawan.setCellValueFactory(new PropertyValueFactory("idUser"));
        colNamaKaryawan.setCellValueFactory(new PropertyValueFactory("nama"));
        colEmail.setCellValueFactory(new PropertyValueFactory("email"));
        colPassword.setCellValueFactory(new PropertyValueFactory("password"));
        colRole.setCellValueFactory(new PropertyValueFactory("role"));
        colStatus.setCellValueFactory(new PropertyValueFactory("status"));

        // Setup kolom Status dengan tombol inline untuk Menunggu persetujuan
        setupKolomStatus();

        tableKaryawan.setItems(DaftarKaryawan);

        // Add Selection Listener to populate fields
        tableKaryawan.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                User user = (User) newSelection;
                txtNamaKaryawan.setText(user.getNama());
                txtUsername.setText(user.getEmail());
                txtPassword.setText(user.getPassword());
                cbRole.setValue(user.getRole());
                cbStatus.setValue(user.getStatus());
            }
        });

        // B. Setup Layanan
        colIdLayanan.setCellValueFactory(new PropertyValueFactory("idLayanan"));
        colNamaLayanan.setCellValueFactory(new PropertyValueFactory("namaLayanan"));
        colHarga.setCellValueFactory(new PropertyValueFactory("harga"));
        colDurasi.setCellValueFactory(new PropertyValueFactory("durasi"));
        tableLayanan.setItems(DaftarLayanan);

        // Add Selection Listener untuk auto-fill form layanan
        tableLayanan.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                Layanan layanan = (Layanan) newSelection;
                txtNamaLayanan.setText(layanan.getNamaLayanan());
                txtHarga.setText(String.valueOf(layanan.getHarga()));
                txtDurasi.setText(String.valueOf(layanan.getDurasi()));
            }
        });

        // C. Setup Laporan
        setupTabelLaporan();

        // C. Tampilkan View Default dan Muat Data
        lihatKaryawan(null);
    }

    // Setup kolom Status: tampilkan tombol ✓/✗
    private void setupKolomStatus() {
        Callback cellFactory = new Callback() {
            public Object call(Object column) {
                TableCell cell = new TableCell() {
                    private Button btnSetuju = new Button("✓");
                    private Button btnTolak = new Button("✗");
                    private Label lblStatus = new Label();
                    private HBox pane = new HBox(8, lblStatus, btnSetuju, btnTolak);

                    {
                        btnSetuju.setStyle(
                                "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 5;");
                        btnTolak.setStyle(
                                "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 5;");
                        lblStatus.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                        pane.setAlignment(Pos.CENTER_LEFT);
                    }

                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            String status = item.toString();
                            if (status.equalsIgnoreCase("Menunggu Persetujuan")) {
                                setText(null);
                                int index = getIndex();
                                User user = (User) tableKaryawan.getItems().get(index);

                                btnSetuju.setOnAction(e -> {
                                    if (DAO.UserDAO.updateStatusUser(user.getIdUser(), "Aktif")) {
                                        tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses",
                                                user.getNama() + " disetujui.");
                                        muatDataKaryawan();
                                    }
                                });

                                btnTolak.setOnAction(e -> {
                                    if (DAO.UserDAO.updateStatusUser(user.getIdUser(), "Ditolak")) {
                                        tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses",
                                                user.getNama() + " ditolak.");
                                        muatDataKaryawan();
                                    }
                                });

                                setGraphic(pane);
                            } else {
                                setText(status);
                                setGraphic(null);
                            }
                        }
                    }
                };
                return cell;
            }
        };
        colStatus.setCellFactory(cellFactory);
    }

    // ===============================================================
    // BAGIAN LOGIKA KARYAWAN (USERS)
    // ===============================================================

    private void muatDataKaryawan() {
        DaftarKaryawan.clear();
        String query = "SELECT * FROM users";
        ResultSet rs = DatabaseManager.executeQuery(query);
        try {
            while (rs != null && rs.next()) {
                DaftarKaryawan.add(new User(
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
    }

    private String generateNextId(String role) {
        String prefix;
        if (role.equalsIgnoreCase("Admin"))
            prefix = "ADM";
        else if (role.equalsIgnoreCase("Kasir"))
            prefix = "KSR";
        else
            prefix = "KRY";

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
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String role = (String) cbRole.getValue();
        String status = (String) cbStatus.getValue();

        // Validasi: semua field wajib diisi
        if (nama == null || nama.isEmpty() ||
                username == null || username.isEmpty() ||
                password == null || password.isEmpty() ||
                role == null) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Warning", "Nama, Username, Password, dan Role harus diisi!");
            return;
        }

        String idUserBaru = generateNextId(role);
        if (status == null)
            status = "Aktif";

        String query = "INSERT INTO users (idUser, nama, email, password, role, status) VALUES (?, ?, ?, ?, ?, ?)";
        int result = DatabaseManager.executeUpdate(query, idUserBaru, nama, username, password, role, status);

        if (result > 0) {
            tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses",
                    "User ID: " + idUserBaru + " berhasil ditambahkan.");
            muatDataKaryawan();
            // Clear all fields
            txtNamaKaryawan.clear();
            txtUsername.clear();
            txtPassword.clear();
            cbRole.setValue(null);
            cbStatus.setValue(null);
        } else {
            tampilkanPeringatan(Alert.AlertType.ERROR, "Gagal", "Gagal menambahkan user.");
        }
    }

    @FXML
    void aksiUpdateKaryawan(ActionEvent event) {
        User selected = (User) tableKaryawan.getSelectionModel().getSelectedItem();
        if (selected == null) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Peringatan", "Pilih user yang ingin diupdate!");
            return;
        }

        String nama = txtNamaKaryawan.getText();
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String role = (String) cbRole.getValue();

        // Validasi: field tidak boleh kosong
        if (nama == null || nama.isEmpty() ||
                username == null || username.isEmpty() ||
                password == null || password.isEmpty() ||
                role == null) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Warning", "Semua field harus diisi untuk update!");
            return;
        }

        // Update query
        String query = "UPDATE users SET nama = ?, email = ?, password = ?, role = ? WHERE idUser = ?";
        int result = DatabaseManager.executeUpdate(query, nama, username, password, role, selected.getIdUser());

        if (result > 0) {
            tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses", "Data user berhasil diperbarui.");
            muatDataKaryawan();
            // Clear fields/Selection
            tableKaryawan.getSelectionModel().clearSelection();
            txtNamaKaryawan.clear();
            txtUsername.clear();
            txtPassword.clear();
            cbRole.setValue(null);
            cbStatus.setValue(null);
        } else {
            tampilkanPeringatan(Alert.AlertType.ERROR, "Gagal", "Gagal mengupdate user.");
        }
    }

    @FXML
    void aksiHapusKaryawan(ActionEvent event) {
        User selected = (User) tableKaryawan.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "DELETE FROM users WHERE idUser = ?";
            if (DatabaseManager.executeUpdate(query, selected.getIdUser()) > 0) {
                DaftarKaryawan.remove(selected);
                tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses", "Data dihapus.");
            }
        }
    }

    @FXML
    void aksiSetujuiKaryawan(ActionEvent event) {
        User selected = (User) tableKaryawan.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Update Database
            if (DAO.UserDAO.updateStatusUser(selected.getIdUser(), "Aktif")) {
                tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses",
                        "User " + selected.getNama() + " telah Disetujui (Aktif).");
                muatDataKaryawan(); // Refresh tabel
            } else {
                tampilkanPeringatan(Alert.AlertType.ERROR, "Gagal", "Gagal mengupdate status user.");
            }
        } else {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Peringatan", "Pilih user yang ingin disetujui.");
        }
    }

    @FXML
    void aksiTolakKaryawan(ActionEvent event) {
        User selected = (User) tableKaryawan.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Update Database -> Ditolak
            if (DAO.UserDAO.updateStatusUser(selected.getIdUser(), "Ditolak")) {
                tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses",
                        "User " + selected.getNama() + " telah Ditolak.");
                muatDataKaryawan();
            } else {
                tampilkanPeringatan(Alert.AlertType.ERROR, "Gagal", "Gagal mengupdate status user.");
            }
        } else {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Peringatan", "Pilih user yang ingin ditolak.");
        }
    }

    // ===============================================================
    // BAGIAN LOGIKA LAYANAN (SERVICES) - INI YANG HILANG SEBELUMNYA
    // ===============================================================

    private void muatDataLayanan() {
        DaftarLayanan.clear();
        String query = "SELECT * FROM layanan";
        ResultSet rs = DatabaseManager.executeQuery(query);
        try {
            while (rs != null && rs.next()) {
                DaftarLayanan.add(new Layanan(
                        rs.getInt("id_layanan"),
                        rs.getString("nama_layanan"),
                        rs.getDouble("harga"),
                        rs.getInt("durasi")));
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
                tampilkanPeringatan(Alert.AlertType.WARNING, "Peringatan", "Semua data layanan harus diisi!");
                return;
            }

            double harga = Double.parseDouble(hargaStr);
            int durasi = Integer.parseInt(durasiStr);

            String query = "INSERT INTO layanan (nama_layanan, harga, durasi) VALUES (?, ?, ?)";
            int result = DatabaseManager.executeUpdate(query, nama, harga, durasi);

            if (result > 0) {
                tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses", "Layanan berhasil ditambahkan.");
                muatDataLayanan();
                txtNamaLayanan.clear();
                txtHarga.clear();
                txtDurasi.clear();
            }
        } catch (NumberFormatException e) {
            tampilkanPeringatan(Alert.AlertType.ERROR, "Format Salah", "Harga dan Durasi harus berupa angka.");
        }
    }

    // Opsional: Tambahkan logika hapus layanan jika tombolnya ada
    @FXML
    void aksiHapusLayanan(ActionEvent event) {
        Layanan selected = (Layanan) tableLayanan.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "DELETE FROM layanan WHERE id_layanan = ?";
            if (DatabaseManager.executeUpdate(query, selected.getIdLayanan()) > 0) {
                DaftarLayanan.remove(selected);
                tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses", "Layanan dihapus.");
            }
        }
    }

    @FXML
    void aksiUpdateLayanan(ActionEvent event) {
        Layanan selected = (Layanan) tableLayanan.getSelectionModel().getSelectedItem();
        if (selected == null) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Peringatan", "Pilih layanan yang ingin diupdate!");
            return;
        }

        String nama = txtNamaLayanan.getText();
        String harga = txtHarga.getText();
        String durasi = txtDurasi.getText();

        // Validasi: field tidak boleh kosong
        if (nama == null || nama.isEmpty() ||
                harga == null || harga.isEmpty() ||
                durasi == null || durasi.isEmpty()) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Warning", "Semua field harus diisi untuk update!");
            return;
        }

        // Update query
        String query = "UPDATE layanan SET nama_layanan = ?, harga = ?, durasi = ? WHERE id_layanan = ?";
        int result = DatabaseManager.executeUpdate(query, nama, harga, durasi, selected.getIdLayanan());

        if (result > 0) {
            tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses", "Data layanan berhasil diperbarui.");
            muatDataLayanan();
            // Clear fields/Selection
            tableLayanan.getSelectionModel().clearSelection();
            txtNamaLayanan.clear();
            txtHarga.clear();
            txtDurasi.clear();
        } else {
            tampilkanPeringatan(Alert.AlertType.ERROR, "Gagal", "Gagal mengupdate user.");
        }
    }

    // ===============================================================
    // HELPER & MODEL CLASSES
    // ===============================================================

    private void tampilkanPeringatan(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void lihatKaryawan(ActionEvent event) {
        switchView(viewKaryawan);
        muatDataKaryawan();
    }

    @FXML
    void lihatLayanan(ActionEvent event) {
        switchView(viewLayanan);
        muatDataLayanan();
    }

    @FXML
    void handleShowLaporan(ActionEvent event) {
        switchView(viewLaporan);
        muatDataLaporan();
    }

    // ===============================================================
    // BAGIAN LOGIKA LAPORAN KENDALA
    // ===============================================================

    private void setupTabelLaporan() {
        colLaporId.setCellValueFactory(new PropertyValueFactory<>("idLaporan"));
        colLaporUser.setCellValueFactory(new PropertyValueFactory<>("namaUser"));
        colLaporIsi.setCellValueFactory(new PropertyValueFactory<>("isiLaporan"));
        colLaporTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colLaporStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colLaporAksi.setCellValueFactory(cellData -> {
            LaporanModel lap = cellData.getValue();
            Button btn = new Button("Selesai");
            btn.getStyleClass().add("action-button-success"); // Reuse existing style

            if ("Selesai".equalsIgnoreCase(lap.getStatus())) {
                btn.setText("✓");
                btn.setDisable(true);
                btn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
            } else {
                btn.setOnAction(e -> {
                    if (updateStatusLaporan(lap.getIdLaporan(), "Selesai")) {
                        tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses", "Laporan ditandai selesai.");
                        muatDataLaporan();
                    }
                });
            }
            return new SimpleObjectProperty<>(btn);
        });

        tableLaporan.setItems(DaftarLaporan);
    }

    private void muatDataLaporan() {
        DaftarLaporan.clear();
        // JOIN users untuk dapatkan nama pelapor
        String query = "SELECT l.id_laporan, l.id_user, u.nama, l.isi_laporan, l.tanggal, l.status " +
                "FROM laporan_kendala l " +
                "LEFT JOIN users u ON l.id_user = u.idUser " +
                "ORDER BY l.status ASC, l.tanggal DESC";

        ResultSet rs = DatabaseManager.executeQuery(query);
        try {
            while (rs != null && rs.next()) {
                String namaUser = rs.getString("nama");
                if (namaUser == null)
                    namaUser = rs.getString("id_user"); // Fallback ke ID

                DaftarLaporan.add(new LaporanModel(
                        rs.getInt("id_laporan"),
                        namaUser,
                        rs.getString("isi_laporan"),
                        rs.getString("tanggal"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean updateStatusLaporan(int id, String status) {
        String query = "UPDATE laporan_kendala SET status = ? WHERE id_laporan = ?";
        return DatabaseManager.executeUpdate(query, status, id) > 0;
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfirmasi Logout");
            alert.setHeaderText(null);
            alert.setContentText("Apakah Anda yakin ingin keluar?");

            // Jika user klik OK, baru proses logout
            if (alert.showAndWait().get() == ButtonType.OK) {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Login Salon");
                stage.centerOnScreen();
                stage.show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            tampilkanPeringatan(Alert.AlertType.ERROR, "Error Navigasi",
                    "Gagal memuat halaman Login.\nCek apakah file Login.fxml ada di folder '/View/'");
        }
    }

    private void switchView(VBox view) {
        if (viewKaryawan != null)
            viewKaryawan.setVisible(false);
        if (viewLayanan != null)
            viewLayanan.setVisible(false);
        if (viewLaporan != null)
            viewLaporan.setVisible(false);
        if (view != null)
            view.setVisible(true);
    }

}