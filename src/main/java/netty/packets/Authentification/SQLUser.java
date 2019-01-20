package netty.packets.Authentification;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mysql.cj.jdbc.MysqlDataSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SQLUser {

    private final String HOST;
    private final String DATABASE;
    private final String USER;
    private final String PASSWORD;
    private final int PORT;
    private final ExecutorService service;

    public SQLUser(final String HOST, final int PORT, final String DATABASE, final String USER, final String PASSWORD) {
        this.HOST = HOST;
        this.PORT = PORT;
        this.DATABASE = DATABASE;
        this.USER = USER;
        this.PASSWORD = PASSWORD;
        this.service = Executors.newCachedThreadPool();
    }

    private Connection conn = null;

    public SQLUser getConnection() {
        try {
            MysqlDataSource dataSource = new MysqlDataSource();
            dataSource.setServerName(HOST);
            dataSource.setPort(PORT);
            dataSource.setDatabaseName(DATABASE);
            dataSource.setAutoReconnect(true);
            dataSource.setUser(USER);
            dataSource.setServerTimezone("UTC");
            dataSource.setPassword(PASSWORD);
            this.conn = dataSource.getConnection();
/*
            final PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO `user` VALUES (?,?)");
            preparedStatement.setString(1, "test");
            preparedStatement.setString(2,BCrypt.hashpw("test", BCrypt.gensalt()));

            preparedStatement.executeUpdate();

            final PreparedStatement preparedStatement1 = conn.prepareStatement("INSERT INTO `user` VALUES (?,?)");
            preparedStatement1.setString(1, "test2");
            preparedStatement1.setString(2,BCrypt.hashpw("test2", BCrypt.gensalt()));

            preparedStatement1.executeUpdate();
            */

            System.out.println("[User-System] MySQL Verbunden!");
        } catch (final SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            ex.printStackTrace();
        }
        return this;
    }

    public void close() {
        try {
            if (this.conn != null && (!this.conn.isClosed()) && this.conn.isValid(10)) {
                this.conn.close();
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void getUser(final String username, final Consumer<User> callback) {
        renewConnection();
        service.execute(() -> {
            try {
                final PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM `user` WHERE username = ?");
                preparedStatement.setString(1, username);

                final ResultSet resultSet = preparedStatement.executeQuery();

                if (!resultSet.next()) {
                    callback.accept(null);
                    return;
                }
                final User user = new User(resultSet.getString("username"), resultSet.getString("password"));
                callback.accept(user);
                resultSet.close();
                preparedStatement.close();
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        });
    }

    public void insertPositionIfChanged(String username, double latitude, double longitude) {
        renewConnection();

        service.execute(() -> {
            try {
                PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM `position` WHERE userID = (SELECT id FROM `user` WHERE username = ?) ORDER BY timestamp DESC LIMIT 1;");
                preparedStatement.setString(1, username);

                final ResultSet resultSet = preparedStatement.executeQuery();

                if (!resultSet.next() || (resultSet.getDouble("latitude") != latitude || resultSet.getDouble("longitude") != longitude)) {
                    preparedStatement = conn.prepareStatement("INSERT INTO `position` (userID, latitude, longitude) VALUES ((SELECT id FROM `user` WHERE username = ?), ?, ?) ");
                    preparedStatement.setString(1, username);
                    preparedStatement.setDouble(2, latitude);
                    preparedStatement.setDouble(3, longitude);
                    preparedStatement.execute();
                    return;
                }

                resultSet.close();
                preparedStatement.close();
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        });
    }


    public void createUserTable() {
        renewConnection();

        service.execute(() -> {
            try {
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS `user` (id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(100) NOT NULL UNIQUE," +
                        "password varchar(100) NOT NULL);").execute();
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        });
    }

    public void createPositionTable() {
        renewConnection();

        service.execute(() -> {
            try {
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS `position` (" +
                        "id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                        "`timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                        "userID BIGINT UNSIGNED NOT NULL," +
                        "latitude DOUBLE PRECISION NOT NULL," +
                        "longitude DOUBLE PRECISION NOT NULL," +
                        "CONSTRAINT userID_fk FOREIGN KEY (userID) REFERENCES `user`(id));").execute();
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        });
    }

    public JsonArray getLatestPositionFromAllUser() {
        renewConnection();
        JsonArray jsonArray = new JsonArray();
        service.execute(() -> {
            try {
                ResultSet resultSet = conn.prepareStatement("select max(`timestamp`) as timestamp, userID, latitude, longitude FROM `position` group by `userID`").executeQuery();

                JsonObject jsonObject = new JsonObject();
                while (resultSet.next()) {
                    jsonObject = new JsonObject();
                    jsonObject.addProperty("timestamp", resultSet.getTimestamp("timestamp").toString());
                    jsonObject.addProperty("userID", resultSet.getInt("userID"));
                    jsonObject.addProperty("latitude", resultSet.getDouble("latitude"));
                    jsonObject.addProperty("longitude", resultSet.getDouble("longitude"));
                }
                jsonArray.add(jsonObject);

            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("VendorError: " + e.getErrorCode());
            }
        });
        return jsonArray;
    }

    private JsonArray getPositionHistoryFromAllUSer() {
        renewConnection();
        JsonArray jsonArray = new JsonArray();
        service.execute(() -> {

                    try {
                        ResultSet resultSet = conn.prepareStatement("SELECT * FROM `user`").executeQuery();

                        ArrayList<Long> userIDs = new ArrayList<Long>();
                        while (resultSet.next()) {
                            userIDs.add(resultSet.getLong("id"));
                        }
                        for (long userID : userIDs) {
                            JsonObject userJsonObject = new JsonObject();
                            final PreparedStatement preparedStatement = conn.prepareStatement("SELECT latitude, longitde, timestamp FROM `position` where userID = ?");
                            preparedStatement.setLong(1, userID);
                            resultSet = preparedStatement.executeQuery();
                            userJsonObject.addProperty("latitude", resultSet.getLong("latitude"));
                            userJsonObject.addProperty("longitude", resultSet.getLong("longitude"));
                            userJsonObject.addProperty("timestamp", resultSet.getTimestamp("timestamp").toString());
                            userJsonObject.addProperty("userid", userID);
                            jsonArray.add(userJsonObject);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
        );
        return jsonArray;
    }

    private void renewConnection() {
        try {
            if (this.conn == null || this.conn.isClosed() || (!this.conn.isValid(5))) {
                getConnection();
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

}
