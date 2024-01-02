package git.aatufutaa.master.communication.packet;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class PacketManager {

    private final Map<Integer, Class<? extends IncomingPacket<?>>> incoming = new HashMap<>();
    private final Map<Class<? extends OutgoingPacket>, Integer> outgoing = new HashMap<>();

    protected void registerIncoming(int id, Class<? extends IncomingPacket<?>> clazz) {
        this.incoming.put(id, clazz);
    }

    protected void registerOutgoing(int id, Class<? extends OutgoingPacket> clazz) {
        this.outgoing.put(clazz, id);
    }

    public IncomingPacket<?> read(ByteBuf buf) throws Exception {
        int packetId = buf.readByte();

        Class<? extends IncomingPacket<?>> clazz = this.incoming.get(packetId);
        if (clazz == null) {
            throw new Exception("read bad packet id " + packetId);
        }

        IncomingPacket<?> packet = clazz.getConstructor().newInstance();
        try {
            packet.read(buf);
        } catch (Exception e) {
            System.out.println("failed to read " + packet);
            throw e;
        }
        if (buf.readableBytes() != 0) {
            throw new Exception(buf.readableBytes() + " bytes left after reading " + packet);
        }

        return packet;
    }

    public void write(OutgoingPacket packet, ByteBuf buf) throws Exception {
        Integer packetId = this.outgoing.get(packet.getClass());

        if (packetId == null) {
            throw new Exception("write bad packet id " + packet.getClass());
        }

        buf.writeByte(packetId);

        packet.write(buf);
    }
}
