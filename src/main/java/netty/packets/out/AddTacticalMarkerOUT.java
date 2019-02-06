package netty.packets.out;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class AddTacticalMarkerOUT implements PacketOUT {

    private JsonArray jsonArray;

    public AddTacticalMarkerOUT(JsonArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.add("tacticalMarkers", jsonArray);
    }

    @Override
    public int getId() {
        return 7;
    }
}
