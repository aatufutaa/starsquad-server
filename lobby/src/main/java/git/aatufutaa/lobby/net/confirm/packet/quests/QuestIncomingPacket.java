package git.aatufutaa.lobby.net.confirm.packet.quests;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.quests.QuestData;
import git.aatufutaa.lobby.session.Session;
import com.mongodb.client.model.Updates;
import io.netty.buffer.ByteBuf;
import org.bson.Document;

import java.util.concurrent.atomic.AtomicBoolean;

public class QuestIncomingPacket extends LobbyPacket {

    private int id;
    private int step;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.id = buf.readByte();
        this.step = buf.readByte();
    }

    @Override
    protected void handle0(Session session) throws Exception {
        QuestData questData = session.getLobbyData().getQuestsMap().get(this.id);
        if (questData == null) {
            session.sendConfirmPacket(new CancelQuestOutgoingPacket());
            return;
        }

        if (questData.getClaimIndex() != this.step) {
            session.sendConfirmPacket(new CancelQuestOutgoingPacket());
            return;
        }

        if (session.isModifyData()) {
            session.sendConfirmPacket(new CancelQuestOutgoingPacket());
            return;
        }

        session.setModifyData(true);

        int claimIndex = this.step + 1;

        LobbyServer.getInstance().getAsyncThread().execute(() -> {
            AtomicBoolean failed = new AtomicBoolean(false);

            try {
                LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(new Document("_id", session.getPlayerId()).append("quests.id", this.id),
                        Updates.set("quests.$.claim_index", claimIndex));
            } catch (Exception e) {
                failed.set(true);
                e.printStackTrace();
            }

            LobbyServer.getInstance().runOnMainThread(() -> {

                session.setModifyData(false);

                if (failed.get()) {
                    session.sendConfirmPacket(new CancelQuestOutgoingPacket());
                    return;
                }

                questData.setClaimIndex(claimIndex);

                session.sendConfirmPacket(new QuestOutgoingPacket(this.id, questData.getAmount(), claimIndex));
            });
        });

    }
}
