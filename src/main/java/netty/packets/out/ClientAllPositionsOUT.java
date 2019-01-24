package netty.packets.out;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class ClientAllPositionsOUT implements PacketOUT {

    private JsonArray jsonArray;

    public ClientAllPositionsOUT(JsonArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.add("positions", jsonArray);
    }

    @Override
    public int getId() {
        return 3;
    }

    public JsonArray getJsonArray() {
        return jsonArray;
    }
}
