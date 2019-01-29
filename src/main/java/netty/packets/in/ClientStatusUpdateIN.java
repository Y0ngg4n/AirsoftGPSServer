package netty.packets.in;

import com.google.gson.JsonObject;
import netty.packets.PacketIN;

public class ClientStatusUpdateIN implements PacketIN {

    private String username;

    private boolean alive, underfire, mission, support;


    @Override
    public void read(JsonObject jsonObject) {
        username = jsonObject.get("username").getAsString();
        alive = jsonObject.get("alive").getAsBoolean();
        underfire = jsonObject.get("underfire").getAsBoolean();
        mission = jsonObject.get("mission").getAsBoolean();
        support = jsonObject.get("support").getAsBoolean();
    }

    @Override
    public int getId() {
        return 3;
    }

    public String getUsername() {
        return username;
    }

    public boolean getAlive() {
        return alive;
    }
    public boolean getUnderfire() {
        return underfire;
    }
    public boolean getMission() {
        return mission;
    }
    public boolean getSupport() {
        return support;
    }

}
