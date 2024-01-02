package git.aatufutaa.game.game.entity;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.game.Game;
import git.aatufutaa.game.game.bot.Bot;
import git.aatufutaa.game.game.hero.Hero;
import git.aatufutaa.game.game.hero.HeroProjectile;
import git.aatufutaa.game.net.packet.TickOutgoingPacket;
import git.aatufutaa.game.session.Session;
import git.aatufutaa.game.util.Vector2;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {

    @Getter
    private final String name;
    @Getter
    @Setter
    private Session session;

    @Getter
    private final Hero hero;

    private boolean attacked;
    private float attackX;
    private float attackY;
    private int attackId;
    private boolean botAttack;

    private static final int MAX_RELOADS = 4;
    private static final int RELOAD_TIME = 48;
    @Getter
    private int reloads = MAX_RELOADS;
    private int reloadTicks;

    @Getter
    @Setter
    private List<TickOutgoingPacket.TickPacket> packets = new ArrayList<>();

    private boolean respawning;
    private int respawnTicks;

    private final Bot bot;

    private static final int ATTACK_ROT_TICKS = 6;
    @Getter
    private int attackRotTicks = ATTACK_ROT_TICKS;

    @Getter
    @Setter
    private int kills;

    @Getter
    private int trophies;
    @Getter
    @Setter
    private int giveTrophies;

    public Player(Game game, int entityId, String name, int team, int maxHealth) {
        super(game, entityId, team, maxHealth);
        this.name = name;

        this.hero = new HeroProjectile(game, this);

        this.bot = new Bot(game, this, 2);
    }

    public boolean isBot() {
        return this.session == null;
    }

    public boolean canSetRot() {
        return this.attackRotTicks > ATTACK_ROT_TICKS;
    }

    @Override
    public void tick() {
        super.tick();

        ++this.attackRotTicks;

        if (this.session == null || this.session.getClient() == null || !this.session.getClient().isConnected()) {
            this.bot.tick();
        }

        // respawning
        if (this.isDead() && this.respawning) {
            if (--this.respawnTicks <= 0) {
                this.respawn();
            }
        }

        // TODO: read input

        if (this.reloads < MAX_RELOADS) {
            if (++this.reloadTicks >= RELOAD_TIME) { // TODO: allow 1 off?
                ++this.reloads;
                this.reloadTicks = 0;
            }
        }

        this.handleAttack();

        this.hero.tick();
    }

    private void cancelAttack(int attackIdRange1, int attackIdRange2, String reason) {
        GameServer.warn("cancelled attack for " + this + " for " + reason);

        // TODO: send cancel attack packet
    }

    public void resetConnection() {
        this.attackId = 0;
        this.packets = new ArrayList<>();
    }

    private void handleAttack() {
        if (!this.attacked) return;

        this.attacked = false;

        if (this.reloads <= 0 && !this.botAttack) {
            this.cancelAttack(this.attackId, -1, "no reloads left " + this.reloadTicks);
            return;
        }

        --this.reloads;

        this.hero.attack(this.attackX, this.attackY);

        Vector2 dir = new Vector2(this.attackX - this.x, this.attackY - this.y);
        float rot = (float) Math.atan2(dir.getX(), dir.getY());
        this.setRot(rot);
        this.attackRotTicks = 0;
    }

    public void attack(int attackId, float targetX, float targetY, boolean botAttack) {
        if (!botAttack) {
            if (this.attackId > attackId) {
                GameServer.warn("server attack id is ahead of client???? server=" + this.attackId + " client=" + attackId);
                this.cancelAttack(attackId, -1, "server attack counter is ahead");
                return;
            }

            if (this.attacked) { // double attack
                this.cancelAttack(this.attackId, -1, "double attack");
            }

            if (attackId != this.attackId + 1) {
                // missed attack (packet lost)?
                // cancel all attacks between last accept attack and current attack
                this.cancelAttack(this.attackId + 1, attackId - 1, "missed attack");
            }
        }
        this.attacked = true;
        this.attackX = targetX;
        this.attackY = targetY;
        this.attackId = attackId;
        this.botAttack = botAttack;
    }

    public void sendPacket(TickOutgoingPacket.TickPacket packet) {
        if (this.session == null) return; // dont send to bot
        this.packets.add(packet);
    }

    @Override
    protected void onDeath(Entity killer) {
        super.onDeath(killer);
    }

    public void respawnIn(int ticks) {
        this.respawning = true;
        this.respawnTicks = ticks;
    }

    public void respawn() {
        this.respawning = false;

        this.health = this.maxHealth;

        this.reloadTicks = 0;
        this.reloads = MAX_RELOADS;

        this.game.broadcast(new TickOutgoingPacket.RespawnPlayerPacket(this.entityId, this.x, this.y));
    }
}
