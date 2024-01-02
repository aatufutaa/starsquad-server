package git.aatufutaa.server.net.packet;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class PacketManager {

    private static final Map<Integer, Class<? extends IncomingPacket>> incoming = new HashMap<>();
    private static final Map<Class<? extends OutgoingPacket>, Integer> outgoing = new HashMap<>();

    public static void registerIncoming(int id, Class<? extends IncomingPacket> clazz) {
        incoming.put(id, clazz);
    }

    public static void registerOutgoing(int id, Class<? extends OutgoingPacket> clazz) {
        outgoing.put(clazz, id);
    }

    public static IncomingPacket read(ByteBuf buf) throws Exception {
        int packetId = buf.readByte();

        Class<? extends IncomingPacket> clazz = incoming.get(packetId);

        if (clazz == null) {
            throw new Exception("read bad packet id " + packetId);
        }

        IncomingPacket packet = clazz.getConstructor().newInstance();
        packet.read(buf);

        if (buf.readableBytes() != 0) {
            throw new Exception(buf.readableBytes() + " bytes left after reading " + packet);
        }

        return packet;
    }

    public static void write(OutgoingPacket packet, ByteBuf buf) {
        Integer packetId = outgoing.get(packet.getClass());

        if (packetId == null) {
            throw new RuntimeException("write bad packet id " + packet.getClass());
        }

        buf.writeByte(packetId);

        packet.write(buf);
    }
}
