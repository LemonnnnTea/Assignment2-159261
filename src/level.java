import java.awt.*;
import java.util.ArrayList;

public abstract class level {

    Image backgroundImage;

    player player1;
    player player2;

    ArrayList<Platform> platforms = new ArrayList<>();
    ArrayList<Trap> traps = new ArrayList<>();
    ArrayList<Portal> portals = new ArrayList<>();

    double spawnX1, spawnY1;
    double spawnX2, spawnY2;

    double goalX, goalY, goalWidth, goalHeight;

    public level(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public abstract void load(
            player player1,
            player player2,
            Image platformImage,
            Image spikeImage,
            Image sawImage,
            Image pitImage,
            Image knifeImage,
            Image portalImage
    );

    public void update(double dt) {

        player1.updatePlayer(dt);
        player2.updatePlayer(dt);

        handlePlatformCollision(player1);
        handlePlatformCollision(player2);

        for (Trap trap : traps) {

            if (!trap.isActive()) {
                continue;
            }

            trap.update(dt, player1, player2);

            if (trap.checkCollision(player1)) {
                trap.onCollide(player1);
            }

            if (trap.checkCollision(player2)) {
                trap.onCollide(player2);
            }
        }

        handlePortals(player1);
        handlePortals(player2);

        respawnIfDead(player1, spawnX1, spawnY1);
        respawnIfDead(player2, spawnX2, spawnY2);
    }

    private void handlePlatformCollision(player p) {

        p.onGround = false;

        for (Platform platform : platforms) {

            if (platform.checkCollision(p)) {

                double playerBottom = p.y + p.height;
                double playerTop = p.y;
                double playerLeft = p.x;
                double playerRight = p.x + p.width;

                double platformBottom = platform.y + platform.height;
                double platformTop = platform.y;
                double platformLeft = platform.x;
                double platformRight = platform.x + platform.width;

                double overlapBottom = playerBottom - platformTop;
                double overlapTop = platformBottom - playerTop;
                double overlapRight = playerRight - platformLeft;
                double overlapLeft = platformRight - playerLeft;

                double minOverlap = Math.min(
                        Math.min(overlapBottom, overlapTop),
                        Math.min(overlapRight, overlapLeft)
                );

                if (minOverlap == overlapBottom && p.velocityY >= 0) {
                    p.y = platformTop - p.height;
                    p.velocityY = 0;
                    p.onGround = true;
                } else if (minOverlap == overlapTop && p.velocityY < 0) {
                    p.y = platformBottom;
                    p.velocityY = 0;
                } else if (minOverlap == overlapRight && p.velocityX > 0) {
                    p.x = platformLeft - p.width;
                    p.velocityX = 0;
                } else if (minOverlap == overlapLeft && p.velocityX < 0) {
                    p.x = platformRight;
                    p.velocityX = 0;
                }
            }
        }
    }

    private void handlePortals(player p) {
        for (Portal portal : portals) {
            if (portal.checkCollision(p)) {
                portal.teleport(p);
            }
        }
    }

    private void respawnIfDead(player p, double spawnX, double spawnY) {
        if (p.dead) {
            p.x = spawnX;
            p.y = spawnY;
            p.velocityX = 0;
            p.velocityY = 0;
            p.dead = false;
        }
    }

    public boolean isCompleted() {
        return CollisionManager.rectCollision(
                player1.x, player1.y, player1.width, player1.height,
                goalX, goalY, goalWidth, goalHeight
        ) &&
                CollisionManager.rectCollision(
                        player2.x, player2.y, player2.width, player2.height,
                        goalX, goalY, goalWidth, goalHeight
                );
    }

    public ArrayList<Platform> getPlatforms() {
        return platforms;
    }

    public ArrayList<Trap> getTraps() {
        return traps;
    }

    public ArrayList<Portal> getPortals() {
        return portals;
    }
}