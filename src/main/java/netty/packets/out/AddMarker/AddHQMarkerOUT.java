package netty.packets.out.AddMarker;

import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class AddHQMarkerOUT implements PacketOUT {
    private double latitude, longitude;

    private String title, description, creator;

    private boolean own;

    private int markerID;

    public AddHQMarkerOUT(double latitude, double longitude, int markerID, String title, String description, String creator, boolean own) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.markerID = markerID;
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.own = own;
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty("latitude", latitude);
        jsonObject.addProperty("longitude", longitude);
        jsonObject.addProperty("title", title);
        jsonObject.addProperty("markerID", markerID);
        jsonObject.addProperty("description", description);
        jsonObject.addProperty("creator", creator);
        jsonObject.addProperty("own", own);
    }

    @Override
    public int getId() {
        return 10;
    }
}
