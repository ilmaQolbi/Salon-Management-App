package Model;

/**
 * Model untuk data komisi karyawan
 */
public class KomisiModel {
    private String namaLayanan;
    private int jumlah;
    private double nilaiKomisi;

    public KomisiModel(String namaLayanan, int jumlah, double nilaiKomisi) {
        this.namaLayanan = namaLayanan;
        this.jumlah = jumlah;
        this.nilaiKomisi = nilaiKomisi;
    }

    public String getNamaLayanan() {
        return namaLayanan;
    }

    public int getJumlah() {
        return jumlah;
    }

    public double getNilaiKomisi() {
        return nilaiKomisi;
    }
}
