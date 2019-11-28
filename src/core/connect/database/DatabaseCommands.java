package core.connect.database;

import java.sql.*;

public class DatabaseCommands {
    private final String url = "jdbc:postgresql://localhost/mediator";
    private final String user = "postgres";
    private final String password = "123";
    private Connection connection = null;

    public DatabaseCommands() {
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized int logInIfUserExists(String name, String pswd) {
        String statement = "SELECT * FROM users WHERE uname = '" + name + "';";
        PreparedStatement ps = null;
        ResultSet set = null;
        try {
            ps = connection.prepareStatement(statement);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        if (ps != null) {
            try {
                set = ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
            if (set != null) {
                try {
                    while (set.next()) {
                        if (set.getString(2).equals(pswd)) {
                            return 1;
                        }
                    }
                } catch (SQLException s) {
                    return -1;
                }
            }
            return 0;
        }
        return -1;
    }

    public synchronized int signUp(String name, String pswd) {
        String statement = "SELECT * FROM users WHERE uname = '" + name + "';";
        String statementNew = "INSERT INTO users(uname, pswd) values ('" + name + "','" + pswd + "');";
        PreparedStatement ps = null;
        PreparedStatement psn = null;
        ResultSet set = null;
        try {
            ps = connection.prepareStatement(statement);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        if (ps != null) {
            try {
                set = ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
            if (set != null) {
                try {
                    while (set.next()) {
                        if (set.getString(1).equals(name)) {
                            return 3;
                        }
                    }
                } catch (SQLException s) {
                    return -1;
                }
                try {
                    psn = connection.prepareStatement(statementNew);
                    psn.executeUpdate();
                } catch (SQLException s) {
                    s.printStackTrace();
                }
                return 1;
            }
        }
        return -1;
    }
}
