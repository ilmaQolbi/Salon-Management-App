package Model;

/**
 *
 * @author Ilma
 */
public class Pelanggan {
    private String idPelanggan;
    private String nama;
    private String noHp;

    // Constructor lengkap
    public Pelanggan(String idPelanggan, String nama, String noHp) {
        this.idPelanggan = idPelanggan;
        this.nama = nama;
        this.noHp = noHp;
    }

    // Constructor untuk pelanggan walk-in (hanya nama)
    public Pelanggan(String nama) {
        this.idPelanggan = null;
        this.nama = nama;
        this.noHp = null;
    }

    // Getter
    public String getIdPelanggan() {
        return idPelanggan;
    }

    public String getNama() {
        return nama;
    }

    public String getNoHp() {
        return noHp;
    }

    public String getInfoPelanggan() {
        if (noHp != null && !noHp.isEmpty()) {
            return nama + " (" + noHp + ")";
        }
        return nama;
    }
}
