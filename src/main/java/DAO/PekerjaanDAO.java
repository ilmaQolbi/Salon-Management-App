package DAO;

import Model.PekerjaanModel;
import Model.KomisiModel;
import Model.Karyawan;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO untuk mengelola data pekerjaan karyawan
 */
public class PekerjaanDAO {

    /**
     * Mengambil daftar pekerjaan untuk karyawan tertentu
     * 
     * @param idKaryawan ID karyawan yang login
     * @return List PekerjaanModel dengan statistik (pendingCount, doneCount,
     *         komisiHariIni)
     */
    public static List<PekerjaanModel> getPekerjaanByKaryawan(String idKaryawan) {
        List<PekerjaanModel> list = new ArrayList<>();
        String query = "SELECT ap.id_antrian, t.id_transaksi, t.nama_pelanggan, l.nama_layanan, l.harga, t.tanggal, ap.status "
                +
                "FROM antrian_Pelanggan ap " +
                "JOIN transaksi t ON ap.id_transaksi = t.id_transaksi " +
                "JOIN layanan l ON ap.id_layanan = l.id_layanan " +
                "WHERE ap.id_karyawan = ? " +
                "ORDER BY CASE WHEN ap.status = 'Selesai' THEN 1 ELSE 0 END, t.tanggal DESC " +
                "LIMIT 50";

        ResultSet rs = DatabaseManager.eksekusiQueryDenganParam(query, idKaryawan);
        try {
            while (rs != null && rs.next()) {
                String status = rs.getString("status");
                if (status == null)
                    status = "Menunggu";

                list.add(new PekerjaanModel(
                        rs.getInt("id_antrian"),
                        rs.getInt("id_transaksi"),
                        rs.getString("nama_pelanggan"),
                        rs.getString("nama_layanan"),
                        rs.getString("tanggal"),
                        status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Mengambil riwayat pekerjaan yang sudah selesai
     * 
     * @param idKaryawan ID karyawan
     * @return List PekerjaanModel yang sudah selesai
     */
    public static List<PekerjaanModel> getRiwayatPekerjaan(String idKaryawan) {
        List<PekerjaanModel> list = new ArrayList<>();
        String query = "SELECT ap.id_antrian, t.id_transaksi, t.nama_pelanggan, l.nama_layanan, t.tanggal, ap.status " +
                "FROM antrian_Pelanggan ap " +
                "JOIN transaksi t ON ap.id_transaksi = t.id_transaksi " +
                "JOIN layanan l ON ap.id_layanan = l.id_layanan " +
                "WHERE ap.status = 'Selesai' AND ap.id_karyawan = ? " +
                "ORDER BY t.tanggal DESC LIMIT 100";

        ResultSet rs = DatabaseManager.eksekusiQueryDenganParam(query, idKaryawan);
        try {
            while (rs != null && rs.next()) {
                list.add(new PekerjaanModel(
                        rs.getInt("id_antrian"),
                        rs.getInt("id_transaksi"),
                        rs.getString("nama_pelanggan"),
                        rs.getString("nama_layanan"),
                        rs.getString("tanggal"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Mengambil data komisi per layanan untuk bulan ini
     * 
     * @param idKaryawan ID karyawan
     * @return List KomisiModel
     */
    public static List<KomisiModel> getKomisiBulanIni(String idKaryawan) {
        List<KomisiModel> list = new ArrayList<>();
        String query = "SELECT l.nama_layanan, COUNT(*) as jumlah, SUM(l.harga) * " + Karyawan.PERSENTASE_KOMISI
                + " as komisi " +
                "FROM antrian_Pelanggan ap " +
                "JOIN transaksi t ON ap.id_transaksi = t.id_transaksi " +
                "JOIN layanan l ON ap.id_layanan = l.id_layanan " +
                "WHERE ap.status = 'Selesai' " +
                "AND ap.id_karyawan = ? " +
                "AND MONTH(t.tanggal) = MONTH(CURRENT_DATE()) " +
                "AND YEAR(t.tanggal) = YEAR(CURRENT_DATE()) " +
                "GROUP BY l.nama_layanan " +
                "ORDER BY komisi DESC";

        ResultSet rs = DatabaseManager.eksekusiQueryDenganParam(query, idKaryawan);
        try {
            while (rs != null && rs.next()) {
                list.add(new KomisiModel(
                        rs.getString("nama_layanan"),
                        rs.getInt("jumlah"),
                        rs.getDouble("komisi")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Hitung statistik pekerjaan (pending, selesai, komisi hari ini)
     * 
     * @param listPekerjaan list pekerjaan dari getPekerjaanByKaryawan
     * @return double[3] = {pendingCount, doneCount, komisiHariIni}
     */
    public static double[] hitungStatistikPekerjaan(List<PekerjaanModel> listPekerjaan, String idKaryawan) {
        int pendingCount = 0;
        int doneCount = 0;
        double komisiHariIni = 0;

        // Perlu query terpisah untuk harga karena PekerjaanModel tidak menyimpan harga
        String query = "SELECT ap.status, l.harga " +
                "FROM antrian_Pelanggan ap " +
                "JOIN layanan l ON ap.id_layanan = l.id_layanan " +
                "WHERE ap.id_karyawan = ?";

        ResultSet rs = DatabaseManager.eksekusiQueryDenganParam(query, idKaryawan);
        try {
            while (rs != null && rs.next()) {
                String status = rs.getString("status");
                double harga = rs.getDouble("harga");

                if ("Menunggu".equalsIgnoreCase(status) || "Menunggu Persetujuan".equalsIgnoreCase(status)) {
                    pendingCount++;
                } else if ("Selesai".equalsIgnoreCase(status)) {
                    doneCount++;
                    komisiHariIni += Karyawan.hitungKomisi(harga);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new double[] { pendingCount, doneCount, komisiHariIni };
    }

    /**
     * Update status pekerjaan menjadi Selesai
     * 
     * @param idAntrian ID antrian
     * @return true jika berhasil
     */
    public static boolean selesaikanPekerjaan(int idAntrian) {
        String query = "UPDATE antrian_Pelanggan SET status = 'Selesai', waktu_selesai = NOW() WHERE id_antrian = ?";
        int result = DatabaseManager.eksekusiUpdate(query, idAntrian);
        return result > 0;
    }
}
