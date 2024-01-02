package git.aatufutaa.game.net.packet;

import git.aatufutaa.game.game.Game;
import git.aatufutaa.game.game.entity.Player;
import git.aatufutaa.game.session.Session;
import io.netty.buffer.ByteBuf;

public class ExitIncomingPacket extends GamePacket {

    @Override
    public void read(ByteBuf buf) throws Exception {
    }

    @Override
    protected void handle0(Session session) {

        Game game = session.getGame();

        if (game == null) return;

        Player p = game.getPlayers().get(session.getPlayerId());

        if (p == null) return;

        int add = p.getTrophies();
        //session.sendPacket(new GameResultOutgoingPacket(p.getTrophies(), add));
    }
}
