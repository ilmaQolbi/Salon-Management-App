package Model;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Ilma
 */
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private List<User> tabelUser = new ArrayList<>();
    private List<Layanan> tabelLayanan = new ArrayList<>();
    private List<Karyawan> tabelKaryawan = new ArrayList<>();
    private List<Transaksi> tabelTransaksi = new ArrayList<>();

    public void tambahUser(User u) {
        tabelUser.add(u);
    }

    public User cariUser(String email, String password) {
        return tabelUser.stream()
                .filter(u -> u.email.equals(email) && u.password.equals(password))
                .findFirst()
                .orElse(null);
    }

    public List<Karyawan> getKaryawan() {
        return tabelKaryawan;
    }

    public void tambahTransaksi(Transaksi t) {
        tabelTransaksi.add(t);
    }

    public List<Transaksi> getTransaksi() {
        return tabelTransaksi;
    }
}
