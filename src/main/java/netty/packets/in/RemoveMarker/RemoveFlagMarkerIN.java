package netty.packets.in.RemoveMarker;

import com.google.gson.JsonObject;
import netty.packets.PacketIN;

public class RemoveFlagMarkerIN implements PacketIN {

    private int markerID;
    private String username;

    @Override
    public void read(JsonObject jsonObject) {
        markerID = jsonObject.get("markerID").getAsInt();
        username = jsonObject.get("username").getAsString();
    }

    @Override
    public int getId() {
        return 16;
    }

    public int getMarkerID() {
        return markerID;
    }

    public String getUsername() {
        return username;
    }
}
