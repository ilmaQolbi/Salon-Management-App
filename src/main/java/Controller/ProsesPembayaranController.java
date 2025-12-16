package Controller;

import Model.Layanan;
import Model.Transaksi;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller untuk ProsesPembayaran.fxml
 * Menangani semua tahap proses pembayaran dalam satu controller.
 */
public class ProsesPembayaranController {

    // === VIEW CONTAINERS ===
    @FXML
    private VBox viewPilihMetode;
    @FXML
    private VBox viewTunai;
    @FXML
    private VBox viewTransfer;
    @FXML
    private VBox viewEWallet;
    @FXML
    private VBox viewReceipt;

    // === LABELS TOTAL ===
    @FXML
    private Label lblTotalMetode;
    @FXML
    private Label lblTotalTunai;
    @FXML
    private Label lblTotalTransfer;
    @FXML
    private Label lblTotalEWallet;
    @FXML
    private Label lblTotalReceipt;

    // === CASH PAYMENT ===
    @FXML
    private TextField txtUangDibayar;
    @FXML
    private Label lblKembalian;
    @FXML
    private Button btnProsesTunai;

    // === E-WALLET ===
    @FXML
    private ImageView qrImageView;

    // === RECEIPT ===
    @FXML
    private Label lblTrxId;
    @FXML
    private Label lblDateTime;
    @FXML
    private Label lblPelanggan;
    @FXML
    private VBox itemsBox;
    @FXML
    private Label lblMetode;
    @FXML
    private Label lblPaymentDetails;

    // === DATA ===
    private double totalHarga;
    private String namaPelanggan;
    private ObservableList<Layanan> listKeranjang;
    private String metodePembayaran;
    private String paymentDetails;
    private int idTransaksi;
    private boolean pembayaranBerhasil = false;

    // === CALLBACK ===
    private Runnable onSelesaiCallback;

    @FXML
    public void initialize() {
        // Listener untuk input uang tunai
        txtUangDibayar.textProperty().addListener((obs, oldVal, newVal) -> {
            hitungKembalian(newVal);
        });

        // Load QR Image
        try {
            Image qrImage = new Image(getClass().getResourceAsStream("/Gambar/qr_payment.png"));
            qrImageView.setImage(qrImage);
        } catch (Exception e) {
            // Fallback jika gambar tidak ada
        }
    }

    // === PUBLIC METHODS ===

    public void setData(double total, String pelanggan, ObservableList<Layanan> keranjang) {
        this.totalHarga = total;
        this.namaPelanggan = pelanggan;
        this.listKeranjang = keranjang;

        String formatted = Transaksi.formatRupiah(total);
        lblTotalMetode.setText("Total: " + formatted);
        lblTotalTunai.setText(formatted);
        lblTotalTransfer.setText(formatted);
        lblTotalEWallet.setText(formatted);
    }

    public void setOnSelesaiCallback(Runnable callback) {
        this.onSelesaiCallback = callback;
    }

    public boolean isPembayaranBerhasil() {
        return pembayaranBerhasil;
    }

    public String getMetodePembayaran() {
        return metodePembayaran;
    }

    public String getPaymentDetails() {
        return paymentDetails;
    }

    // === VIEW SWITCHING ===

    private void tampilkanView(VBox targetView) {
        viewPilihMetode.setVisible(false);
        viewTunai.setVisible(false);
        viewTransfer.setVisible(false);
        viewEWallet.setVisible(false);
        viewReceipt.setVisible(false);
        targetView.setVisible(true);
    }

    @FXML
    void handlePilihTunai() {
        metodePembayaran = "Tunai";
        tampilkanView(viewTunai);
        txtUangDibayar.clear();
        lblKembalian.setText("Rp 0");
    }

    @FXML
    void handlePilihTransfer() {
        metodePembayaran = "Transfer";
        tampilkanView(viewTransfer);
    }

    @FXML
    void handlePilihEWallet() {
        metodePembayaran = "E-Wallet";
        tampilkanView(viewEWallet);
    }

    @FXML
    void handleKembali() {
        tampilkanView(viewPilihMetode);
    }

    @FXML
    void handleBatal() {
        pembayaranBerhasil = false;
        tutupDialog();
    }

    // === PROSES PEMBAYARAN ===

    @FXML
    void handleProsesTunai() {
        try {
            String cleanedVal = txtUangDibayar.getText().replaceAll("[^0-9]", "");
            double uangDibayar = Double.parseDouble(cleanedVal);
            double kembalian = uangDibayar - totalHarga;

            if (kembalian >= 0) {
                paymentDetails = "Dibayar: " + Transaksi.formatRupiah(uangDibayar) +
                        " | Kembalian: " + Transaksi.formatRupiah(kembalian);
                pembayaranBerhasil = true;
                tampilkanReceipt();
            } else {
                tampilkanAlert("Uang Kurang", "Jumlah uang yang dibayarkan kurang!");
            }
        } catch (NumberFormatException e) {
            tampilkanAlert("Input Invalid", "Masukkan jumlah uang yang valid!");
        }
    }

    @FXML
    void handleKonfirmasiTransfer() {
        paymentDetails = "Transfer Bank BNI - Rek: 0234503418";
        pembayaranBerhasil = true;
        tampilkanReceipt();
    }

    @FXML
    void handleKonfirmasiEWallet() {
        paymentDetails = "E-Wallet (OVO/GoPay/DANA)";
        pembayaranBerhasil = true;
        tampilkanReceipt();
    }

    // === RECEIPT ===

    public void tampilkanReceipt() {
        tampilkanView(viewReceipt);

        lblTrxId.setText("No. Transaksi: #" + String.format("%06d", idTransaksi));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblDateTime.setText("Tanggal: " + LocalDateTime.now().format(formatter));

        lblPelanggan.setText("Pelanggan: " + namaPelanggan);
        lblTotalReceipt.setText(Transaksi.formatRupiah(totalHarga));
        lblMetode.setText("Metode: " + metodePembayaran);
        lblPaymentDetails.setText(paymentDetails);

        // Populate items
        itemsBox.getChildren().clear();
        if (listKeranjang != null) {
            for (Layanan item : listKeranjang) {
                Label lblItem = new Label("  • " + item.getNamaLayanan());
                lblItem.getStyleClass().add("receipt-item-name");
                Label lblPrice = new Label("    " + Transaksi.formatRupiah(item.getHarga()));
                lblPrice.getStyleClass().add("receipt-item-price");
                itemsBox.getChildren().addAll(lblItem, lblPrice);
            }
        }
    }

    public void setIdTransaksi(int id) {
        this.idTransaksi = id;
    }

    @FXML
    void handleSelesai() {
        if (onSelesaiCallback != null) {
            onSelesaiCallback.run();
        }
        tutupDialog();
    }

    // === HELPER ===

    private void hitungKembalian(String input) {
        try {
            String cleanedVal = input.replaceAll("[^0-9]", "");
            if (!cleanedVal.isEmpty()) {
                double cash = Double.parseDouble(cleanedVal);
                double kembalian = cash - totalHarga;
                if (kembalian >= 0) {
                    lblKembalian.setText(Transaksi.formatRupiah(kembalian));
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
    }

    private void tampilkanAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void tutupDialog() {
        Stage stage = (Stage) viewPilihMetode.getScene().getWindow();
        stage.close();
    }
}
