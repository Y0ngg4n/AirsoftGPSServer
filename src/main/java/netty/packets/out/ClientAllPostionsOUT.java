package netty.packets.out;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class ClientAllPostionsOUT implements PacketOUT {

    private JsonArray jsonArray;

    public ClientAllPostionsOUT(JsonArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.add("array", jsonArray);
    }

    @Override
    public int getId() {
        return 3;
    }
}
