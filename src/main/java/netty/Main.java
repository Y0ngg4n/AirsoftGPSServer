package netty;

import netty.server.NettyServer;

public class Main {
    public static void main(String[] args) {
        final NettyServer nettyServer = new NettyServer(12345).start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            nettyServer.shutdown();
        }));
    }
}
