package git.aatufutaa.lobby.net.confirm.packet.shop;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.net.confirm.packet.misc.RemoveCoinsOutgoingPacket;
import git.aatufutaa.lobby.net.confirm.packet.misc.RewardOutgoingPacket;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import com.mongodb.client.model.Updates;
import io.netty.buffer.ByteBuf;
import org.bson.Document;

import java.util.concurrent.atomic.AtomicBoolean;

public class BuyShopItemIncomingPacket extends LobbyPacket {

    private int position;
    private int id;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.position = buf.readShortLE();
        this.id = buf.readByte();
    }

    @Override
    protected void handle0(Session session) throws Exception {

        if (session.isModifyData()) {
            session.sendConfirmPacket(new CancelPurchaseOutgoingPacket());
            return;
        }

        int[] coinPack = {
                20,
                50,
                60,
                69,
                80,
                60
        };

        int[] amount = {
                10,
                20,
                30,
                40,
                50,
                60
        };

        int gemPrice = coinPack[this.id];
        int add = amount[this.id];

        if (session.getLobbyData().getGems() < gemPrice) {
            session.sendConfirmPacket(new CancelPurchaseOutgoingPacket());
            return;
        }

        session.setModifyData(true);

        LobbyServer.getInstance().getAsyncThread().execute(() -> {

            AtomicBoolean failed = new AtomicBoolean(false);

            try {
                LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(
                        new Document("_id", session.getPlayerId()),
                        Updates.combine(
                                Updates.inc("gems", -gemPrice),
                                Updates.inc("coins", add)));
            } catch (Exception e) {
                e.printStackTrace();
                failed.set(true);
            }

            LobbyServer.getInstance().runOnMainThread(() -> {
                session.setModifyData(false);

                if (failed.get()) {
                    session.sendConfirmPacket(new CancelPurchaseOutgoingPacket());
                    return;
                }

                session.getLobbyData().setGems(session.getLobbyData().getGems() - gemPrice);
                session.getLobbyData().setCoins(session.getLobbyData().getCoins() + add);

                session.sendConfirmPacket(new RemoveCoinsOutgoingPacket(RemoveCoinsOutgoingPacket.RemoveType.GEMS, gemPrice));
                session.sendConfirmPacket(new RewardOutgoingPacket(this.position, 0, this.id, add));
            });
        });
    }
}
