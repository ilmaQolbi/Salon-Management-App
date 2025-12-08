package Model;

import java.util.UUID;

/**
 *
 * @author AERO
 */
public class User {
    protected String idUser;
    protected String nama;
    protected String email;
    protected String password;
    protected String role;

    public User(String idUser, String nama, String email, String password, String role) {
        this.idUser = idUser;
        this.nama = nama;
        this.email = email;
        this.password = password;
        this.role = role;

    }
    public void SetUser(String idUser, String nama, String email, String password, String role) {
        this.idUser = idUser;
    }


    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return password;
    }

    public void setPass(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
     
}