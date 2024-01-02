package git.aatufutaa.game.game.games;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.game.Game;
import git.aatufutaa.game.game.GameType;
import git.aatufutaa.game.game.block.BlockBox;
import git.aatufutaa.game.game.block.BlockTree;
import git.aatufutaa.game.game.block.BlockWater;
import git.aatufutaa.game.game.entity.Entity;
import git.aatufutaa.game.game.entity.Player;
import git.aatufutaa.game.game.map.GameMap;
import git.aatufutaa.game.net.packet.GameResultOutgoingPacket;
import git.aatufutaa.game.net.packet.TickOutgoingPacket;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LHSGame extends Game {

    public LHSGame(int gameId) {
        super(GameType.LAST_HERO_STANDING, gameId, getTutorialMap());
    }

    // TODO: have multiple maps
    private static GameMap getTutorialMap() {
        int sizeX = 48;
        int sizeY = 48;
        int sizeYHalf = sizeY / 2;

        GameMap gameMap = new GameMap("ForestMap", sizeX, sizeY);

        /*gameMap.addBlock(new BlockBox(5, 5));
        gameMap.addBlock(new BlockGrass(6, 5));
        gameMap.addBlock(new BlockGrass(7, 5));
        gameMap.addBlock(new BlockWater(0, 7));
        gameMap.addBlock(new BlockWater(0, 9));

        gameMap.addBlock(new BlockTree(-5, -5));
        gameMap.addBlock(new BlockBox(-5, -7));
        gameMap.addBlock(new BlockBox(-5, -6));*/

        gameMap.addBlock(new BlockTree(3, -23));
        gameMap.addBlock(new BlockTree(0, -21));

        for (int i = 0; i < 7; i++) {
            gameMap.addBlock(new BlockBox(-1 + i, -(sizeYHalf - 5)));
        }

        for (int i = 0; i < 7; i++) {
            gameMap.addBlock(new BlockBox(-6 + i, -(sizeYHalf - 10)));
        }

        gameMap.addBlock(new BlockTree(-6, -(sizeYHalf - 12)));

        for (int i = 0; i < 4; i++) {
            gameMap.addBlock(new BlockBox(2 + i, -(sizeYHalf - 15)));
        }

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i == 0 || i == 5) && (j == 0 || j == 7)) continue;
                gameMap.addBlock(new BlockWater(-3 + i, -(sizeYHalf - 18) + j));
            }
        }

        gameMap.addBlock(new BlockTree(-6, 3));
        gameMap.addBlock(new BlockTree(4, 4));

        gameMap.addBlock(new BlockBox(-6, 13));
        gameMap.addBlock(new BlockBox(-5, 13));
        gameMap.addBlock(new BlockBox(-4, 13));

        gameMap.addBlock(new BlockBox(3, 13));
        gameMap.addBlock(new BlockBox(4, 13));
        gameMap.addBlock(new BlockBox(5, 13));

        gameMap.addBlock(new BlockBox(-4, 14));
        gameMap.addBlock(new BlockBox(3, 14));

        gameMap.addBlock(new BlockBox(-4, 15));
        gameMap.addBlock(new BlockBox(3, 15));

        gameMap.addBlock(new BlockBox(-4, 16));
        gameMap.addBlock(new BlockBox(3, 16));

        gameMap.addBlock(new BlockBox(-6, 17));
        gameMap.addBlock(new BlockBox(-5, 17));
        gameMap.addBlock(new BlockBox(-4, 17));

        gameMap.addBlock(new BlockBox(3, 17));
        gameMap.addBlock(new BlockBox(4, 17));
        gameMap.addBlock(new BlockBox(5, 17));

        gameMap.addBlock(new BlockTree(-6, 19));
        gameMap.addBlock(new BlockTree(4, 19));

        for (int i = -6; i < 5; i++) {
            gameMap.addBlock(new BlockWater(i, 21));
        }

        gameMap.addSpawnPoint(0, -20, 20);
        gameMap.addSpawnPoint(1, 0, 20);
        gameMap.addSpawnPoint(2, 20, 20);

        gameMap.addSpawnPoint(3, -20, 0);
        gameMap.addSpawnPoint(4, 20, 0);

        gameMap.addSpawnPoint(5, -20, -20);
        gameMap.addSpawnPoint(6, 0, -20);
        gameMap.addSpawnPoint(7, 20, -20);

        return gameMap;
    }

    @Override
    public void handleDeath(Entity entity) {
        int alive = 0;
        int dead = 0;
        for (Entity other : this.entities.values()) {
            if (!(other instanceof Player)) continue;

            if (other.isDead()) ++dead;
            else ++alive;
        }

        if (entity instanceof Player p) {
            p.sendPacket(new TickOutgoingPacket.YouDiedPacket());

            this.savePlayer(p, alive + 1);

            // check if team is dead
            /*List<Player> team = new ArrayList<>();
            boolean allTeamDead = true;
            for (Entity e : this.entities.values()) {
                if (e instanceof Player && e.getTeam() == entity.getTeam()) {
                    if (!e.isDead()) {
                        allTeamDead = false;
                        break;
                    }
                    team.add((Player) e);
                }
            }

            if (allTeamDead) {
                int total = alive + dead;
                float place = alive / (float) total;

                // save data
                for (Player p1 : team) {
                    this.savePlayer(p1, place);
                }
            }*/
        }

        if (alive > 1) {
            return;
        }

        this.end();
    }

    private void savePlayer(Player player, int place) {

        if (player.isBot()) return;

        boolean winner = place == 1f;
        int kills = player.getKills();

        int oldRating = player.getTrophies();

        int maxRating = 32;
        int minRating = 3;

        int giveRating = 0;

        int hero = 0;

        for (int i = 0; i < kills; i++) {
            giveRating += new Random().nextInt(3) * kills;
        }

        if (winner) {
            giveRating += 15 + new Random().nextInt(5);
        } else {
            giveRating += (int) (10 * place - 10);
        }

        if (giveRating > maxRating) {
            giveRating = maxRating;
        }

        if (giveRating > 0 && giveRating < minRating) {
            giveRating = minRating;
        }

        int giveTokens = 1;

        int add = giveRating;
        GameServer.getInstance().getThreadPool().execute(() -> {
            try {
                GameServer.getInstance().getGameMongoManager().getPlayers().updateOne(new Document("_id", player.getSession().getPlayerId()).append("heroes.id", hero),
                        Updates.combine(
                                Updates.inc("heroes.$.trophies", add),
                                Updates.inc("give_trophies", add),
                                Updates.inc("hero_tokens", giveTokens)
                        ));
            } catch (Exception e) {
                GameServer.warn("failed to save game data for " + player);
                e.printStackTrace();
            }
            GameServer.getInstance().runOnMainThread(() -> {
                //player.setGiveTrophies(add);
                player.getSession().sendPacket(new GameResultOutgoingPacket(place, player.getKills(), add, giveTokens));
                //player.getSession().sendPacket(new GameDataSavedOutgoingPacket());
            });
        });

        System.out.println("save player " + add);
    }

    @Override
    protected boolean isDataSaved() {
        return true;
    }

    @Override
    public void end() {
        super.end();

        Player winner = null;
        for (Entity e : this.entities.values()) {
            if (!(e instanceof Player)) continue;
            if (e.isDead()) continue;
            winner = (Player) e;
        }

        if (winner == null) {
            GameServer.warn("failed to get winner for game " + this);
            return;
        }

        List<Integer> winners = new ArrayList<>();
        for (Entity e : this.entities.values()) {
            if (e instanceof Player && e.getTeam() == winner.getTeam()) {
                winners.add(e.getEntityId());
                this.savePlayer((Player) e, 1);
            }
        }

        this.broadcast(new TickOutgoingPacket.GameWinnerPacket(winners));
    }
}
