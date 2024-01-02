package git.aatufutaa.lobby.net.packet.hello;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.mongo.PlayerDataManager;
import git.aatufutaa.lobby.net.LobbyClient;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.lobby.session.SessionState;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import io.netty.buffer.ByteBuf;

public class RequestDataIncomingPacket implements IncomingPacket {

    @Override
    public void read(ByteBuf buf) throws Exception {
    }

    @Override
    public void handle(ClientBase client) throws Exception {

        LobbyServer.getInstance().runOnMainThread(() -> {

            Session session = LobbyServer.getInstance().getSessionManager().getSession(client.getPlayerId());

            if (session == null) {
                LobbyServer.warn("cant find session in stage for " + client);
                client.getChannel().close();
                return;
            }

            ((LobbyClient)client).setSentStage0(true);

            if (session.getSessionState() == SessionState.NOT_LOADED) {
                LobbyServer.log("Load data for " + session.getPlayerId());
                session.setSessionState(SessionState.LOADING);
                LobbyServer.getInstance().getAsyncThread().execute(() -> {
                    PlayerDataManager.loadPlayerData(session);
                });
                return;
            }

            if (session.getSessionState() == SessionState.LOADING) {
                return; // will get sent after load
            }

            session.getPacketConfirmManager().reset(); // no reconnect -> reset confirm queue
            session.sendData(); // data loaded
        });
    }
}
