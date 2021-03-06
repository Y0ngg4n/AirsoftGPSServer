package netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import netty.packets.Authentification.BCrypt;
import netty.packets.Authentification.SQLUser;
import netty.packets.PacketDecoder;
import netty.packets.PacketEncoder;
import netty.packets.out.ServerShutdownPacketOUT;
import netty.utils.Authenticated;
import netty.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class NettyServer {

    private final EventLoopGroup producer, consumer;
    private final int port;
    private Channel channel;
    public static SQLUser sqlUser = new SQLUser("localhost", 3306, "airsoftgps", "airsoftgps", "bierbrauer-beerzone");
    private final boolean EPOLL = Epoll.isAvailable();

    public NettyServer(final int port) {
        this.producer = EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        this.consumer = EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        this.port = port;
    }

    public NettyServer start() {
        if (port == -1) return this;
        final ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(producer, consumer).channel(EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(final SocketChannel channel) {
                channel.pipeline().
                        addLast(new LengthFieldPrepender(4, true)).
                        addLast("decoder", new PacketDecoder()).
                        addLast("encoder", new PacketEncoder()).addLast(new NetworkHandler());
            }
        });
        this.channel = bootstrap.bind(port).syncUninterruptibly().channel();
        createTables();
        System.out.println("Server started...");
        System.out.println("Enter commands:");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        try {

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equalsIgnoreCase("shutdown")) {

                    shutdown();
                    System.out.println("Send Shutdown");
                    return this;
                } else if (line.toLowerCase().startsWith("hashpass")) {
                    System.out.println(BCrypt.hashpw(line.split(" ")[1], BCrypt.gensalt()));
                } else if (line.toLowerCase().startsWith("orga")) {
                    String args[] = line.split(" ");
                    if (args[1].equalsIgnoreCase("add")) {
                        sqlUser.addOrgaUser(aBoolean -> {
                            if (aBoolean) Logger.debug("§eUser " + args[2] + "sucessfully created!");
                            else Logger.debug("§eUser could not be created!");
                        }, args[2], Boolean.valueOf(args[3]), Boolean.valueOf(args[4]), Boolean.valueOf(args[5]), Boolean.valueOf(args[6]));
                    } else if (args[1].equalsIgnoreCase("remove")) {
                        sqlUser.removeOrgaUser(aBoolean -> {
                            if (aBoolean) Logger.debug("§eUser " + args[2] + "sucessfully removed!");
                            else Logger.debug("§eUser could not be removed!");
                        }, args[2]);
                    }
                }
                System.out.println("Enter commands:");
            }
        } catch (IOException ignored) {
        }
        return this;
    }

    public void shutdown() {
        Authenticated.getChannels().forEach(channel1 -> channel1.writeAndFlush(new ServerShutdownPacketOUT()));
        producer.shutdownGracefully();
        consumer.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
    }

    private void createTables() {
        sqlUser.createTeamsTable(aVoid -> {
            sqlUser.createUserTable(aVoid1 -> {
                sqlUser.createPositionTable(aVoid2 -> {
                    sqlUser.createOrgaTable(aVoid3 -> {
                        sqlUser.createTacticalMarkerTable(aVoid4 -> {
                            sqlUser.createMissionMarkerTable(aVoid5 -> {
                                sqlUser.createRespawnMarkerTable(aVoid6 -> {
                                    sqlUser.createHQMarkerTable(aVoid7 -> {
                                        sqlUser.createFlagMarkerTable(aVoid8 -> {
                                            Logger.info("§eTables successfully created!");
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }
}
