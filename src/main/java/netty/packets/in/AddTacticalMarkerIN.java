package netty.packets.in;

import com.google.gson.JsonObject;
import netty.packets.PacketIN;

public class AddTacticalMarkerIN implements PacketIN {

    private double latitude, longitude;

    private String teamname, title, description, username;

    @Override
    public void read(JsonObject jsonObject) {
        latitude = jsonObject.get("latitude").getAsDouble();
        longitude = jsonObject.get("longitude").getAsDouble();
        title = jsonObject.get("title").getAsString();
        teamname = jsonObject.get("teamname").getAsString();
        description = jsonObject.get("description").getAsString();
        username = jsonObject.get("username").getAsString();
    }

    @Override
    public int getId() {
        return 7;
    }

    public String getUsername() {
        return username;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getTeamname() {
        return teamname;
    }
}
