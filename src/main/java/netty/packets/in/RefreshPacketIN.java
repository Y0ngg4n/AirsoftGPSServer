package netty.packets.in;

import com.google.gson.JsonObject;
import netty.packets.PacketIN;

public class RefreshPacketIN implements PacketIN {

    @Override
    public void read(JsonObject jsonObject) {
    }

    @Override
    public int getId() {
        return 50;
    }
}
