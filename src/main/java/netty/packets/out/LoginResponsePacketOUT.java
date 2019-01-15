package netty.packets.out;

import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class LoginResponsePacketOUT implements PacketOUT {

    private final boolean success;

    public LoginResponsePacketOUT(boolean success) {
        this.success = success;
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty("success", this.success);
    }

    @Override
    public int getId() {
        return 1;
    }
}
