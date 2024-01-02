package git.aatufutaa.lobby.net.confirm.packet.heropass;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.net.confirm.packet.misc.RemoveCoinsOutgoingPacket;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import com.mongodb.client.model.Updates;
import io.netty.buffer.ByteBuf;
import org.bson.Document;

import java.util.concurrent.atomic.AtomicBoolean;

public class BuyHeroPassIncomingPacket extends LobbyPacket {

    @Override
    public void read(ByteBuf buf) throws Exception {
    }

    @Override
    protected void handle0(Session session) throws Exception {

        if (session.getLobbyData().isHasHeroPass()) {
            session.sendConfirmPacket(new CancelBuyHeroPassOutgoingPacket());
            return;
        }

        int price = LobbyServer.getInstance().getLevelManager().getHeroPassPrice();
        if (session.getLobbyData().getGems() < price) {
            session.sendConfirmPacket(new CancelBuyHeroPassOutgoingPacket());
            return;
        }

        if (session.isModifyData()) {
            session.sendConfirmPacket(new CancelBuyHeroPassOutgoingPacket());
            return;
        }

        session.setModifyData(true);

        LobbyServer.getInstance().getAsyncThread().execute(() -> {

            AtomicBoolean failed = new AtomicBoolean(false);

            try {
                LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(new Document("_id", session.getPlayerId()),
                        Updates.combine(
                                Updates.set("hero_pass_" + LobbyServer.getInstance().getLevelManager().getSeason(), System.currentTimeMillis()),
                                Updates.inc("gems", -price)
                        ));
            } catch (Exception e) {
                failed.set(true);
                e.printStackTrace();
            }

            LobbyServer.getInstance().runOnMainThread(() -> {
                session.setModifyData(false);

                if (failed.get()) {
                    session.sendConfirmPacket(new CancelBuyHeroPassOutgoingPacket());
                    return;
                }

                session.getLobbyData().setGems(session.getLobbyData().getGems() - price);
                session.getLobbyData().setHasHeroPass(true);

                session.sendConfirmPacket(new RemoveCoinsOutgoingPacket(RemoveCoinsOutgoingPacket.RemoveType.GEMS, price));
                session.sendConfirmPacket(new BuyHeroPassOutgoingPacket());
            });
        });
    }
}
