package git.aatufutaa.server.communication.packet;

import git.aatufutaa.server.communication.packet.packets.HelloMasterIncomingPacket;
import git.aatufutaa.server.communication.packet.packets.HelloMasterOutgoingPacket;
import git.aatufutaa.server.communication.packet.packets.PingMasterIncomingPacket;
import git.aatufutaa.server.communication.packet.packets.PingMasterOutgoingPacket;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class MasterPacketManager {

    private static final Map<Integer, Class<? extends MasterIncomingPacket>> incoming = new HashMap<>();
    private static final Map<Class<? extends MasterOutgoingPacket>, Integer> outgoing = new HashMap<>();

    public static void registerIncoming(int id, Class<? extends MasterIncomingPacket> clazz) {
        incoming.put(id, clazz);
    }

    public static void registerOutgoing(int id, Class<? extends MasterOutgoingPacket> clazz) {
        outgoing.put(clazz, id);
    }

    public static MasterIncomingPacket read(ByteBuf buf) throws Exception {
        int packetId = buf.readByte();

        Class<? extends MasterIncomingPacket> clazz = incoming.get(packetId);

        if (clazz == null) {
            throw new Exception("read bad packet id " + packetId);
        }

        MasterIncomingPacket packet = clazz.getConstructor().newInstance();
        packet.read(buf);

        if (buf.readableBytes() != 0) {
            throw new Exception(buf.readableBytes() + " bytes left after reading " + packet);
        }

        return packet;
    }

    public static void write(MasterOutgoingPacket packet, ByteBuf buf) throws Exception {
        Integer packetId = outgoing.get(packet.getClass());

        if (packetId == null) {
            throw new Exception("write bad packet id " + packet.getClass());
        }

        buf.writeByte(packetId);

        packet.write(buf);
    }

    static {
        registerIncoming(0, HelloMasterIncomingPacket.class);
        registerOutgoing(0, HelloMasterOutgoingPacket.class);

        registerIncoming(1, PingMasterIncomingPacket.class);
        registerOutgoing(1, PingMasterOutgoingPacket.class);
    }
}
