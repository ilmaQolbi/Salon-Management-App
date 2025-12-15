package Model;

/**
 *
 * @author Ilma
 */
import java.util.List;

import java.util.ArrayList;

public class Admin extends User {
    // 1. Hapus tanda <Karyawan> dan <Layanan>
    private List daftarKaryawan;
    private List daftarLayanan;

    public Admin(String idUser, String nama, String email, String password) {
        super(idUser, nama, email, password, "admin", "Aktif");

        // 2. Inisialisasi List harus dilakukan (biasanya di Constructor)
        // Perhatikan: new ArrayList() tanpa < >
        this.daftarKaryawan = new ArrayList();
        this.daftarLayanan = new ArrayList();
    }

    public void tambahKaryawan(Karyawan k) {
        // Tetap bisa ditambahkan, tapi Java tidak akan mengecek apakah 'k' benar-benar
        // Karyawan
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
