package netty.server;

import com.google.gson.JsonArray;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import netty.packets.Authentification.Authentificator;
import netty.packets.Authentification.User;
import netty.packets.PacketIN;
import netty.packets.in.AuthPacketIN;
import netty.packets.in.ClientPositionIN;
import netty.packets.in.ClientShutdownPacketIN;
import netty.packets.out.ClientAllPositionsOUT;
import netty.packets.out.LoginResponsePacketOUT;
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
                }
            }));
        } else if (packet instanceof ClientPositionIN) {
            final ClientPositionIN clientPositionIN = (ClientPositionIN) packet;
            Logger.debug("Incoming Client Position: lat: " + clientPositionIN.getLatitude() + " long: " + clientPositionIN.getLongitude());
            NettyServer.sqlUser.insertPositionIfChanged(clientPositionIN.getUsername(), clientPositionIN.getLatitude(), clientPositionIN.getLongitude());

            NettyServer.sqlUser.getLatestPositionFromAllUser(jsonArray -> {
                final ClientAllPositionsOUT clientAllPositionsOUT = new ClientAllPositionsOUT(jsonArray);
                for (Channel channel1 : Authenticated.getChannels()) {
                    channel1.writeAndFlush(clientAllPositionsOUT);
                    Logger.debug("Packets send to " + channel.id());
                }
            });
        }
    }
}
