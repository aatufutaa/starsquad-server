package git.aatufutaa.server.play.confirm;

import git.aatufutaa.server.net.packet.IncomingPacket;
import git.aatufutaa.server.net.packet.OutgoingPacket;

import java.util.HashMap;
import java.util.Map;

public class ConfirmPacketRegistry {

    private static final Map<Integer, Class<? extends IncomingPacket>> incoming = new HashMap<>();
    private static final Map<Class<? extends OutgoingPacket>, Integer> outgoing = new HashMap<>();

    public static void registerIncoming(int id, Class<? extends IncomingPacket> clazz) {
        incoming.put(id, clazz);
    }

    public static void registerOutgoing(int id, Class<? extends OutgoingPacket> clazz) {
        outgoing.put(clazz, id);
    }

    public static int getId(Class<? extends OutgoingPacket> clazz) {
        return outgoing.get(clazz);
    }

    public static Class<? extends IncomingPacket> getClazz(int id) {
        return incoming.get(id);
    }
}
