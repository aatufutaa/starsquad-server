package git.aatufutaa.game.game.hero;

import git.aatufutaa.game.game.Game;
import git.aatufutaa.game.game.block.BlockCollision;
import git.aatufutaa.game.game.entity.Entity;
import git.aatufutaa.game.game.entity.Player;
import git.aatufutaa.game.net.packet.TickOutgoingPacket;
import git.aatufutaa.game.util.MathUtil;
import git.aatufutaa.game.util.Vector2;

import java.util.ArrayList;
import java.util.List;

public class HeroProjectile extends Hero {

    private Projectile[] projectiles;
    private int currentId;

    public HeroProjectile(Game game, Player player) {
        super(game, player);

        this.projectiles = new Projectile[5];
        for (int i = 0; i < this.projectiles.length; i++) {
            this.projectiles[i] = new Projectile(i);
        }
    }

    @Override
    public void tick() {
        for (Projectile projectile : this.projectiles) {
            projectile.tick();
        }
    }


    @Override
    public void attack(float targetX, float targetY) {
        System.out.println("hero attack -> " + targetX + " " + targetY);

        Projectile projectile = this.projectiles[this.currentId++];
        if (this.currentId >= this.projectiles.length) {
            this.currentId = 0;
        }

        //Vector2 forward = new Vector2(targetX - this.player.getX(), targetY - this.player.getY());
        //forward.normalize();
        //forward.multiply(this.player.getWidth());
        float shootX = this.player.getX()/* + forward.x*/;
        float shootY = this.player.getY()/* + forward.y*/;

        Vector2 vec = new Vector2(targetX - this.player.getX(), targetY - this.player.getY());
        float speed = 0.6f;
        vec.normalize();
        vec.multiply(speed);

        projectile.shoot(shootX, shootY, vec);

        //System.out.println("shot projectile");

        //System.out.println("shoot at " + game.getCurrentTick());
    }

    public class Projectile {

        private final int id;

        private float x;
        private float y;
        private float lastX;
        private float lastY;

        private Vector2 vel;

        private int age;
        private boolean dead = true;

        private final float width = 0.1f;

        public Projectile(int id) {
            this.id = id;
        }

        private Player getShooter() {
            return HeroProjectile.this.player;
        }

        private Game getGame() {
            return HeroProjectile.this.game;
        }

        public void shoot(float x, float y, Vector2 vel) {
            this.x = x;
            this.y = y;
            this.lastX = x;
            this.lastY = y;

            this.vel = vel;

            this.age = 0;
            this.dead = false;

            this.getGame().broadcast(new TickOutgoingPacket.AddProjectilePacket(this.getShooter().getEntityId(), this.id, x, y, vel.getX(), vel.getY()));
        }

        private void checkForCollision() {
            Vector2 from = new Vector2(this.lastX, this.lastY);
            Vector2 to = new Vector2(this.x, this.y);

            Vector2 add = new Vector2(to.getX() - from.getX(), to.getY() - from.getY());
            add.normalize();
            float d = this.width;
            add.multiply(d);

            float dist = from.distance(to);
            float current = 0f;

            float currentX = from.getX();
            float currentY = from.getY();

            int max = 200;
            while (max-- > 0) {
                if (this.checkForCollision(currentX, currentY)) break;

                if (current >= dist) break;

                currentX += add.getX();
                currentY += add.getY();

                current += d;
            }
        }

        private boolean checkForCollision(float currentX, float currentY) {
            float minPlayerX = currentX - this.width;
            float maxPlayerX = currentX + this.width;
            float minPlayerY = currentY - this.width;
            float maxPlayerY = currentY + this.width;

            int sizeX = this.getGame().getGameMap().getSizeX() / 2;
            int sizeY = this.getGame().getGameMap().getSizeY() / 2;
            if (minPlayerX < -sizeX || maxPlayerX > sizeX || minPlayerY < -sizeY || maxPlayerY > sizeY) {
                this.collide(null);
                return true;
            }

            //boolean collidedWithBlock = false;
            //int blockX = 0;
            //int blockY = 0;

            // block collision
            for (int x = MathUtil.floorToInt(minPlayerX); x <= MathUtil.floorToInt(maxPlayerX); x++) {
                for (int y = MathUtil.floorToInt(minPlayerY); y <= MathUtil.floorToInt(maxPlayerY); y++) {
                    BlockCollision blockCollision = this.getGame().getGameMap().getCollision(x, y);

                    if (blockCollision == null) continue;
                    if (!blockCollision.isBlocksProjectiles()) continue;

                    float minBlockX = x + blockCollision.getMinX();
                    float maxBlockX = x + blockCollision.getMaxX();
                    float minBlockY = y + blockCollision.getMinY();
                    float maxBlockY = y + blockCollision.getMaxY();

                    if (maxPlayerX >= minBlockX && minPlayerX <= maxBlockX && maxPlayerY >= minBlockY && minPlayerY <= maxBlockY) {
                        //collidedWithBlock = true;
                        //blockX = x;
                        //blockY = y;
                        //this.collide(null);
                        //this.getGame().broadcast(new TickOutgoingPacket.BlockHitPacket(x, y, this.vel.getX(), this.vel.getY()));
                        //return true; // collide also with players

                        // send animation to parent
                        if (blockCollision.getParent() != null) {
                            blockCollision = blockCollision.getParent();
                        }

                        this.collide(null);
                        this.getGame().broadcast(new TickOutgoingPacket.BlockHitPacket(blockCollision.getX(), blockCollision.getY(), this.vel.getX(), this.vel.getY()));
                        return true; // dont collide with players (this is called in order most likely) so block should be before players
                    }
                }
            }

            List<Entity> targets = new ArrayList<>();

            for (Entity entity : this.getGame().getEntities().values()) {

                if (entity.isDead() || entity.getTeam() == this.getShooter().getTeam()) continue;

                Vector2 from = new Vector2(entity.getLastX(), entity.getLastY());
                Vector2 to = new Vector2(entity.getX(), entity.getY());
                Vector2 add = new Vector2(to.getX() - from.getX(), to.getY() - from.getY());
                add.normalize();
                float d = entity.getWidth();
                add.multiply(d);
                float dist = from.distance(to);
                float current = 0f;

                float currentX2 = from.getX();
                float currentY2 = from.getY();

                int max = 200;
                while (max-- > 0) {
                    float minBlockX = currentX2 - entity.getWidth();
                    float maxBlockX = currentX2 + entity.getWidth();
                    float minBlockY = currentY2 - entity.getWidth();
                    float maxBlockY = currentY2 + entity.getWidth();

                    if (maxPlayerX >= minBlockX && minPlayerX <= maxBlockX && maxPlayerY >= minBlockY &&
                            minPlayerY <= maxBlockY) {
                        targets.add(entity);
                        break;
                    }

                    if (current >= dist) break;

                    currentX2 += add.getX();
                    currentY2 += add.getY();

                    current += d;
                }
            }

            if (!targets.isEmpty()) {
                this.collide(targets);
                return true;
            }

            //if (collidedWithBlock) {
            //    this.collide(null);
            //    this.getGame().broadcast(new TickOutgoingPacket.BlockHitPacket(blockX, blockY, this.vel.getX(), this.vel.getY()));
             //   return true;
            //}

            return false;

        }

        protected void collide(List<Entity> targets) {
            System.out.println("projectile collide " + targets);
            this.dead = true;

            if (targets != null) {
                for (Entity target : targets) {
                    target.damage(290, this.getShooter());
                }
            }

            this.getGame().broadcast(new TickOutgoingPacket.RemoveProjectilePacket(this.getShooter().getEntityId(), this.id, this.x, this.y));
        }

        public void tick() {
            if (this.dead) return;

            if (++this.age > 20) {
                this.dead = true;
                return;
            }

            // skip first tick so position is correct on client side
            if (this.age > 1) {
                this.lastX = this.x;
                this.lastY = this.y;

                this.x += this.vel.getX();
                this.y += this.vel.getY();
            }

            this.checkForCollision();
        }
    }
}
