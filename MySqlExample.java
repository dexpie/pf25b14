import java.sql.*;

public class MySqlExample {
    public static void main(String[] args) throws ClassNotFoundException {
        String host, port, databaseName, userName, password;
        host = "mysql-1fccdfe6-thaliaharnum-4a7b.d.aivencloud.com";
        port = "18628";
        databaseName = "defaultdb";
        userName = "avnadmin";
        password = "AVNS_Al_-WVmdV5uALzNlLQl";

        // Pastikan informasi koneksi lengkap
        if (host == null || port == null || databaseName == null) {
            System.out.println("Host, port, database information is required");
            return;
        }

        // Muat driver JDBC
        Class.forName("com.mysql.cj.jdbc.Driver");

        // URL koneksi yang diperbaiki
        String url = "jdbc:mysql://" + host + ":" + port + "/" + databaseName
                + "?useSSL=true&verifyServerCertificate=false&requireSSL=true";

        try (
                final Connection connection = DriverManager.getConnection(url, userName, password);
                final Statement statement = connection.createStatement();
                final ResultSet resultSet = statement.executeQuery("SELECT username FROM gameuser")
        ) {
            while (resultSet.next()) {
                System.out.println("Username: " + resultSet.getString("username"));
            }
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
    }
}
