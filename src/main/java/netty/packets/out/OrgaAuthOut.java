package netty.packets.out;

import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class OrgaAuthOut implements PacketOUT {

    private boolean successfull;

    public OrgaAuthOut(boolean successfull) {
        this.successfull = successfull;
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty("successfull", successfull);

    }

    @Override
    public int getId() {
        return 6;
    }
}
