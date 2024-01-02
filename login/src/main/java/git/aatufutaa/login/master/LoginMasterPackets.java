package git.aatufutaa.login.master;

import git.aatufutaa.server.communication.packet.MasterPacketManager;

public class LoginMasterPackets {

    public static void register() {
        MasterPacketManager.registerIncoming(2, LoginMasterIncomingPacket.class);
        MasterPacketManager.registerOutgoing(2, LoginMasterOutgoingPacket.class);
    }

}
