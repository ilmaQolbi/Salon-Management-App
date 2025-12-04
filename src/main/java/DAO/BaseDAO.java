package DAO;

import java.sql.Connection;
import java.sql.DriverManager;

public class BaseDAO {
    public static Connection getCon() {
        Connection con = null;
        try {
            String url = "jdbc:mysql://localhost:3306/salon_db?useSSL=false&serverTimezone=UTC";
            con = DriverManager.getConnection(url, "root", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }

    public static void closeCon(Connection con) {
        try {
            if (con != null) con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
