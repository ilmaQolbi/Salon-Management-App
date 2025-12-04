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

    public Transaksi(Pelanggan pelanggan, List<Layanan> layanan, Kasir kasir, String metode) {
        this.pelanggan = pelanggan;
        this.daftarLayanan = layanan;
        this.kasir = kasir;
        this.metodePembayaran = metode;
        this.tanggal = new Date();
        this.totalHarga = hitungTotal();
        this.statusPembayaran = "Belum Lunas";
    }

    public double hitungTotal() {
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
}
