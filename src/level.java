import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public abstract class level {
    private static final double RESPAWN_DELAY = 1.0;

    Image backgroundImage;

    player player1;
    player player2;

    ArrayList<Platform> platforms = new ArrayList<>();
    ArrayList<Trap> traps = new ArrayList<>();
    ArrayList<Portal> portals = new ArrayList<>();
    ArrayList<PortalParticle> portalParticles = new ArrayList<>();
    ArrayList<WindVent> windVents = new ArrayList<>();
    ArrayList<Enemy> enemies = new ArrayList<>();
    ArrayList<SurfaceSegment> enemyPlatformSegments = new ArrayList<>();
    Gate gate;
    private boolean playerTeleportedThisFrame;
    private boolean player1EatenThisFrame;
    private boolean player2EatenThisFrame;
    private boolean player1FakeGateEatenThisFrame;
    private boolean player2FakeGateEatenThisFrame;
    private boolean enemyDiedThisFrame;
    private int enemiesKilledThisFrame;
    private int player1EnemyKillsThisFrame;
    private int player2EnemyKillsThisFrame;

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
            Image[] sawFrames,
            Image pitImage,
            Image knifeImage,
            Image[] portalImage,
            Image[] gateImage,
            Image[] enemyIdleFrames,
            Image[] enemyLeftFrames
    );

    public void update(double dt) {
        playerTeleportedThisFrame = false;
        player1EatenThisFrame = false;
        player2EatenThisFrame = false;
        player1FakeGateEatenThisFrame = false;
        player2FakeGateEatenThisFrame = false;
        enemyDiedThisFrame = false;
        enemiesKilledThisFrame = 0;
        player1EnemyKillsThisFrame = 0;
        player2EnemyKillsThisFrame = 0;

        updateDeadPlayers(dt);

        updateWindVents(dt);

        if (isPlayerActive(player1)) {
            player1.updatePlayer(dt);
        }

        if (isPlayerActive(player2)) {
            player2.updatePlayer(dt);
        }

        updatePlatforms(dt);

        for (Portal portal : portals) {
            portal.update(dt);
        }

        updatePortalParticles(dt);

        boolean player1Teleported = isPlayerActive(player1) && handlePortals(player1);
        boolean player2Teleported = isPlayerActive(player2) && handlePortals(player2);

        updateEnemies(dt);

        if (isPlayerActive(player1)) {
            handlePlatformCollision(player1);
        }

        if (isPlayerActive(player2)) {
            handlePlatformCollision(player2);
        }

        handlePlayerCollision();

        if (!player1Teleported && isPlayerActive(player1)) {
            handlePortals(player1);
        }

        if (!player2Teleported && isPlayerActive(player2)) {
            handlePortals(player2);
        }

        for (Trap trap : traps) {

            if (!trap.isActive()) {
                continue;
            }

            trap.update(dt, player1, player2);
            collectFakeGateEvents(trap);

            if (isPlayerActive(player1) && trap.checkCollision(player1)) {
                trap.onCollide(player1);
                if (trap instanceof FakeGateTrap) {
                    ((FakeGateTrap)trap).setTrappedPlayerNumber(1);
                }
            }

            if (isPlayerActive(player2) && trap.checkCollision(player2)) {
                trap.onCollide(player2);
                if (trap instanceof FakeGateTrap) {
                    ((FakeGateTrap)trap).setTrappedPlayerNumber(2);
                }
            }
        }

        if (isPlayerActive(player1)) {
            handleGate(player1);
        }

        if (isPlayerActive(player2)) {
            handleGate(player2);
        }

        if (gate != null) {
            gate.update(dt);
        }

    }

    private boolean isPlayerActive(player p) {
        return p != null && !p.dead && !p.reachedGate && !p.trappedInFakeGate;
    }

    private void collectFakeGateEvents(Trap trap) {
        if (!(trap instanceof FakeGateTrap)) {
            return;
        }

        FakeGateTrap fakeGate = (FakeGateTrap)trap;

        if (fakeGate.didEatPlayer(1)) {
            player1FakeGateEatenThisFrame = true;
        }

        if (fakeGate.didEatPlayer(2)) {
            player2FakeGateEatenThisFrame = true;
        }
    }

    private void updateDeadPlayers(double dt) {
        updateDeadPlayer(player1, spawnX1, spawnY1, dt);
        updateDeadPlayer(player2, spawnX2, spawnY2, dt);
    }

    private void updateDeadPlayer(player p, double spawnX, double spawnY, double dt) {
        if (p == null || !p.dead) {
            return;
        }

        p.updateDeadTimer(dt);

        if (p.deadTimer >= RESPAWN_DELAY) {
            p.respawn(spawnX, spawnY);
        }
    }

    private void handlePlatformCollision(player p) {

        p.onGround = false;

        for (Platform platform : platforms) {
            for (Rectangle2D.Double bounds : platform.getCollisionBounds()) {
                if (!CollisionManager.rectCollision(
                        p.x, p.y, p.width, p.height,
                        bounds.x, bounds.y, bounds.width, bounds.height
                )) {
                    continue;
                }

                double playerBottom = p.y + p.height;
                double playerTop = p.y;
                double playerLeft = p.x;
                double playerRight = p.x + p.width;

                double platformBottom = bounds.y + bounds.height;
                double platformTop = bounds.y;
                double platformLeft = bounds.x;
                double platformRight = bounds.x + bounds.width;

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

    private void updatePlatforms(double dt) {
        for (Platform platform : platforms) {
            platform.update(dt, player1, player2);
        }
    }

    private void updateWindVents(double dt) {
        for (WindVent windVent : windVents) {
            windVent.update(dt, player1, player2);
        }
    }

    private void updateEnemies(double dt) {
        for (Enemy enemy : enemies) {
            enemy.update(dt, player1, player2);

            handleEnemyCollision(enemy, player1, 1);
            handleEnemyCollision(enemy, player2, 2);
        }
    }

    private void handleEnemyCollision(Enemy enemy, player p, int playerNumber) {
        if (!enemy.canCollide() || !isPlayerActive(p)) {
            return;
        }

        Enemy.ContactResult result = enemy.handleCollision(p);

        if (result == Enemy.ContactResult.PLAYER_EATEN) {
            if (playerNumber == 1) {
                player1EatenThisFrame = true;
            } else {
                player2EatenThisFrame = true;
            }
        } else if (result == Enemy.ContactResult.ENEMY_KILLED) {
            enemyDiedThisFrame = true;
            enemiesKilledThisFrame++;

            if (playerNumber == 1) {
                player1EnemyKillsThisFrame++;
            } else {
                player2EnemyKillsThisFrame++;
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
        if (!isPlayerActive(p)) {
            return false;
        }

        for (Portal portal : portals) {
            if (portal.checkCollision(p)) {
                createPortalParticles(portal.x + portal.width / 2, portal.y + portal.height / 2);
                portal.teleport(p);
                playerTeleportedThisFrame = true;
                createPortalParticles(p.x + p.width / 2, p.y + p.height / 2);
                return true;
            }
        }

        return false;
    }
    private void handleGate(player p) {
        if (!isPlayerActive(p)) {
            return;
        }

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

    public boolean didTeleport() {
        return playerTeleportedThisFrame;
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

    public ArrayList<PortalParticle> getPortalParticles() {
        return portalParticles;
    }

    public ArrayList<WindVent> getWindVents() {
        return windVents;
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public boolean wasPlayerEaten(int playerNumber) {
        if (playerNumber == 1) {
            return player1EatenThisFrame;
        }

        if (playerNumber == 2) {
            return player2EatenThisFrame;
        }

        return false;
    }

    public boolean wasPlayerEatenByFakeGate(int playerNumber) {
        if (playerNumber == 1) {
            return player1FakeGateEatenThisFrame;
        }

        if (playerNumber == 2) {
            return player2FakeGateEatenThisFrame;
        }

        return false;
    }

    public boolean didEnemyDie() {
        return enemyDiedThisFrame;
    }

    public int getEnemiesKilledThisFrame() {
        return enemiesKilledThisFrame;
    }

    public int getEnemyKillsForPlayer(int playerNumber) {
        if (playerNumber == 1) {
            return player1EnemyKillsThisFrame;
        }

        if (playerNumber == 2) {
            return player2EnemyKillsThisFrame;
        }

        return 0;
    }

    protected void clearLevelObjects() {
        platforms.clear();
        traps.clear();
        portals.clear();
        portalParticles.clear();
        windVents.clear();
        enemies.clear();
        enemyPlatformSegments.clear();
    }

    protected void registerEnemyPlatform(double x, double y, int blocks) {
        if (blocks <= 0) {
            return;
        }

        enemyPlatformSegments.add(new SurfaceSegment(x, x + blocks * Platform.TILE_SIZE, y));
    }

    protected void addEnemiesToHalfPlatforms(Image[] enemyIdleFrames, Image[] enemyLeftFrames) {
        enemies.clear();

        if (enemyIdleFrames == null || enemyLeftFrames == null ||
                enemyIdleFrames.length == 0 || enemyLeftFrames.length == 0) {
            return;
        }

        ArrayList<SurfaceSegment> segments = enemyPlatformSegments.isEmpty()
                ? buildSurfaceSegments()
                : new ArrayList<>(enemyPlatformSegments);
        ArrayList<SurfaceSegment> eligibleSegments = new ArrayList<>();

        for (SurfaceSegment segment : segments) {
            if (isEnemyEligibleSegment(segment)) {
                eligibleSegments.add(segment);
            }
        }

        if (eligibleSegments.isEmpty()) {
            return;
        }

        int enemyCount = Math.max(1, eligibleSegments.size() / 2);

        for (int i = 0; i < eligibleSegments.size() && enemies.size() < enemyCount; i += 2) {
            addEnemyOnSegment(eligibleSegments.get(i), enemyIdleFrames, enemyLeftFrames);
        }

        for (int i = 1; i < eligibleSegments.size() && enemies.size() < enemyCount; i += 2) {
            addEnemyOnSegment(eligibleSegments.get(i), enemyIdleFrames, enemyLeftFrames);
        }
    }

    private ArrayList<SurfaceSegment> buildSurfaceSegments() {
        ArrayList<Rectangle2D.Double> bounds = new ArrayList<>();

        for (Platform platform : platforms) {
            if (platform instanceof BreakawayPitPlatform) {
                continue;
            }

            bounds.addAll(platform.getCollisionBounds());
        }

        bounds.sort((a, b) -> {
            int yCompare = Double.compare(a.y, b.y);

            if (yCompare != 0) {
                return yCompare;
            }

            return Double.compare(a.x, b.x);
        });

        ArrayList<SurfaceSegment> segments = new ArrayList<>();

        for (Rectangle2D.Double bound : bounds) {
            if (bound.width <= 0 || bound.height <= 0) {
                continue;
            }

            if (segments.isEmpty()) {
                segments.add(new SurfaceSegment(bound.x, bound.x + bound.width, bound.y));
                continue;
            }

            SurfaceSegment current = segments.get(segments.size() - 1);

            if (Math.abs(current.top - bound.y) < 0.01 &&
                    Math.abs(current.right - bound.x) < 0.01) {
                current.right = bound.x + bound.width;
            } else {
                segments.add(new SurfaceSegment(bound.x, bound.x + bound.width, bound.y));
            }
        }

        return segments;
    }

    private boolean isEnemyEligibleSegment(SurfaceSegment segment) {
        if (segment.width() < 100) {
            return false;
        }

        return !isPointStandingOnSegment(spawnX1, spawnY1, segment) &&
                !isPointStandingOnSegment(spawnX2, spawnY2, segment) &&
                !isPointStandingOnSegment(goalX, goalY, segment);
    }

    private boolean isPointStandingOnSegment(double x, double y, SurfaceSegment segment) {
        double bottom = y + 50;

        return bottom >= segment.top - 1 &&
                bottom <= segment.top + 1 &&
                x + 50 > segment.left &&
                x < segment.right;
    }

    private void addEnemyOnSegment(SurfaceSegment segment, Image[] enemyIdleFrames, Image[] enemyLeftFrames) {
        enemies.add(new Enemy(segment.left, segment.right, segment.top, enemyIdleFrames, enemyLeftFrames));
    }

    private static class SurfaceSegment {
        double left;
        double right;
        double top;

        SurfaceSegment(double left, double right, double top) {
            this.left = left;
            this.right = right;
            this.top = top;
        }

        double width() {
            return right - left;
        }
    }

    private void updatePortalParticles(double dt) {
        for (int i = portalParticles.size() - 1; i >= 0; i--) {
            PortalParticle particle = portalParticles.get(i);
            particle.update(dt);

            if (!particle.isAlive()) {
                portalParticles.remove(i);
            }
        }
    }

    private void createPortalParticles(double centerX, double centerY) {
        for (int i = 0; i < 24; i++) {
            double angle = Math.PI * 2 * i / 24.0 + Math.random() * 0.35;
            double speed = 90 + Math.random() * 170;
            double radius = 4 + Math.random() * 6;
            double life = 0.35 + Math.random() * 0.35;
            Color color = i % 2 == 0
                    ? new Color(90, 220, 255)
                    : new Color(255, 120, 240);

            portalParticles.add(new PortalParticle(
                    centerX,
                    centerY,
                    Math.cos(angle) * speed,
                    Math.sin(angle) * speed,
                    radius,
                    life,
                    color
            ));
        }
    }
}
