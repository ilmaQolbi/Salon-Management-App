package Controller;

import DAO.DatabaseManager;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class KasirController {

    // --- UI ELEMENTS ---
    @FXML private Label lblNamaKasir;
    @FXML private TextField txtNamaPelanggan;
    @FXML private Label lblTotalHarga;

    // --- TABEL LAYANAN (SUMBER) ---
    @FXML private TableView<LayananModel> tableLayanan;
    @FXML private TableColumn<LayananModel, String> colNamaLayanan;
    @FXML private TableColumn<LayananModel, Double> colHargaLayanan;
    @FXML private TableColumn<LayananModel, Void> colActionAdd;

    // --- TABEL KERANJANG (TUJUAN) ---
    @FXML private TableView<LayananModel> tableKeranjang;
    @FXML private TableColumn<LayananModel, String> colItemNama;
    @FXML private TableColumn<LayananModel, Double> colItemHarga;

    // --- DATA ---
    private ObservableList<LayananModel> DaftarLayanan = FXCollections.observableArrayList();
    private ObservableList<LayananModel> listKeranjang = FXCollections.observableArrayList();

    // Simpan ID User yang sedang login (Nanti bisa diambil dari Session)
    private String currentUserId = "KRY-UNKNOWN";

    @FXML
    public void initialize() {
        // 1. Setup Data Kasir (Sementara Hardcode, nanti dari Login)
        lblNamaKasir.setText("Kasir Bertugas");

        // 2. Konfigurasi Tabel Layanan (Kiri)
        colNamaLayanan.setCellValueFactory(new PropertyValueFactory<>("namaLayanan"));
        colHargaLayanan.setCellValueFactory(new PropertyValueFactory<>("harga"));

        // Buat Tombol "Tambah" di setiap baris tabel layanan
        addButtonToTable();

        tableLayanan.setItems(DaftarLayanan);

        // 3. Konfigurasi Tabel Keranjang (Kanan)
        colItemNama.setCellValueFactory(new PropertyValueFactory<>("namaLayanan"));
        colItemHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        tableKeranjang.setItems(listKeranjang);

        // 4. Load Data dari Database
        loadLayanan();
    }

    // --- LOGIKA LOAD DATA ---
    private void loadLayanan() {
        DaftarLayanan.clear();
        String query = "SELECT * FROM layanan";
        ResultSet rs = DatabaseManager.executeQuery(query);
        try {
            while (rs != null && rs.next()) {
                DaftarLayanan.add(new LayananModel(
                        rs.getInt("id_layanan"),
                        rs.getString("nama_layanan"),
                        rs.getDouble("harga")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- LOGIKA TOMBOL "TAMBAH" DI TABEL ---
    private void addButtonToTable() {
        Callback<TableColumn<LayananModel, Void>, TableCell<LayananModel, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<LayananModel, Void> call(final TableColumn<LayananModel, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("+");

                    {
                        btn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold;");
                        btn.setOnAction((ActionEvent event) -> {
                            LayananModel data = getTableView().getItems().get(getIndex());
                            tambahKeKeranjang(data);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };
        colActionAdd.setCellFactory(cellFactory);
    }

    // --- LOGIKA KERANJANG ---
    private void tambahKeKeranjang(LayananModel item) {
        listKeranjang.add(item);
        hitungTotal();
    }

    private void hitungTotal() {
        double total = 0;
        for (LayananModel item : listKeranjang) {
            total += item.getHarga();
        }
        // Format ke Rupiah
        NumberFormat currenyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        lblTotalHarga.setText(currenyFormat.format(total));
    }

    // --- LOGIKA PEMBAYARAN (TRANSAKSI) ---
    @FXML
    void handleBayar(ActionEvent event) {
        // 1. Validasi
        if (listKeranjang.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Keranjang Kosong", "Pilih layanan terlebih dahulu.");
            return;
        }
        if (txtNamaPelanggan.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Data Kurang", "Masukkan nama pelanggan.");
            return;
        }

        String namaPelanggan = txtNamaPelanggan.getText();
        double totalHarga = listKeranjang.stream().mapToDouble(LayananModel::getHarga).sum();

        // 2. Insert ke Tabel Header (TRANSAKSI)
        // Pastikan currentUserId sesuai dengan ID user yang login
        // Disini kita ambil ID user pertama dari database sebagai fallback jika session belum dibuat
        if(currentUserId.equals("KRY-UNKNOWN")) {
            currentUserId = getFirstUserId();
        }

        String sqlHeader = "INSERT INTO transaksi (id_user, nama_pelanggan, total_harga, tanggal) VALUES (?, ?, ?, NOW())";
        // Kita butuh executeUpdate yang mengembalikan ID generated, tapi DatabaseManager kamu sederhana.
        // Jadi kita insert dulu, lalu ambil ID terakhir.

        int res = DatabaseManager.executeUpdate(sqlHeader, currentUserId, namaPelanggan, totalHarga);

        if (res > 0) {
            // 3. Ambil ID Transaksi Terakhir
            int idTransaksi = getLastTransactionId();

            // 4. Insert Detail Transaksi (Looping keranjang)
            for (LayananModel item : listKeranjang) {
                String sqlDetail = "INSERT INTO detail_transaksi (id_transaksi, id_layanan, harga_saat_itu) VALUES (?, ?, ?)";
                DatabaseManager.executeUpdate(sqlDetail, idTransaksi, item.getIdLayanan(), item.getHarga());
            }

            // 5. Sukses & Reset
            showAlert(Alert.AlertType.INFORMATION, "Transaksi Berhasil", "Data transaksi telah disimpan.\nTotal: Rp " + totalHarga);
            handleReset(null);
        } else {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menyimpan transaksi.");
        }
    }

    @FXML
    void handleReset(ActionEvent event) {
        listKeranjang.clear();
        txtNamaPelanggan.clear();
        lblTotalHarga.setText("Rp 0");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- HELPER SQL ---
    private int getLastTransactionId() {
        String query = "SELECT MAX(id_transaksi) as last_id FROM transaksi";
        ResultSet rs = DatabaseManager.executeQuery(query);
        try {
            if (rs != null && rs.next()) {
                return rs.getInt("last_id");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private String getFirstUserId() {
        // Fallback method: mengambil sembarang ID user agar tidak error foreign key
        String query = "SELECT idUser FROM users LIMIT 1";
        ResultSet rs = DatabaseManager.executeQuery(query);
        try {
            if (rs != null && rs.next()) return rs.getString("idUser");
        } catch (SQLException e) {}
        return "ADM001"; // Default extreme fallback
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- METHOD UNTUK MENERIMA DATA USER DARI LOGIN (OPSIONAL) ---
    public void setKasirData(String idUser, String nama) {
        this.currentUserId = idUser;
        this.lblNamaKasir.setText("Kasir: " + nama);
    }

    // --- MODEL CLASS (INNER) ---
    public static class LayananModel {
        private int idLayanan;
        private String namaLayanan;
        private double harga;

        public LayananModel(int id, String nama, double harga) {
            this.idLayanan = id;
            this.namaLayanan = nama;
            this.harga = harga;
        }
        public int getIdLayanan() { return idLayanan; }
        public String getNamaLayanan() { return namaLayanan; }
        public Double getHarga() { return harga; }
    }
}