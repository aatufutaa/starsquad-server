package git.aatufutaa.master.server.game.packet;

import git.aatufutaa.master.communication.ByteBufUtil;
import git.aatufutaa.master.communication.packet.OutgoingPacket;
import git.aatufutaa.master.session.Session;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class StartGameOutgoingPacket implements OutgoingPacket {

    public enum GameType {
        TUTORIAL,
        //TOWER_WARS,
        //CANDY_RUSH,
        LAST_HERO_STANDING
    }

    public static class GameSession {
        private int playerId;
        private String secret;
        private byte[] key;
        private int team;

        public GameSession(Session session, int team) {
            this.playerId = session.getPlayerId();
            this.secret = session.getToken();
            this.key = session.getKey();
            this.team = team;
        }

        public void write(ByteBuf buf) {
            buf.writeInt(this.playerId);
            ByteBufUtil.writeString(this.secret, buf);
            buf.writeBytes(this.key);
            buf.writeByte(this.team);
        }
    }

    private int gameId;
    private GameType gameType;

    private List<GameSession> players;

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeInt(this.gameId);
        buf.writeByte(this.gameType.ordinal());

        buf.writeByte(this.players.size());
        for (GameSession session : this.players) {
            session.write(buf);
        }
    }
}
