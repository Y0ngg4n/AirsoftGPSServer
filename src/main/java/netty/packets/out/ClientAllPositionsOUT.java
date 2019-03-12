package netty.packets.out;

import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class ClientAllPositionsOUT implements PacketOUT {

    private int userID, teamID;
    private boolean alive, underfire, mission, support;
    private String timestamp,  username, teamname ;
    private double latitude, longitude;

    public ClientAllPositionsOUT(int userID, int teamID, boolean alive, boolean underfire, boolean mission, boolean support, String timestamp, String username, String teamname, double latitude, double longitude) {
        this.userID = userID;
        this.teamID = teamID;
        this.alive = alive;
        this.underfire = underfire;
        this.mission = mission;
        this.support = support;
        this.timestamp = timestamp;
        this.username = username;
        this.teamname = teamname;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty("timestamp", timestamp);
        jsonObject.addProperty("userID", userID);
        jsonObject.addProperty("latitude", latitude);
        jsonObject.addProperty("longitude", longitude);
        jsonObject.addProperty("username", username);
        jsonObject.addProperty("teamname", teamname);
        jsonObject.addProperty("teamid", teamID);
        jsonObject.addProperty("alive", alive);
        jsonObject.addProperty("underfire", underfire);
        jsonObject.addProperty("mission", mission);
        jsonObject.addProperty("support", support);
    }

    @Override
    public int getId() {
        return 3;
    }

}
