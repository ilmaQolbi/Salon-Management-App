package DAO;

import Model.Layanan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class TransaksiDAO {

    /**
     * Menyimpan transaksi lengkap (Header + Detail) dalam satu atomic transaction.
     * Mengembalikan ID Transaksi jika berhasil, atau -1 jika gagal.
     */
    public int simpanTransaksi(String userId, String namaPelanggan, double total, String metode, List<Layanan> items,
            String idKaryawan) {
        Connection conn = null;
        PreparedStatement psHeader = null;
        PreparedStatement psDetail = null;
        ResultSet rsKey = null;
        int generatedId = -1;

        try {
            conn = DatabaseManager.connect();
            // Matikan auto-commit untuk memulai transaksi manual
            conn.setAutoCommit(false);

            // 1. Insert Header Transaksi
            String sqlHeader = "INSERT INTO transaksi (id_user, nama_pelanggan, total_harga, metode_pembayaran, tanggal) VALUES (?, ?, ?, ?, NOW())";
            psHeader = conn.prepareStatement(sqlHeader, Statement.RETURN_GENERATED_KEYS);
            psHeader.setString(1, userId);
            psHeader.setString(2, namaPelanggan);
            psHeader.setDouble(3, total);
            psHeader.setString(4, metode);

            int affectedRows = psHeader.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Gagal menyimpan transaksi, tidak ada baris yang terpengaruh.");
            }

            // Ambil ID Transaksi yang baru dibuat (SAFE WAY)
            rsKey = psHeader.getGeneratedKeys();
            if (rsKey.next()) {
                generatedId = rsKey.getInt(1);
            } else {
                throw new SQLException("Gagal mengambil ID transaksi.");
            }

            // 2. Insert Detail Transaksi (Batch Insert)
            String sqlDetail = "INSERT INTO antrian_Pelanggan (id_transaksi, id_layanan, harga, status, id_karyawan) VALUES (?, ?, ?, 'Menunggu', ?)";
            psDetail = conn.prepareStatement(sqlDetail);

            for (Layanan item : items) {
                psDetail.setInt(1, generatedId);
                psDetail.setInt(2, item.getIdLayanan());
                psDetail.setDouble(3, item.getHarga());
                psDetail.setString(4, idKaryawan);
                psDetail.addBatch(); // Tambahkan ke batch
            }

            // Eksekusi batch
            psDetail.executeBatch();

            // Commit transaksi (Simpan permanen)
            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    System.err.println("Transaksi gagal, melakukan rollback...");
                    conn.rollback(); // Batalkan semua perubahan jika ada error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return -1;
        } finally {
            // Tutup resource manual karena kita manage koneksi sendiri
            try {
                if (rsKey != null)
                    rsKey.close();
                if (psHeader != null)
                    psHeader.close();
                if (psDetail != null)
                    psDetail.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return generatedId;
    }
}
