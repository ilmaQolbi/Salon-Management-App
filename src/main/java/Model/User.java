package Model;

/**
 *
 * @author AERO
 */
/**
 * Abstract class User - Base class untuk semua tipe pengguna sistem salon.
 * 
 * KONSEP ABSTRACTION:
 * - Class ini tidak dapat di-instantiate secara langsung (harus melalui
 * subclass)
 * - Menyembunyikan detail implementasi dan hanya menampilkan fungsionalitas
 * penting
 * - Abstract methods memaksa subclass untuk memberikan implementasi spesifik
 * 
 * @author AERO
 */
public abstract class User {
    protected String idUser;
    protected String nama;
    protected String email;
    protected String password;
    protected String role;
    protected String status;

    /**
     * Constructor untuk User.
     * Dipanggil oleh subclass menggunakan super().
     */
    public User(String idUser, String nama, String email, String password, String role, String status) {
        this.idUser = idUser;
        this.nama = nama;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    public void SetUser(String idUser, String nama, String email, String password, String role) {
        this.idUser = idUser;
    }

    // ========== GETTER & SETTER (Encapsulation) ==========

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ========== ABSTRACT METHODS (Abstraction + Polymorphism) ==========

    /**
     * Menampilkan informasi user.
     * ABSTRACT: Setiap subclass WAJIB memberikan implementasi spesifik.
     * 
     * @return String informasi user sesuai role
     */
    public abstract String getInfoUser();

    /**
     * Menampilkan deskripsi tugas berdasarkan role.
     * ABSTRACT: Setiap subclass WAJIB memberikan implementasi spesifik.
     * 
     * @return String deskripsi tugas sesuai role
     */
    public abstract String getDeskripsiTugas();

}