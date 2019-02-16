package netty.packets.out.AddMarker;

import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class AddTacticalMarkerOUT implements PacketOUT {

    private int markerID;

    private double latitude, longitude;

    private String teamname, title, description, creator;

    public AddTacticalMarkerOUT(double latitude, double longitude, int id,  String title, String teamname, String description, String creator) {
        this.markerID = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.teamname = teamname;
        this.title = title;
        this.description = description;
        this.creator = creator;
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty("markerID", markerID);
        jsonObject.addProperty("latitude", latitude);
        jsonObject.addProperty("longitude", longitude);
        jsonObject.addProperty("teamname", teamname);
        jsonObject.addProperty("title", title);
        jsonObject.addProperty("description", description);
        jsonObject.addProperty("creator", creator);
    }

    @Override
    public int getId() {
        return 7;
    }
}
