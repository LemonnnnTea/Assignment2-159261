import java.awt.*;

// Standard platform enemy: patrols, chases players on its own surface, and dies to stomps/projectiles.
public class Enemy {
    private static final double SIZE = 50;
    private static final double WORLD_HEIGHT = 1080;
    private static final double PATROL_SPEED = 85;
    private static final double CHASE_SPEED = 135;
    private static final double DEATH_GRAVITY = 1400;
    private static final double EDGE_PAUSE_TIME = 1.0;
    private static final double ANIMATION_SPEED = 0.12;
    private static final double PLATFORM_TOLERANCE = 18;
    private static final double STOMP_BOUNCE = 330;
    private static final double STOMP_TOP_TOLERANCE = 10;
    private static final double STOMP_MIN_OVERLAP = 12;

    enum ContactResult {
        NONE,
        PLAYER_EATEN,
        ENEMY_KILLED
    }

    double x;
    double y;
    double width = SIZE;
    double height = SIZE;

    private final double patrolLeft;
    private final double patrolRight;
    private final double platformTop;
    private final Image[] idleFrames;
    private final Image[] walkLeftFrames;

    private boolean alive = true;
    private boolean fallingAfterDeath = false;
    private boolean facingLeft = true;
    private boolean pausedAtEdge = false;
    private double pauseTimer = 0;
    private double deathVelocityY = 0;
    private int direction = -1;
    private int currentFrame = 0;
    private double animationTimer = 0;
    private boolean moving = false;

    public Enemy(double patrolLeft,
                 double patrolRight,
                 double platformTop,
                 Image[] idleFrames,
                 Image[] walkLeftFrames) {
        this.patrolLeft = patrolLeft;
        this.patrolRight = patrolRight;
        this.platformTop = platformTop;
        this.idleFrames = idleFrames;
        this.walkLeftFrames = walkLeftFrames;
        this.x = patrolLeft + (patrolRight - patrolLeft - width) / 2.0;
        this.y = platformTop - height;
    }

    public void update(double dt, player player1, player player2) {
        if (!alive) {
            updateDeathFall(dt);
            return;
        }

        player target = chooseTarget(player1, player2);

        if (target != null) {
            chase(target, dt);
        } else {
            patrol(dt);
        }

        updateAnimation(dt);
    }

    private void updateDeathFall(double dt) {
        if (!fallingAfterDeath) {
            return;
        }

        deathVelocityY += DEATH_GRAVITY * dt;
        y += deathVelocityY * dt;
        moving = false;

        if (y + height >= WORLD_HEIGHT) {
            y = WORLD_HEIGHT - height;
            fallingAfterDeath = false;
        }
    }

    private player chooseTarget(player player1, player player2) {
        // Keep targeting local to the platform so enemies do not chase through unrelated routes.
        boolean player1OnPlatform = isPlayerOnPlatform(player1);
        boolean player2OnPlatform = isPlayerOnPlatform(player2);

        if (!player1OnPlatform && !player2OnPlatform) {
            return null;
        }

        if (player1OnPlatform && !player2OnPlatform) {
            return player1;
        }

        if (player2OnPlatform && !player1OnPlatform) {
            return player2;
        }

        double enemyCenter = x + width / 2.0;
        double player1Distance = Math.abs((player1.x + player1.width / 2.0) - enemyCenter);
        double player2Distance = Math.abs((player2.x + player2.width / 2.0) - enemyCenter);

        return player1Distance <= player2Distance ? player1 : player2;
    }

    private boolean isPlayerOnPlatform(player p) {
        if (p == null || p.dead || p.reachedGate) {
            return false;
        }

        double playerBottom = p.y + p.height;
        boolean onThisHeight = playerBottom >= platformTop - PLATFORM_TOLERANCE &&
                playerBottom <= platformTop + PLATFORM_TOLERANCE;
        boolean onThisSurface = p.x + p.width > patrolLeft && p.x < patrolRight;

        return onThisHeight && onThisSurface;
    }

    private void chase(player target, double dt) {
        pausedAtEdge = false;
        pauseTimer = 0;

        double targetCenter = target.x + target.width / 2.0;
        double enemyCenter = x + width / 2.0;
        double delta = targetCenter - enemyCenter;

        if (Math.abs(delta) < 2.0) {
            moving = false;
            return;
        }

        direction = delta < 0 ? -1 : 1;
        facingLeft = direction < 0;
        moving = true;
        x += direction * CHASE_SPEED * dt;
        clampToPatrol();
    }

    private void patrol(double dt) {
        if (pausedAtEdge) {
            moving = false;
            pauseTimer += dt;

            if (pauseTimer >= EDGE_PAUSE_TIME) {
                pausedAtEdge = false;
                pauseTimer = 0;
                direction *= -1;
                facingLeft = direction < 0;
            }

            return;
        }

        moving = true;
        facingLeft = direction < 0;
        x += direction * PATROL_SPEED * dt;

        if (x <= patrolLeft) {
            x = patrolLeft;
            pauseAtEdge();
        } else if (x + width >= patrolRight) {
            x = patrolRight - width;
            pauseAtEdge();
        }
    }

    private void pauseAtEdge() {
        pausedAtEdge = true;
        pauseTimer = 0;
        moving = false;
    }

    private void clampToPatrol() {
        if (x < patrolLeft) {
            x = patrolLeft;
        }

        if (x + width > patrolRight) {
            x = patrolRight - width;
        }
    }

    public ContactResult handleCollision(player p) {
        if (!alive || p == null || p.dead || p.reachedGate) {
            return ContactResult.NONE;
        }

        if (isStompedBy(p)) {
            alive = false;
            fallingAfterDeath = true;
            deathVelocityY = 0;
            moving = false;
            p.y = y - p.height;
            p.velocityY = -STOMP_BOUNCE;
            p.onGround = false;
            return ContactResult.ENEMY_KILLED;
        }

        if (!isPlayerOnPlatform(p)) {
            return ContactResult.NONE;
        }

        if (!CollisionManager.rectCollision(p.x, p.y, p.width, p.height, x, y, width, height)) {
            return ContactResult.NONE;
        }

        p.die();
        return ContactResult.PLAYER_EATEN;
    }

    public boolean killByProjectile() {
        if (!alive) {
            return false;
        }

        alive = false;
        fallingAfterDeath = true;
        deathVelocityY = 0;
        moving = false;
        return true;
    }

    private boolean isStompedBy(player p) {
        // Sweep from previous to current position so fast falling players still get fair stomp hits.
        double previousBottom = p.previousY + p.height;
        double currentBottom = p.y + p.height;

        double previousLeft = p.previousX;
        double previousRight = p.previousX + p.width;
        double currentLeft = p.x;
        double currentRight = p.x + p.width;
        double sweepLeft = Math.min(previousLeft, currentLeft);
        double sweepRight = Math.max(previousRight, currentRight);
        double horizontalOverlap = Math.min(sweepRight, x + width) - Math.max(sweepLeft, x);

        boolean falling = p.velocityY >= 0;
        boolean crossedEnemyTop = previousBottom <= y + STOMP_TOP_TOLERANCE && currentBottom >= y;
        boolean overlapsEnemy = horizontalOverlap >= STOMP_MIN_OVERLAP;

        return falling && crossedEnemyTop && overlapsEnemy;
    }

    private void updateAnimation(double dt) {
        animationTimer += dt;

        if (animationTimer >= ANIMATION_SPEED) {
            currentFrame = (currentFrame + 1) % 5;
            animationTimer = 0;
        }
    }

    public boolean isActive() {
        return alive || fallingAfterDeath;
    }

    public boolean canCollide() {
        return alive;
    }

    public boolean isFallingAfterDeath() {
        return fallingAfterDeath;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    public Image getCurrentImage() {
        Image[] frames = moving ? walkLeftFrames : idleFrames;
        return frames[currentFrame % frames.length];
    }
}
