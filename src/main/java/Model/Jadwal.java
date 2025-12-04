package Model;

/**
 *
 * @author Ilma
 */
import java.time.LocalDate;
import java.time.LocalTime;

public class Jadwal {
    private String idJadwal;
    private LocalDate tanggal;
    private LocalTime jamMulai;
    private LocalTime jamSelesai;

    public Jadwal(String idJadwal, LocalDate tanggal, LocalTime mulai, LocalTime selesai) {
        this.idJadwal = idJadwal;
        this.tanggal = tanggal;
        this.jamMulai = mulai;
        this.jamSelesai = selesai;
    }

    public void tampilkanJadwal() {
        System.out.println(tanggal + " | " + jamMulai + " - " + jamSelesai);
    }
}
