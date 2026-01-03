package DAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO untuk mengambil data laporan penjualan dari database
 */
public class LaporanPenjualanDAO {

    /**
     * Mendapatkan total penjualan hari ini
     */
    public double getPenjualanHariIni() {
        String query = "SELECT COALESCE(SUM(total_harga), 0) as total FROM transaksi WHERE DATE(tanggal) = CURDATE()";
        try {
            ResultSet rs = DatabaseManager.eksekusiQuery(query);
            if (rs != null && rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Mendapatkan total penjualan 7 hari terakhir (minggu ini)
     */
    public double getPenjualanMingguIni() {
        String query = "SELECT COALESCE(SUM(total_harga), 0) as total FROM transaksi WHERE tanggal >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
        try {
            ResultSet rs = DatabaseManager.eksekusiQuery(query);
            if (rs != null && rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Mendapatkan total penjualan bulan ini
     */
    public double getPenjualanBulanIni() {
        String query = "SELECT COALESCE(SUM(total_harga), 0) as total FROM transaksi WHERE MONTH(tanggal) = MONTH(CURDATE()) AND YEAR(tanggal) = YEAR(CURDATE())";
        try {
            ResultSet rs = DatabaseManager.eksekusiQuery(query);
            if (rs != null && rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Mendapatkan total penjualan keseluruhan
     */
    public double getTotalPenjualan() {
        String query = "SELECT COALESCE(SUM(total_harga), 0) as total FROM transaksi";
        try {
            ResultSet rs = DatabaseManager.eksekusiQuery(query);
            if (rs != null && rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Mendapatkan jumlah transaksi hari ini
     */
    public int getJumlahTransaksiHariIni() {
        String query = "SELECT COUNT(*) as jumlah FROM transaksi WHERE DATE(tanggal) = CURDATE()";
        try {
            ResultSet rs = DatabaseManager.eksekusiQuery(query);
            if (rs != null && rs.next()) {
                return rs.getInt("jumlah");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Mendapatkan data penjualan per hari untuk chart (7 hari terakhir)
     * 
     * @return List of Object[] {tanggal (String), total (Double)}
     */
    public List<Object[]> getPenjualanPerHari() {
        List<Object[]> data = new ArrayList<>();
        // Query untuk mendapatkan semua 7 hari terakhir dengan LEFT JOIN agar hari
        // tanpa transaksi tetap muncul
        String query = "SELECT d.date as tgl, COALESCE(SUM(t.total_harga), 0) as total " +
                "FROM ( " +
                "    SELECT CURDATE() as date " +
                "    UNION SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY) " +
                "    UNION SELECT DATE_SUB(CURDATE(), INTERVAL 2 DAY) " +
                "    UNION SELECT DATE_SUB(CURDATE(), INTERVAL 3 DAY) " +
                "    UNION SELECT DATE_SUB(CURDATE(), INTERVAL 4 DAY) " +
                "    UNION SELECT DATE_SUB(CURDATE(), INTERVAL 5 DAY) " +
                "    UNION SELECT DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                ") d " +
                "LEFT JOIN transaksi t ON DATE(t.tanggal) = d.date " +
                "GROUP BY d.date " +
                "ORDER BY d.date ASC";
        try {
            ResultSet rs = DatabaseManager.eksekusiQuery(query);
            while (rs != null && rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("tgl");
                row[1] = rs.getDouble("total");
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Mendapatkan data penjualan per layanan untuk pie chart
     * 
     * @return List of Object[] {namaLayanan (String), total (Double)}
     */
    public List<Object[]> getPenjualanPerLayanan() {
        List<Object[]> data = new ArrayList<>();
        String query = "SELECT l.nama_layanan, COALESCE(SUM(ap.harga), 0) as total " +
                "FROM antrian_Pelanggan ap " +
                "JOIN layanan l ON ap.id_layanan = l.id_layanan " +
                "GROUP BY l.id_layanan, l.nama_layanan " +
                "ORDER BY total DESC";
        try {
            ResultSet rs = DatabaseManager.eksekusiQuery(query);
            while (rs != null && rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("nama_layanan");
                row[1] = rs.getDouble("total");
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
}
