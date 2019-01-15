package netty.packets.in;

import com.google.gson.JsonObject;
import netty.packets.PacketIN;

public class AuthPacketIN implements PacketIN {

    private String username;
    private String password;

    @Override
    public void read(JsonObject jsonObject) {
        this.username = jsonObject.get("username").getAsString();
        this.password = jsonObject.get("password").getAsString();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public int getId() {
        return 1;
    }

}
