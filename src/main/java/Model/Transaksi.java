package Model;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Ilma
 */
import Model.Layanan;
import Model.Kasir;
import java.util.Date;
import java.util.List;

public class Transaksi {
    private String idTransaksi;
    private Date tanggal;
    private Pelanggan pelanggan;
    private List<Layanan> daftarLayanan;
    private double totalHarga;
    private Kasir kasir;
    private String metodePembayaran;
    private String statusPembayaran; // Lunas / Belum Lunas

    // Constructor lengkap dengan Kasir
    public Transaksi(Pelanggan pelanggan, List<Layanan> layanan, Kasir kasir, String metode) {
        this.pelanggan = pelanggan;
        this.daftarLayanan = layanan;
        this.kasir = kasir;
        this.metodePembayaran = metode;
        this.tanggal = new Date();
        this.totalHarga = hitungTotal();
        this.statusPembayaran = "Belum Lunas";
    }

    // Constructor alternatif tanpa objek Kasir (untuk use case saat ini)
    public Transaksi(Pelanggan pelanggan, List<Layanan> layanan, String metode) {
        this.pelanggan = pelanggan;
        this.daftarLayanan = layanan;
        this.kasir = null;
        this.metodePembayaran = metode;
        this.tanggal = new Date();
        this.totalHarga = hitungTotal();
        this.statusPembayaran = "Belum Lunas";
    }

    // ========== BUSINESS LOGIC ==========

    public double hitungTotal() {
        if (daftarLayanan == null || daftarLayanan.isEmpty()) {
            return 0;
        }
        return daftarLayanan.stream().mapToDouble(Layanan::getHarga).sum();
    }

    public boolean prosesPembayaran(String metode, double jumlahDibayar) {
        if (jumlahDibayar >= totalHarga) {
            statusPembayaran = "Lunas";
            return true;
        }
        return false;
    }

    public void cetakStruk() {
        System.out.println("=== STRUK TRANSAKSI ===");
        System.out.println("Pelanggan: " + pelanggan.getNama());
        System.out.println("Total: Rp" + totalHarga);
        System.out.println("Metode: " + metodePembayaran);
        System.out.println("Status: " + statusPembayaran);
    }

    // ========== STATIC UTILITY METHODS ==========

    /**
     * Format angka ke format mata uang Rupiah
     * 
     * @param amount jumlah uang
     * @return String format Rupiah (contoh: Rp 50.000)
     */
    public static String formatRupiah(double amount) {
        java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance(
                java.util.Locale.of("id", "ID"));
        return format.format(amount);
    }

    /**
     * Hitung total harga dari list layanan (static version)
     * 
     * @param layananList daftar layanan
     * @return total harga
     */
    public static double hitungTotalDariList(List<Layanan> layananList) {
        if (layananList == null || layananList.isEmpty()) {
            return 0;
        }
        return layananList.stream().mapToDouble(Layanan::getHarga).sum();
    }

    // ========== GETTERS ==========

    public String getIdTransaksi() {
        return idTransaksi;
    }

    public Date getTanggal() {
        return tanggal;
    }

    public Pelanggan getPelanggan() {
        return pelanggan;
    }

    public List<Layanan> getDaftarLayanan() {
        return daftarLayanan;
    }

    public double getTotalHarga() {
        return totalHarga;
    }

    public Kasir getKasir() {
        return kasir;
    }

    public String getMetodePembayaran() {
        return metodePembayaran;
    }

    public String getStatusPembayaran() {
        return statusPembayaran;
    }

    // ========== SETTERS ==========

    public void setIdTransaksi(String idTransaksi) {
        this.idTransaksi = idTransaksi;
    }

    public void setStatusPembayaran(String statusPembayaran) {
        this.statusPembayaran = statusPembayaran;
    }
}
