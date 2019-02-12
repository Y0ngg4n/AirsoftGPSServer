package netty.packets.out.AddMarker;

import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class AddFlagMarkerOUT implements PacketOUT {

    private double latitude, longitude;

    private String title, description, username;

    public AddFlagMarkerOUT(double latitude, double longitude, String title, String description, String username) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.description = description;
        this.username = username;
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty("latitude", latitude);
        jsonObject.addProperty("longitude", longitude);
        jsonObject.addProperty("title", title);
        jsonObject.addProperty("description", description);
        jsonObject.addProperty("username", username);
    }

    @Override
    public int getId() {
        return 11;
    }
}
