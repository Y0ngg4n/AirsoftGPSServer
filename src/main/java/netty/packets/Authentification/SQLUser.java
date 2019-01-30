package netty.packets.Authentification;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mysql.cj.jdbc.MysqlDataSource;
import netty.utils.Logger;

import javax.security.auth.callback.Callback;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.*;
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
            System.out.println("[User-System] MySQL Connected!");
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
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
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

    public void setOnlineUser(final String username, final boolean online) {
        renewConnection();
        service.execute(() -> {
            try {
                PreparedStatement preparedStatement = conn.prepareStatement("UPDATE `user` SET online=? WHERE username = ?");
                preparedStatement.setBoolean(1, online);
                preparedStatement.setString(2, username);
                preparedStatement.execute();
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        });
    }


    public void insertPositionIfChanged(final String username, final double latitude, final double longitude) {
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


    public void createUserTable(final Consumer<Void> consumer) {
        renewConnection();

        service.execute(() -> {
            try {
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS `user` (" +
                        "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT," +
                        "  `username` varchar(100) NOT NULL," +
                        "  `password` varchar(100) NOT NULL," +
                        "  `teamid` bigint(20) unsigned DEFAULT 1," +
                        "  `alive` bool NOT NULL DEFAULT true," +
                        "  `underfire` bool NOT NULL DEFAULT false," +
                        "  `mission` bool NOT NULL DEFAULT false," +
                        "  `support` bool NOT NULL DEFAULT false," +
                        "  `online` bool NOT NULL DEFAULT false," +
                        "  PRIMARY KEY (`id`)," +
                        "  UNIQUE KEY `username` (`username`)," +
                        "  KEY `user_teams_FK` (`teamid`)," +
                        "  CONSTRAINT `user_teams_FK` FOREIGN KEY (`teamid`) REFERENCES `teams` (`id`)" +
                        ");").execute();
                consumer.accept(null);
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        });
    }

    public void createPositionTable(final Consumer<Void> consumer) {
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
                consumer.accept(null);
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        });
    }

    public void createTeamsTable(final Consumer<Void> consumer) {
        renewConnection();

        service.execute(() -> {
            try {
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS `teams` (" +
                        "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT," +
                        "  `teamname` varchar(100) NOT NULL," +
                        "  PRIMARY KEY (`id`));").execute();
                ResultSet resultSet = conn.prepareStatement("SELECT * FROM `teams`").executeQuery();
                if (!resultSet.next())
                    conn.prepareStatement("INSERT INTO `teams` (teamname) VALUES ('NO TEAM')").execute();
                consumer.accept(null);
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        });
    }

    public void getLatestPositionFromAllUser(final Consumer<JsonArray> callback) {
        renewConnection();
        service.execute(() -> {
            try {
                JsonArray jsonArray = new JsonArray();
                //TODO: FIX SQL QUERY
                ResultSet resultSet = conn.prepareStatement("select max(`timestamp`) as timestamp, " +
                        "userID, latitude, longitude, username, alive, underfire, mission, support, teamid, teamname " +
                        "FROM `teams` inner join (`position` inner join `user`) WHERE online=true group by `userID`").executeQuery();

                JsonObject jsonObject = null;
                while (resultSet.next()) {
                    jsonObject = new JsonObject();
                    jsonObject.addProperty("timestamp", resultSet.getTimestamp("timestamp").toString());
                    jsonObject.addProperty("userID", resultSet.getInt("userID"));
                    jsonObject.addProperty("latitude", resultSet.getDouble("latitude"));
                    jsonObject.addProperty("longitude", resultSet.getDouble("longitude"));
                    jsonObject.addProperty("username", resultSet.getString("username"));
                    jsonObject.addProperty("teamname", resultSet.getString("teamname"));
                    jsonObject.addProperty("teamid", resultSet.getString("teamid"));
                    jsonObject.addProperty("alive", resultSet.getBoolean("alive"));
                    jsonObject.addProperty("underfire", resultSet.getString("underfire"));
                    jsonObject.addProperty("mission", resultSet.getString("mission"));
                    jsonObject.addProperty("support", resultSet.getString("support"));
                    jsonArray.add(jsonObject);
                }
                Logger.debug(String.valueOf(jsonObject));

                callback.accept(jsonArray);
                return;
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("VendorError: " + e.getErrorCode());

            }
        });
        callback.accept(null);
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
                        System.out.println("SQLException: " + e.getMessage());
                        System.out.println("SQLState: " + e.getSQLState());
                        System.out.println("VendorError: " + e.getErrorCode());
                    }
                }
        );
        return jsonArray;
    }

    public void updateUserStatus(final String username, final boolean alive, final boolean underfire, final boolean mission, final boolean support) {
        renewConnection();
        service.execute(() -> {
            try {
                PreparedStatement preparedStatement = conn.prepareStatement("SELECT id FROM `user` where username = ?");
                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();
                int userID;
                if (resultSet.next()) {
                    userID = resultSet.getInt("id");
                } else {
                    throw new SQLException();
                }

                preparedStatement = conn.prepareStatement("UPDATE `user` SET alive = ?, underfire = ?, mission = ?, support = ? WHERE id = ? ");
                preparedStatement.setBoolean(1, alive);
                preparedStatement.setBoolean(2, underfire);
                preparedStatement.setBoolean(3, mission);
                preparedStatement.setBoolean(4, support);
                preparedStatement.setInt(5, userID);
                preparedStatement.execute();
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("VendorError: " + e.getErrorCode());
            }
        });
    }

    private void renewConnection() {
        try {
            if (this.conn == null || this.conn.isClosed() || (!this.conn.isValid(5))) {
                getConnection();
            }
        } catch (final SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }
    }

}
