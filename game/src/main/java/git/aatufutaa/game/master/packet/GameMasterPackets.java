package git.aatufutaa.game.master.packet;

import git.aatufutaa.server.communication.packet.MasterPacketManager;

public class GameMasterPackets {

    public static void register() {
        MasterPacketManager.registerIncoming(2, StartGameMasterIncomingPacket.class);
        MasterPacketManager.registerOutgoing(2, StartGameMasterOutgoingPacket.class);

        MasterPacketManager.registerIncoming(3, UpdateSessionMasterIncomingPacket.class);

        MasterPacketManager.registerIncoming(4, HomeMasterIncomingPacket.class);
        MasterPacketManager.registerOutgoing(4, HomeMasterOutgoingPacket.class);

        MasterPacketManager.registerOutgoing(5, DisconnectPlayerMasterOutgoingPacket.class);
    }
}
