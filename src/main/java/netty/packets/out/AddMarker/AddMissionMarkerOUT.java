package netty.packets.out.AddMarker;

import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class AddMissionMarkerOUT implements PacketOUT {
    private double latitude, longitude;

    private String title, description, creator;

    private int markerID;
    public AddMissionMarkerOUT(double latitude, double longitude, int id, String title, String description, String creator) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.markerID = id;
        this.title = title;
        this.description = description;
        this.creator = creator;
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty("latitude", latitude);
        jsonObject.addProperty("longitude", longitude);
        jsonObject.addProperty("markerID", markerID);
        jsonObject.addProperty("title", title);
        jsonObject.addProperty("description", description);
        jsonObject.addProperty("creator", creator);
    }

    @Override
    public int getId() {
        return 8;
    }
}
