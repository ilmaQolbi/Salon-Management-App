package Controller;

import DAO.DatabaseManager;
import DAO.UserDAO;
import DAO.LayananDAO;
import Model.Admin;
import Model.Kasir;
import Model.Karyawan;
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

    // Ini bagian UI (FXML) ya ges

    // Navigasi Sidebar
    @FXML
    private VBox viewKaryawan;
    @FXML
    private VBox viewLayanan;
    @FXML
    private VBox viewLaporan;
    @FXML
    private VBox viewPenjualan;
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
    private TableView<Layanan> tableLayanan;
    @FXML
    private TableColumn colNoLayanan;
    @FXML
    private TableColumn colNamaLayanan;
    @FXML
    private TableColumn colHargaLayanan;
    @FXML
    private TableColumn colDurasiLayanan;
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

    private ObservableList DaftarKaryawan = FXCollections.observableArrayList();
    private ObservableList<Layanan> DaftarLayanan = FXCollections.observableArrayList();
    private ObservableList<LaporanModel> DaftarLaporan = FXCollections.observableArrayList();

    // ini bagian insialisasi aplikasinyaa

    @FXML
    public void initialize() {

        cbRole.setItems(FXCollections.observableArrayList("Admin", "Kasir", "Karyawan"));
        cbStatus.setItems(FXCollections.observableArrayList("Aktif", "Nonaktif"));

        colIdKaryawan.setCellValueFactory(new PropertyValueFactory("idUser"));
        colNamaKaryawan.setCellValueFactory(new PropertyValueFactory("nama"));
        colEmail.setCellValueFactory(new PropertyValueFactory("email"));
        colPassword.setCellValueFactory(new PropertyValueFactory("password"));
        colRole.setCellValueFactory(new PropertyValueFactory("role"));
        colStatus.setCellValueFactory(new PropertyValueFactory("status"));

        setupKolomStatus();

        tableKaryawan.setItems(DaftarKaryawan);

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

        setupTableLayanan();
        setupTabelLaporan();

        lihatKaryawan(null);
    }

    // ini bagian setup untuk menyetujui pendaftara karyawan
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

    private void muatDataKaryawan() {
        DaftarKaryawan.clear();

        DaftarKaryawan.addAll(UserDAO.getAllUsers());
    }

    // bagian utntuk membuat id pake Admin.generateUserId()

    private String generateNextId(String role) {
        return Admin.generateUserId(role);
    }

    @FXML
    void aksiTambahKaryawan(ActionEvent event) {
        String nama = txtNamaKaryawan.getText();
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String role = (String) cbRole.getValue();
        String status = (String) cbStatus.getValue();

        // validasi kolom yg harus terisi
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

        // Buat user sesuai role (karena User adalah abstract class)
        // Ini menerapkan konsep Abstraction & Factory Pattern
        User newUser;
        switch (role.toLowerCase()) {
            case "admin":
                newUser = new Admin(idUserBaru, nama, username, password);
                break;
            case "kasir":
                newUser = new Kasir(idUserBaru, nama, username, password);
                break;
            case "karyawan":
            default:
                newUser = new Karyawan(idUserBaru, nama, username, password);
                break;
        }
        newUser.setStatus(status);

        boolean sukses = UserDAO.tambahUser(newUser);

        if (sukses) {
            tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses",
                    "User ID: " + idUserBaru + " berhasil ditambahkan.");
            muatDataKaryawan();
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
        User dipilih = (User) tableKaryawan.getSelectionModel().getSelectedItem();
        if (dipilih == null) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Peringatan", "Pilih user yang ingin diupdate!");
            return;
        }

        String nama = txtNamaKaryawan.getText();
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String role = (String) cbRole.getValue();
        String status = (String) cbStatus.getValue();

        if (nama == null || nama.isEmpty() ||
                username == null || username.isEmpty() ||
                password == null || password.isEmpty() ||
                role == null || status == null) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Warning", "Semua field harus diisi untuk update!");
            return;
        }

        boolean sukses = UserDAO.updateUser(dipilih.getIdUser(), nama, username, password, role, status);

        if (sukses) {
            tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses", "Data user berhasil diperbarui.");
            muatDataKaryawan();
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
            // Gunakan DAO untuk hapus user
            if (UserDAO.hapusUser(selected.getIdUser())) {
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
    // BAGIAN LAYANAN - TABLE VIEW
    // ===============================================================

    private void setupTableLayanan() {
        // Kolom nomor urut
        colNoLayanan.setCellFactory(column -> new TableCell<Layanan, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        colNamaLayanan.setCellValueFactory(new PropertyValueFactory("namaLayanan"));
        colHargaLayanan.setCellValueFactory(new PropertyValueFactory("harga"));
        colDurasiLayanan.setCellValueFactory(new PropertyValueFactory("durasi"));

        tableLayanan.setItems(DaftarLayanan);

        // Listener untuk mengisi form saat baris dipilih
        tableLayanan.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                Layanan layanan = newSelection;
                txtNamaLayanan.setText(layanan.getNamaLayanan());
                txtHarga.setText(String.valueOf((int) layanan.getHarga()));
                txtDurasi.setText(String.valueOf(layanan.getDurasi()));
            }
        });
    }

    private void muatDataLayanan() {
        DaftarLayanan.clear();
        LayananDAO dao = new LayananDAO();
        DaftarLayanan.addAll(dao.getAllLayanan());
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

            LayananDAO dao = new LayananDAO();
            boolean success = dao.tambahLayanan(nama, harga, durasi);

            if (success) {
                tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses", "Layanan berhasil ditambahkan.");
                muatDataLayanan();
                clearLayananForm();
            }
        } catch (NumberFormatException e) {
            tampilkanPeringatan(Alert.AlertType.ERROR, "Format Salah", "Harga dan Durasi harus berupa angka.");
        }
    }

    @FXML
    void aksiUpdateLayanan(ActionEvent event) {
        Layanan dipilih = tableLayanan.getSelectionModel().getSelectedItem();
        if (dipilih == null) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Peringatan", "Pilih layanan yang ingin diupdate!");
            return;
        }

        String nama = txtNamaLayanan.getText();
        String hargaStr = txtHarga.getText();
        String durasiStr = txtDurasi.getText();

        if (nama == null || nama.isEmpty() ||
                hargaStr == null || hargaStr.isEmpty() ||
                durasiStr == null || durasiStr.isEmpty()) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Warning", "Semua field harus diisi untuk update!");
            return;
        }

        try {
            double harga = Double.parseDouble(hargaStr);
            int durasi = Integer.parseInt(durasiStr);

            LayananDAO dao = new LayananDAO();
            boolean success = dao.updateLayanan(dipilih.getIdLayanan(), nama, harga, durasi);

            if (success) {
                tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses", "Data layanan berhasil diperbarui.");
                muatDataLayanan();
                clearLayananForm();
            } else {
                tampilkanPeringatan(Alert.AlertType.ERROR, "Gagal", "Gagal mengupdate layanan.");
            }
        } catch (NumberFormatException e) {
            tampilkanPeringatan(Alert.AlertType.ERROR, "Format Salah", "Harga dan Durasi harus berupa angka.");
        }
    }

    @FXML
    void aksiHapusLayanan(ActionEvent event) {
        Layanan dipilih = tableLayanan.getSelectionModel().getSelectedItem();
        if (dipilih == null) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Peringatan", "Pilih layanan yang ingin dihapus!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText(null);
        confirm.setContentText("Yakin ingin menghapus layanan '" + dipilih.getNamaLayanan() + "'?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            LayananDAO dao = new LayananDAO();
            if (dao.hapusLayanan(dipilih.getIdLayanan())) {
                tampilkanPeringatan(Alert.AlertType.INFORMATION, "Sukses", "Layanan dihapus.");
                muatDataLayanan();
                clearLayananForm();
            } else {
                tampilkanPeringatan(Alert.AlertType.ERROR, "Gagal", "Gagal menghapus layanan.");
            }
        }
    }

    private void clearLayananForm() {
        txtNamaLayanan.clear();
        txtHarga.clear();
        txtDurasi.clear();
        tableLayanan.getSelectionModel().clearSelection();
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

        ResultSet rs = DatabaseManager.eksekusiQuery(query);
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
        return DatabaseManager.eksekusiUpdate(query, status, id) > 0;
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

                // Simpan status maximized sebelum ganti scene
                boolean wasMaximized = stage.isMaximized();

                // Buat scene dengan ukuran yang sama
                Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
                stage.setScene(scene);
                stage.setTitle("Login Salon");

                // Restore maximized state
                if (wasMaximized) {
                    stage.setMaximized(true);
                }
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
        if (viewPenjualan != null)
            viewPenjualan.setVisible(false);
        if (view != null)
            view.setVisible(true);
    }

    @FXML
    void handleShowPenjualan(ActionEvent event) {
        switchView(viewPenjualan);
    }

}