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
        ResultSet rs = DatabaseManager.executeQuery(query);
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
}
