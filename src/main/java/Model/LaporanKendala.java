package Model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Model class for Laporan Kendala (Issue Report)
 */
public class LaporanKendala {
    private final SimpleIntegerProperty idLaporan;
    private final SimpleStringProperty idUser;
    private final SimpleStringProperty isiLaporan;
    private final SimpleStringProperty tanggal;
    private final SimpleStringProperty status;

    public LaporanKendala(int idLaporan, String idUser, String isiLaporan, String tanggal, String status) {
        this.idLaporan = new SimpleIntegerProperty(idLaporan);
        this.idUser = new SimpleStringProperty(idUser);
        this.isiLaporan = new SimpleStringProperty(isiLaporan);
        this.tanggal = new SimpleStringProperty(tanggal);
        this.status = new SimpleStringProperty(status);
    }

    // Getters
    public int getIdLaporan() {
        return idLaporan.get();
    }

    public String getIdUser() {
        return idUser.get();
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

    // Property getters (for TableView binding)
    public SimpleIntegerProperty idLaporanProperty() {
        return idLaporan;
    }

    public SimpleStringProperty idUserProperty() {
        return idUser;
    }

    public SimpleStringProperty isiLaporanProperty() {
        return isiLaporan;
    }

    public SimpleStringProperty tanggalProperty() {
        return tanggal;
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    // Setters
    public void setStatus(String status) {
        this.status.set(status);
    }
}
