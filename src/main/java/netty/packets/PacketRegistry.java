package netty.packets;

import netty.packets.in.*;

public enum PacketRegistry {
    AuthIN(AuthPacketIN.class),
    ClientPositionIN(ClientPositionIN.class),
    ClientStatusUpdateIN(ClientStatusUpdateIN.class),
    ClientShutdownPacketIN(ClientShutdownPacketIN.class),
    AddFlagMarkerIN(AddFlagMarkerIN.class),
    AddHQMarkerIN(AddHQMarkerIN.class),
    AddMissionMarkerIN(AddMissionMarkerIN.class),
    AddRespawnMarkerIN(AddRespawnMarkerIN.class),
    AddTacticalMarkerIN(AddTacticalMarkerIN.class);

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
