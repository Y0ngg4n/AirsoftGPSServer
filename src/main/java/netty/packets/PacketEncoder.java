package netty.packets;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import netty.utils.ByteBuffers;

public class PacketEncoder extends MessageToByteEncoder<PacketOUT> {

    @Override
    protected void encode(final ChannelHandlerContext ctx, final PacketOUT packet, final ByteBuf byteBuf) {
        byteBuf.writeInt(packet.getId());
        final JsonObject object = new JsonObject();
        packet.write(object);
        ByteBuffers.writeString(byteBuf,object.toString());
    }

}