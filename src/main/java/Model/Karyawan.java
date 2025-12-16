package Model;

import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class Karyawan extends User {
    private List<Jadwal> jadwalKerja;
    private List<Pelanggan> daftarPelanggan;
    private double komisi;

    // ========== KONSTANTA BISNIS ==========
    public static final double PERSENTASE_KOMISI = 0.05; // 5% komisi per layanan
    public static final double GAJI_POKOK = 4800000;

    public Karyawan(String idUser, String nama, String Username, String password) {
        super(idUser, nama, Username, password, "karyawan", "Aktif");
        this.komisi = 0;
    }

    // ========== STATIC UTILITY METHODS ==========

    /**
     * Hitung komisi dari harga layanan
     * 
     * @param hargaLayanan harga layanan yang diselesaikan
     * @return nilai komisi (5% dari harga)
     */
    public static double hitungKomisi(double hargaLayanan) {
        return hargaLayanan * PERSENTASE_KOMISI;
    }

    /**
     * Hitung total komisi dari list harga layanan
     * 
     * @param listHarga list harga layanan yang sudah diselesaikan
     * @return total nilai komisi
     */
    public static double hitungTotalKomisi(List<Double> listHarga) {
        if (listHarga == null || listHarga.isEmpty()) {
            return 0;
        }
        return listHarga.stream().mapToDouble(h -> h * PERSENTASE_KOMISI).sum();
    }

    /**
     * Format angka ke format mata uang Rupiah
     * 
     * @param amount jumlah uang
     * @return String format Rupiah
     */
    public static String formatRupiah(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));
        return format.format(amount);
    }

    /**
     * Hitung estimasi gaji (Pokok + Komisi)
     * 
     * @param totalKomisi total komisi bulan ini
     * @return estimasi total gaji
     */
    public static double hitungEstimasiGaji(double totalKomisi) {
        return GAJI_POKOK + totalKomisi;
    }

    // ========== INSTANCE METHODS ==========

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
