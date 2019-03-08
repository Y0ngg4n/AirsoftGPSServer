package netty.packets.in.UpdateMarker;

import com.google.gson.JsonObject;
import netty.packets.PacketIN;

public class UpdateFlagMarkerIN implements PacketIN {

    private boolean isOwn;
    private int flagID;

    public boolean isOwn() {
        return isOwn;
    }

    public int getFlagID() {
        return flagID;
    }

    @Override
    public void read(JsonObject jsonObject) {
        isOwn = jsonObject.get("flagID").getAsBoolean();
        flagID = jsonObject.get("own").getAsInt();
    }

    @Override
    public int getId() {
        return 17;
    }
}
