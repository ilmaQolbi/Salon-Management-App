package DAO;

import static DAO.BaseDAO.closeCon;
import static DAO.BaseDAO.getCon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Model.User;

public class UserDAO {

    private static PreparedStatement st;
    private static Connection con;

    // LOGIN VALIDATION
    public static User validate(String email, String password) {
        User u = null;
        try {
            con = getCon();
            String query = "SELECT idUser, email, password FROM users WHERE email = ? AND password = ?";
            st = con.prepareStatement(query);

            st.setString(1, email);
            st.setString(2, password);

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                u = new User(
                    rs.getString("idUser"),   
                    rs.getString("email"),
                    rs.getString("password")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeCon(con);
        }
        return u;
    }

    // SEARCH USER BY ID (STRING)
    public static User searchByUid(String idUser) {
        User u = null;
        try {
            con = getCon();
            String query = "SELECT * FROM users WHERE idUser = ?";

            st = con.prepareStatement(query);
            st.setString(1, idUser);

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                u = new User(
                    rs.getString("idUser"),
                    rs.getString("email"),
                    rs.getString("password")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeCon(con);
        }
        return u;
    }

    // REGISTER USER
    public static void registerUser(User u) {
        try {
            con = getCon();
            String query = "INSERT INTO users (idUser, email, password) VALUES (?,?,?)";

            st = con.prepareStatement(query);

            st.setString(1, u.getIdUser()); // String now
            st.setString(2, u.getEmail());
            st.setString(3, u.getPass());

            st.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCon(con);
        }
    }
}
