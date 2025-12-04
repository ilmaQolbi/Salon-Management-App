package Model;

import java.util.List;

public class Karyawan extends User {
    private List<Jadwal> jadwalKerja;
    private List<Pelanggan> daftarPelanggan;
    private double komisi;

    public Karyawan(String idUser, String nama, String Username, String password) {
        super(idUser, nama, Username, password, "karyawan");
        this.komisi = 0;
    }

    public List<Jadwal> lihatJadwal() {
        return jadwalKerja;
    }

    public List<Pelanggan> lihatDaftarPelanggan() {
        return daftarPelanggan;
    }

    public double lihatKomisi() {
        return komisi;
    }

    // otomatis menambah komisi setelah menyelesaikan layanan
    public void tambahKomisi(double nilai) {
        this.komisi += nilai;
    }

    public void laporKendala(String deskripsi) {
        System.out.println("Kendala dilaporkan: " + deskripsi);
    }
}
