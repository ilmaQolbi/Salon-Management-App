package Controller;

import DAO.PekerjaanDAO;
import Model.PekerjaanModel;
import Model.KomisiModel;
import Model.Karyawan;
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

import java.io.IOException;
import java.util.Optional;

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
    @FXML
    private Label lblEstimasiGaji;

    // Views
    @FXML
    private VBox viewAntrian;
    @FXML
    private VBox viewRiwayat;
    @FXML
    private VBox viewKomisi;
    @FXML
    private VBox viewStatistik;
    @FXML
    private KaryawanStatistikController statistikViewController;

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

    // Konstanta dipindahkan ke Model Karyawan
    // Gunakan Karyawan.PERSENTASE_KOMISI dan Karyawan.GAJI_POKOK

    @FXML
    public void initialize() {
        // Setup Table Antrian
        setupTableAntrian();

        // Setup Table Riwayat
        setupTableRiwayat();

        // Setup Table Komisi
        setupTableKomisi();

        // NOTE: Data akan di-load setelah setUserData() dipanggil
        // karena currentUserId masih null di sini
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
            // Gunakan method dari Model Karyawan
            return new SimpleStringProperty(Karyawan.formatRupiah(cellData.getValue().getNilaiKomisi()));
        });
        tableKomisi.setItems(listKomisi);
    }

    // --- LOAD DATA ---
    private void muatDataPekerjaan() {
        listPekerjaan.clear();

        // Gunakan DAO untuk mengambil data dan statistik
        listPekerjaan.addAll(PekerjaanDAO.getPekerjaanByKaryawan(currentUserId));
        double[] stats = PekerjaanDAO.hitungStatistikPekerjaan(listPekerjaan, currentUserId);

        lblPending.setText(String.valueOf((int) stats[0]));
        lblSelesai.setText(String.valueOf((int) stats[1]));
        lblKomisiHariIni.setText(Karyawan.formatRupiah(stats[2]));
    }

    private void muatDataRiwayat() {
        listRiwayat.clear();
        // Gunakan DAO untuk mengambil data
        listRiwayat.addAll(PekerjaanDAO.getRiwayatPekerjaan(currentUserId));
    }

    private void muatDataKomisi() {
        listKomisi.clear();
        double totalKomisi = 0;
        int totalPekerjaan = 0;

        // Gunakan DAO untuk mengambil data komisi
        java.util.List<KomisiModel> komisiList = PekerjaanDAO.getKomisiBulanIni(currentUserId);
        listKomisi.addAll(komisiList);

        // Hitung total dari list
        for (KomisiModel km : komisiList) {
            totalKomisi += km.getNilaiKomisi();
            totalPekerjaan += km.getJumlah();
        }

        // Gunakan method dari Model Karyawan
        lblTotalKomisi.setText(Karyawan.formatRupiah(totalKomisi));
        lblTotalPekerjaan.setText(String.valueOf(totalPekerjaan));

        // Hitung Estimasi Gaji menggunakan method dari Model Karyawan
        double estimasiGaji = Karyawan.hitungEstimasiGaji(totalKomisi);
        lblEstimasiGaji.setText(Karyawan.formatRupiah(estimasiGaji));
    }

    // --- VIEW SWITCHING ---
    private void switchView(VBox targetView) {
        viewAntrian.setVisible(false);
        viewRiwayat.setVisible(false);
        viewKomisi.setVisible(false);
        viewStatistik.setVisible(false);
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

    @FXML
    void handleShowStatistik(ActionEvent event) {
        switchView(viewStatistik);
        if (statistikViewController != null) {
            statistikViewController.setKaryawanId(currentUserId);
        }
    }

    // --- ACTIONS ---
    private void aksiSelesaikanPekerjaan(PekerjaanModel data) {
        // Gunakan JavaFX Alert untuk konfirmasi
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText(
                "Selesaikan layanan: " + data.getNamaLayanan() + " untuk " + data.getNamaPelanggan() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Gunakan DAO untuk update status
            boolean success = PekerjaanDAO.selesaikanPekerjaan(data.getIdDetail());

            if (success) {
                tampilkanAlert(Alert.AlertType.INFORMATION, "Berhasil", "Pekerjaan Selesai!");
                muatDataPekerjaan();
            } else {
                tampilkanAlert(Alert.AlertType.ERROR, "Gagal", "Gagal mengupdate data.");
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

            // Simpan status maximized sebelum ganti scene
            boolean wasMaximized = stage.isMaximized();

            // Buat scene dengan ukuran yang sama
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);

            // Restore maximized state
            if (wasMaximized) {
                stage.setMaximized(true);
            }
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

        // Load data setelah userId tersedia
        muatDataPekerjaan();
    }

    // Helper method untuk menampilkan Alert JavaFX
    private void tampilkanAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}