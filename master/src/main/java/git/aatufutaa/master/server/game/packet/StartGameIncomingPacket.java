package git.aatufutaa.master.server.game.packet;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.game.GameServer;
import io.netty.buffer.ByteBuf;

public class StartGameIncomingPacket implements IncomingPacket<GameServer> {

    public enum StartType {
        OK,
        FAIL,
        STATE_PLAY,
        STATE_ENDING,
        STATE_ENDED
    }

    private int gameId;
    private StartType startType;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.gameId = buf.readInt();
        this.startType = StartType.values()[buf.readByte()];
    }

    @Override
    public void handle(GameServer server) {
        MasterServer.getInstance().getServerManager(server.getServerLocation()).runOnServerThread(() -> server.confirm(this.gameId, this.startType));
    }
}
