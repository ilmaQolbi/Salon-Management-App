package Model;
/**
 *
 * @author Ilma
 */
import java.util.List;

public class Kasir extends User {
    private List<Transaksi> daftarTransaksi;

    public Kasir(String idUser, String nama, String email, String password) {
        super(idUser, nama, email, password, "kasir");
    }

    public Transaksi buatTransaksi(Pelanggan pelanggan, List<Layanan> layanan, String metodePembayaran) {
        Transaksi t = new Transaksi(pelanggan, layanan, this, metodePembayaran);
        daftarTransaksi.add(t);
        return t;
    }

    public double hitungTotal(List<Layanan> layanan) {
        return layanan.stream().mapToDouble(Layanan::getHarga).sum();
    }

    public void cetakStruk(Transaksi t) {
        t.cetakStruk();
    }

    public List<Transaksi> lihatRiwayatTransaksi() {
        return daftarTransaksi;
    }
}
