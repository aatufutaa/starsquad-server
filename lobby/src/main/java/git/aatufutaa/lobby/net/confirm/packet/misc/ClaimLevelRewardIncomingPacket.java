package git.aatufutaa.lobby.net.confirm.packet.misc;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.level.PlayerLevel;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import com.mongodb.client.model.Updates;
import io.netty.buffer.ByteBuf;
import org.bson.Document;

import java.util.concurrent.atomic.AtomicBoolean;

public class ClaimLevelRewardIncomingPacket extends LobbyPacket {

    private int id;
    private int level;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.id = buf.readShortLE();
        this.level = buf.readByte();
    }

    @Override
    protected void handle0(Session session) throws Exception {

        System.out.println("claim reward level " + this.level + " " + session.getLobbyData().getLevel() + " " + session.getLobbyData().getClaimedLevelRewardIndex());

        // make sure collecting right index
        if (this.level != session.getLobbyData().getClaimedLevelRewardIndex() + 1) {
            session.sendConfirmPacket(new CancelRewardOutgoingPacket(this.id));
            return;
        }

        // make sure player has level before giving any reward
        if (session.getLobbyData().getLevel() < this.level) {
            session.sendConfirmPacket(new CancelRewardOutgoingPacket(this.id));
            return;
        }

        if (session.isModifyData()) {
            session.sendConfirmPacket(new CancelRewardOutgoingPacket(this.id));
            return;
        }

        PlayerLevel nextLevel = LobbyServer.getInstance().getLevelManager().getLevel(this.level - 1);
        if (nextLevel == null) {
            session.sendConfirmPacket(new CancelRewardOutgoingPacket(this.id));
            return;
        }

        // claim reward
        session.setModifyData(true);
        int newIndex = session.getLobbyData().getClaimedLevelRewardIndex() + 1;

        int type = nextLevel.getRewardType();
        int amount = nextLevel.getRewardAmount();

        LobbyServer.getInstance().getAsyncThread().execute(() -> {
            AtomicBoolean failed = new AtomicBoolean(false);
            try {

                String key = getKey(type);

                if (key == null) {
                    throw new Exception("dont know what to do with type " + type);
                }

                LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(new Document("_id", session.getPlayerId()),
                        Updates.combine(
                                Updates.set("claimed_level_index", newIndex),
                                Updates.inc(key, amount)
                        ));
            } catch (Exception e) {
                e.printStackTrace();
                failed.set(true);
            }

            LobbyServer.getInstance().runOnMainThread(() -> {
                session.setModifyData(false);

                if (failed.get()) {
                    session.sendConfirmPacket(new CancelRewardOutgoingPacket(this.id));
                } else {

                    addReward(session, type, amount);

                    session.getLobbyData().setClaimedLevelRewardIndex(newIndex);
                    session.sendConfirmPacket(new ClaimLevelRewardOutgoingPacket(this.level));
                    session.sendConfirmPacket(new RewardOutgoingPacket(this.id, type, 0, amount));
                }
            });
        });
    }

    public static String getKey(int type) {
        return switch (type) {
            case 0 -> "coins";
            case 1 -> "gems";
            case 2 -> "exp_common";
            case 3 -> "exp_rare";
            case 4 -> "exp_legendary";
            default -> null;
        };
    }

    public static void addReward(Session session, int type, int amount) {
        switch (type) {
            case 0:
                session.getLobbyData().setCoins(session.getLobbyData().getCoins() + amount);
                break;
            case 1:
                session.getLobbyData().setGems(session.getLobbyData().getGems() + amount);
                break;
            case 2:
                session.getLobbyData().setExpCommon(session.getLobbyData().getExpCommon() + amount);
                break;
            case 3:
                session.getLobbyData().setExpRare(session.getLobbyData().getExpRare() + amount);
                break;
            case 4:
                session.getLobbyData().setExpLegendary(session.getLobbyData().getExpLegendary() + amount);
                break;
        }
    }
}
