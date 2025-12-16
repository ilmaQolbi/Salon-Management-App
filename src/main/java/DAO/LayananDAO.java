package DAO;

import Model.Layanan;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LayananDAO {

    /**
     * Mengambil semua data layanan dari database
     */
    public List<Layanan> getAllLayanan() {
        List<Layanan> list = new ArrayList<>();
        String query = "SELECT * FROM layanan";
        ResultSet rs = DatabaseManager.eksekusiQuery(query);
        try {
            while (rs != null && rs.next()) {
                list.add(new Layanan(
                        rs.getInt("id_layanan"),
                        rs.getString("nama_layanan"),
                        rs.getDouble("harga"),
                        rs.getInt("durasi")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Generate ID berurutan berikutnya
     * Mencari ID terkecil yang belum terpakai (gap) atau MAX+1
     * 
     * @return ID berikutnya yang tersedia
     */
    private int generateNextId() {
        // Cari gap pertama dalam sequence ID
        String queryGap = "SELECT MIN(t1.id_layanan + 1) as next_id " +
                "FROM layanan t1 " +
                "LEFT JOIN layanan t2 ON t1.id_layanan + 1 = t2.id_layanan " +
                "WHERE t2.id_layanan IS NULL";

        ResultSet rs = DatabaseManager.eksekusiQuery(queryGap);
        try {
            if (rs != null && rs.next()) {
                int nextId = rs.getInt("next_id");
                if (nextId > 0) {
                    return nextId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Fallback: Jika tabel kosong, mulai dari 1
        String queryMax = "SELECT COALESCE(MAX(id_layanan), 0) + 1 as next_id FROM layanan";
        rs = DatabaseManager.eksekusiQuery(queryMax);
        try {
            if (rs != null && rs.next()) {
                return rs.getInt("next_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 1; // Default
    }

    /**
     * Tambah layanan baru dengan ID berurutan
     * 
     * @return true jika berhasil
     */
    public boolean tambahLayanan(String nama, double harga, int durasi) {
        int nextId = generateNextId();
        String query = "INSERT INTO layanan (id_layanan, nama_layanan, harga, durasi) VALUES (?, ?, ?, ?)";
        int result = DatabaseManager.eksekusiUpdate(query, nextId, nama, harga, durasi);
        return result > 0;
    }

    /**
     * Update data layanan
     * 
     * @return true jika berhasil
     */
    public boolean updateLayanan(int idLayanan, String nama, double harga, int durasi) {
        String query = "UPDATE layanan SET nama_layanan = ?, harga = ?, durasi = ? WHERE id_layanan = ?";
        int result = DatabaseManager.eksekusiUpdate(query, nama, harga, durasi, idLayanan);
        return result > 0;
    }

    /**
     * Hapus layanan berdasarkan ID
     * 
     * @return true jika berhasil
     */
    public boolean hapusLayanan(int idLayanan) {
        String query = "DELETE FROM layanan WHERE id_layanan = ?";
        int result = DatabaseManager.eksekusiUpdate(query, idLayanan);
        return result > 0;
    }
}
