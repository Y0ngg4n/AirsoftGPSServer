package netty.packets.out.AddMarker;

import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class AddFlagMarkerOUT implements PacketOUT {

    private double latitude, longitude;

    private String title, description, creator;

    private boolean own;

    private int markerID;

    public AddFlagMarkerOUT(double latitude, double longitude, int id,String title, String description, String creator, boolean own) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.markerID = id;
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.own = own;
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty("latitude", latitude);
        jsonObject.addProperty("longitude", longitude);
        jsonObject.addProperty("markerID", markerID);
        jsonObject.addProperty("title", title);
        jsonObject.addProperty("description", description);
        jsonObject.addProperty("creator", creator);
        jsonObject.addProperty("own", own);
    }

    @Override
    public int getId() {
        return 11;
    }
}
