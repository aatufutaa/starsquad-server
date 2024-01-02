package git.aatufutaa.game.game;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.game.entity.Entity;
import git.aatufutaa.game.game.entity.EntityData;
import git.aatufutaa.game.game.entity.Player;
import git.aatufutaa.game.game.games.tutorial.TutorialGame;
import git.aatufutaa.game.game.map.GameMap;
import git.aatufutaa.game.game.map.GameMapImage;
import git.aatufutaa.game.net.cllient.GameClient;
import git.aatufutaa.game.net.packet.hello.DynamicGameDataOutgoingPacket;
import git.aatufutaa.game.net.packet.hello.GameDataOutgoingPacket;
import git.aatufutaa.game.net.udp.UdpPacketHandler;
import git.aatufutaa.game.session.Session;
import git.aatufutaa.game.util.Vector2;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.game.net.packet.TickOutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Game {

    private final GameType gameType;

    @Getter
    private final int gameId;

    private int currentTick;

    @Getter
    protected final Map<Integer, Player> players = new HashMap<>();

    @Getter
    protected final Map<Integer, Entity> entities = new HashMap<>();
    private int currentEntityId;

    @Getter
    private boolean cancelled;

    @Getter
    private final GameMapImage gameMap;

    @Getter
    protected GameState gameState = GameState.STARTING;
    private GameState lastGameState = GameState.STARTING;

    private int endingTicks;

    private boolean allSaved;

    public Game(GameType gameType, int gameId, GameMap gameMap) {
        this.gameType = gameType;
        this.gameId = gameId;
        this.gameMap = gameMap.createImage();
    }

    public void cancel() {
        this.cancelled = true;
    }

    public Player addPlayer(String name, int maxHealth, int team) {
        int entityId = this.currentEntityId++;

        Player player = new Player(this, entityId, name, team, maxHealth);

        this.entities.put(entityId, player);

        if (!(this instanceof TutorialGame)) {
            GameMap.SpawnPoint spawnPoint = this.gameMap.getHandle().getSpawnPoint(team);
            if (spawnPoint == null) {
                GameServer.warn("failed to find spawnpoint for " + team + " in " + this.gameMap);
            } else {
                player.teleport(spawnPoint.getX(), spawnPoint.getY());
            }
        }

        return player;
    }

    // fill empty player slots with bots
    public void fillWithBots() {
        int[] teams = new int[this.gameType.getTeamCount()];
        Arrays.fill(teams, 0);

        for (Player player : this.players.values()) {
            teams[player.getTeam()] = teams[player.getTeam()] + 1;
        }

        for (int teamId = 0; teamId < teams.length; teamId++) {
            int botsNeeded = this.gameType.getTeamSize() - teams[teamId];
            for (int i = 0; i < botsNeeded; i++) {
                Player p1 = this.addPlayer("bot_" + teamId + "_" + i, 1500, teamId);
                //p1.teleport(new Random().nextInt(5), new Random().nextInt(5));
            }
        }
    }

    public void registerPlayer(Session session, Player player) {
        player.setSession(session);
        this.players.put(session.getPlayerId(), player);
    }

    public void tick() {

        // game start time packet
        if (this.gameState == GameState.STARTING && this.currentTick % 20 == 0) {
            int startTime = 10;
            int seconds = this.currentTick / 20;
            this.broadcast(new TickOutgoingPacket.GameStartingPacket(startTime - seconds));
            if (seconds == startTime) {
                this.gameState = GameState.STARTED;
            }
        }

        if (this.gameState == GameState.ENDING) {
            if (++this.endingTicks > 60 && this.isDataSaved()) {
                this.gameState = GameState.ENDED;
                for (Player player : this.players.values()) {
                    ClientBase client = player.getSession().getClient();
                    //if (client != null) client.sendPacket(new GameResultOutgoingPacket(player.getTrophies(), player.getGiveTrophies()));
                }
            }
        }

        /*for (Player player : this.players.values()) {
            if (this.currentTick % 40 == 0) { // ping every 2sec
                GameClient client = player.getSession().getClient();
                if (client != null && client.isConnected() && client.canSendUdp()) {
                    client.sendPacket(new PingOutgoingPacket());
                }
            }
        }*/

        // pre tick
        for (Entity entity : this.entities.values()) {
            entity.tick();
        }

        // post tick

        if (this.gameState != this.lastGameState) {
            this.lastGameState = this.gameState;
            this.broadcast(new TickOutgoingPacket.GameStatePacket(this.gameState));
        }

        // send packets
        for (Player player : this.players.values()) {
            GameClient client = (GameClient) player.getSession().getClient();
            if (client != null && client.canSendUdp()) {
                List<TickOutgoingPacket.TickPacket> packets = player.getPackets();

                List<EntityData> entities = new ArrayList<>();
                for (Entity entity : this.entities.values()) {
                    if (entity.isDead()) continue;
                    if (player == entity) continue;
                    entities.add(entity.getEntityData());
                }

                // send tcp packets
                if (!packets.isEmpty() || client.isFallbackToTcp()) { // only send tcp if has packets
                    player.setPackets(new ArrayList<>());
                    List<EntityData> playerEntityData = client.isFallbackToTcp() ? entities : null; // attach entity data if fallback to tcp
                    client.sendPacket(new TickOutgoingPacket(this.currentTick, packets, playerEntityData));
                }

                // send udp packet
                if (!client.isFallbackToTcp()) {
                    GameServer.getInstance().getUdpNetworkManager().sendPacket(new UdpPacketHandler.MoveOutgoingPacket(player.getSession().getUdpId(), this.currentTick, entities), client.getUdpAddress());
                }
            } else {
                player.getPackets().clear();
            }
        }

        ++this.currentTick;
    }

    public void sendDynamicData(Session session) {
        ClientBase client = session.getClient();
        if (client == null) return;

        Player player = this.players.get(session.getPlayerId());
        if (player != null) { // not spectator
            player.resetConnection();
        }

        List<DynamicGameDataOutgoingPacket.PlayerInfo> players = new ArrayList<>();
        for (Entity entity : this.entities.values()) {
            players.add(new DynamicGameDataOutgoingPacket.PlayerInfo(entity.getEntityId(), entity.getHealth(), entity.getX(), entity.getY()));
        }

        client.sendPacket(new DynamicGameDataOutgoingPacket(this.currentTick, this.gameState, this.getDynamicGameData(), players, this.gameMap.getDynamicData()));
    }

    public void sendStaticData(Session session) {
        ClientBase client = session.getClient();
        if (client == null) return;

        Player thePlayer = this.players.get(session.getPlayerId());
        int entityId = /*thePlayer == null ? -1 : */thePlayer.getEntityId();
        int thePlayerTeam = /*thePlayer == null ? 0 : */thePlayer.getTeam();

        List<GameDataOutgoingPacket.PlayerInfo> players = new ArrayList<>();
        for (Entity entity : this.entities.values()) {
            if (entity instanceof Player player) {
                players.add(new GameDataOutgoingPacket.PlayerInfo(entity.getEntityId(), player.getName(),
                        0,
                        0,
                        entity.getTeam() == thePlayerTeam,
                        entity.getMaxHealth()));
            }
        }

        client.sendPacket(new GameDataOutgoingPacket(GameServer.getInstance().getUdpNetworkManager().getPort(), this.gameType, entityId, players, this.gameMap));
    }

    @Getter
    public static class PlayerInput {
        private int clientTick;

        private float x;
        private float y;

        private float lastX;
        private float lastY;

        private boolean attacked;
        private float attackX;
        private float attackY;
        private int attackId;

        private boolean tcp;

        public PlayerInput(boolean tcp) {
            this.tcp = tcp;
        }

        public void read(ByteBuf buf) {
            this.clientTick = buf.readShortLE();

            this.x = buf.readFloatLE();
            this.y = buf.readFloatLE();

            if (!this.tcp) {
                this.lastX = buf.readFloatLE();
                this.lastY = buf.readFloatLE();
            }

            this.attacked = buf.readBoolean();
            if (this.attacked) {
                this.attackX = buf.readFloatLE();
                this.attackY = buf.readFloatLE();
                this.attackId = buf.readShortLE();
            }
        }
    }

    public void handleInput(Session session, PlayerInput playerInput) {
        Player player = this.players.get(session.getPlayerId());

        if (player == null) {
            GameServer.warn("cant find player in handle input for " + session + " " + this);
            return;
        }

        if (this.gameState != GameState.STARTED) return;

        if (player.isDead()) return;

        float fromX = player.getX();
        float fromY = player.getY();

        // TODO: check if sending packets too fast
        // TODO: check if sending double packets per tick

        float x = playerInput.getX();
        float y = playerInput.getY();

        // TODO: check collision

        player.setX(x);
        player.setY(y);

        if (player.canSetRot()) {
            Vector2 dir = new Vector2(x - fromX, y - fromY);
            if (dir.length() > 0.001f) {
                float rot = (float) Math.atan2(dir.getX(), dir.getY());
                player.setRot(rot);
            }
        }

        if (playerInput.isAttacked()) {
            player.attack(playerInput.getAttackId(), playerInput.getAttackX(), playerInput.getAttackY(), false);
        }
    }

    public void broadcast(TickOutgoingPacket.TickPacket packet) {
        for (Player player : this.players.values()) {
            player.sendPacket(packet);
        }
    }

    public DynamicGameData getDynamicGameData() {
        return new DynamicGameData();
    }

    public void handleDeath(Entity entity) {
    }

    protected void end() {
        GameServer.log("ending game");
        this.gameState = GameState.ENDING;


    }

    protected boolean isDataSaved() {
        return this.allSaved;
    }

    protected void savePlayers() {
        AtomicInteger saved = new AtomicInteger(0);
        for (Player player : this.players.values()) {
            GameServer.getInstance().getThreadPool().execute(() -> {
                try {
                    this.saveData(player);
                } catch (Exception e) {
                    GameServer.warn("Failed to save data for " + player.getSession().getPlayerId());
                    e.printStackTrace();
                }
                if (saved.incrementAndGet() == this.players.size()) {
                    this.allSaved = true;
                }
            });
        }
    }

    public void handleEnded() {
        for (Player player : this.players.values()) {
            player.getSession().setGame(null);
        }
    }

    protected void saveData(Player player) {
    }
}
