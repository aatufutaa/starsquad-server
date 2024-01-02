package git.aatufutaa.game.game.games.tutorial;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.game.DynamicGameData;
import git.aatufutaa.game.game.Game;
import git.aatufutaa.game.game.GameState;
import git.aatufutaa.game.game.GameType;
import git.aatufutaa.game.game.block.BlockBox;
import git.aatufutaa.game.game.block.BlockGrass;
import git.aatufutaa.game.game.block.BlockTree;
import git.aatufutaa.game.game.entity.Entity;
import git.aatufutaa.game.game.entity.Player;
import git.aatufutaa.game.game.map.GameMap;
import git.aatufutaa.game.mongo.PlayerDataCache;
import git.aatufutaa.game.net.packet.GameResultOutgoingPacket;
import git.aatufutaa.game.net.packet.TickOutgoingPacket;
import git.aatufutaa.game.util.Vector2;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.List;

public class TutorialGame extends Game {

    private TutorialStage tutorialStage = TutorialStage.BEGIN;

    private boolean spinner;
    private int spinnerX;
    private int spinnerY;

    private int entitiesKilled;

    private int endingTicks;

    public TutorialGame(int gameId) {
        super(GameType.TUTORIAL, gameId, getTutorialMap());

        this.gameState = GameState.STARTED;

        this.showSpinner(-3, -16);
    }

    private void showSpinner(int x, int y) {
        this.spinner = true;
        this.spinnerX = x;
        this.spinnerY = y;
    }

    private void hideSpinner() {
        this.spinner = false;
    }

    private static GameMap getTutorialMap() {
        int sizeX = 12;
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
                gameMap.addBlock(new BlockGrass(-3 + i, -(sizeYHalf - 18) + j));
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
            gameMap.addBlock(new BlockBox(i, 21));
        }

        return gameMap;
    }

    private Player getPlayer() {
        return (Player) this.entities.get(0);
    }

    private List<Player> getEnemies() {
        return Arrays.asList(
                (Player) this.entities.get(1),
                (Player) this.entities.get(2)
        );
    }

    private Player getSkillTarget() {
        return (Player) this.entities.get(3);
    }

    public void initPlayer() {
        Player player = this.getPlayer();
        player.setX(-3);
        player.setY(-22);
    }

    private boolean testSpinner(Vector2 pos) {
        Vector2 target = new Vector2(this.spinnerX, this.spinnerY);

        float distance = pos.distance(target);

        if (distance < 1f) {
            return true;
        }

        return false;
    }

    @Override
    public void tick() {
        if (this.getGameState() == GameState.STARTED) {
            this.handleTutorial();

            // disconnected over 30s just cancel the game
            if (this.getPlayer().getSession().getDisconnectTicks() > 20 * 30) {
                this.cancel();
                //this.getPlayer().getSession().kick(null);
                //GameSever.getInstance().getMasterConnection().sendPacket(new DisconnectPlayerMasterOutgoingPacket(this.getPlayer().getSession().getPlayerId()));
            }
        }

        super.tick();
    }

    private void handleTutorial() {
        Player player = this.getPlayer();

        // TODO: check if player is online
        // TODO: check if tutorial is less than 10m

        Vector2 pos = new Vector2(player.getX(), player.getY());

        switch (this.tutorialStage) {
            case BEGIN -> {

                if (this.testSpinner(pos)) {
                    this.tutorialStage = TutorialStage.SWIPE_RIGHT;
                    this.showSpinner(3, -15);
                    this.broadcast(new TickOutgoingPacket.TutorialPacket((TutorialDynamicGameData) this.getDynamicGameData()));
                }

                break;
            }

            case SWIPE_RIGHT -> {

                if (this.testSpinner(pos)) {
                    this.tutorialStage = TutorialStage.SWIPE_UP;
                    this.showSpinner(3, -11);
                    this.broadcast(new TickOutgoingPacket.TutorialPacket((TutorialDynamicGameData) this.getDynamicGameData()));
                }

                break;
            }

            case SWIPE_UP -> {

                if (this.testSpinner(pos)) {
                    this.tutorialStage = TutorialStage.SWIPE_LEFT;
                    this.showSpinner(0, -10);
                    this.broadcast(new TickOutgoingPacket.TutorialPacket((TutorialDynamicGameData) this.getDynamicGameData()));
                }

                break;
            }

            case SWIPE_LEFT -> {

                if (this.testSpinner(pos)) {
                    this.tutorialStage = TutorialStage.GO_HIDE_BUSHES;
                    this.showSpinner(0, -3);
                    this.broadcast(new TickOutgoingPacket.TutorialPacket((TutorialDynamicGameData) this.getDynamicGameData()));
                }

                break;
            }

            case GO_HIDE_BUSHES -> {

                if (this.testSpinner(pos)) {
                    this.tutorialStage = TutorialStage.IN_BUSHES;
                    this.showSpinner(0, 4);
                    this.broadcast(new TickOutgoingPacket.TutorialPacket((TutorialDynamicGameData) this.getDynamicGameData()));
                }

                break;
            }

            case IN_BUSHES -> {

                if (this.testSpinner(pos)) {
                    this.tutorialStage = TutorialStage.SHOOT_TARGETS;
                    this.hideSpinner();
                    this.broadcast(new TickOutgoingPacket.TutorialPacket((TutorialDynamicGameData) this.getDynamicGameData()));

                    List<Player> targets = this.getEnemies();

                    Player p1 = targets.get(0);
                    Player p2 = targets.get(1);

                    p1.teleport(-1, 9);
                    p2.teleport(1, 9);


                    p1.respawn();
                    p2.respawn();
                }

                break;

            }

            case HEALTH_INFO -> {

                if (this.testSpinner(pos)) {
                    this.tutorialStage = TutorialStage.ACTIVATE_SKILL_OR_SHOOT_ENEMIES;
                    this.hideSpinner();
                    this.broadcast(new TickOutgoingPacket.TutorialPacket((TutorialDynamicGameData) this.getDynamicGameData()));

                    Player target = this.getSkillTarget();

                    target.teleport(0, 20);
                    target.respawn();
                }

                break;
            }

            case TUTORIAL_DONE -> {

                if (--this.endingTicks == 0) {
                    System.out.println("tutorial finished");
                    this.end();
                }

                break;
            }
        }
    }

    @Override
    public DynamicGameData getDynamicGameData() {
        return new TutorialDynamicGameData(this.tutorialStage, this.spinner, this.spinnerX, this.spinnerY);
    }

    @Override
    public void handleDeath(Entity entity) {
        ++this.entitiesKilled;

        if (this.entitiesKilled == 2) {
            this.tutorialStage = TutorialStage.HEALTH_INFO;
            this.showSpinner(0, 16);
            this.broadcast(new TickOutgoingPacket.TutorialPacket((TutorialDynamicGameData) this.getDynamicGameData()));
        } else if (this.entitiesKilled == 3) {
            this.tutorialStage = TutorialStage.TUTORIAL_DONE;
            this.endingTicks = 20;
            this.broadcast(new TickOutgoingPacket.TutorialPacket((TutorialDynamicGameData) this.getDynamicGameData()));
        }
    }

    @Override
    protected boolean isDataSaved() {
        return true;
    }

    @Override
    public void end() {
        super.end();

        Player player = this.getPlayer();

        GameServer.getInstance().getThreadPool().execute(() -> {

            try {
                GameServer.getInstance().getGameMongoManager().getPlayers().updateOne(new Document("_id", player.getSession().getPlayerId()),
                        Updates.set("tutorial_stage", "played_at_" + System.currentTimeMillis()));
            } catch (Exception e) {
                e.printStackTrace();

                GameServer.getInstance().runOnMainThread(() -> {
                    player.getSession().kick("Tutorial error! Try again later!");
                });
                return;
            }

            try (Jedis jedis = GameServer.getInstance().getRedisManager().getResource()) {
                PlayerDataCache.clearCache(jedis, player.getSession().getPlayerId());
            } catch (Exception e) {
                e.printStackTrace();

                GameServer.getInstance().runOnMainThread(() -> {
                    player.getSession().kick("Tutorial error! Try again later!");
                });
                return;
            }

            GameServer.getInstance().runOnMainThread(() -> {
                player.getSession().sendPacket(new GameResultOutgoingPacket(-1,0,0,0));
            });
        });
    }
}
