package DAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO untuk mengambil data statistik karyawan dari database
 */
public class KaryawanStatistikDAO {

    /**
     * Mendapatkan jumlah pelanggan yang dikerjakan hari ini
     */
    public int getJumlahPelangganHariIni(String idKaryawan) {
        String query = "SELECT COUNT(*) as jumlah FROM antrian_Pelanggan " +
                "WHERE id_karyawan = '" + idKaryawan + "' " +
                "AND status = 'Selesai' " +
                "AND DATE(waktu_selesai) = CURDATE()";
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
     * Mendapatkan jumlah pelanggan yang dikerjakan minggu ini
     */
    public int getJumlahPelangganMingguIni(String idKaryawan) {
        String query = "SELECT COUNT(*) as jumlah FROM antrian_Pelanggan " +
                "WHERE id_karyawan = '" + idKaryawan + "' " +
                "AND status = 'Selesai' " +
                "AND waktu_selesai >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
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
     * Mendapatkan jumlah pelanggan yang dikerjakan bulan ini
     */
    public int getJumlahPelangganBulanIni(String idKaryawan) {
        String query = "SELECT COUNT(*) as jumlah FROM antrian_Pelanggan " +
                "WHERE id_karyawan = '" + idKaryawan + "' " +
                "AND status = 'Selesai' " +
                "AND MONTH(waktu_selesai) = MONTH(CURDATE()) " +
                "AND YEAR(waktu_selesai) = YEAR(CURDATE())";
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
     * Mendapatkan total pelanggan yang pernah dikerjakan
     */
    public int getTotalPelanggan(String idKaryawan) {
        String query = "SELECT COUNT(*) as jumlah FROM antrian_Pelanggan " +
                "WHERE id_karyawan = '" + idKaryawan + "' " +
                "AND status = 'Selesai'";
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
     * Mendapatkan data jumlah pelanggan per hari untuk chart (7 hari terakhir)
     * 
     * @return List of Object[] {tanggal (String), jumlah (Integer)}
     */
    public List<Object[]> getJumlahPelangganPerHari(String idKaryawan) {
        List<Object[]> data = new ArrayList<>();
        String query = "SELECT d.date as tgl, COALESCE(COUNT(ap.id_antrian), 0) as jumlah " +
                "FROM ( " +
                "    SELECT CURDATE() as date " +
                "    UNION SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY) " +
                "    UNION SELECT DATE_SUB(CURDATE(), INTERVAL 2 DAY) " +
                "    UNION SELECT DATE_SUB(CURDATE(), INTERVAL 3 DAY) " +
                "    UNION SELECT DATE_SUB(CURDATE(), INTERVAL 4 DAY) " +
                "    UNION SELECT DATE_SUB(CURDATE(), INTERVAL 5 DAY) " +
                "    UNION SELECT DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                ") d " +
                "LEFT JOIN antrian_Pelanggan ap ON DATE(ap.waktu_selesai) = d.date " +
                "AND ap.id_karyawan = '" + idKaryawan + "' AND ap.status = 'Selesai' " +
                "GROUP BY d.date " +
                "ORDER BY d.date ASC";
        try {
            ResultSet rs = DatabaseManager.eksekusiQuery(query);
            while (rs != null && rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("tgl");
                row[1] = rs.getInt("jumlah");
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Mendapatkan data layanan yang paling sering dikerjakan untuk pie chart
     * 
     * @return List of Object[] {namaLayanan (String), jumlah (Integer)}
     */
    public List<Object[]> getLayananTerseringDikerjakan(String idKaryawan) {
        List<Object[]> data = new ArrayList<>();
        String query = "SELECT l.nama_layanan, COUNT(ap.id_antrian) as jumlah " +
                "FROM antrian_Pelanggan ap " +
                "JOIN layanan l ON ap.id_layanan = l.id_layanan " +
                "WHERE ap.id_karyawan = '" + idKaryawan + "' AND ap.status = 'Selesai' " +
                "GROUP BY l.id_layanan, l.nama_layanan " +
                "ORDER BY jumlah DESC";
        try {
            ResultSet rs = DatabaseManager.eksekusiQuery(query);
            while (rs != null && rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("nama_layanan");
                row[1] = rs.getInt("jumlah");
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
}
