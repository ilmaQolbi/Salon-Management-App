package Controller;

import DAO.LaporanPenjualanDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Controller untuk Laporan Penjualan
 * Dapat digunakan di Admin Dashboard dan Kasir Dashboard
 */
public class LaporanPenjualanController {

    @FXML
    private Label lblHariIni;
    @FXML
    private Label lblJumlahTransaksi;
    @FXML
    private Label lblMingguan;
    @FXML
    private Label lblBulanan;
    @FXML
    private Label lblTotal;
    @FXML
    private BarChart<String, Number> barChartHarian;
    @FXML
    private PieChart pieChartLayanan;

    private LaporanPenjualanDAO dao;
    private NumberFormat currencyFormat;

    @FXML
    public void initialize() {
        dao = new LaporanPenjualanDAO();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        loadAllData();
    }

    /**
     * Load semua data laporan
     */
    private void loadAllData() {
        loadSummaryData();
        loadBarChartData();
        loadPieChartData();
    }

    /**
     * Load data untuk summary cards
     */
    private void loadSummaryData() {
        // Hari ini
        double hariIni = dao.getPenjualanHariIni();
        lblHariIni.setText(currencyFormat.format(hariIni));

        int jumlahTransaksi = dao.getJumlahTransaksiHariIni();
        lblJumlahTransaksi.setText(jumlahTransaksi + " transaksi");

        // Mingguan
        double mingguan = dao.getPenjualanMingguIni();
        lblMingguan.setText(currencyFormat.format(mingguan));

        // Bulanan
        double bulanan = dao.getPenjualanBulanIni();
        lblBulanan.setText(currencyFormat.format(bulanan));

        // Total
        double total = dao.getTotalPenjualan();
        lblTotal.setText(currencyFormat.format(total));
    }

    /**
     * Load data untuk bar chart (penjualan 7 hari terakhir)
     */
    @SuppressWarnings("unchecked")
    private void loadBarChartData() {
        barChartHarian.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pendapatan");

        List<Object[]> data = dao.getPenjualanPerHari();

        for (Object[] row : data) {
            String tanggal = (String) row[0];
            Double total = (Double) row[1];

            // Format tanggal untuk display (ambil hari saja)
            String displayDate = tanggal.substring(8); // Ambil DD dari YYYY-MM-DD
            series.getData().add(new XYChart.Data<>(displayDate, total));
        }

        barChartHarian.getData().add(series);
        barChartHarian.setLegendVisible(false);
    }

    /**
     * Load data untuk pie chart (penjualan per layanan)
     */
    private void loadPieChartData() {
        pieChartLayanan.getData().clear();

        List<Object[]> data = dao.getPenjualanPerLayanan();

        // Hanya tampilkan jika ada data
        if (data.isEmpty()) {
            pieChartLayanan.setData(FXCollections.observableArrayList());
            return;
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Object[] row : data) {
            String namaLayanan = (String) row[0];
            Double total = (Double) row[1];

            // Hanya tambahkan jika total > 0
            if (total > 0) {
                pieData.add(new PieChart.Data(namaLayanan, total));
            }
        }

        pieChartLayanan.setData(pieData);
        pieChartLayanan.setStartAngle(90);
        pieChartLayanan.setClockwise(true);
    }

    /**
     * Handler untuk tombol refresh
     */
    @FXML
    void handleRefresh(ActionEvent event) {
        loadAllData();
    }
}
