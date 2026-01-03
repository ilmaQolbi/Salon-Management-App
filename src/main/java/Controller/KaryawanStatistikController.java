package Controller;

import DAO.KaryawanStatistikDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.util.List;

/**
 * Controller untuk Statistik Karyawan
 * Menampilkan data pelanggan yang dikerjakan dan layanan tersering
 */
public class KaryawanStatistikController {

    @FXML
    private Label lblHariIni;
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

    private KaryawanStatistikDAO dao;
    private String idKaryawan;

    @FXML
    public void initialize() {
        dao = new KaryawanStatistikDAO();
    }

    /**
     * Set ID Karyawan dan load data
     */
    public void setKaryawanId(String id) {
        this.idKaryawan = id;
        loadAllData();
    }

    /**
     * Load semua data statistik
     */
    private void loadAllData() {
        if (idKaryawan == null || idKaryawan.isEmpty()) {
            return;
        }
        loadSummaryData();
        loadBarChartData();
        loadPieChartData();
    }

    /**
     * Load data untuk summary cards
     */
    private void loadSummaryData() {
        int hariIni = dao.getJumlahPelangganHariIni(idKaryawan);
        lblHariIni.setText(String.valueOf(hariIni));

        int mingguan = dao.getJumlahPelangganMingguIni(idKaryawan);
        lblMingguan.setText(String.valueOf(mingguan));

        int bulanan = dao.getJumlahPelangganBulanIni(idKaryawan);
        lblBulanan.setText(String.valueOf(bulanan));

        int total = dao.getTotalPelanggan(idKaryawan);
        lblTotal.setText(String.valueOf(total));
    }

    /**
     * Load data untuk bar chart (pelanggan 7 hari terakhir)
     */
    private void loadBarChartData() {
        barChartHarian.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pelanggan");

        List<Object[]> data = dao.getJumlahPelangganPerHari(idKaryawan);

        for (Object[] row : data) {
            String tanggal = (String) row[0];
            Integer jumlah = (Integer) row[1];

            // Format tanggal untuk display (ambil hari saja)
            String displayDate = tanggal.substring(8); // Ambil DD dari YYYY-MM-DD
            series.getData().add(new XYChart.Data<>(displayDate, jumlah));
        }

        barChartHarian.getData().add(series);
        barChartHarian.setLegendVisible(false);
    }

    /**
     * Load data untuk pie chart (layanan tersering dikerjakan)
     */
    private void loadPieChartData() {
        pieChartLayanan.getData().clear();

        List<Object[]> data = dao.getLayananTerseringDikerjakan(idKaryawan);

        if (data.isEmpty()) {
            pieChartLayanan.setData(FXCollections.observableArrayList());
            return;
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Object[] row : data) {
            String namaLayanan = (String) row[0];
            Integer jumlah = (Integer) row[1];

            if (jumlah > 0) {
                pieData.add(new PieChart.Data(namaLayanan + " (" + jumlah + ")", jumlah));
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
