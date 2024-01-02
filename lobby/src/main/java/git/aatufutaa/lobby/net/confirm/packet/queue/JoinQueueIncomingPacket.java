package git.aatufutaa.lobby.net.confirm.packet.queue;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.master.queue.JoinQueueMasterOutgoingPacket;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import io.netty.buffer.ByteBuf;

public class JoinQueueIncomingPacket extends LobbyPacket {

    private QueueType gameType;

    public enum QueueType {
        //TOWER_WARS,
        //CANDY_RUSH,
        LAST_HERO_STANDING
    }

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.gameType = QueueType.values()[buf.readByte()];
    }

    @Override
    protected void handle0(Session session) {

        if (session.isInQueue()) {
            System.out.println("player already in queue");
            return;
        }

        //TODO: dont spam master

        LobbyServer.getInstance().getMasterConnection().sendPacket(new JoinQueueMasterOutgoingPacket(session.getPlayerId(), this.gameType));
    }
}
