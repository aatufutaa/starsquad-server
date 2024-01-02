package git.aatufutaa.lobby.net.packet;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.lobby.session.SessionState;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;

public abstract class LobbyPacket implements IncomingPacket {

    @Override
    public void handle(ClientBase client) throws Exception {
        LobbyServer.getInstance().runOnMainThread(() -> {
            if (!client.isDataLoaded()) return;

            Session session = LobbyServer.getInstance().getSessionManager().getSession(client.getPlayerId());

            if (session == null) {
                LobbyServer.warn("Cant find session in " + this.getClass() + " for " + client);
                return;
            }

            if (session.getSessionState() != SessionState.LOADED) return;

            try {
                this.handle0(session);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    protected abstract void handle0(Session session) throws Exception;
}
