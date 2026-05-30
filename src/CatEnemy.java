import java.awt.*;
import java.util.ArrayList;

public class CatEnemy {
    static final double SIZE = 50;

    private static final double WALK_ANIMATION_TIME = 1.0;
    private static final double ATTACK_ANIMATION_TIME = 1.0;
    private static final double ATTACK_RANGE = 200;
    private static final double ATTACK_COOLDOWN = 2.0;
    private static final double WALK_SPEED = 90;
    private static final double FIREBALL_SIZE = 28;
    private static final double FIREBALL_SPEED = 360;
    private static final double PLATFORM_TOLERANCE = 18;
    private static final double STOMP_BOUNCE = 340;
    private static final double STOMP_TOP_TOLERANCE = 10;
    private static final double STOMP_MIN_OVERLAP = 12;
    private static final double DEATH_GRAVITY = 1400;
    private static final double WORLD_WIDTH = 1920;
    private static final double WORLD_HEIGHT = 1080;

    private static Image[] walkFrames;
    private static Image[] attackFrames;

    static void setFrames(Image[] walk, Image[] attack) {
        walkFrames = walk;
        attackFrames = attack;
    }

    static boolean hasFrames() {
        return walkFrames != null && attackFrames != null &&
                walkFrames.length > 0 && attackFrames.length > 0;
    }

    static class Fireball {
        double x;
        double y;
        double width = FIREBALL_SIZE;
        double height = FIREBALL_SIZE;
        double velocityX = -FIREBALL_SPEED;
        double velocityY = 0;
        player target;

        Fireball(double x, double y, player target, boolean facingLeft) {
            this.x = x;
            this.y = y;
            this.target = target;
            velocityX = facingLeft ? -FIREBALL_SPEED : FIREBALL_SPEED;
        }
    }

    static class HitParticle {
        double x;
        double y;
        double velocityX;
        double velocityY;
        double radius;
        double life;
        double maxLife;
        Color color;

        HitParticle(double x, double y, double velocityX, double velocityY,
                    double radius, double maxLife, Color color) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.radius = radius;
            this.life = maxLife;
            this.maxLife = maxLife;
            this.color = color;
        }

        void update(double dt) {
            x += velocityX * dt;
            y += velocityY * dt;
            velocityX *= 0.95;
            velocityY = velocityY * 0.95 + 260 * dt;
            life -= dt;
        }

        boolean isAlive() {
            return life > 0;
        }

        double alphaRatio() {
            if (maxLife <= 0) {
                return 0;
            }

            return Math.max(0, life / maxLife);
        }
    }

    double x;
    double y;
    double width = SIZE;
    double height = SIZE;

    private final double patrolLeft;
    private final double patrolRight;
    private final double platformTop;
    private final ArrayList<Fireball> fireballs = new ArrayList<>();
    private final ArrayList<HitParticle> hitParticles = new ArrayList<>();

    private boolean alive = true;
    private boolean fallingAfterDeath = false;
    private boolean facingLeft = true;
    private boolean attacking = false;
    private boolean attackSoundRequested = false;
    private int direction = -1;
    private int currentFrame = 0;
    private double walkAnimationTimer = 0;
    private double attackTimer = 0;
    private double cooldownTimer = 0;
    private double deathVelocityY = 0;
    private player attackTarget;

    CatEnemy(double patrolLeft, double patrolRight, double platformTop) {
        this.patrolLeft = patrolLeft;
        this.patrolRight = patrolRight;
        this.platformTop = platformTop;
        this.x = patrolLeft + Math.max(0, patrolRight - patrolLeft - width) / 2.0;
        this.y = platformTop - height;
    }

    void update(double dt, player player1, player player2) {
        attackSoundRequested = false;
        updateFireballs(dt, player1, player2);
        updateHitParticles(dt);

        if (!alive) {
            updateDeathFall(dt);
            return;
        }

        if (cooldownTimer > 0) {
            cooldownTimer = Math.max(0, cooldownTimer - dt);
        }

        if (attacking) {
            updateAttack(dt);
            return;
        }

        player target = attackTarget(player1, player2);
        if (target != null && cooldownTimer <= 0) {
            startAttack(target);
            return;
        }

        patrol(dt);
        updateWalkAnimation(dt);
    }

    private void updateDeathFall(double dt) {
        if (!fallingAfterDeath) {
            return;
        }

        deathVelocityY += DEATH_GRAVITY * dt;
        y += deathVelocityY * dt;

        if (y > WORLD_HEIGHT + height) {
            fallingAfterDeath = false;
        }
    }

    private void updateAttack(double dt) {
        attackTimer += dt;
        int frameCount = Math.max(1, attackFrames.length);
        currentFrame = Math.min(frameCount - 1, (int)(attackTimer / (ATTACK_ANIMATION_TIME / frameCount)));

        if (attackTimer >= ATTACK_ANIMATION_TIME) {
            spawnFireball();
            attacking = false;
            attackTimer = 0;
            cooldownTimer = ATTACK_COOLDOWN;
            currentFrame = 0;
        }
    }

    private void startAttack(player target) {
        attacking = true;
        attackTarget = target;
        attackTimer = 0;
        currentFrame = 0;
        attackSoundRequested = true;
    }

    private void spawnFireball() {
        if (!isActivePlayer(attackTarget)) {
            return;
        }

        double fireballX = facingLeft ? x - FIREBALL_SIZE : x + width;
        double fireballY = y + height * 0.42 - FIREBALL_SIZE / 2.0;
        fireballs.add(new Fireball(fireballX, fireballY, attackTarget, facingLeft));
    }

    private void patrol(double dt) {
        x += direction * WALK_SPEED * dt;
        facingLeft = direction < 0;

        if (x <= patrolLeft) {
            x = patrolLeft;
            direction = 1;
            facingLeft = false;
        } else if (x + width >= patrolRight) {
            x = patrolRight - width;
            direction = -1;
            facingLeft = true;
        }
    }

    private void updateWalkAnimation(double dt) {
        walkAnimationTimer += dt;
        double frameTime = WALK_ANIMATION_TIME / Math.max(1, walkFrames.length);

        while (walkAnimationTimer >= frameTime) {
            walkAnimationTimer -= frameTime;
            currentFrame = (currentFrame + 1) % walkFrames.length;
        }
    }

    private player attackTarget(player player1, player player2) {
        player best = null;
        double bestDistance = Double.MAX_VALUE;

        player[] players = {player1, player2};
        for (player candidate : players) {
            if (!canAttack(candidate)) {
                continue;
            }

            double distance = horizontalDistanceTo(candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }

        return best;
    }

    private boolean canAttack(player p) {
        if (!isActivePlayer(p) || !isPlayerOnPlatform(p)) {
            return false;
        }

        double distance = horizontalDistanceTo(p);
        if (distance >= ATTACK_RANGE) {
            return false;
        }

        double playerCenter = p.x + p.width / 2.0;
        double catCenter = x + width / 2.0;
        return facingLeft ? playerCenter < catCenter : playerCenter > catCenter;
    }

    private double horizontalDistanceTo(player p) {
        return Math.abs((p.x + p.width / 2.0) - (x + width / 2.0));
    }

    private boolean isPlayerOnPlatform(player p) {
        if (p == null) {
            return false;
        }

        double playerBottom = p.y + p.height;
        boolean onThisHeight = playerBottom >= platformTop - PLATFORM_TOLERANCE &&
                playerBottom <= platformTop + PLATFORM_TOLERANCE;
        boolean onThisSurface = p.x + p.width > patrolLeft && p.x < patrolRight;
        return onThisHeight && onThisSurface;
    }

    private boolean isActivePlayer(player p) {
        return p != null && !p.dead && !p.reachedGate && !p.trappedInFakeGate;
    }

    private void updateFireballs(double dt, player player1, player player2) {
        for (int i = fireballs.size() - 1; i >= 0; i--) {
            Fireball fireball = fireballs.get(i);
            player target = isActivePlayer(fireball.target) ? fireball.target : nearestActivePlayer(fireball, player1, player2);
            fireball.target = target;

            if (target != null) {
                double targetX = target.x + target.width / 2.0;
                double targetY = target.y + target.height / 2.0;
                double centerX = fireball.x + fireball.width / 2.0;
                double centerY = fireball.y + fireball.height / 2.0;
                double dx = targetX - centerX;
                double dy = targetY - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance > 0.001) {
                    fireball.velocityX = dx / distance * FIREBALL_SPEED;
                    fireball.velocityY = dy / distance * FIREBALL_SPEED;
                }
            }

            fireball.x += fireball.velocityX * dt;
            fireball.y += fireball.velocityY * dt;

            if (tryHitPlayer(fireball, player1) || tryHitPlayer(fireball, player2) || isFireballOutsideWorld(fireball)) {
                fireballs.remove(i);
            }
        }
    }

    private player nearestActivePlayer(Fireball fireball, player player1, player player2) {
        player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        player[] players = {player1, player2};

        for (player p : players) {
            if (!isActivePlayer(p)) {
                continue;
            }

            double dx = p.x + p.width / 2.0 - (fireball.x + fireball.width / 2.0);
            double dy = p.y + p.height / 2.0 - (fireball.y + fireball.height / 2.0);
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = p;
            }
        }

        return nearest;
    }

    private boolean tryHitPlayer(Fireball fireball, player p) {
        if (!isActivePlayer(p)) {
            return false;
        }

        if (!CollisionManager.obbCollision(
                fireball.x, fireball.y, fireball.width, fireball.height, fireballAngle(fireball),
                p.x, p.y, p.width, p.height, 0
        )) {
            return false;
        }

        spawnHitParticles(p.x + p.width / 2.0, p.y + p.height / 2.0);
        p.die();
        return true;
    }

    private boolean isFireballOutsideWorld(Fireball fireball) {
        return fireball.x < -80 || fireball.x > WORLD_WIDTH + 80 ||
                fireball.y < -80 || fireball.y > WORLD_HEIGHT + 80;
    }

    private double fireballAngle(Fireball fireball) {
        return CollisionManager.angleFromVelocity(fireball.velocityX, fireball.velocityY);
    }

    private void spawnHitParticles(double centerX, double centerY) {
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 80 + Math.random() * 180;
            Color color = i % 2 == 0
                    ? new Color(255, 95, 40)
                    : new Color(255, 210, 70);
            hitParticles.add(new HitParticle(
                    centerX,
                    centerY,
                    Math.cos(angle) * speed,
                    Math.sin(angle) * speed,
                    4 + Math.random() * 5,
                    0.35 + Math.random() * 0.25,
                    color
            ));
        }
    }

    private void updateHitParticles(double dt) {
        for (int i = hitParticles.size() - 1; i >= 0; i--) {
            HitParticle particle = hitParticles.get(i);
            particle.update(dt);

            if (!particle.isAlive()) {
                hitParticles.remove(i);
            }
        }
    }

    Enemy.ContactResult handleCollision(player p) {
        if (!alive || !isActivePlayer(p)) {
            return Enemy.ContactResult.NONE;
        }

        if (isStompedBy(p)) {
            kill();
            p.y = y - p.height;
            p.velocityY = -STOMP_BOUNCE;
            p.onGround = false;
            return Enemy.ContactResult.ENEMY_KILLED;
        }

        if (!CollisionManager.rectCollision(p.x, p.y, p.width, p.height, x, y, width, height)) {
            return Enemy.ContactResult.NONE;
        }

        p.die();
        return Enemy.ContactResult.PLAYER_EATEN;
    }

    boolean killByProjectile() {
        if (!alive) {
            return false;
        }

        kill();
        return true;
    }

    private void kill() {
        alive = false;
        fallingAfterDeath = true;
        attacking = false;
        deathVelocityY = 0;
        fireballs.clear();
    }

    private boolean isStompedBy(player p) {
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
        boolean crossedCatTop = previousBottom <= y + STOMP_TOP_TOLERANCE && currentBottom >= y;
        boolean overlapsCat = horizontalOverlap >= STOMP_MIN_OVERLAP;
        return falling && crossedCatTop && overlapsCat;
    }

    boolean canCollide() {
        return alive;
    }

    boolean isActive() {
        return alive || fallingAfterDeath || !fireballs.isEmpty() || !hitParticles.isEmpty();
    }

    boolean isFallingAfterDeath() {
        return fallingAfterDeath;
    }

    boolean isFacingLeft() {
        return facingLeft;
    }

    boolean isAttacking() {
        return attacking;
    }

    boolean consumeAttackSoundRequest() {
        if (!attackSoundRequested) {
            return false;
        }

        attackSoundRequested = false;
        return true;
    }

    Image getCurrentImage() {
        Image[] frames = attacking ? attackFrames : walkFrames;
        if (frames == null || frames.length == 0) {
            return null;
        }

        return frames[currentFrame % frames.length];
    }

    ArrayList<Fireball> getFireballs() {
        return fireballs;
    }

    ArrayList<HitParticle> getHitParticles() {
        return hitParticles;
    }
}
