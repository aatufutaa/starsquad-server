package git.aatufutaa.game.game.entity;

import git.aatufutaa.game.game.Game;
import git.aatufutaa.game.game.GameState;
import git.aatufutaa.game.net.packet.TickOutgoingPacket;
import lombok.Getter;
import lombok.Setter;

public class Entity {

    protected final Game game;

    @Getter
    protected final int entityId;

    @Getter
    private final int team;

    @Getter
    protected final int maxHealth;
    @Getter
    protected int health;

    @Getter
    @Setter
    protected float x;
    @Getter
    @Setter
    protected float y;

    @Getter
    @Setter
    private float rot;

    @Getter
    protected float lastX;
    @Getter
    protected float lastY;

    @Getter
    private final float width = 0.4f;

    public Entity(Game game, int entityId, int team, int maxHealth) {
        this.game = game;
        this.entityId = entityId;
        this.team = team;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void tick() {
        this.lastX = this.x;
        this.lastY = this.y;
    }

    public boolean isDead() {
        return this.health <= 0f;
    }

    protected void onDeath(Entity killer) {
        System.out.println(this + " deayth");
        if (killer instanceof Player p) {
            p.setKills(p.getKills() + 1);
        }
        this.game.handleDeath(this);
    }

    public void teleport(float x, float y) {
        this.x = x;
        this.y = y;
        this.lastX = x;
        this.lastY = y;
    }

    public void damage(int damage, Entity cause) {
        if (this.isDead()) return;

        System.out.println("damage " + this + " with " + damage);

        if (this.game.getGameState() != GameState.STARTED) return;

        this.health -= damage;
        if (this.health <= 0) {
            this.onDeath(cause);
            this.health = 0;
        }

        this.game.broadcast(new TickOutgoingPacket.DamageEntityPacket(this.entityId, this.health));
    }

    public EntityData getEntityData() {
        return new EntityData(this.entityId, this.x, this.y, this.rot);
    }

    public void setDead() {
        this.health = 0;
    }
}
