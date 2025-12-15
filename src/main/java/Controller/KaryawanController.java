package Controller;

import DAO.DatabaseManager;
import Model.PekerjaanModel;
import Model.KomisiModel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class KaryawanController {

    // --- UI ELEMENTS ---
    @FXML
    private Label lblNamaUser;
    @FXML
    private Label lblPending;
    @FXML
    private Label lblSelesai;
    @FXML
    private Label lblKomisiHariIni;
    @FXML
    private Label lblTotalKomisi;
    @FXML
    private Label lblTotalPekerjaan;

    // Views
    @FXML
    private VBox viewAntrian;
    @FXML
    private VBox viewRiwayat;
    @FXML
    private VBox viewKomisi;

    // Table Antrian
    @FXML
    private TableView<PekerjaanModel> tablePekerjaan;
    @FXML
    private TableColumn<PekerjaanModel, Integer> colIdTransaksi;
    @FXML
    private TableColumn<PekerjaanModel, String> colPelanggan;
    @FXML
    private TableColumn<PekerjaanModel, String> colLayanan;
    @FXML
    private TableColumn<PekerjaanModel, String> colWaktu;
    @FXML
    private TableColumn<PekerjaanModel, String> colStatus;
    @FXML
    private TableColumn<PekerjaanModel, Button> colAction;

    // Table Riwayat
    @FXML
    private TableView<PekerjaanModel> tableRiwayat;
    @FXML
    private TableColumn<PekerjaanModel, Integer> colRiwayatId;
    @FXML
    private TableColumn<PekerjaanModel, String> colRiwayatPelanggan;
    @FXML
    private TableColumn<PekerjaanModel, String> colRiwayatLayanan;
    @FXML
    private TableColumn<PekerjaanModel, String> colRiwayatTanggal;
    @FXML
    private TableColumn<PekerjaanModel, String> colRiwayatStatus;

    // Table Komisi
    @FXML
    private TableView<KomisiModel> tableKomisi;
    @FXML
    private TableColumn<KomisiModel, String> colKomisiLayanan;
    @FXML
    private TableColumn<KomisiModel, Integer> colKomisiJumlah;
    @FXML
    private TableColumn<KomisiModel, String> colKomisiNilai;

    // --- DATA ---
    private ObservableList<PekerjaanModel> listPekerjaan = FXCollections.observableArrayList();
    private ObservableList<PekerjaanModel> listRiwayat = FXCollections.observableArrayList();
    private ObservableList<KomisiModel> listKomisi = FXCollections.observableArrayList();
    private String currentUserId;

    private static final double Komisi = 0.10; // 10% komisi per layanan
    private NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @FXML
    public void initialize() {
        // Setup Table Antrian
        setupTableAntrian();

        // Setup Table Riwayat
        setupTableRiwayat();

        // Setup Table Komisi
        setupTableKomisi();

        // Load Data
        muatDataPekerjaan();
    }

    private void setupTableAntrian() {
        colIdTransaksi.setCellValueFactory(new PropertyValueFactory<>("idTransaksi"));
        colPelanggan.setCellValueFactory(new PropertyValueFactory<>("namaPelanggan"));
        colLayanan.setCellValueFactory(new PropertyValueFactory<>("namaLayanan"));
        colWaktu.setCellValueFactory(new PropertyValueFactory<>("waktu"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colAction.setCellValueFactory(cellData -> {
            PekerjaanModel data = cellData.getValue();
            Button btn = new Button("✓ Selesai");
            btn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 11px;");

            if ("Selesai".equalsIgnoreCase(data.getStatus())) {
                btn.setText("✓ Tuntas");
                btn.setDisable(true);
                btn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
            } else {
                btn.setOnAction(e -> aksiSelesaikanPekerjaan(data));
            }
            return new SimpleObjectProperty<>(btn);
        });

        tablePekerjaan.setItems(listPekerjaan);
    }

    private void setupTableRiwayat() {
        colRiwayatId.setCellValueFactory(new PropertyValueFactory<>("idTransaksi"));
        colRiwayatPelanggan.setCellValueFactory(new PropertyValueFactory<>("namaPelanggan"));
        colRiwayatLayanan.setCellValueFactory(new PropertyValueFactory<>("namaLayanan"));
        colRiwayatTanggal.setCellValueFactory(new PropertyValueFactory<>("waktu"));
        colRiwayatStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableRiwayat.setItems(listRiwayat);
    }

    private void setupTableKomisi() {
        colKomisiLayanan.setCellValueFactory(new PropertyValueFactory<>("namaLayanan"));
        colKomisiJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colKomisiNilai.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(rupiah.format(cellData.getValue().getNilaiKomisi()));
        });
        tableKomisi.setItems(listKomisi);
    }

    // --- LOAD DATA ---
    private void muatDataPekerjaan() {
        listPekerjaan.clear();
        int pendingCount = 0;
        int doneCount = 0;
        double komisiHariIni = 0;

        // Filter berdasarkan assigned_to = ID Karyawan yang login
        String query = "SELECT dt.id_detail, t.id_transaksi, t.nama_pelanggan, l.nama_layanan, l.harga, t.tanggal, dt.status "
                +
                "FROM detail_transaksi dt " +
                "JOIN transaksi t ON dt.id_transaksi = t.id_transaksi " +
                "JOIN layanan l ON dt.id_layanan = l.id_layanan " +
                "WHERE dt.assigned_to = '" + currentUserId + "' " +
                "ORDER BY CASE WHEN dt.status = 'Selesai' THEN 1 ELSE 0 END, t.tanggal DESC " +
                "LIMIT 50";

        ResultSet rs = DatabaseManager.executeQuery(query);
        try {
            while (rs != null && rs.next()) {
                String status = rs.getString("status");
                if (status == null)
                    status = "Menunggu";
                double harga = rs.getDouble("harga");

                listPekerjaan.add(new PekerjaanModel(
                        rs.getInt("id_detail"),
                        rs.getInt("id_transaksi"),
                        rs.getString("nama_pelanggan"),
                        rs.getString("nama_layanan"),
                        rs.getString("tanggal"),
                        status));

                if ("Menunggu".equalsIgnoreCase(status) || "Menunggu Persetujuan".equalsIgnoreCase(status)) {
                    pendingCount++;
                } else if ("Selesai".equalsIgnoreCase(status)) {
                    doneCount++;
                    komisiHariIni += harga * Komisi;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        lblPending.setText(String.valueOf(pendingCount));
        lblSelesai.setText(String.valueOf(doneCount));
        lblKomisiHariIni.setText(rupiah.format(komisiHariIni));
    }

    private void muatDataRiwayat() {
        listRiwayat.clear();
        // Filter berdasarkan assigned_to = ID Karyawan yang login
        String query = "SELECT dt.id_detail, t.id_transaksi, t.nama_pelanggan, l.nama_layanan, t.tanggal, dt.status " +
                "FROM detail_transaksi dt " +
                "JOIN transaksi t ON dt.id_transaksi = t.id_transaksi " +
                "JOIN layanan l ON dt.id_layanan = l.id_layanan " +
                "WHERE dt.status = 'Selesai' AND dt.assigned_to = '" + currentUserId + "' " +
                "ORDER BY t.tanggal DESC LIMIT 100";

        ResultSet rs = DatabaseManager.executeQuery(query);
        try {
            while (rs != null && rs.next()) {
                listRiwayat.add(new PekerjaanModel(
                        rs.getInt("id_detail"),
                        rs.getInt("id_transaksi"),
                        rs.getString("nama_pelanggan"),
                        rs.getString("nama_layanan"),
                        rs.getString("tanggal"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void muatDataKomisi() {
        listKomisi.clear();
        double totalKomisi = 0;
        int totalPekerjaan = 0;

        // Filter berdasarkan assigned_to = ID Karyawan yang login
        String query = "SELECT l.nama_layanan, COUNT(*) as jumlah, SUM(l.harga) * " + Komisi + " as komisi " +
                "FROM detail_transaksi dt " +
                "JOIN transaksi t ON dt.id_transaksi = t.id_transaksi " +
                "JOIN layanan l ON dt.id_layanan = l.id_layanan " +
                "WHERE dt.status = 'Selesai' AND dt.assigned_to = '" + currentUserId + "' " +
                "GROUP BY l.nama_layanan " +
                "ORDER BY komisi DESC";

        ResultSet rs = DatabaseManager.executeQuery(query);
        try {
            while (rs != null && rs.next()) {
                int jumlah = rs.getInt("jumlah");
                double komisi = rs.getDouble("komisi");
                listKomisi.add(new KomisiModel(
                        rs.getString("nama_layanan"),
                        jumlah,
                        komisi));
                totalKomisi += komisi;
                totalPekerjaan += jumlah;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        lblTotalKomisi.setText(rupiah.format(totalKomisi));
        lblTotalPekerjaan.setText(String.valueOf(totalPekerjaan));
    }

    // --- VIEW SWITCHING ---
    private void switchView(VBox targetView) {
        viewAntrian.setVisible(false);
        viewRiwayat.setVisible(false);
        viewKomisi.setVisible(false);
        targetView.setVisible(true);
    }

    @FXML
    void handleShowAntrian(ActionEvent event) {
        switchView(viewAntrian);
        muatDataPekerjaan();
    }

    @FXML
    void handleShowRiwayat(ActionEvent event) {
        switchView(viewRiwayat);
        muatDataRiwayat();
    }

    @FXML
    void handleShowKomisi(ActionEvent event) {
        switchView(viewKomisi);
        muatDataKomisi();
    }

    // --- ACTIONS ---
    private void aksiSelesaikanPekerjaan(PekerjaanModel data) {
        int confirm = JOptionPane.showConfirmDialog(null,
                "Selesaikan layanan: " + data.getNamaLayanan() + " untuk " + data.getNamaPelanggan() + "?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String query = "UPDATE detail_transaksi SET status = 'Selesai' WHERE id_detail = ?";
            int res = DatabaseManager.executeUpdate(query, data.getIdDetail());

            if (res > 0) {
                JOptionPane.showMessageDialog(null, "Pekerjaan Selesai!");
                muatDataPekerjaan();
            } else {
                JOptionPane.showMessageDialog(null, "Gagal mengupdate data.");
            }
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        muatDataPekerjaan();
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

    @FXML
    void handleLaporKendala(ActionEvent event) {
        LaporKendalaController.showDialog(currentUserId);
    }

    public void setUserData(String id, String nama) {
        this.currentUserId = id;
        this.lblNamaUser.setText("Halo, " + nama);
    }

}