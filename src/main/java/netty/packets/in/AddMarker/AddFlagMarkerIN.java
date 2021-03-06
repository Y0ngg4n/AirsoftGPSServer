package netty.packets.in.AddMarker;

import com.google.gson.JsonObject;
import netty.packets.PacketIN;

public class AddFlagMarkerIN implements PacketIN {

    private double latitude, longitude;

    private String title, description, username;

    private boolean own;

    @Override
    public void read(JsonObject jsonObject) {
        latitude = jsonObject.get("latitude").getAsDouble();
        longitude = jsonObject.get("longitude").getAsDouble();
        title = jsonObject.get("title").getAsString();
        description = jsonObject.get("description").getAsString();
        username = jsonObject.get("username").getAsString();
        own = jsonObject.get("own").getAsBoolean();
    }

    @Override
    public int getId() {
        return 11;
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

    public boolean isOwn() {
        return own;
    }
}
