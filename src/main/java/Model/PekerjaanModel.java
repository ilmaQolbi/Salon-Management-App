package Model;

/**
 * Model untuk data pekerjaan karyawan
 */
public class PekerjaanModel {
    private int idDetail;
    private int idTransaksi;
    private String namaPelanggan;
    private String namaLayanan;
    private String waktu;
    private String status;

    public PekerjaanModel(int idDetail, int idTransaksi, String namaPelanggan, String namaLayanan, String waktu,
            String status) {
        this.idDetail = idDetail;
        this.idTransaksi = idTransaksi;
        this.namaPelanggan = namaPelanggan;
        this.namaLayanan = namaLayanan;
        this.waktu = waktu;
        this.status = status;
    }

    public int getIdDetail() {
        return idDetail;
    }

    public int getIdTransaksi() {
        return idTransaksi;
    }

    public String getNamaPelanggan() {
        return namaPelanggan;
    }

    public String getNamaLayanan() {
        return namaLayanan;
    }

    public String getWaktu() {
        return waktu;
    }

    public String getStatus() {
        return status;
    }
}
