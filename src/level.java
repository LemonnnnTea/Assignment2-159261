import java.awt.*;
import java.util.ArrayList;

public abstract class level {

    Image backgroundImage;

    player player1;
    player player2;

    ArrayList<Platform> platforms = new ArrayList<>();
    ArrayList<Trap> traps = new ArrayList<>();
    ArrayList<Portal> portals = new ArrayList<>();
    Gate gate;

    double spawnX1, spawnY1;
    double spawnX2, spawnY2;

    double goalX, goalY, goalWidth, goalHeight;

    public level(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public abstract void load(
            player player1,
            player player2,
            Image[] platformImage,
            Image spikeImage,
            Image[] sawFrames,
            Image pitImage,
            Image knifeImage,
            Image[] portalImage,
            Image[] gateImage
    );

    public void update(double dt) {

        if (!player1.reachedGate) {
            player1.updatePlayer(dt);
        }

        if (!player2.reachedGate) {
            player2.updatePlayer(dt);
        }

        updateMovingPlatforms(dt);

        for (Portal portal : portals) {
            portal.update(dt);
        }

        boolean player1Teleported = handlePortals(player1);
        boolean player2Teleported = handlePortals(player2);

        if (!player1.reachedGate) {
            handlePlatformCollision(player1);
        }

        if (!player2.reachedGate) {
            handlePlatformCollision(player2);
        }

        handlePlayerCollision();

        if (!player1Teleported) {
            handlePortals(player1);
        }

        if (!player2Teleported) {
            handlePortals(player2);
        }

        for (Trap trap : traps) {

            if (!trap.isActive()) {
                continue;
            }

            trap.update(dt, player1, player2);

            if (!player1.reachedGate && trap.checkCollision(player1)) {
                trap.onCollide(player1);
            }

            if (!player2.reachedGate && trap.checkCollision(player2)) {
                trap.onCollide(player2);
            }
        }

        if (!player1.reachedGate) {
            handleGate(player1);
        }

        if (!player2.reachedGate) {
            handleGate(player2);
        }

        if (gate != null) {
            gate.update(dt);
        }

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

    private void updateMovingPlatforms(double dt) {
        for (Platform platform : platforms) {
            if (platform instanceof MovingPit) {
                ((MovingPit) platform).update(dt, player1, player2);
            }
        }
    }

    private void handlePlayerCollision() {
        if (player1.reachedGate || player2.reachedGate || player1.dead || player2.dead) {
            return;
        }

        if (!CollisionManager.rectCollision(
                player1.x, player1.y, player1.width, player1.height,
                player2.x, player2.y, player2.width, player2.height
        )) {
            return;
        }

        double player1Bottom = player1.y + player1.height;
        double player2Bottom = player2.y + player2.height;
        double player1Right = player1.x + player1.width;
        double player2Right = player2.x + player2.width;

        double overlapPlayer1Top = player1Bottom - player2.y;
        double overlapPlayer2Top = player2Bottom - player1.y;
        double overlapPlayer1Left = player1Right - player2.x;
        double overlapPlayer2Left = player2Right - player1.x;

        double verticalOverlap = Math.min(overlapPlayer1Top, overlapPlayer2Top);
        double horizontalOverlap = Math.min(overlapPlayer1Left, overlapPlayer2Left);

        if (verticalOverlap <= horizontalOverlap) {
            if (overlapPlayer1Top < overlapPlayer2Top) {
                putPlayerOnTop(player1, player2);
            } else {
                putPlayerOnTop(player2, player1);
            }
        } else {
            if (overlapPlayer1Left < overlapPlayer2Left) {
                separatePlayersHorizontally(player1, player2, overlapPlayer1Left);
            } else {
                separatePlayersHorizontally(player2, player1, overlapPlayer2Left);
            }
        }
    }

    private void putPlayerOnTop(player topPlayer, player bottomPlayer) {
        topPlayer.y = bottomPlayer.y - topPlayer.height;
        topPlayer.velocityY = 0;
        topPlayer.onGround = true;
    }

    private void separatePlayersHorizontally(player leftPlayer, player rightPlayer, double overlap) {
        boolean leftMovingRight = leftPlayer.velocityX > 0;
        boolean rightMovingLeft = rightPlayer.velocityX < 0;

        if (leftMovingRight && !rightMovingLeft) {
            leftPlayer.x -= overlap;
        } else if (rightMovingLeft && !leftMovingRight) {
            rightPlayer.x += overlap;
        } else {
            double halfOverlap = overlap / 2.0;
            leftPlayer.x -= halfOverlap;
            rightPlayer.x += halfOverlap;
        }

        if (leftPlayer.velocityX > 0) {
            leftPlayer.velocityX = 0;
        }

        if (rightPlayer.velocityX < 0) {
            rightPlayer.velocityX = 0;
        }
    }

    private boolean handlePortals(player p) {
        if (p.reachedGate) {
            return false;
        }

        for (Portal portal : portals) {
            if (portal.checkCollision(p)) {
                portal.teleport(p);
                return true;
            }
        }

        return false;
    }
    private void handleGate(player p) {
        if (gate != null && gate.checkCollision(p)) {
            if (p == player1) {
                gate.playerReach(1);
                player1.reachedGate = true;
            } else if (p == player2) {
                gate.playerReach(2);
                player2.reachedGate = true;
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
    public boolean isLevelComplete() {
        return gate != null && gate.isCompleted();
    }

    public Gate getGate() {
        return gate;
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
