package Controller;

import DAO.DatabaseManager;
import Model.Layanan;
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
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.Optional;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

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
    private ObservableList<Layanan> DaftarLayanan = FXCollections.observableArrayList();
    private ObservableList<Layanan> listKeranjang = FXCollections.observableArrayList();

    // Simpan ID User yang sedang login (Nanti bisa diambil dari Session)
    private String currentUserId = ""; // Simpan ID User yang login

    @FXML
    public void initialize() {
        // 1. Setup Data Kasir (Sementara Hardcode, nanti dari Login)
        lblNamaKasir.setText("Kasir Bertugas");
        currentUserId = "KRY-UNKNOWN"; // Default jika belum diset

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
        muatLayanan();
    }

    // --- LOGIKA LOAD DATA ---
    private void muatLayanan() {
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
        double total = 0;
        for (Layanan item : listKeranjang) {
            total += item.getHarga();
        }
        // Format ke Rupiah
        NumberFormat currenyFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));
        lblTotalHarga.setText(currenyFormat.format(total));
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

        String namaPelanggan = txtNamaPelanggan.getText();
        double totalHarga = listKeranjang.stream().mapToDouble(Layanan::getHarga).sum();

        // 2. Tampilkan Dialog Pilih Metode Pembayaran
        String metodePembayaran = showPaymentMethodDialog(totalHarga);

        // Jika user membatalkan dialog
        if (metodePembayaran == null) {
            return;
        }

        // 3. Proses sesuai metode pembayaran yang dipilih
        boolean paymentConfirmed = false;
        String paymentDetails = "";

        switch (metodePembayaran) {
            case "Tunai":
                Double[] cashResult = showCashPaymentDialog(totalHarga);
                if (cashResult != null) {
                    paymentConfirmed = true;
                    paymentDetails = String.format("Bayar: %s | Kembalian: %s",
                            formatMataUang(cashResult[0]), formatMataUang(cashResult[1]));
                }
                break;
            case "Transfer":
                paymentConfirmed = showTransferPaymentDialog(totalHarga);
                if (paymentConfirmed) {
                    paymentDetails = "Bank BNI - 0234503418";
                }
                break;
            case "E-Wallet":
                paymentConfirmed = showEWalletPaymentDialog(totalHarga);
                if (paymentConfirmed) {
                    paymentDetails = "Pembayaran via QR Code";
                }
                break;
        }

        // Jika pembayaran dibatalkan
        if (!paymentConfirmed) {
            return;
        }

        // 4. Pilih Karyawan yang akan mengerjakan
        String selectedKaryawanId = showSelectKaryawanDialog();
        if (selectedKaryawanId == null) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Dibatalkan",
                    "Transaksi dibatalkan. Pilih karyawan terlebih dahulu.");
            return;
        }

        // 5. Insert ke Tabel Header (TRANSAKSI)
        if (currentUserId.equals("KRY-UNKNOWN")) {
            currentUserId = getFirstUserId();
        }

        String sqlHeader = "INSERT INTO transaksi (id_user, nama_pelanggan, total_harga, metode_pembayaran, tanggal) VALUES (?, ?, ?, ?, NOW())";

        int res = DatabaseManager.executeUpdate(sqlHeader, currentUserId, namaPelanggan, totalHarga, metodePembayaran);

        if (res > 0) {
            // 6. Ambil ID Transaksi Terakhir
            int idTransaksi = getLastTransactionId();

            // 7. Insert Detail Transaksi dengan assigned_to karyawan
            for (Layanan item : listKeranjang) {
                String sqlDetail = "INSERT INTO detail_transaksi (id_transaksi, id_layanan, harga_saat_itu, status, assigned_to) VALUES (?, ?, ?, 'Menunggu', ?)";
                DatabaseManager.executeUpdate(sqlDetail, idTransaksi, item.getIdLayanan(), item.getHarga(),
                        selectedKaryawanId);
            }

            // 8. Tampilkan Struk/Receipt
            showReceiptDialog(namaPelanggan, totalHarga, metodePembayaran, paymentDetails, idTransaksi);
            handleReset(null);
        } else {
            tampilkanPeringatan(Alert.AlertType.ERROR, "Gagal", "Gagal menyimpan transaksi.");
        }
    }

    /**
     * Format angka ke format Rupiah
     */
    private String formatMataUang(double amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));
        return currencyFormat.format(amount);
    }

    /**
     * Dialog pembayaran TUNAI - input uang cash dan hitung kembalian
     * 
     * @return array [uangDibayar, kembalian] atau null jika dibatalkan
     */
    private Double[] showCashPaymentDialog(double totalHarga) {
        Dialog<Double[]> dialog = new Dialog<>();
        dialog.setTitle("Pembayaran Tunai");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/Style/kasir.css").toExternalForm());

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("dialog-content");
        content.setPrefWidth(350);

        // Icon dan Title
        Label lblIcon = new Label("💵");
        lblIcon.getStyleClass().add("icon-label");

        Label lblTitle = new Label("PEMBAYARAN TUNAI");
        lblTitle.getStyleClass().add("title-label");

        // Total yang harus dibayar
        Label lblTotalLabel = new Label("Total yang harus dibayar:");
        lblTotalLabel.getStyleClass().add("total-label-header");

        Label lblTotal = new Label(formatMataUang(totalHarga));
        lblTotal.getStyleClass().add("total-value-label");

        // Input uang yang diterima
        Label lblInputLabel = new Label("Uang yang diterima:");
        lblInputLabel.getStyleClass().add("instruction-label");

        TextField txtCash = new TextField();
        txtCash.setPromptText("Masukkan jumlah uang...");
        txtCash.getStyleClass().add("cash-input-field");
        txtCash.setAlignment(Pos.CENTER);

        // Label kembalian
        Label lblKembalianLabel = new Label("Kembalian:");
        lblKembalianLabel.getStyleClass().add("instruction-label");

        Label lblKembalian = new Label("Rp 0");
        lblKembalian.getStyleClass().add("kembalian-value");

        // Update kembalian saat input berubah
        txtCash.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                String cleanedVal = newVal.replaceAll("[^0-9]", "");
                if (!cleanedVal.isEmpty()) {
                    double cash = Double.parseDouble(cleanedVal);
                    double kembalian = cash - totalHarga;
                    if (kembalian >= 0) {
                        lblKembalian.setText(formatMataUang(kembalian));
                        lblKembalian.getStyleClass().setAll("kembalian-value");
                    } else {
                        lblKembalian.setText("Uang kurang!");
                        lblKembalian.getStyleClass().setAll("kembalian-value-error");
                    }
                } else {
                    lblKembalian.setText("Rp 0");
                }
            } catch (NumberFormatException e) {
                lblKembalian.setText("Input tidak valid");
                lblKembalian.getStyleClass().setAll("kembalian-value-invalid");
            }
        });

        content.getChildren().addAll(lblIcon, lblTitle, lblTotalLabel, lblTotal,
                lblInputLabel, txtCash, lblKembalianLabel, lblKembalian);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Styling tombol OK
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Proses Pembayaran");
        okButton.getStyleClass().add("btn-primary");

        // Styling tombol Cancel
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Batal");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    String cleanedVal = txtCash.getText().replaceAll("[^0-9]", "");
                    double cash = Double.parseDouble(cleanedVal);
                    double kembalian = cash - totalHarga;
                    if (kembalian >= 0) {
                        return new Double[] { cash, kembalian };
                    }
                } catch (NumberFormatException e) {
                    // Invalid input
                }
            }
            return null;
        });

        Optional<Double[]> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Dialog pembayaran TRANSFER - menampilkan info rekening bank
     * 
     * @return true jika dikonfirmasi, false jika dibatalkan
     */
    private boolean showTransferPaymentDialog(double totalHarga) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Pembayaran Transfer Bank");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/Style/kasir.css").toExternalForm());

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("dialog-content");
        content.setPrefWidth(380);

        // Icon dan Title
        Label lblIcon = new Label("🏦");
        lblIcon.getStyleClass().add("icon-label");

        Label lblTitle = new Label("TRANSFER BANK");
        lblTitle.getStyleClass().add("title-label");

        // Total
        Label lblTotalLabel = new Label("Total Pembayaran:");
        lblTotalLabel.getStyleClass().add("total-label-header");

        Label lblTotal = new Label(formatMataUang(totalHarga));
        lblTotal.getStyleClass().add("total-value-label");

        // Separator
        Label lblSeparator = new Label("─────────────────────────");
        lblSeparator.getStyleClass().add("separator-line");

        // Info Bank
        Label lblBankLabel = new Label("Transfer ke rekening:");
        lblBankLabel.getStyleClass().add("instruction-label");

        // Bank Logo/Name
        HBox bankBox = new HBox(10);
        bankBox.setAlignment(Pos.CENTER);
        Label lblBankName = new Label("🏛️ Bank BNI");
        lblBankName.getStyleClass().add("bank-name");
        bankBox.getChildren().add(lblBankName);

        // Nomor Rekening
        Label lblRekLabel = new Label("Nomor Rekening:");
        lblRekLabel.getStyleClass().add("instruction-label");

        Label lblRekening = new Label("0234503418");
        lblRekening.getStyleClass().add("rekening-value");

        // Nama Pemilik
        Label lblNamaPemilik = new Label("a.n. Salon Beauty");
        lblNamaPemilik.getStyleClass().add("owner-name");

        // Instruksi
        Label lblInstruksi = new Label("⚠️ Pastikan jumlah transfer sesuai");
        lblInstruksi.getStyleClass().add("warning-instruction");

        content.getChildren().addAll(lblIcon, lblTitle, lblTotalLabel, lblTotal, lblSeparator,
                lblBankLabel, bankBox, lblRekLabel, lblRekening, lblNamaPemilik, lblInstruksi);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Styling tombol
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Konfirmasi Sudah Transfer");
        okButton.getStyleClass().add("btn-primary");

        // Styling tombol Cancel
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Batal");

        dialog.setResultConverter(dialogButton -> dialogButton == ButtonType.OK);

        Optional<Boolean> result = dialog.showAndWait();
        return result.orElse(false);
    }

    /**
     * Dialog pembayaran E-WALLET - menampilkan QR Code
     * 
     * @return true jika dikonfirmasi, false jika dibatalkan
     */
    private boolean showEWalletPaymentDialog(double totalHarga) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Pembayaran E-Wallet");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/Style/kasir.css").toExternalForm());

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("dialog-content");
        content.setPrefWidth(380);

        // Icon dan Title
        Label lblIcon = new Label("📱");
        lblIcon.getStyleClass().add("icon-label");

        Label lblTitle = new Label("PEMBAYARAN E-WALLET");
        lblTitle.getStyleClass().add("title-label");

        // Total
        Label lblTotalLabel = new Label("Total Pembayaran:");
        lblTotalLabel.getStyleClass().add("total-label-header");

        Label lblTotal = new Label(formatMataUang(totalHarga));
        lblTotal.getStyleClass().add("total-value-label");

        // QR Code
        Label lblScanLabel = new Label("Scan QR Code untuk membayar:");
        lblScanLabel.getStyleClass().add("instruction-label");

        ImageView qrImageView = new ImageView();
        try {
            Image qrImage = new Image(getClass().getResourceAsStream("/Gambar/qr_payment.png"));
            qrImageView.setImage(qrImage);
            qrImageView.setFitWidth(180);
            qrImageView.setFitHeight(180);
            qrImageView.setPreserveRatio(true);
            qrImageView.getStyleClass().add("qr-image-view");
        } catch (Exception e) {
            // Fallback jika gambar tidak ditemukan
            Label lblQRPlaceholder = new Label("📲 QR CODE");
            lblQRPlaceholder.getStyleClass().add("qr-placeholder");
        }

        // Supported e-wallets
        Label lblSupported = new Label("Didukung: OVO • GoPay • DANA • ShopeePay • LinkAja");
        lblSupported.getStyleClass().add("supported-wallets");

        // Instruksi
        Label lblInstruksi = new Label("⚠️ Tekan konfirmasi setelah pembayaran berhasil");
        lblInstruksi.getStyleClass().add("warning-instruction");

        content.getChildren().addAll(lblIcon, lblTitle, lblTotalLabel, lblTotal,
                lblScanLabel, qrImageView, lblSupported, lblInstruksi);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Styling tombol
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Konfirmasi Pembayaran");
        okButton.getStyleClass().add("btn-primary");

        // Styling tombol Cancel
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Batal");

        dialog.setResultConverter(dialogButton -> dialogButton == ButtonType.OK);

        Optional<Boolean> result = dialog.showAndWait();
        return result.orElse(false);
    }

    /**
     * Menampilkan struk/receipt setelah transaksi berhasil
     */
    private void showReceiptDialog(String namaPelanggan, double totalHarga,
            String metodePembayaran, String paymentDetails, int idTransaksi) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Struk Pembayaran");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/Style/kasir.css").toExternalForm());

        VBox content = new VBox(8);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("receipt-content");
        content.setPrefWidth(320);

        // Header Struk
        Label lblStoreName = new Label("✨ SALON BEAUTY ✨");
        lblStoreName.getStyleClass().add("store-name");

        Label lblAddress = new Label("Jl. Veteran No. 8");
        lblAddress.getStyleClass().add("store-address");

        Label lblSeparator1 = new Label("═══════════════════════════════");
        lblSeparator1.getStyleClass().add("receipt-separator");

        // Info Transaksi
        Label lblTrxId = new Label("No. Transaksi: #" + String.format("%06d", idTransaksi));
        lblTrxId.getStyleClass().add("receipt-info");

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label lblDateTime = new Label("Tanggal: " + now.format(formatter));
        lblDateTime.getStyleClass().add("receipt-info");

        Label lblPelanggan = new Label("Pelanggan: " + namaPelanggan);
        lblPelanggan.getStyleClass().add("receipt-info");

        Label lblSeparator2 = new Label("───────────────────────────────");
        lblSeparator2.getStyleClass().add("receipt-separator");

        // Items
        VBox itemsBox = new VBox(3);
        itemsBox.setAlignment(Pos.CENTER_LEFT);
        for (Layanan item : listKeranjang) {
            Label lblItem = new Label("  • " + item.getNamaLayanan());
            lblItem.getStyleClass().add("receipt-item-name");
            Label lblItemPrice = new Label("    " + formatMataUang(item.getHarga()));
            lblItemPrice.getStyleClass().add("receipt-item-price");
            itemsBox.getChildren().addAll(lblItem, lblItemPrice);
        }

        Label lblSeparator3 = new Label("───────────────────────────────");
        lblSeparator3.getStyleClass().add("receipt-separator");

        // Total
        Label lblTotalLabel = new Label("TOTAL");
        lblTotalLabel.getStyleClass().add("receipt-total-label");

        Label lblTotalValue = new Label(formatMataUang(totalHarga));
        lblTotalValue.getStyleClass().add("receipt-total-value");

        Label lblSeparator4 = new Label("───────────────────────────────");
        lblSeparator4.getStyleClass().add("receipt-separator");

        // Payment Info
        Label lblMetode = new Label("Metode: " + metodePembayaran);
        lblMetode.getStyleClass().add("receipt-method");

        Label lblPaymentDetails = new Label(paymentDetails);
        lblPaymentDetails.getStyleClass().add("receipt-info");

        Label lblSeparator5 = new Label("═══════════════════════════════");
        lblSeparator5.getStyleClass().add("receipt-separator");

        // Footer
        Label lblThankYou = new Label("🙏 Terima Kasih 🙏");
        lblThankYou.getStyleClass().add("receipt-thankyou");

        Label lblVisitAgain = new Label("Sampai jumpa kembali!");
        lblVisitAgain.getStyleClass().add("receipt-visit-again");

        content.getChildren().addAll(lblStoreName, lblAddress, lblSeparator1,
                lblTrxId, lblDateTime, lblPelanggan, lblSeparator2,
                itemsBox, lblSeparator3,
                lblTotalLabel, lblTotalValue, lblSeparator4,
                lblMetode, lblPaymentDetails, lblSeparator5,
                lblThankYou, lblVisitAgain);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Selesai");
        okButton.getStyleClass().add("btn-primary");

        dialog.showAndWait();
    }

    /**
     * Menampilkan dialog untuk memilih metode pembayaran
     * 
     * @param totalHarga Total harga yang harus dibayar
     * @return Metode pembayaran yang dipilih, atau null jika dibatalkan
     */
    private String showPaymentMethodDialog(double totalHarga) {
        // Buat dialog kustom
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Pilih Metode Pembayaran");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/Style/kasir.css").toExternalForm());

        // Format total harga
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));

        // Container utama
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("payment-method-content");

        // Label total
        Label lblTotal = new Label("Total: " + currencyFormat.format(totalHarga));
        lblTotal.getStyleClass().add("title-label");
        // We can reuse title-label or create specific one, but title-label is similar
        // enough (18px bold)
        // Wait, original was 20px bold. Let's stick with title-label or override if
        // needed.
        // Actually, let's just use "total-value-label" but smaller? Or just
        // "title-label" which is 18px.
        // The original was 20px. 18px is close enough.

        // Label instruksi
        Label lblInstruksi = new Label("Pilih metode pembayaran:");
        lblInstruksi.getStyleClass().add("instruction-label");

        // Tombol Tunai
        Button btnTunai = new Button("💵  TUNAI (Uang Tunai)");
        btnTunai.setPrefWidth(280);
        btnTunai.setPrefHeight(50);
        btnTunai.getStyleClass().add("btn-payment-tunai");

        // Tombol Transfer
        Button btnTransfer = new Button("🏦  TRANSFER Bank");
        btnTransfer.setPrefWidth(280);
        btnTransfer.setPrefHeight(50);
        btnTransfer.getStyleClass().add("btn-payment-transfer");

        // Tombol E-Wallet
        Button btnEWallet = new Button("📱  E-WALLET (OVO/GoPay/DANA)");
        btnEWallet.setPrefWidth(280);
        btnEWallet.setPrefHeight(50);
        btnEWallet.getStyleClass().add("btn-payment-ewallet");

        // Set action untuk setiap tombol
        btnTunai.setOnAction(e -> {
            dialog.setResult("Tunai");
            dialog.close();
        });

        btnTransfer.setOnAction(e -> {
            dialog.setResult("Transfer");
            dialog.close();
        });

        btnEWallet.setOnAction(e -> {
            dialog.setResult("E-Wallet");
            dialog.close();
        });

        // Tambahkan semua elemen ke container
        content.getChildren().addAll(lblTotal, lblInstruksi, btnTunai, btnTransfer, btnEWallet);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #efebe9;");

        // Ubah teks tombol Cancel menjadi Batal
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Batal");

        // Handle cancel
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.CANCEL) {
                return null;
            }
            return dialog.getResult();
        });

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Dialog untuk memilih Karyawan yang akan mengerjakan pesanan
     * 
     * @return ID Karyawan yang dipilih, atau null jika dibatalkan
     */
    private String showSelectKaryawanDialog() {
        // Load daftar karyawan dari database
        ObservableList<String[]> listKaryawan = FXCollections.observableArrayList();
        String query = "SELECT idUser, nama FROM users WHERE role = 'Karyawan' AND status = 'Aktif' ORDER BY nama";
        ResultSet rs = DatabaseManager.executeQuery(query);

        try {
            while (rs != null && rs.next()) {
                listKaryawan.add(new String[] { rs.getString("idUser"), rs.getString("nama") });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (listKaryawan.isEmpty()) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Tidak Ada Karyawan",
                    "Tidak ada karyawan aktif yang tersedia.");
            return null;
        }

        // Create dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Pilih Karyawan");
        dialog.setHeaderText("Pilih stylist yang akan mengerjakan pesanan ini");

        // Buttons
        ButtonType pilihButtonType = new ButtonType("Pilih", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(pilihButtonType, ButtonType.CANCEL);

        // ComboBox untuk pilih karyawan
        ComboBox<String> comboKaryawan = new ComboBox<>();
        for (String[] kry : listKaryawan) {
            comboKaryawan.getItems().add(kry[0] + " - " + kry[1]); // "KRY-001 - Nama Karyawan"
        }
        comboKaryawan.setPromptText("-- Pilih Karyawan --");
        comboKaryawan.setPrefWidth(300);

        // Layout
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(20));
        content.getChildren().addAll(
                new Label("Karyawan yang tersedia:"),
                comboKaryawan);

        // Styling
        content.setStyle("-fx-background-color: #fdfbf7;");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: #fdfbf7;");

        // Disable Pilih button if no selection
        Node pilihButton = dialog.getDialogPane().lookupButton(pilihButtonType);
        pilihButton.setDisable(true);
        comboKaryawan.valueProperty().addListener((obs, oldVal, newVal) -> {
            pilihButton.setDisable(newVal == null || newVal.isEmpty());
        });

        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == pilihButtonType && comboKaryawan.getValue() != null) {
                // Extract ID from "KRY-001 - Nama"
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

    // --- HELPER SQL ---
    private int getLastTransactionId() {
        String query = "SELECT MAX(id_transaksi) as last_id FROM transaksi";
        ResultSet rs = DatabaseManager.executeQuery(query);
        try {
            if (rs != null && rs.next()) {
                return rs.getInt("last_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String getFirstUserId() {
        // Fallback method: mengambil sembarang ID user agar tidak error foreign key
        String query = "SELECT idUser FROM users LIMIT 1";
        ResultSet rs = DatabaseManager.executeQuery(query);
        try {
            if (rs != null && rs.next())
                return rs.getString("idUser");
        } catch (SQLException e) {
        }
        return "ADM001"; // Default extreme fallback
    }

    private void tampilkanPeringatan(Alert.AlertType type, String title, String content) {
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

    @FXML
    void handleLaporKendala(ActionEvent event) {
        // Panggil dialog reusable dari LaporKendalaController
        LaporKendalaController.showDialog(currentUserId);
    }

}
