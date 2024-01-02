package git.aatufutaa.master.communication.packet.handshake;

import git.aatufutaa.master.communication.packet.PacketManager;

public class HandshakePacketManager extends PacketManager {

    public static final HandshakePacketManager MANAGER = new HandshakePacketManager();

    public HandshakePacketManager() {
        this.registerIncoming(0, HelloIncomingPacket.class);
        this.registerOutgoing(0, HelloOutgoingPacket.class);
    }
}
