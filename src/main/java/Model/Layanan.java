package Model;

/**
 *
 * @author Ilma
 */
public class Layanan {
    private String idLayanan;
    private String namaLayanan;
    private double harga;
    private int durasi; // menit

    public Layanan(String idLayanan, String nama, double harga, int durasi) {
        this.idLayanan = idLayanan;
        this.namaLayanan = nama;
        this.harga = harga;
        this.durasi = durasi;
    }

    public double getHarga() {
        return harga;
    }

    public int getDurasi() {
        return durasi;
    }

    public String getInfoLayanan() {
        return namaLayanan + " - Rp" + harga + " (" + durasi + " menit)";
    }
}
