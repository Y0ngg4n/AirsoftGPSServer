package netty.utils;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.DefaultEventExecutor;

public class Authenticated {

    private static final ChannelGroup channels = new DefaultChannelGroup(new DefaultEventExecutor());

    public static void add(final Channel channel) {
        channels.add(channel);
    }

    public static void remove(final Channel channel) {
        channels.remove(channel);
    }

    public static ChannelGroup getChannels() {
        return channels;
    }
}
