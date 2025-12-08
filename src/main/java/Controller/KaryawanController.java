package Controller;

import DAO.DatabaseManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KaryawanController {

    // --- UI ELEMENTS ---
    @FXML private Label lblNamaUser;
    @FXML private Label lblPending;
    @FXML private Label lblSelesai;

    @FXML private TableView<PekerjaanModel> tablePekerjaan;
    @FXML private TableColumn<PekerjaanModel, Integer> colIdTransaksi;
    @FXML private TableColumn<PekerjaanModel, String> colPelanggan;
    @FXML private TableColumn<PekerjaanModel, String> colLayanan;
    @FXML private TableColumn<PekerjaanModel, String> colWaktu;
    @FXML private TableColumn<PekerjaanModel, String> colStatus;
    @FXML private TableColumn<PekerjaanModel, Button> colAction;

    // --- DATA ---
    private ObservableList<PekerjaanModel> listPekerjaan = FXCollections.observableArrayList();
    private String currentUserId; // ID Stylist yang sedang login

    @FXML
    public void initialize() {
        // 1. Setup Kolom Tabel
        colIdTransaksi.setCellValueFactory(new PropertyValueFactory<>("idTransaksi"));
        colPelanggan.setCellValueFactory(new PropertyValueFactory<>("namaPelanggan"));
        colLayanan.setCellValueFactory(new PropertyValueFactory<>("namaLayanan"));
        colWaktu.setCellValueFactory(new PropertyValueFactory<>("waktu"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Setup Tombol Aksi (Selesai)
        colAction.setCellValueFactory(cellData -> {
            PekerjaanModel data = cellData.getValue();
            Button btn = new Button("Tandai Selesai");

            // Style tombol
            btn.getStyleClass().add("btn-success"); // Menggunakan CSS .btn-success
            btn.setStyle("-fx-font-size: 12px; -fx-padding: 5 10 5 10;");

            // Logika: Jika sudah selesai, tombol mati. Jika belum, tombol aktif.
            if ("Selesai".equalsIgnoreCase(data.getStatus())) {
                btn.setText("Tuntas");
                btn.setDisable(true);
                btn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
            } else {
                btn.setOnAction(e -> aksiSelesaikanPekerjaan(data));
            }
            return new SimpleObjectProperty<>(btn);
        });

        tablePekerjaan.setItems(listPekerjaan);

        // 3. Load Data Awal
        loadDataPekerjaan();
    }

    // --- LOGIKA LOAD DATA (QUERY JOIN) ---
    private void loadDataPekerjaan() {
        listPekerjaan.clear();
        int pendingCount = 0;
        int doneCount = 0;

        // Kita ambil data dari detail_transaksi, join ke transaksi (dapat nama pelanggan), join ke layanan (dapat nama layanan)
        // Filter: Hanya tampilkan data hari ini atau yang masih Pending
        String query = "SELECT dt.id_detail, t.id_transaksi, t.nama_pelanggan, l.nama_layanan, t.tanggal, dt.status " +
                "FROM detail_transaksi dt " +
                "JOIN transaksi t ON dt.id_transaksi = t.id_transaksi " +
                "JOIN layanan l ON dt.id_layanan = l.id_layanan " +
                "ORDER BY dt.status ASC, t.tanggal DESC";
        // Urutkan: Pending di atas, Selesai di bawah.

        ResultSet rs = DatabaseManager.executeQuery(query);
        try {
            while (rs != null && rs.next()) {
                String status = rs.getString("status");
                if (status == null) status = "Pending"; // Default jika null

                listPekerjaan.add(new PekerjaanModel(
                        rs.getInt("id_detail"),
                        rs.getInt("id_transaksi"),
                        rs.getString("nama_pelanggan"),
                        rs.getString("nama_layanan"),
                        rs.getString("tanggal"),
                        status
                ));

                // Hitung Statistik Dashboard
                if ("Pending".equalsIgnoreCase(status)) {
                    pendingCount++;
                } else if ("Selesai".equalsIgnoreCase(status)) {
                    doneCount++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Update Kartu Ringkasan
        lblPending.setText(String.valueOf(pendingCount));
        lblSelesai.setText(String.valueOf(doneCount));
    }

    // --- LOGIKA UPDATE STATUS ---
    private void aksiSelesaikanPekerjaan(PekerjaanModel data) {
        int confirm = JOptionPane.showConfirmDialog(null,
                "Selesaikan layanan: " + data.getNamaLayanan() + " untuk " + data.getNamaPelanggan() + "?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Update database: set status = 'Selesai'
            // Optional: set 'dikerjakan_oleh' = ID Stylist yang login
            String query = "UPDATE detail_transaksi SET status = 'Selesai' WHERE id_detail = ?";

            // Jika kolom 'dikerjakan_oleh' ada di DB, gunakan query ini:
            // String query = "UPDATE detail_transaksi SET status = 'Selesai', dikerjakan_oleh = ? WHERE id_detail = ?";
            // DatabaseManager.executeUpdate(query, currentUserId, data.getIdDetail());

            int res = DatabaseManager.executeUpdate(query, data.getIdDetail());

            if (res > 0) {
                JOptionPane.showMessageDialog(null, "Pekerjaan Selesai!");
                loadDataPekerjaan(); // Refresh tabel
            } else {
                JOptionPane.showMessageDialog(null, "Gagal mengupdate data.");
            }
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        loadDataPekerjaan();
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method ini dipanggil dari LoginController untuk oper data nama stylist
    public void setUserData(String id, String nama) {
        this.currentUserId = id;
        this.lblNamaUser.setText("| Halo, " + nama);
    }

    // --- INNER CLASS MODEL ---
    public static class PekerjaanModel {
        private int idDetail;
        private int idTransaksi;
        private String namaPelanggan;
        private String namaLayanan;
        private String waktu;
        private String status;

        public PekerjaanModel(int idDetail, int idTransaksi, String namaPelanggan, String namaLayanan, String waktu, String status) {
            this.idDetail = idDetail;
            this.idTransaksi = idTransaksi;
            this.namaPelanggan = namaPelanggan;
            this.namaLayanan = namaLayanan;
            this.waktu = waktu;
            this.status = status;
        }
        // Getters
        public int getIdDetail() { return idDetail; }
        public int getIdTransaksi() { return idTransaksi; }
        public String getNamaPelanggan() { return namaPelanggan; }
        public String getNamaLayanan() { return namaLayanan; }
        public String getWaktu() { return waktu; }
        public String getStatus() { return status; }
    }
}