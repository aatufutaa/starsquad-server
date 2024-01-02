package git.aatufutaa.master.communication;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ByteBufUtil {

    public static String readString(ByteBuf buf, int limit) throws Exception {
        int length = buf.readShortLE();

        if (length > limit) {
            throw new Exception("too big string " + length + " > " + limit);
        }

        byte[] b = new byte[length];

        buf.readBytes(b);

        return new String(b, StandardCharsets.UTF_8);
    }

    public static void writeString(String s, ByteBuf buf) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        buf.writeShortLE(bytes.length);
        buf.writeBytes(bytes);
    }

    public static String readWideString(ByteBuf buf, int limit) throws Exception {
        int length = buf.readShortLE();

        if (length > limit) {
            throw new Exception("too big string " + length + " > " + limit);
        }

        byte[] b = new byte[length];

        buf.readBytes(b);

        return new String(b, StandardCharsets.UTF_16LE);
    }

    public static void writeWideString(String s, ByteBuf buf) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_16LE);
        buf.writeShortLE(bytes.length);
        buf.writeBytes(bytes);
    }

    public static UUID readUUID(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }
}
