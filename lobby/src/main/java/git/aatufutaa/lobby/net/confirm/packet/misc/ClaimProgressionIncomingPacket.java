package git.aatufutaa.lobby.net.confirm.packet.misc;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.level.PlayerProgression;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import com.mongodb.client.model.Updates;
import io.netty.buffer.ByteBuf;
import org.bson.Document;

import java.util.concurrent.atomic.AtomicBoolean;

public class ClaimProgressionIncomingPacket extends LobbyPacket {

    private int id;
    private int progressionId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.id = buf.readShortLE();
        this.progressionId = buf.readByte();
    }

    @Override
    protected void handle0(Session session) throws Exception {

        if (session.getLobbyData().getClaimedProgression().contains(this.progressionId)) {
            // already claimed that
            session.sendConfirmPacket(new CancelClaimProgressionOutgoingPacket());
            return;
        }

        PlayerProgression playerProgression = LobbyServer.getInstance().getLevelManager().getProgression().get(this.progressionId);

        if (playerProgression == null) {
            // cant find player progression with this id
            session.sendConfirmPacket(new CancelClaimProgressionOutgoingPacket());
            return;
        }

        if (session.getLobbyData().getTotalTrophies() < playerProgression.getTrophies()) {
            session.sendConfirmPacket(new CancelClaimProgressionOutgoingPacket());
            return;
        }

        if (session.isModifyData()) {
            // already claiming reward or doing something with coin data
            session.sendConfirmPacket(new CancelClaimProgressionOutgoingPacket());
            return;
        }

        session.setModifyData(true);

        LobbyServer.getInstance().getAsyncThread().execute(() -> {
            int type = playerProgression.getRewardType();
            int amount = playerProgression.getRewardAmount();
            String key = ClaimLevelRewardIncomingPacket.getKey(type);

            AtomicBoolean failed = new AtomicBoolean(false);
            try {
                LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(new Document("_id", session.getPlayerId()),
                        Updates.combine(
                                Updates.addToSet("claimed_progression", this.progressionId),
                                Updates.inc(key, amount)
                        ));
            } catch (Exception e) {
                failed.set(true);
                e.printStackTrace();
            }

            LobbyServer.getInstance().runOnMainThread(() -> {

                session.setModifyData(false);

                if (failed.get()) {
                    session.sendConfirmPacket(new CancelClaimProgressionOutgoingPacket());
                    return;
                }

                session.getLobbyData().getClaimedProgression().add(this.progressionId);
                session.sendConfirmPacket(new ClaimProgressionOutgoingPacket(this.progressionId));
                session.sendConfirmPacket(new RewardOutgoingPacket(this.id, type, 0, amount));
            });
        });
    }
}
