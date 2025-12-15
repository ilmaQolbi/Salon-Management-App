package Model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Model untuk data laporan kendala
 */
public class LaporanModel {
    private final int idLaporan;
    private final SimpleStringProperty namaUser;
    private final SimpleStringProperty isiLaporan;
    private final SimpleStringProperty tanggal;
    private final SimpleStringProperty status;

    public LaporanModel(int id, String nama, String isi, String tgl, String sts) {
        this.idLaporan = id;
        this.namaUser = new SimpleStringProperty(nama);
        this.isiLaporan = new SimpleStringProperty(isi);
        this.tanggal = new SimpleStringProperty(tgl);
        this.status = new SimpleStringProperty(sts);
    }

    public int getIdLaporan() {
        return idLaporan;
    }

    public String getNamaUser() {
        return namaUser.get();
    }

    public String getIsiLaporan() {
        return isiLaporan.get();
    }

    public String getTanggal() {
        return tanggal.get();
    }

    public String getStatus() {
        return status.get();
    }
}
