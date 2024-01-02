package git.aatufutaa.lobby.net.confirm.packet.hero;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.hero.Hero;
import git.aatufutaa.lobby.level.PlayerHero;
import git.aatufutaa.lobby.mongo.PlayerDataManager;
import git.aatufutaa.lobby.net.confirm.packet.misc.RemoveCoinsOutgoingPacket;
import git.aatufutaa.lobby.net.confirm.packet.misc.RewardOutgoingPacket;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import com.mongodb.client.model.Updates;
import io.netty.buffer.ByteBuf;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpgradeHeroIncomingPacket extends LobbyPacket {

    private int hero;
    private int level;

    private int exp;
    private int coins;
    private int gems;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.hero = buf.readByte();
        this.level = buf.readByte();

        this.exp = buf.readShortLE();
        this.coins = buf.readShortLE();
        this.gems = buf.readShortLE();
    }

    @Override
    protected void handle0(Session session) throws Exception {
        boolean buy = this.level == 0;

        Hero hero = session.getLobbyData().getHeroes().get(this.hero);
        if (hero == null) {
            if (!buy) {
                session.sendConfirmPacket(new CancelHeroUpgradeOutgoingPacket(buy));
                return;
            }
        } else {

            if (buy) {
                session.sendConfirmPacket(new CancelHeroUpgradeOutgoingPacket(buy));
                return;
            }

            if (hero.getLevel() + 1 != this.level) {
                session.sendConfirmPacket(new CancelHeroUpgradeOutgoingPacket(buy));
                return;
            }
        }

        if (session.isModifyData()) {
            session.sendConfirmPacket(new CancelHeroUpgradeOutgoingPacket(buy));
            return;
        }

        PlayerHero playerHero = LobbyServer.getInstance().getLevelManager().getHeroes().get(this.hero);
        if (playerHero == null) {
            session.sendConfirmPacket(new CancelHeroUpgradeOutgoingPacket(buy));
            return;
        }

        PlayerHero.HeroLevel heroLevel;
        if (!buy) {
            heroLevel = playerHero.getLevel(this.level - 1);
            if (heroLevel == null) {
                session.sendConfirmPacket(new CancelHeroUpgradeOutgoingPacket(buy));
                return;
            }
        } else {
            heroLevel = playerHero.getLevel(0);
        }

        int expPrice = heroLevel.getExpPrice();
        int coinPrice = heroLevel.getCoinPrice();

        int coins = session.getLobbyData().getCoins();
        int exp = session.getLobbyData().getExp(playerHero.getRarity());
        int gems = session.getLobbyData().getGems();

        int needExp = expPrice - exp;
        int needCoins = coinPrice - coins;

        boolean notEnoughExp = needExp > 0;
        boolean notEnoughCoins = needCoins > 0;

        int useExp;
        int useCoins;
        int useGems;

        if (notEnoughExp || notEnoughCoins) {
            int needGems = 0;

            if (notEnoughExp) {
                float manaAsGems = switch (playerHero.getRarity()) {
                    case 0 -> LobbyServer.getInstance().getLevelManager().getShop().getCommonManaAsGems();
                    case 1 -> LobbyServer.getInstance().getLevelManager().getShop().getEpicManaAsGems();
                    case 2 -> LobbyServer.getInstance().getLevelManager().getShop().getLegendaryManaAsGems();
                    default -> 0;
                };

                needGems += (int) Math.ceil(manaAsGems * needExp);
                useExp = expPrice - needExp;
            } else {
                useExp = expPrice;
            }

            if (notEnoughCoins) {
                needGems += (int) Math.ceil(LobbyServer.getInstance().getLevelManager().getShop().getCoinAsGems() * needCoins);
                useCoins = coinPrice - needCoins;
            } else {
                useCoins = coinPrice;
            }

            // not enough gems
            if (gems < needGems) {
                session.sendConfirmPacket(new CancelHeroUpgradeOutgoingPacket(buy));
                return;
            }

            useGems = needGems;
        } else {
            useExp = expPrice;
            useCoins = coinPrice;
            useGems = 0;
        }

        if (useExp != this.exp || useCoins != this.coins || useGems != this.gems) {
            LobbyServer.warn("failed to verify upgrade for " + session + " " +
                    useExp + "->" + this.exp + " " + useCoins + "->" + this.coins + " " + useGems + "->" + this.gems);
            session.sendConfirmPacket(new CancelHeroUpgradeOutgoingPacket(buy));
            return;
        }

        session.setModifyData(true);

        int giveLevelPoints = 20;

        LobbyServer.getInstance().getAsyncThread().execute(() -> {

            AtomicBoolean failed = new AtomicBoolean(false);

            try {

                if (buy) {
                    LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(new Document("_id", session.getPlayerId()),
                            Updates.addToSet("heroes", PlayerDataManager.createHeroDocument(this.hero)));
                }

                List<Bson> updates = new ArrayList<>();
                if (!buy) {
                    updates.add(Updates.set("heroes.$.level", this.level));
                }

                if (useExp > 0) {
                    String key = switch (playerHero.getRarity()) {
                        case 0 -> "exp_common";
                        case 1 -> "exp_rare";
                        case 2 -> "exp_legendary";
                        default -> throw new Exception("what to do with rarity " + playerHero.getRarity());
                    };
                    updates.add(Updates.inc(key, -useExp));
                }

                if (useCoins > 0) {
                    updates.add(Updates.inc("coins", -useCoins));
                }

                if (useGems > 0) {
                    updates.add(Updates.inc("gems", -useGems));
                }

                if (giveLevelPoints > 0) {
                    updates.add(Updates.inc("level_points", giveLevelPoints));
                }

                Document key = new Document("_id", session.getPlayerId());
                if (!buy) {
                    key.append("heroes.id", this.hero);
                }
                LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(key, Updates.combine(updates));

            } catch (Exception e) {
                failed.set(true);
                e.printStackTrace();
            }

            LobbyServer.getInstance().runOnMainThread(() -> {
                session.setModifyData(false);

                if (failed.get()) {
                    session.sendConfirmPacket(new CancelHeroUpgradeOutgoingPacket(buy));
                    return;
                }

                if (!buy) {
                    hero.setLevel(this.level);
                }

                if (useCoins > 0) {
                    session.getLobbyData().setCoins(session.getLobbyData().getCoins() - useCoins);
                    session.sendConfirmPacket(new RemoveCoinsOutgoingPacket(RemoveCoinsOutgoingPacket.RemoveType.COINS, useCoins));
                }

                if (buy) {
                    session.getLobbyData().getHeroes().put(this.hero, new Hero(playerHero, 1, 0));
                }

                if (useExp > 0) {
                    session.getLobbyData().setExp(playerHero.getRarity(), session.getLobbyData().getExp(playerHero.getRarity()) - useExp);
                    RemoveCoinsOutgoingPacket.RemoveType type = switch (playerHero.getRarity()) {
                        case 0 -> RemoveCoinsOutgoingPacket.RemoveType.EXP_COMMON;
                        case 1 -> RemoveCoinsOutgoingPacket.RemoveType.EXP_RARE;
                        case 2 -> RemoveCoinsOutgoingPacket.RemoveType.EXP_LEGENDARY;
                        default -> null;
                    };
                    session.sendConfirmPacket(new RemoveCoinsOutgoingPacket(type, useExp));
                }

                if (useGems > 0) {
                    session.getLobbyData().setGems(session.getLobbyData().getGems() - useGems);
                    session.sendConfirmPacket(new RemoveCoinsOutgoingPacket(RemoveCoinsOutgoingPacket.RemoveType.GEMS, useGems));
                }

                if (giveLevelPoints > 0) {
                    session.sendConfirmPacket(new RewardOutgoingPacket(-1, 5, 0, giveLevelPoints));
                }

                session.sendConfirmPacket(new UpgradeHeroOutgoingPacket(this.hero, this.level));
            });
        });
    }
}
