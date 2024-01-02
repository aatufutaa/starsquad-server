package git.aatufutaa.master.communication.packet;

import git.aatufutaa.master.communication.packet.handshake.PingIncomingPacket;
import git.aatufutaa.master.communication.packet.handshake.PingOutgoingPacket;

public class PingPacketManager extends PacketManager{

    public PingPacketManager() {
        this.registerIncoming(1, PingIncomingPacket.class);
        this.registerOutgoing(1, PingOutgoingPacket.class);
    }

}
