package Model;

/**
 *
 * @author Ilma
 */
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;
import DAO.DatabaseManager;

public class Admin extends User {
    private List<Karyawan> daftarKaryawan;
    private List<Layanan> daftarLayanan;

    public Admin(String idUser, String nama, String email, String password) {
        super(idUser, nama, email, password, "admin", "Aktif");

        this.daftarKaryawan = new ArrayList<>();
        this.daftarLayanan = new ArrayList<>();
    }

    // ========== STATIC UTILITY METHODS ==========

    /**
     * Generate ID user baru berdasarkan role
     * 
     * @param role Admin, Kasir, atau Karyawan
     * @return ID baru dengan format PREFIX + 3 digit (contoh: ADM001, KSR002,
     *         KRY003)
     */
    public static String generateUserId(String role) {
        String prefix;
        if (role.equalsIgnoreCase("Admin")) {
            prefix = "ADM";
        } else if (role.equalsIgnoreCase("Kasir")) {
            prefix = "KSR";
        } else {
            prefix = "KRY";
        }

        String query = "SELECT idUser FROM users WHERE idUser LIKE '" + prefix + "%' ORDER BY idUser DESC LIMIT 1";
        ResultSet rs = DatabaseManager.eksekusiQuery(query);
        int nextNumber = 1;

        try {
            if (rs != null && rs.next()) {
                String lastId = rs.getString("idUser");
                if (lastId.length() >= 3) {
                    String numberPart = lastId.substring(3);
                    nextNumber = Integer.parseInt(numberPart) + 1;
                }
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
        return String.format("%s%03d", prefix, nextNumber);
    }

    // ========== BUSINESS LOGIC METHODS ==========

    public void tambahKaryawan(Karyawan k) {
        daftarKaryawan.add(k);
    }

    public void editKaryawan(String id, Karyawan baru) {
        // implement edit
    }

    public void hapusKaryawan(String id) {
        // implement hapus
    }

    public void tambahLayanan(Layanan layanan) {
        daftarLayanan.add(layanan);
    }

    public void editLayanan(String id, Layanan baru) {
        // implement
    }

    public void hapusLayanan(String id) {
        // implement
    }

    public void lihatLaporan() {
        // generate laporan
    }

    // ========== GETTERS ==========

    public List<Karyawan> getDaftarKaryawan() {
        return daftarKaryawan;
    }

    public List<Layanan> getDaftarLayanan() {
        return daftarLayanan;
    }
}
