package git.aatufutaa.game.net.packet.hello;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.game.Game;
import git.aatufutaa.game.game.GameState;
import git.aatufutaa.game.master.packet.HomeMasterOutgoingPacket;
import git.aatufutaa.game.net.cllient.GameClient;
import git.aatufutaa.game.session.Session;
import git.aatufutaa.game.net.cllient.SessionState;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import io.netty.buffer.ByteBuf;

public class RequestDataIncomingPacket implements IncomingPacket {

    @Override
    public void read(ByteBuf buf) throws Exception {
    }

    @Override
    public void handle(ClientBase client) throws Exception {
        GameServer.getInstance().runOnMainThread(() -> {
            GameClient gameClient = (GameClient) client;
            if (gameClient.assertState(SessionState.STAGE)) return;

            Session session = GameServer.getInstance().getSessionManager().getSession(client.getPlayerId());

            if (session == null) {
                GameServer.warn("request data did not find session for " + client);
                client.getChannel().close();
                return;
            }

            session.setAllowReconnect(true);
            gameClient.setSentStage0(true);

            Game game = session.getGame();
            if (game != null) {

                // dont allow new connect when game is ending or ended
                if (game.getGameState() == GameState.ENDING || game.getGameState() == GameState.ENDED) {
                    session.kick("This game is ending. Please wait till it ends and try again.");
                    return;
                }

                game.sendStaticData(session);
                return;
            }

            // player is not in game
            // send player to lobby
            GameServer.getInstance().getMasterConnection().sendPacket(new HomeMasterOutgoingPacket(session.getPlayerId()));
        });
    }
}
