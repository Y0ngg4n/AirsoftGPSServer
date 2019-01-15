package netty.packets.in;

import com.google.gson.JsonObject;
import netty.packets.PacketIN;

public class ClientShutdownPacketIN implements PacketIN {

    @Override
    public void read(final JsonObject jsonObject) {}

    @Override
    public int getId() {
        return 4;
    }
}
