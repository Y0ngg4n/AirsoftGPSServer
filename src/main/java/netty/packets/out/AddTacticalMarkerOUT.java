package netty.packets.out;

import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class AddTacticalMarkerOUT implements PacketOUT {
    private double latitude, longitude;

    private String teamname, title, description, username;

    public AddTacticalMarkerOUT(double latitude, double longitude, String teamname, String title, String description, String username) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.description = description;
        this.username = username;
        this.teamname = teamname;
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty("latitude", latitude);
        jsonObject.addProperty("longitude", longitude);
        jsonObject.addProperty("teamname", teamname);
        jsonObject.addProperty("title", title);
        jsonObject.addProperty("description", description);
        jsonObject.addProperty("username", username);
    }

    @Override
    public int getId() {
        return 0;
    }
}
