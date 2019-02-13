package netty.packets.out.AddMarker;

import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class AddMissionMarkerOUT implements PacketOUT {
    private double latitude, longitude;

    private String title, description, username;

    private int markerID;
    public AddMissionMarkerOUT(double latitude, double longitude, int id, String title, String description, String username) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.markerID = id;
        this.title = title;
        this.description = description;
        this.username = username;
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty("latitude", latitude);
        jsonObject.addProperty("longitude", longitude);
        jsonObject.addProperty("markerID", markerID);
        jsonObject.addProperty("title", title);
        jsonObject.addProperty("description", description);
        jsonObject.addProperty("username", username);
    }

    @Override
    public int getId() {
        return 8;
    }
}
