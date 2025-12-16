package Controller;

import Model.Layanan;
import Model.Pelanggan;
import Model.Transaksi;
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
import java.util.Optional;
import javafx.stage.Stage;
import javafx.util.Callback;

import DAO.LayananDAO;
import DAO.TransaksiDAO;
import java.io.IOException;

public class KasirController {

    // --- UI ELEMENTS ---
    @FXML
    private Label lblNamaKasir;
    @FXML
    private TextField txtNamaPelanggan;
    @FXML
    private Label lblTotalHarga;

    // --- TABEL LAYANAN (SUMBER) ---
    @FXML
    private TableView<Layanan> tableLayanan;
    @FXML
    private TableColumn<Layanan, String> colNamaLayanan;
    @FXML
    private TableColumn<Layanan, Double> colHargaLayanan;
    @FXML
    private TableColumn<Layanan, Void> colActionAdd;

    // --- TABEL KERANJANG (TUJUAN) ---
    @FXML
    private TableView<Layanan> tableKeranjang;
    @FXML
    private TableColumn<Layanan, String> colItemNama;
    @FXML
    private TableColumn<Layanan, Double> colItemHarga;

    // --- DATA ---
    private ObservableList<Layanan> daftarLayanan = FXCollections.observableArrayList();
    private ObservableList<Layanan> listKeranjang = FXCollections.observableArrayList();

    // Simpan ID User yang sedang login
    private String currentUserId = "";

    @FXML
    public void initialize() {
        // 1. Setup Data Kasir (Sementara Hardcode, nanti dari Login)
        lblNamaKasir.setText("Kasir Bertugas");
        currentUserId = "KRY-UNKNOWN";

        // 2. Konfigurasi Tabel Layanan (Kiri)
        colNamaLayanan.setCellValueFactory(new PropertyValueFactory<>("namaLayanan"));
        colHargaLayanan.setCellValueFactory(new PropertyValueFactory<>("harga"));

        // Buat Tombol "Tambah" di setiap baris tabel layanan
        addButtonToTable();

        tableLayanan.setItems(daftarLayanan);

        // 3. Konfigurasi Tabel Keranjang (Kanan)
        colItemNama.setCellValueFactory(new PropertyValueFactory<>("namaLayanan"));
        colItemHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        tableKeranjang.setItems(listKeranjang);

        // 4. Load Data dari Database
        muatLayanan();
    }

    // --- LOGIKA LOAD DATA ---
    private void muatLayanan() {
        daftarLayanan.clear();
        LayananDAO dao = new LayananDAO();
        daftarLayanan.addAll(dao.getAllLayanan());
    }

    // --- LOGIKA TOMBOL "TAMBAH" DI TABEL ---
    private void addButtonToTable() {
        Callback<TableColumn<Layanan, Void>, TableCell<Layanan, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Layanan, Void> call(final TableColumn<Layanan, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("+");

                    {
                        btn.getStyleClass().add("btn-primary");
                        btn.setOnAction((ActionEvent event) -> {
                            Layanan data = getTableView().getItems().get(getIndex());
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
    private void tambahKeKeranjang(Layanan item) {
        listKeranjang.add(item);
        hitungTotal();
    }

    private void hitungTotal() {
        double total = Transaksi.hitungTotalDariList(new java.util.ArrayList<>(listKeranjang));
        lblTotalHarga.setText(Transaksi.formatRupiah(total));
    }

    // --- LOGIKA PEMBAYARAN (TRANSAKSI) ---
    @FXML
    void handleBayar(ActionEvent event) {
        // 1. Validasi
        if (listKeranjang.isEmpty()) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Keranjang Kosong", "Pilih layanan terlebih dahulu.");
            return;
        }
        if (txtNamaPelanggan.getText().isEmpty()) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Data Kurang", "Masukkan nama pelanggan.");
            return;
        }

        // 2. Pilih Karyawan terlebih dahulu
        String selectedKaryawanId = showSelectKaryawanDialog();
        if (selectedKaryawanId == null) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Dibatalkan",
                    "Transaksi dibatalkan. Pilih karyawan terlebih dahulu.");
            return;
        }

        // Hitung total menggunakan method dari Model Transaksi
        Pelanggan pelanggan = new Pelanggan(txtNamaPelanggan.getText());
        double totalHarga = Transaksi.hitungTotalDariList(new java.util.ArrayList<>(listKeranjang));

        // 3. Buka dialog proses pembayaran
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/ProsesPembayaran.fxml"));
            Parent root = loader.load();
            ProsesPembayaranController controller = loader.getController();

            // Set data ke controller
            controller.setData(totalHarga, pelanggan.getNama(), listKeranjang);

            // Buat stage modal
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Proses Pembayaran");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            // Callback untuk menyimpan transaksi setelah pembayaran berhasil
            controller.setOnSelesaiCallback(() -> {
                if (controller.isPembayaranBerhasil()) {
                    if (currentUserId.equals("KRY-UNKNOWN")) {
                        currentUserId = DAO.UserDAO.ambilUserIdPertama();
                    }

                    TransaksiDAO trxDao = new TransaksiDAO();
                    int idTransaksi = trxDao.simpanTransaksi(currentUserId, pelanggan.getNama(),
                            totalHarga, controller.getMetodePembayaran(), listKeranjang, selectedKaryawanId);

                    if (idTransaksi != -1) {
                        controller.setIdTransaksi(idTransaksi);
                        controller.tampilkanReceipt();
                    } else {
                        tampilkanPeringatan(Alert.AlertType.ERROR, "Gagal",
                                "Gagal menyimpan transaksi. Silakan coba lagi.");
                    }
                }
            });

            dialogStage.showAndWait();

            // Reset keranjang jika pembayaran berhasil
            if (controller.isPembayaranBerhasil()) {
                handleReset(null);
            }

        } catch (IOException e) {
            e.printStackTrace();
            tampilkanPeringatan(Alert.AlertType.ERROR, "Error", "Gagal membuka dialog pembayaran.");
        }
    }

    /**
     * Dialog untuk memilih Karyawan yang akan mengerjakan pesanan
     * 
     * @return ID Karyawan yang dipilih, atau null jika dibatalkan
     */
    private String showSelectKaryawanDialog() {
        ObservableList<String[]> listKaryawan = FXCollections.observableArrayList();
        listKaryawan.addAll(DAO.UserDAO.getKaryawanAktif());

        if (listKaryawan.isEmpty()) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Tidak Ada Karyawan",
                    "Tidak ada karyawan aktif yang tersedia.");
            return null;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Pilih Karyawan");
        dialog.setHeaderText("Pilih stylist yang akan mengerjakan pesanan ini");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/Style/kasir.css").toExternalForm());

        ButtonType pilihButtonType = new ButtonType("Pilih", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(pilihButtonType, ButtonType.CANCEL);

        ComboBox<String> comboKaryawan = new ComboBox<>();
        for (String[] kry : listKaryawan) {
            comboKaryawan.getItems().add(kry[0] + " - " + kry[1]);
        }
        comboKaryawan.setPromptText("-- Pilih Karyawan --");
        comboKaryawan.setPrefWidth(300);

        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(20));
        content.getChildren().addAll(
                new Label("Karyawan yang tersedia:"),
                comboKaryawan);

        content.getStyleClass().add("karyawan-dialog-content");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getStyleClass().add("karyawan-dialog-pane");

        Node pilihButton = dialog.getDialogPane().lookupButton(pilihButtonType);
        pilihButton.setDisable(true);
        comboKaryawan.valueProperty().addListener((obs, oldVal, newVal) -> {
            pilihButton.setDisable(newVal == null || newVal.isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == pilihButtonType && comboKaryawan.getValue() != null) {
                String selected = comboKaryawan.getValue();
                return selected.split(" - ")[0];
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
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

    private void tampilkanPeringatan(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- METHOD UNTUK MENERIMA DATA USER DARI LOGIN ---
    public void setKasirData(String idUser, String nama) {
        this.currentUserId = idUser;
        this.lblNamaKasir.setText("Kasir: " + nama);
    }

    @FXML
    void handleLaporKendala(ActionEvent event) {
        LaporKendalaController.showDialog(currentUserId);
    }
}
