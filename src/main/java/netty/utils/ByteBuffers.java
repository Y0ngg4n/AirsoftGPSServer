package netty.utils;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class ByteBuffers {

    public static void writeString(final ByteBuf buffer, final String string) {
        final byte[] stringBuf = string.getBytes(StandardCharsets.UTF_8);
        buffer.writeInt(stringBuf.length);
        buffer.writeCharSequence(string,StandardCharsets.UTF_8);
    }

    public static String readString(final ByteBuf buffer) {
        final int length = buffer.readInt();
        return buffer.readCharSequence(length,StandardCharsets.UTF_8).toString();
    }
}
