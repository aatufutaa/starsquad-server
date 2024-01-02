package git.aatufutaa.game.net.packet;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.game.Game;
import git.aatufutaa.game.net.cllient.GameClient;
import git.aatufutaa.game.session.Session;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import io.netty.buffer.ByteBuf;

public class TickIncomingPacket implements IncomingPacket {

    private Game.PlayerInput playerInput;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerInput = new Game.PlayerInput(true);
        this.playerInput.read(buf);
    }
    @Override
    public void handle(ClientBase client) throws Exception {
        GameServer.getInstance().runOnMainThread(() -> {
            GameClient gameClient = (GameClient) client;
            if (!gameClient.canSendUdp()) return;

            Session session = GameServer.getInstance().getSessionManager().getSession(client.getPlayerId());

            Game game = session.getGame();

            if (game != null) {
                game.handleInput(session, this.playerInput);
            }
        });
    }
}
