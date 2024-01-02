package git.aatufutaa.master.server.game;

import git.aatufutaa.master.communication.packet.PingPacketManager;
import git.aatufutaa.master.server.game.packet.*;
import git.aatufutaa.master.server.lobby.packet.session.UpdateSessionOutgoingPacket;

public class GamePacketManager extends PingPacketManager {

    public static final GamePacketManager MANAGER = new GamePacketManager();

    public GamePacketManager() {
        this.registerIncoming(2, StartGameIncomingPacket.class);
        this.registerOutgoing(2, StartGameOutgoingPacket.class);

        this.registerOutgoing(3, UpdateSessionOutgoingPacket.class); // lobby packet

        this.registerIncoming(4, HomeIncomingPacket.class);
        this.registerOutgoing(4, HomeOutgoingPacket.class);

        this.registerIncoming(5, DisconnectPlayerIncomingPacket.class);
    }
}
