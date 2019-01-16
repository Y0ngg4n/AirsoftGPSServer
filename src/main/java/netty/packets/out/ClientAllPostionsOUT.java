package netty.packets.out;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class ClientAllPostionsOUT implements PacketOUT {
    @Override
    public void write(JsonObject jsonObject) {

    }

    @Override
    public int getId() {
        return 3;
    }
}
