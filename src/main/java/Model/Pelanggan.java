package Model;

/**
 *
 * @author Ilma
 */
public class Pelanggan {
    private String idPelanggan;
    private String nama;
    private String noHp;

    public Pelanggan(String idPelanggan, String nama, String noHp) {
        this.idPelanggan = idPelanggan;
        this.nama = nama;
        this.noHp = noHp;
    }

    public String getNama() {
        return nama;
    }

    public String getInfoPelanggan() {
        return nama + " (" + noHp + ")";
    }
}
