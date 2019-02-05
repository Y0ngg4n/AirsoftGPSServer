package netty.packets.out;

import com.google.gson.JsonObject;
import netty.packets.PacketOUT;

public class OrgaAuthOut implements PacketOUT {

    private boolean successful, tacticalMarker, missionMarker, hqMarker, respawnMarker, flagMarker;

    public OrgaAuthOut(boolean successful, boolean tacticalMarker, boolean missionMarker, boolean hqMarker, boolean respawnMarker, boolean flagMarker) {
        this.successful = successful;
        this.tacticalMarker = tacticalMarker;
        this.missionMarker = missionMarker;
        this.hqMarker = hqMarker;
        this.respawnMarker = respawnMarker;
        this.flagMarker = flagMarker;
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty("successful", successful);
        jsonObject.addProperty("tacticalMarker", tacticalMarker);
        jsonObject.addProperty("missionMarker", missionMarker);
        jsonObject.addProperty("hqMarker", hqMarker);
        jsonObject.addProperty("respawnMarker", respawnMarker);
        jsonObject.addProperty("flagMarker", flagMarker);
    }

    @Override
    public int getId() {
        return 6;
    }
}
