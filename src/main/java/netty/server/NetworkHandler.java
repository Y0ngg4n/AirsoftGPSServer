package netty.server;

import com.google.gson.JsonElement;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import netty.packets.Authentification.Authentificator;
import netty.packets.Authentification.User;
import netty.packets.PacketIN;
import netty.packets.in.*;
import netty.packets.in.AddMarker.*;
import netty.packets.in.RemoveMarker.*;
import netty.packets.in.UpdateMarker.UpdateFlagMarkerIN;
import netty.packets.out.*;
import netty.packets.out.AddMarker.*;
import netty.utils.Authenticated;
import netty.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkHandler extends SimpleChannelInboundHandler<PacketIN> {

    private Map<Channel, User> userMap = new HashMap<Channel, User>();
    private List<User> users = new ArrayList<User>();

    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) {
        Authenticated.remove(ctx.channel());
        userMap.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final PacketIN packet) {
        final Channel channel = ctx.channel();
        if (packet instanceof ClientShutdownPacketIN) {
            try {
                channel.close().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        } else if (packet instanceof AuthPacketIN) {
            final AuthPacketIN authPacket = (AuthPacketIN) packet;

            NettyServer.sqlUser.getUser(authPacket.getUsername(), user -> Authentificator.checkLogin(user, authPacket.getPassword(), aBoolean -> {
                if (users.contains(user)) aBoolean = false;
                ctx.writeAndFlush(new LoginResponsePacketOUT(aBoolean));
                if (aBoolean) {
                    users.add(user);
                    userMap.put(channel, user);
                    Authenticated.add(channel);
                    Logger.info("§eUser: §c" + user.getUsername() + "§e Loggte sich ein");
                    //Send Orga Packet
                    NettyServer.sqlUser.isOrga(aBoolean1 -> {
                        if (aBoolean1.get(0)) {
                            ctx.writeAndFlush(new OrgaAuthOut(aBoolean1.get(0), aBoolean1.get(1), aBoolean1.get(2), aBoolean1.get(3), aBoolean1.get(4), aBoolean1.get(5)));
                            Logger.info("§eSending OrgaAuth-Packet to " + user.getUsername());
                        }
                    }, user.getUsername());
                    //Set User Online
                    NettyServer.sqlUser.setOnlineUser(user.getUsername(), true);
                    //Send All Positions out
                    sendLatestPositionMarkers(channel);
                    //Send all Status out
                    sendStatusUpdateOUT(channel);
                    //Send all TacticalMarker
                    sendTacticalMarkers(channel);
                    sendMissionMarkers(channel);
                    sendRespawnMarkers(channel);
                    sendHQMarkers(channel);
                    sendFlagMarkers(channel);
                }
            }));
        } else if (packet instanceof ClientPositionIN) {
            final ClientPositionIN clientPositionIN = (ClientPositionIN) packet;
            Logger.debug("Incoming Client Position: lat: " + clientPositionIN.getLatitude() + " long: " + clientPositionIN.getLongitude());
            NettyServer.sqlUser.insertOrUpdatePositionIfChanged(clientPositionIN.getUsername(), clientPositionIN.getLatitude(), clientPositionIN.getLongitude());
            sendLatestPositionMarkers();
        } else if (packet instanceof ClientStatusUpdateIN) {
            final ClientStatusUpdateIN clientStatusUpdateIN = (ClientStatusUpdateIN) packet;
            Logger.debug("Incoming Client Status: alive: " + clientStatusUpdateIN.getAlive() + " underfire: " + clientStatusUpdateIN.getUnderfire() + " mission: " + clientStatusUpdateIN.getMission() + " support: " + clientStatusUpdateIN.getSupport());
            NettyServer.sqlUser.updateUserStatus(clientStatusUpdateIN.getUsername(), clientStatusUpdateIN.getAlive(), clientStatusUpdateIN.getUnderfire(), clientStatusUpdateIN.getMission(), clientStatusUpdateIN.getSupport());
            sendStatusUpdateOUT();
        } else if (packet instanceof AddTacticalMarkerIN) {
            AddTacticalMarkerIN addTacticalMarkerIN = (AddTacticalMarkerIN) packet;
            NettyServer.sqlUser.addTacticalMarker(addTacticalMarkerIN.getLatitude(), addTacticalMarkerIN.getLongitude(), addTacticalMarkerIN.getTeamname(), addTacticalMarkerIN.getTitle(), addTacticalMarkerIN.getDescription(), addTacticalMarkerIN.getUsername());
            Logger.debug("Add Tactical Marker");
            sendTacticalMarkers();
        } else if (packet instanceof AddMissionMarkerIN) {
            AddMissionMarkerIN addMissionMarkerIN = (AddMissionMarkerIN) packet;
            NettyServer.sqlUser.addMissionMarker(addMissionMarkerIN.getLatitude(), addMissionMarkerIN.getLongitude(), addMissionMarkerIN.getTitle(), addMissionMarkerIN.getDescription(), addMissionMarkerIN.getUsername());
            Logger.debug("Add Mission Marker");
            sendMissionMarkers();
        } else if (packet instanceof AddRespawnMarkerIN) {
            AddRespawnMarkerIN addRespawnMarkerIN = (AddRespawnMarkerIN) packet;
            NettyServer.sqlUser.addRespawnMarker(addRespawnMarkerIN.getLatitude(), addRespawnMarkerIN.getLongitude(), addRespawnMarkerIN.getTitle(), addRespawnMarkerIN.getDescription(), addRespawnMarkerIN.getUsername());
            Logger.debug("Add Respawn Marker");
            sendRespawnMarkers();
        } else if (packet instanceof AddHQMarkerIN) {
            AddHQMarkerIN addHQMarkerIN = (AddHQMarkerIN) packet;
            NettyServer.sqlUser.addHQMarker(addHQMarkerIN.getLatitude(), addHQMarkerIN.getLongitude(), addHQMarkerIN.getTitle(), addHQMarkerIN.getDescription(), addHQMarkerIN.getUsername());
            Logger.debug("Add HQ Marker");
            sendHQMarkers();
        } else if (packet instanceof AddFlagMarkerIN) {
            AddFlagMarkerIN addFlagMarkerIN = (AddFlagMarkerIN) packet;
            NettyServer.sqlUser.addFlagMarker(addFlagMarkerIN.getLatitude(), addFlagMarkerIN.getLongitude(), addFlagMarkerIN.getTitle(), addFlagMarkerIN.getDescription(), addFlagMarkerIN.getUsername());
            Logger.debug("Add Flag Marker");
            sendFlagMarkers();
        } else if (packet instanceof RemoveTacticalMarkerIN) {
            RemoveTacticalMarkerIN removeTacticalMarkerIN = (RemoveTacticalMarkerIN) packet;
            NettyServer.sqlUser.removeTacticalMarker(removeTacticalMarkerIN.getMarkerID(), removeTacticalMarkerIN.getUsername());
            Logger.debug("Removed Tactical Marker");
            sendTacticalMarkers();
        } else if (packet instanceof RemoveMissionMarkerIN) {
            RemoveMissionMarkerIN removeMissionMarkerIN = (RemoveMissionMarkerIN) packet;
            NettyServer.sqlUser.removeMissionMarker(removeMissionMarkerIN.getMarkerID(), removeMissionMarkerIN.getUsername());
            Logger.debug("Removed Mission Marker");
            sendMissionMarkers();
        } else if (packet instanceof RemoveRespawnMarkerIN) {
            RemoveRespawnMarkerIN removeRespawnMarkerIN = (RemoveRespawnMarkerIN) packet;
            NettyServer.sqlUser.removeRespawnMarker(removeRespawnMarkerIN.getMarkerID(), removeRespawnMarkerIN.getUsername());
            Logger.debug("Removed Respawn Marker");
            sendRespawnMarkers();
        } else if (packet instanceof RemoveHQMarkerIN) {
            RemoveHQMarkerIN removeHQMarkerIN = (RemoveHQMarkerIN) packet;
            NettyServer.sqlUser.removeHQMarker(removeHQMarkerIN.getMarkerID(), removeHQMarkerIN.getUsername());
            Logger.debug("Removed HQ Marker");
            sendHQMarkers();
        } else if (packet instanceof RemoveFlagMarkerIN) {
            RemoveFlagMarkerIN removeFlagMarkerIN = (RemoveFlagMarkerIN) packet;
            NettyServer.sqlUser.removeFlagMarker(removeFlagMarkerIN.getMarkerID(), removeFlagMarkerIN.getUsername());
            Logger.debug("Removed Flag Marker");
            sendFlagMarkers();
        } else if (packet instanceof UpdateFlagMarkerIN) {
            UpdateFlagMarkerIN updateFlagMarkerIN = (UpdateFlagMarkerIN) packet;
            NettyServer.sqlUser.updateFlagMarker(aVoid ->{
                sendFlagMarkers();
            }, updateFlagMarkerIN.getFlagID(), updateFlagMarkerIN.isOwn());

        } else if (packet instanceof RefreshPacketIN) {
            Logger.debug("Refresh requested. Sending Packets...");
            sendLatestPositionMarkers(channel);
            sendStatusUpdateOUT(channel);
            sendTacticalMarkers(channel);
            sendMissionMarkers(channel);
            sendRespawnMarkers(channel);
            sendHQMarkers(channel);
            sendFlagMarkers(channel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Logger.error(cause.getMessage());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (userMap.containsKey(ctx.channel())) {
            Logger.info(userMap.get(ctx.channel()).getUsername());
            NettyServer.sqlUser.setOnlineUser(userMap.get(ctx.channel()).getUsername(), false);
        }
    }


    private void sendLatestPositionMarkers() {
        NettyServer.sqlUser.getLatestPositionFromAllUser(jsonArray -> {
            final ClientAllPositionsOUT clientAllPositionsOUT = new ClientAllPositionsOUT(jsonArray);
            for (Channel channel : Authenticated.getChannels()) {
                channel.writeAndFlush(clientAllPositionsOUT);
                Logger.debug("Packets send to " + channel.id());
                Logger.debug("ClientPositionIN " + String.valueOf(jsonArray));
            }
        });
    }

    private void sendLatestPositionMarkers(Channel channel) {
        NettyServer.sqlUser.getLatestPositionFromAllUser(jsonArray -> {
            final ClientAllPositionsOUT clientAllPositionsOUT = new ClientAllPositionsOUT(jsonArray);
            channel.writeAndFlush(clientAllPositionsOUT);
            Logger.debug("Packets send to " + channel.id());
            Logger.debug("ClientPositionIN " + String.valueOf(jsonArray));
        });
    }

    private void sendStatusUpdateOUT() {
        NettyServer.sqlUser.getLatestPositionFromAllUser(jsonArray -> {
            final ClientAllPositionsOUT clientAllPositionsOUT = new ClientAllPositionsOUT(jsonArray);
            for (Channel channel : Authenticated.getChannels()) {
                channel.writeAndFlush(clientAllPositionsOUT);
                Logger.debug("ClientStatusOUT-Packet send to " + channel.id());
                Logger.debug("ClientStatusIN " + String.valueOf(jsonArray));
            }
        });
    }

    private void sendStatusUpdateOUT(Channel channel) {
        NettyServer.sqlUser.getLatestPositionFromAllUser(jsonArray -> {
            final ClientAllPositionsOUT clientAllPositionsOUT = new ClientAllPositionsOUT(jsonArray);
            channel.writeAndFlush(clientAllPositionsOUT);
            Logger.debug("ClientStatusOUT-Packet send to " + channel.id());
            Logger.debug("ClientStatusIN " + String.valueOf(jsonArray));
        });
    }

    private void sendTacticalMarkers() {
        NettyServer.sqlUser.getAllTacticalMarker(jsonArray -> {
            for (JsonElement jsonElement : jsonArray) {
                final AddTacticalMarkerOUT addTacticalMarkerOUT = new AddTacticalMarkerOUT(
                        jsonElement.getAsJsonObject().get("latitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("longitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("markerID").getAsInt(),
                        jsonElement.getAsJsonObject().get("title").getAsString(),
                        jsonElement.getAsJsonObject().get("teamname").getAsString(),
                        jsonElement.getAsJsonObject().get("description").getAsString(),
                        jsonElement.getAsJsonObject().get("creator").getAsString()
                );
                for (Channel channel : Authenticated.getChannels()) {
                    channel.writeAndFlush(addTacticalMarkerOUT);
                    Logger.debug("AddTacticalMarkerOUT-Packet send to " + channel.id());
                    Logger.debug("AddTacticalMarkerOUT-Packet " + jsonElement);
                }
            }
        });
    }

    private void sendTacticalMarkers(Channel channel) {
        NettyServer.sqlUser.getAllTacticalMarker(jsonArray -> {
            for (JsonElement jsonElement : jsonArray) {
                final AddTacticalMarkerOUT addTacticalMarkerOUT = new AddTacticalMarkerOUT(
                        jsonElement.getAsJsonObject().get("latitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("longitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("markerID").getAsInt(),
                        jsonElement.getAsJsonObject().get("title").getAsString(),
                        jsonElement.getAsJsonObject().get("teamname").getAsString(),
                        jsonElement.getAsJsonObject().get("description").getAsString(),
                        jsonElement.getAsJsonObject().get("creator").getAsString()
                );
                channel.writeAndFlush(addTacticalMarkerOUT);
                Logger.debug("AddTacticalMarkerOUT-Packet send to " + channel.id());
                Logger.debug("AddTacticalMarkerOUT-Packet " + jsonElement);
            }
        });
    }

    private void sendMissionMarkers() {
        NettyServer.sqlUser.getAllMissionMarker(jsonArray -> {
            for (JsonElement jsonElement : jsonArray) {
                final AddMissionMarkerOUT addMissionMarkerOUT = new AddMissionMarkerOUT(
                        jsonElement.getAsJsonObject().get("latitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("longitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("markerID").getAsInt(),
                        jsonElement.getAsJsonObject().get("title").getAsString(),
                        jsonElement.getAsJsonObject().get("description").getAsString(),
                        jsonElement.getAsJsonObject().get("creator").getAsString()
                );
                for (Channel channel : Authenticated.getChannels()) {
                    channel.writeAndFlush(addMissionMarkerOUT);
                    Logger.debug("AddMissionMarkerOUT-Packet send to " + channel.id());
                    Logger.debug("AddMissionMarkerOUT-Packet " + jsonElement);
                }
            }
        });
    }

    private void sendMissionMarkers(Channel channel) {
        NettyServer.sqlUser.getAllMissionMarker(jsonArray -> {
            for (JsonElement jsonElement : jsonArray) {
                final AddMissionMarkerOUT addMissionMarkerOUT = new AddMissionMarkerOUT(
                        jsonElement.getAsJsonObject().get("latitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("longitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("markerID").getAsInt(),
                        jsonElement.getAsJsonObject().get("title").getAsString(),
                        jsonElement.getAsJsonObject().get("description").getAsString(),
                        jsonElement.getAsJsonObject().get("creator").getAsString()
                );
                channel.writeAndFlush(addMissionMarkerOUT);
                Logger.debug("AddMissionMarkerOUT-Packet send to " + channel.id());
                Logger.debug("AddMissionMarkerOUT-Packet " + jsonElement);
            }
        });
    }

    private void sendRespawnMarkers() {
        NettyServer.sqlUser.getAllRespawnMarker(jsonArray -> {
            for (JsonElement jsonElement : jsonArray) {
                final AddRespawnMarkerOUT addRespawnMarkerOUT = new AddRespawnMarkerOUT(
                        jsonElement.getAsJsonObject().get("latitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("longitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("markerID").getAsInt(),
                        jsonElement.getAsJsonObject().get("title").getAsString(),
                        jsonElement.getAsJsonObject().get("description").getAsString(),
                        jsonElement.getAsJsonObject().get("creator").getAsString(),
                        jsonElement.getAsJsonObject().get("own").getAsBoolean()
                );
                for (Channel channel : Authenticated.getChannels()) {
                    channel.writeAndFlush(addRespawnMarkerOUT);
                    Logger.debug("AddRespawnMarkerOUT-Packet send to " + channel.id());
                    Logger.debug("AddRespawnMarkerOUT-Packet " + jsonElement);
                }
            }
        });
    }

    private void sendRespawnMarkers(Channel channel) {
        NettyServer.sqlUser.getAllRespawnMarker(jsonArray -> {
            for (JsonElement jsonElement : jsonArray) {
                final AddRespawnMarkerOUT addRespawnMarkerOUT = new AddRespawnMarkerOUT(
                        jsonElement.getAsJsonObject().get("latitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("longitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("markerID").getAsInt(),
                        jsonElement.getAsJsonObject().get("title").getAsString(),
                        jsonElement.getAsJsonObject().get("description").getAsString(),
                        jsonElement.getAsJsonObject().get("creator").getAsString(),
                        jsonElement.getAsJsonObject().get("own").getAsBoolean()
                );
                channel.writeAndFlush(addRespawnMarkerOUT);
                Logger.debug("AddRespawnMarkerOUT-Packet send to " + channel.id());
                Logger.debug("AddRespawnMarkerOUT-Packet " + jsonElement);
            }
        });
    }

    private void sendHQMarkers() {
        NettyServer.sqlUser.getAllHQMarker(jsonArray -> {
            for (JsonElement jsonElement : jsonArray) {
                final AddHQMarkerOUT addHQMarkerOUT = new AddHQMarkerOUT(
                        jsonElement.getAsJsonObject().get("latitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("longitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("markerID").getAsInt(),
                        jsonElement.getAsJsonObject().get("title").getAsString(),
                        jsonElement.getAsJsonObject().get("description").getAsString(),
                        jsonElement.getAsJsonObject().get("creator").getAsString(),
                        jsonElement.getAsJsonObject().get("own").getAsBoolean()
                );
                for (Channel channel : Authenticated.getChannels()) {
                    channel.writeAndFlush(addHQMarkerOUT);
                    Logger.debug("AddHQMarkerOUT-Packet send to " + channel.id());
                    Logger.debug("AddHQMarkerOUT-Packet " + jsonElement);
                }
            }
        });
    }

    private void sendHQMarkers(Channel channel) {
        NettyServer.sqlUser.getAllHQMarker(jsonArray -> {
            for (JsonElement jsonElement : jsonArray) {
                final AddHQMarkerOUT addHQMarkerOUT = new AddHQMarkerOUT(
                        jsonElement.getAsJsonObject().get("latitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("longitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("markerID").getAsInt(),
                        jsonElement.getAsJsonObject().get("title").getAsString(),
                        jsonElement.getAsJsonObject().get("description").getAsString(),
                        jsonElement.getAsJsonObject().get("creator").getAsString(),
                        jsonElement.getAsJsonObject().get("own").getAsBoolean()
                );
                channel.writeAndFlush(addHQMarkerOUT);
                Logger.debug("AddHQMarkerOUT-Packet send to " + channel.id());
                Logger.debug("AddHQMarkerOUT-Packet " + jsonElement);
            }
        });
    }

    private void sendFlagMarkers() {
        NettyServer.sqlUser.getAllFlagMarker(jsonArray -> {
            for (JsonElement jsonElement : jsonArray) {
                final AddFlagMarkerOUT addFlagMarkerOUT = new AddFlagMarkerOUT(
                        jsonElement.getAsJsonObject().get("latitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("longitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("markerID").getAsInt(),
                        jsonElement.getAsJsonObject().get("title").getAsString(),
                        jsonElement.getAsJsonObject().get("description").getAsString(),
                        jsonElement.getAsJsonObject().get("creator").getAsString(),
                        jsonElement.getAsJsonObject().get("own").getAsBoolean()
                );
                for (Channel channel : Authenticated.getChannels()) {
                    channel.writeAndFlush(addFlagMarkerOUT);
                    Logger.debug("AddFlagMarkerOUT-Packet send to " + channel.id());
                    Logger.debug("AddFlagMarkerOUT-Packet " + jsonElement);
                }
            }
        });
    }

    private void sendFlagMarkers(Channel channel) {
        NettyServer.sqlUser.getAllFlagMarker(jsonArray -> {
            for (JsonElement jsonElement : jsonArray) {
                final AddFlagMarkerOUT addFlagMarkerOUT = new AddFlagMarkerOUT(
                        jsonElement.getAsJsonObject().get("latitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("longitude").getAsDouble(),
                        jsonElement.getAsJsonObject().get("markerID").getAsInt(),
                        jsonElement.getAsJsonObject().get("title").getAsString(),
                        jsonElement.getAsJsonObject().get("description").getAsString(),
                        jsonElement.getAsJsonObject().get("creator").getAsString(),
                        jsonElement.getAsJsonObject().get("own").getAsBoolean()
                );
                channel.writeAndFlush(addFlagMarkerOUT);
                Logger.debug("AddFlagMarkerOUT-Packet send to " + channel.id());
                Logger.debug("AddFlagMarkerOUT-Packet " + jsonElement);
            }
        });
    }
}
