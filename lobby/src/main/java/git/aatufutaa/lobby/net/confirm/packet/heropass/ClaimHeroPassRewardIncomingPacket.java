package git.aatufutaa.lobby.net.confirm.packet.heropass;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.level.PlayerHeroPassItem;
import git.aatufutaa.lobby.net.confirm.packet.misc.ClaimLevelRewardIncomingPacket;
import git.aatufutaa.lobby.net.confirm.packet.misc.RewardOutgoingPacket;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import com.mongodb.client.model.Updates;
import io.netty.buffer.ByteBuf;
import org.bson.Document;

import java.util.concurrent.atomic.AtomicBoolean;

public class ClaimHeroPassRewardIncomingPacket extends LobbyPacket {

    private int rewardId;
    private int id;
    private boolean hero;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.rewardId = buf.readShortLE();
        this.id = buf.readByte();
        this.hero = buf.readBoolean();
    }

    @Override
    protected void handle0(Session session) throws Exception {

        if (this.hero) {
            if (session.getLobbyData().getHeroPassHeroClaimIndex() != this.id) {
                session.sendConfirmPacket(new CancelHeroPassRewardOutgoingPacket());
                return;
            }

        } else {
            if (session.getLobbyData().getHeroPassFreeClaimIndex() != this.id) {
                session.sendConfirmPacket(new CancelHeroPassRewardOutgoingPacket());
                return;
            }
        }

        if (this.id >= LobbyServer.getInstance().getLevelManager().getHeroPassItems().length) {
            session.sendConfirmPacket(new CancelHeroPassRewardOutgoingPacket());
            return;
        }

        if (this.hero && !session.getLobbyData().isHasHeroPass()) {
            session.sendConfirmPacket(new CancelHeroPassRewardOutgoingPacket());
            return;
        }

        PlayerHeroPassItem heroPassItem = LobbyServer.getInstance().getLevelManager().getHeroPassItems()[this.id];

        int neededTokens = heroPassItem.getTokens();
        if (session.getLobbyData().getHeroTokens() < neededTokens) {
            session.sendConfirmPacket(new CancelHeroPassRewardOutgoingPacket());
            return;
        }

        if (session.isModifyData()) {
            session.sendConfirmPacket(new CancelHeroPassRewardOutgoingPacket());
            return;
        }

        session.setModifyData(true);

        LobbyServer.getInstance().getAsyncThread().execute(() -> {

            int rewardType = this.hero ? heroPassItem.getHeroRewardType() : heroPassItem.getFreeRewardType();
            int rewardAmount = this.hero ? heroPassItem.getHeroRewardAmount() : heroPassItem.getFreeRewardAmount();
            String key = ClaimLevelRewardIncomingPacket.getKey(rewardType);

            AtomicBoolean failed = new AtomicBoolean(false);
            try {
                LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(new Document("_id", session.getPlayerId()),
                        Updates.combine(Updates.set(this.hero ? "hero_pass_hero_claim_index" : "hero_pass_free_claim_index", this.id + 1),
                                Updates.inc(key, rewardAmount)));
            } catch (Exception e) {
                failed.set(true);
                e.printStackTrace();
            }

            LobbyServer.getInstance().runOnMainThread(() -> {
                session.setModifyData(false);

                if (failed.get()) {
                    session.sendConfirmPacket(new CancelHeroPassRewardOutgoingPacket());
                    return;
                }

                if (this.hero) {
                    session.getLobbyData().setHeroPassHeroClaimIndex(this.id + 1);
                } else {
                    session.getLobbyData().setHeroPassFreeClaimIndex(this.id + 1);
                }

                ClaimLevelRewardIncomingPacket.addReward(session, rewardType, rewardAmount);

                session.sendConfirmPacket(new ClaimHeroPassRewardOutgoingPacket(this.id, this.hero));
                session.sendConfirmPacket(new RewardOutgoingPacket(this.rewardId, rewardType, 0, rewardAmount));
            });
        });
    }
}
