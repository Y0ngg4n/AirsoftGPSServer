package netty.packets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import netty.utils.ByteBuffers;
import netty.utils.Logger;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {

    private final Gson gson = new Gson();

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf byteBuf, final List<Object> list) throws Exception {
        if (byteBuf instanceof EmptyByteBuf) return;
//        System.out.println(byteBuf.toString(StandardCharsets.UTF_8));
        int id = byteBuf.readInt();
        final Class<? extends PacketIN> p = PacketRegistry.getPacket(id);
        if (p == null) {
            Logger.error("§cInvalid packets received");
            return;
        }
        final PacketIN packet = p.newInstance();
        final String buff = ByteBuffers.readString(byteBuf);
        packet.read(gson.fromJson(buff, JsonObject.class));
        list.add(packet);
    }
}