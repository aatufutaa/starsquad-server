package git.aatufutaa.master.server.lobby.packet.misc;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.lobby.LobbyServer;
import git.aatufutaa.master.session.Session;
import git.aatufutaa.master.session.SessionManager;
import git.aatufutaa.master.session.SessionThread;
import io.netty.buffer.ByteBuf;

public class RemoveFriendIncomingPacket implements IncomingPacket<LobbyServer> {

    private int playerId;
    private int friendId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.friendId = buf.readInt();
    }

    @Override
    public void handle(LobbyServer server) {
        int threadId = SessionManager.getThread(this.playerId);
        SessionThread thread = MasterServer.getInstance().getSessionManager().getSessionThread(threadId);

        thread.run(() -> {

            Session session = thread.getSession(this.playerId);
            if (session == null) return;

            session.sendPacket(new RemoveFriendOutgoingPacket(this.playerId, this.friendId));

            MasterServer.getInstance().getSessionManager().getSession(this.friendId, friend -> {

                if (friend == null) return;

                friend.getOnlineFriends().remove(session.getPlayerId());

                thread.run(() -> {
                    session.getOnlineFriends().remove(friend.getPlayerId());
                });
            });
        });
    }
}
