package netty.packets.in;

import com.google.gson.JsonObject;
import netty.packets.PacketIN;

public class ClientPositionIN implements PacketIN {

    private double latitude, longitude;

    private String username;

    @Override
    public void read(JsonObject jsonObject) {
        this.username = jsonObject.get("username").getAsString();
        this.latitude = jsonObject.get("latitude").getAsDouble();
        this.longitude = jsonObject.get("longitude").getAsDouble();
    }

    @Override
    public int getId() {
        return 2;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getUsername() {
        return username;
    }
}
