package Model;
/**
 *
 * @author Ilma
 */
import java.util.List;

public class Admin extends User {
    private List<Karyawan> daftarKaryawan;
    private List<Layanan> daftarLayanan;

    public Admin(String idUser, String nama, String email, String password) {
        super(idUser, nama, email, password, "admin");
    }

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
}
