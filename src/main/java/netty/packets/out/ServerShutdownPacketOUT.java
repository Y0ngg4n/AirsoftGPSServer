package netty.packets.out;

import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class ServerShutdownPacketOUT implements PacketOUT {

    @Override
    public void write(final JsonObject jsonObject) {}

    @Override
    public int getId() {
        return 5;
    }
}
