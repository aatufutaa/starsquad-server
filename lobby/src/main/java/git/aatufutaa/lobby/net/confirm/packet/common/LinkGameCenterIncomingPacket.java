package git.aatufutaa.lobby.net.confirm.packet.common;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.mongo.PlayerId;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.ByteBufUtil;
import com.mongodb.client.model.Updates;
import io.netty.buffer.ByteBuf;
import org.bson.Document;

public class LinkGameCenterIncomingPacket extends LobbyPacket {

    private String id;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.id = ByteBufUtil.readString(buf, 64);
    }

    @Override
    protected void handle0(Session session) throws Exception {

        // TODO: limit packet

        // TOOD: check if current doc.gamecenter == this.id

        boolean android = false;
        String key = android ? "google_play" : "game_center";

        Document document = LobbyServer.getInstance().getLobbyMongoManager().getPlayers().find(new Document(key, this.id)).first();

        if (document == null) {
            // no link found? auto link

            LobbyServer.getInstance().getLobbyMongoManager().getLoginPlayers().updateOne(new Document("_id", session.getPlayerId()),
                    Updates.set(key, this.id));

            return;
        }

        int otherId = document.getInteger("_id");
        if (otherId != session.getPlayerId()) {
            // different account linked with game center

            // find player
            Document data = LobbyServer.getInstance().getLobbyMongoManager().getPlayers().find(new Document("_id", otherId)).first();
            if (data == null) return;

            String name = data.getString("name");
            String tag = PlayerId.convertIdToHash(otherId);
            String token = document.getString("token");

            session.sendConfirmPacket(new LinkGameCenterOutgoingPacket(name, tag, token));
        }
    }
}
