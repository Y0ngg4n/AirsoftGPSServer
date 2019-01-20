package netty.packets.out;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class ClientAllPositionsHistoryOut implements PacketOUT {

    private JsonArray jsonArray;

    public ClientAllPositionsHistoryOut(JsonArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.add("array", jsonArray);
    }

    @Override
    public int getId() {
        return 4;
    }
}
