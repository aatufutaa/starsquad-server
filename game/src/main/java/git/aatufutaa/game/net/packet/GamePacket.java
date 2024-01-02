package git.aatufutaa.game.net.packet;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.session.Session;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;

public abstract class GamePacket implements IncomingPacket {

    @Override
    public void handle(ClientBase client) throws Exception {
        GameServer.getInstance().runOnMainThread(() -> {
            if (!client.isPlayerIdSet()) return;

            Session session = GameServer.getInstance().getSessionManager().getSession(client.getPlayerId());
            if (session == null) return;

            this.handle0(session);
        });
    }

    protected abstract void handle0(Session session);
}
