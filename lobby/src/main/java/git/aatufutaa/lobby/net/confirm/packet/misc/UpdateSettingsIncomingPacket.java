package git.aatufutaa.lobby.net.confirm.packet.misc;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.master.misc.UpdateLocationMasterOutgoingPacket;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.quests.QuestType;
import git.aatufutaa.lobby.session.Session;
import com.mongodb.client.model.Updates;
import io.netty.buffer.ByteBuf;
import org.bson.Document;

public class UpdateSettingsIncomingPacket extends LobbyPacket {

    private boolean allowFriendRequests;
    private int location;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.allowFriendRequests = buf.readBoolean();
        this.location = buf.readByte();
    }

    @Override
    protected void handle0(Session session) throws Exception {
        if (true) {
            session.updateQuest(QuestType.WIN_GAMES, 20, (r) -> {
            });
        }

        // TODO: check for limit

        if (session.getLobbyData().isAllowFriendRequests() != this.allowFriendRequests) {
            session.getLobbyData().setAllowFriendRequests(this.allowFriendRequests);

            LobbyServer.getInstance().getAsyncThread().execute(() -> {
                try {
                    LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(
                            new Document("_id", session.getPlayerId()),
                            Updates.set("allow_friend_requests", this.allowFriendRequests)
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // TODO: validate location
        if (this.location != 0 && this.location != 1) return;

        if (session.getLocation() != this.location) {
            LobbyServer.getInstance().getAsyncThread().execute(() -> {
                try {
                    LobbyServer.getInstance().getLobbyMongoManager().getLoginPlayers().updateOne(
                            new Document("_id", session.getPlayerId()),
                            Updates.set("location", this.location)
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        session.setLocation(this.location);
        LobbyServer.getInstance().getMasterConnection().sendPacket(new UpdateLocationMasterOutgoingPacket(session.getPlayerId(), this.location));
    }
}
