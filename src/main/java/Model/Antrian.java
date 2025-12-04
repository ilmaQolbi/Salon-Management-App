package Model;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Ilma
 */
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Antrian {

    private Queue<Pelanggan> daftarAntrian = new LinkedList<>();
    private int nomor = 1;

    public void tambahAntrian(Pelanggan p) {
        daftarAntrian.add(p);
        System.out.println("Nomor antrian: " + nomor);
        nomor++;
    }

    public Pelanggan hapusAntrian() {
        return daftarAntrian.poll();
    }

    public void tampilkanAntrian() {
        System.out.println("=== ANTRIAN ===");
        for (Pelanggan p : daftarAntrian) {
            System.out.println(p.getInfoPelanggan());
        }
    }
}
