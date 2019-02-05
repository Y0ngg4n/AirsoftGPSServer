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
import java.util.Arrays;
import java.util.List;
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
                final PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM `users` WHERE username = ?");
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
                PreparedStatement preparedStatement = conn.prepareStatement("UPDATE `users` SET online=? WHERE username = ?");
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
                PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM `positions` WHERE userID = (SELECT id FROM `users` WHERE username = ?) ORDER BY timestamp DESC LIMIT 1;");
                preparedStatement.setString(1, username);

                final ResultSet resultSet = preparedStatement.executeQuery();

                if (!resultSet.next() || (resultSet.getDouble("latitude") != latitude || resultSet.getDouble("longitude") != longitude)) {
                    preparedStatement = conn.prepareStatement("INSERT INTO `positions` (userID, latitude, longitude) VALUES ((SELECT id FROM `users` WHERE username = ?), ?, ?) ");
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

    public void isOrga(Consumer<ArrayList<Boolean>> consumer, final String username){
        renewConnection();
        service.execute(()->{
            try {
                PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM `orga` WHERE userID = (SELECT id FROM `users` where username = ?) LIMIT 1");
                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();

                    ArrayList<Boolean> booleans = new ArrayList<Boolean>();
                if(resultSet.next()){
                    booleans.add(true);
                    booleans.add(resultSet.getBoolean("tacticalMarker"));
                    booleans.add(resultSet.getBoolean("missionMarker"));
                    booleans.add(resultSet.getBoolean("hqMarker"));
                    booleans.add(resultSet.getBoolean("respawnMarker"));
                    booleans.add(resultSet.getBoolean("flagMarker"));
                    consumer.accept(booleans);
                }
                else {
                    booleans.add(true);
                    consumer.accept(booleans);
                }
            }catch (SQLException ex){
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
                consumer.accept(null);
            }
        });
    }

    public void createUserTable(final Consumer<Void> consumer) {
        renewConnection();

        service.execute(() -> {
            try {
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS `users` (" +
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
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS `positions` (" +
                        "id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                        "`timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                        "userID BIGINT UNSIGNED NOT NULL," +
                        "latitude DOUBLE PRECISION NOT NULL," +
                        "longitude DOUBLE PRECISION NOT NULL," +
                        "CONSTRAINT userID_fk FOREIGN KEY (userID) REFERENCES `users`(id));").execute();
                consumer.accept(null);
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        });
    }

    public void createOrgaTable(final Consumer<Void> consumer) {
        renewConnection();

        service.execute(() -> {
            try {
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS `orga` (" +
                        "id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT," +
                        "userID BIGINT UNSIGNED NOT NULL," +
                        "tacticalMarker BOOL DEFAULT false NOT NULL," +
                        "missionMarker BOOL DEFAULT false NOT NULL," +
                        "hqMarker BOOL DEFAULT false NOT NULL," +
                        "respawnMarker BOOL DEFAULT false NOT NULL," +
                        "flagMarker BOOL DEFAULT false NOT NULL," +
                        "CONSTRAINT orga_PK PRIMARY KEY (id)," +
                        "CONSTRAINT orga_user_FK FOREIGN KEY (userID) REFERENCES `users`(id)" +
                        ");").execute();
                consumer.accept(null);
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        });
    }

    public void createTacticalMarkerTable(final Consumer<Void> consumer) {
        renewConnection();

        service.execute(() -> {
            try {
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS `tacticalMarkers` (" +
                        "`id` bigint(20) unsigned NOT NULL AUTO_INCREMENT," +
                        "  `title` varchar(100) NOT NULL," +
                        "  `description` varchar(255) DEFAULT NULL," +
                        "  `teamID` bigint(20) unsigned DEFAULT NULL," +
                        "  `creator` bigint(20) unsigned NOT NULL," +
                        "  `latitude` double PRECISION NOT NULL," +
                        "  `longitude` double PRECISION NOT NULL," +
                        "  PRIMARY KEY (`id`)," +
                        "  KEY `tacticalPin_teams_FK` (`teamID`)," +
                        "  KEY `tacticalPin_user_FK` (`creator`)," +
                        "  CONSTRAINT `tacticalPin_teams_FK` FOREIGN KEY (`teamID`) REFERENCES `teams` (`id`)," +
                        "  CONSTRAINT `tacticalPin_user_FK` FOREIGN KEY (`creator`) REFERENCES `users` (`id`)" +
                        ");").execute();
                consumer.accept(null);
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        });
    }

    public void createMissionMarkerTable(final Consumer<Void> consumer) {
        renewConnection();

        service.execute(() -> {
            try {
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS `missionMarkers` (" +
                        "`id` bigint(20) unsigned NOT NULL AUTO_INCREMENT," +
                        "  `title` varchar(100) NOT NULL," +
                        "  `description` varchar(255) DEFAULT NULL," +
                        "  `creator` bigint(20) unsigned NOT NULL," +
                        "  `latitude` double PRECISION NOT NULL," +
                        "  `longitude` double PRECISION NOT NULL," +
                        "  PRIMARY KEY (`id`)," +
                        "  KEY `missionPin_user_FK` (`creator`)," +
                        "  CONSTRAINT `missionPin_user_FK` FOREIGN KEY (`creator`) REFERENCES `users` (`id`)" +
                        ");").execute();
                consumer.accept(null);
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        });
    }

    public void createRespawnMarkerTable(final Consumer<Void> consumer) {
        renewConnection();

        service.execute(() -> {
            try {
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS `respawnMarkers` (" +
                        "`id` bigint(20) unsigned NOT NULL AUTO_INCREMENT," +
                        "  `title` varchar(100) NOT NULL," +
                        "  `description` varchar(255) DEFAULT NULL," +
                        "  `creator` bigint(20) unsigned NOT NULL," +
                        "  `latitude` double PRECISION NOT NULL," +
                        "  `longitude` double PRECISION NOT NULL," +
                        "  PRIMARY KEY (`id`)," +
                        "  KEY `respawnPin_user_FK` (`creator`)," +
                        "  CONSTRAINT `respawnPin_user_FK` FOREIGN KEY (`creator`) REFERENCES `users` (`id`)" +
                        ");").execute();
                consumer.accept(null);
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        });
    }

    public void createHQMarkerTable(final Consumer<Void> consumer) {
        renewConnection();

        service.execute(() -> {
            try {
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS `hqMarkers` (" +
                        "`id` bigint(20) unsigned NOT NULL AUTO_INCREMENT," +
                        "  `title` varchar(100) NOT NULL," +
                        "  `description` varchar(255) DEFAULT NULL," +
                        "  `creator` bigint(20) unsigned NOT NULL," +
                        "  `latitude` double PRECISION NOT NULL," +
                        "  `longitude` double PRECISION NOT NULL," +
                        "  PRIMARY KEY (`id`)," +
                        "  KEY `hqPin_user_FK` (`creator`)," +
                        "  CONSTRAINT `hqPin_user_FK` FOREIGN KEY (`creator`) REFERENCES `users` (`id`)" +
                        ");").execute();
                consumer.accept(null);
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        });
    }

    public void createFlagMarkerTable(final Consumer<Void> consumer) {
        renewConnection();

        service.execute(() -> {
            try {
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS `flagMarkers` (" +
                        "`id` bigint(20) unsigned NOT NULL AUTO_INCREMENT," +
                        "  `title` varchar(100) NOT NULL," +
                        "  `description` varchar(255) DEFAULT NULL," +
                        "  `creator` bigint(20) unsigned NOT NULL," +
                        "  `latitude` double PRECISION NOT NULL," +
                        "  `longitude` double PRECISION NOT NULL," +
                        "  PRIMARY KEY (`id`)," +
                        "  KEY `flagPin_user_FK` (`creator`)," +
                        "  CONSTRAINT `flagPin_user_FK` FOREIGN KEY (`creator`) REFERENCES `users` (`id`)" +
                        ");").execute();
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
                        "UNIQUE KEY `teams_UN` (`teamname`),"+
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
                        "FROM `teams` inner join (`positions` inner join `users`) WHERE online=true group by `userID`").executeQuery();

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
                Logger.debug("SQLUser: " + String.valueOf(jsonArray));

                callback.accept(jsonArray);
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("VendorError: " + e.getErrorCode());
                callback.accept(null);
            }
        });

    }

    public void addTacticalMarker(double latitude, double longitude, String teamname, String title, String description, String username){
        renewConnection();
        service.execute(()->{
            try {
                PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO `tacticalMarkers` (latitude, longitude, teamID, title, description, creator) VALUES (?, ?, (SELECT teamname from `teams` WHERE id=?), ?, ?,(SELECT id from `users` WHERE username=?));");
                preparedStatement.setDouble(1, latitude);
                preparedStatement.setDouble(2, longitude);
                preparedStatement.setString(3, teamname);
                preparedStatement.setString(4, title);
                preparedStatement.setString(5, description);
                preparedStatement.setString(6, username);
                preparedStatement.execute();
                System.out.println("asodasdasd");
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("VendorError: " + e.getErrorCode());
            }
        });
    }
    public void addMissionMarker(double latitude, double longitude, String title, String description, String username){
        renewConnection();
        service.execute(()->{
            try {
                PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO `missionMarkers` (latitude, longitude, title, description, creator) VALUES (?, ?, ?, ?,(SELECT id from `users` WHERE username=?));");
                preparedStatement.setDouble(1, latitude);
                preparedStatement.setDouble(2, longitude);
                preparedStatement.setString(3, title);
                preparedStatement.setString(4, description);
                preparedStatement.setString(5, username);
                preparedStatement.execute();
                System.out.println("asodasdasd");
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("VendorError: " + e.getErrorCode());
            }
        });
    }
    public void addRespawnMarker(double latitude, double longitude, String title, String description, String username){
        renewConnection();
        service.execute(()->{
            try {
                PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO `respawnMarkers` (latitude, longitude, title, description, creator) VALUES (?, ?, ?, ?,(SELECT id from `users` WHERE username=?));");
                preparedStatement.setDouble(1, latitude);
                preparedStatement.setDouble(2, longitude);
                preparedStatement.setString(3, title);
                preparedStatement.setString(4, description);
                preparedStatement.setString(5, username);
                preparedStatement.execute();
                System.out.println("asodasdasd");
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("VendorError: " + e.getErrorCode());
            }
        });
    }
    public void addHQMarker(double latitude, double longitude, String title, String description, String username){
        renewConnection();
        service.execute(()->{
            try {
                PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO `hqMarkers` (latitude, longitude, title, description, creator) VALUES (?, ?, ?, ?,(SELECT id from `users` WHERE username=?));");
                preparedStatement.setDouble(1, latitude);
                preparedStatement.setDouble(2, longitude);
                preparedStatement.setString(3, title);
                preparedStatement.setString(4, description);
                preparedStatement.setString(5, username);
                preparedStatement.execute();
                System.out.println("asodasdasd");
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("VendorError: " + e.getErrorCode());
            }
        });
    }
    //TODO: Welche Seite eingenommen
    public void addFlagMarker(double latitude, double longitude, String title, String description, String username){
        renewConnection();
        service.execute(()->{
            try {
                PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO `flagMarkers` (latitude, longitude, title, description, creator) VALUES (?, ?, ?, ?,(SELECT id from `users` WHERE username=?));");
                preparedStatement.setDouble(1, latitude);
                preparedStatement.setDouble(2, longitude);
                preparedStatement.setString(3, title);
                preparedStatement.setString(4, description);
                preparedStatement.setString(5, username);
                preparedStatement.execute();
                System.out.println("asodasdasd");
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("VendorError: " + e.getErrorCode());
            }
        });
    }



    private JsonArray getPositionHistoryFromAllUser() {
        renewConnection();
        JsonArray jsonArray = new JsonArray();
        service.execute(() -> {

                    try {
                        ResultSet resultSet = conn.prepareStatement("SELECT * FROM `users`").executeQuery();

                        ArrayList<Long> userIDs = new ArrayList<Long>();
                        while (resultSet.next()) {
                            userIDs.add(resultSet.getLong("id"));
                        }
                        for (long userID : userIDs) {
                            JsonObject userJsonObject = new JsonObject();
                            final PreparedStatement preparedStatement = conn.prepareStatement("SELECT latitude, longitde, timestamp FROM `positions` where userID = ?");
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
                PreparedStatement preparedStatement = conn.prepareStatement("SELECT id FROM `users` where username = ?");
                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();
                int userID;
                if (resultSet.next()) {
                    userID = resultSet.getInt("id");
                } else {
                    throw new SQLException();
                }

                preparedStatement = conn.prepareStatement("UPDATE `users` SET alive = ?, underfire = ?, mission = ?, support = ? WHERE id = ? ");
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

    public void addOrgaUser(Consumer<Boolean> consumer, final String username, final boolean tacticalPin, final boolean missionPin, final  boolean hqPin, final  boolean respawnPin) {
        renewConnection();
        service.execute(() -> {
            try {
                PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO `orga` (userID, tacticalPin, missionPin, hqPin, respawnPin) VALUES ((SELECT id FROM `users` where username = ?), ?, ?, ?, ?");
                preparedStatement.setString(1, username);
                preparedStatement.setBoolean(2, tacticalPin);
                preparedStatement.setBoolean(3, missionPin);
                preparedStatement.setBoolean(4, hqPin);
                preparedStatement.setBoolean(5, respawnPin);
                preparedStatement.execute();
                consumer.accept(true);
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("VendorError: " + e.getErrorCode());
                consumer.accept(false);
            }
        });
    }

    public void removeOrgaUser(Consumer<Boolean> consumer, final String username){
        renewConnection();
        service.execute(() -> {
            try{
                PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM TABLE `orga` WHERE userID = (SELECT id FROM `users` where username = ?);");
                preparedStatement.setString(1, username);
                preparedStatement.execute();
                consumer.accept(true);
            }catch (SQLException e){
                System.out.println("SQLException: " + e.getMessage());
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("VendorError: " + e.getErrorCode());
                consumer.accept(false);
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
