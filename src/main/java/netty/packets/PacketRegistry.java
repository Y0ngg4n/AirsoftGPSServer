package netty.packets;

import netty.packets.in.*;
import netty.utils.Logger;

public enum PacketRegistry {
    AuthIN(AuthPacketIN.class),
    ClientPositionIN(ClientPositionIN.class),
    ClientStatusUpdateIN(ClientStatusUpdateIN.class),
    ClientShutdownPacketIN(ClientShutdownPacketIN.class),
    AddFlagMarkerIN(netty.packets.in.AddMarker.AddFlagMarkerIN.class),
    AddHQMarkerIN(netty.packets.in.AddMarker.AddHQMarkerIN.class),
    AddMissionMarkerIN(netty.packets.in.AddMarker.AddMissionMarkerIN.class),
    AddRespawnMarkerIN(netty.packets.in.AddMarker.AddRespawnMarkerIN.class),
    AddTacticalMarkerIN(netty.packets.in.AddMarker.AddTacticalMarkerIN.class),
    RefreshPacketIN(netty.packets.in.RefreshPacketIN.class),
    RemoveTacticalMarkerIN(netty.packets.in.RemoveMarker.RemoveTacticalMarkerIN.class);

    private Class<? extends PacketIN> clazz;

    PacketRegistry(Class<? extends PacketIN> clazz) {
        this.clazz = clazz;
    }

    public static Class<? extends PacketIN> getPacket(int id) {
        for (PacketRegistry value : values()) {
            try {
                if (value.getClazz().newInstance().getId() == id) {
                    return value.getClazz();
                }
            } catch (final InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public Class<? extends PacketIN> getClazz() {
        return clazz;
    }
}
