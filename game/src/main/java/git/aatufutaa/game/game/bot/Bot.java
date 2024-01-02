package git.aatufutaa.game.game.bot;

import git.aatufutaa.game.game.Game;
import git.aatufutaa.game.game.GameState;
import git.aatufutaa.game.game.entity.Entity;
import git.aatufutaa.game.game.entity.Player;
import git.aatufutaa.game.game.games.tutorial.TutorialGame;
import git.aatufutaa.game.util.Vector2;

import java.util.Random;

public class Bot {

    private final Game game;
    private final Player player;

    private int skill;

    private Vector2 vel;

    private int lastDirectionFix;

    private float currentRot;

    private boolean move;

    private int lastAttack;

    public Bot(Game game, Player player, int skill) {
        this.game = game;
        this.player = player;
        this.skill = skill;
    }

    public void tick() {
        if (this.player.isDead()) return;
        if (this.game instanceof TutorialGame) return;
        if (this.game.getGameState() != GameState.STARTED) return;

        Entity target = null;
        float dist = 0f;
        for (Entity entity : this.game.getEntities().values()) {
            if (entity.getTeam() == this.player.getTeam()) continue;
            if (entity.isDead()) continue;
            target = entity;
            dist = (float) Math.hypot(this.player.getX() - entity.getX(), this.player.getY() - entity.getY());
        }
        target = this.game.getPlayers().values().iterator().next();

        ++this.lastDirectionFix;

        ++this.lastAttack;

        if (target != null) {

            if (dist < 7f && this.lastAttack > 5) {
                if (this.player.getReloads() > 0) {
                    this.player.attack(-1, target.getX(), target.getY(), true);
                    this.lastAttack = 0;
                }
            }
            if (this.lastDirectionFix > 10) {
                this.lastDirectionFix = 0;
                float targetX = target.getX();
                float targetY = target.getY();

                targetX += new Random().nextFloat() * 4f;
                targetY += new Random().nextFloat() * 4f;

                Vector2 dir = new Vector2(targetX - this.player.getX(), targetY - this.player.getY());

                this.currentRot = (float) Math.atan2(dir.getX(), dir.getY());

                dir.normalize();

                float speed1 = 0.12f;
                dir.multiply(speed1);
                this.vel = dir;

                this.move = true;
            }
        } else {
            this.move = false;
        }

        if (this.move) {
            if (this.player.canSetRot()) {
                this.player.setRot(this.currentRot);
            }
        }

        if (this.vel != null)
            this.move(this.vel.getX(), this.vel.getY());
    }

    private void move(float velX, float velY) {
        float x = this.player.getX() + velX;
        float y = this.player.getY() + velY;

        this.player.setX(x);
        this.player.setY(y);
    }
}
