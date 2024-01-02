package git.aatufutaa.game.net.packet;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.game.Game;
import git.aatufutaa.game.game.GameState;
import git.aatufutaa.game.master.packet.HomeMasterOutgoingPacket;
import git.aatufutaa.game.session.Session;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import io.netty.buffer.ByteBuf;

public class HomeIncomingPacket implements IncomingPacket {
    @Override
    public void read(ByteBuf buf) throws Exception {
    }

    @Override
    public void handle(ClientBase client) throws Exception {
        GameServer.getInstance().runOnMainThread(() -> {
            if (!client.isPlayerIdSet()) return;

            Session session = GameServer.getInstance().getSessionManager().getSession(client.getPlayerId());
            if (session == null) return;

            Game game = session.getGame();

            if (game == null || game.getGameState() == GameState.ENDED) {
                System.out.println("player sent home");

                if (session.isSentHome()) {
                    // only one send home packet for player
                    GameServer.warn(session + " tried to send home again");
                    return;
                }

                session.setSentHome(true);
                GameServer.getInstance().getMasterConnection().sendPacket(new HomeMasterOutgoingPacket(session.getPlayerId()));
            }
        });
    }
}
