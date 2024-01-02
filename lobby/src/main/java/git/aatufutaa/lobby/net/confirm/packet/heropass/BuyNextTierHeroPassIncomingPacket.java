package git.aatufutaa.lobby.net.confirm.packet.heropass;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.level.PlayerHeroPassItem;
import git.aatufutaa.lobby.net.confirm.packet.misc.RemoveCoinsOutgoingPacket;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import com.mongodb.client.model.Updates;
import io.netty.buffer.ByteBuf;
import org.bson.Document;

import java.util.concurrent.atomic.AtomicBoolean;

public class BuyNextTierHeroPassIncomingPacket extends LobbyPacket {

    private int id;
    private int tokens;
    private int gems;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.id = buf.readByte();
        this.tokens = buf.readShortLE();
        this.gems = buf.readShortLE();
    }

    @Override
    protected void handle0(Session session) throws Exception {

        int currentIndex = 0;
        for (int i = 0; i < LobbyServer.getInstance().getLevelManager().getHeroPassItems().length; i++) {
            PlayerHeroPassItem item = LobbyServer.getInstance().getLevelManager().getHeroPassItems()[i];
            if (session.getLobbyData().getHeroTokens() >= item.getTokens()) {
                currentIndex = i;
            } else {
                break;
            }
        }

        if (currentIndex != this.id) {
            session.sendConfirmPacket(new CancelBuyNextTierHeroPassOutgoingPacket());
            return;
        }

        if (session.isModifyData()) {
            session.sendConfirmPacket(new CancelBuyNextTierHeroPassOutgoingPacket());
            return;
        }

        if (currentIndex + 1 >= LobbyServer.getInstance().getLevelManager().getHeroPassItems().length) {
            session.sendConfirmPacket(new CancelBuyNextTierHeroPassOutgoingPacket());
            return;
        }

        var currentItem = LobbyServer.getInstance().getLevelManager().getHeroPassItems()[currentIndex + 1];
        int giveTokens = currentItem.getTokens();

        int tokens = session.getLobbyData().getHeroTokens();
        int needTokens = giveTokens - tokens;
        if (needTokens <= 0) { // just for safe
            session.sendConfirmPacket(new CancelBuyNextTierHeroPassOutgoingPacket());
            return;
        }

        int needGems = (int) Math.ceil(needTokens * LobbyServer.getInstance().getLevelManager().getShop().getHeroTokenAsGems());

        if (session.getLobbyData().getGems() < needGems) {
            session.sendConfirmPacket(new CancelBuyNextTierHeroPassOutgoingPacket());
            return;
        }

        if (this.tokens != needTokens || this.gems != needGems) {
            session.sendConfirmPacket(new CancelBuyNextTierHeroPassOutgoingPacket());
            return;
        }

        session.setModifyData(true);

        LobbyServer.getInstance().getAsyncThread().execute(() -> {
            AtomicBoolean failed = new AtomicBoolean(false);
            try {
                LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(new Document("_id", session.getPlayerId()),
                        Updates.combine(
                                Updates.inc("hero_tokens", needTokens),
                                Updates.inc("gems", -needGems)
                        ));
            } catch (Exception e) {
                failed.set(true);
                e.printStackTrace();
            }

            LobbyServer.getInstance().runOnMainThread(() -> {
                session.setModifyData(false);

                if (failed.get()) {
                    session.sendConfirmPacket(new CancelBuyNextTierHeroPassOutgoingPacket());
                    return;
                }

                session.getLobbyData().setGems(session.getLobbyData().getGems() - needGems);
                session.getLobbyData().setHeroTokens(session.getLobbyData().getHeroTokens() + needTokens);

                session.sendConfirmPacket(new RemoveCoinsOutgoingPacket(RemoveCoinsOutgoingPacket.RemoveType.GEMS, needGems));
                session.sendConfirmPacket(new BuyNextTierHeroPassOutgoingPacket(this.id));
            });
        });
    }
}
