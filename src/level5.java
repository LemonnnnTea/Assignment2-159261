import java.awt.*;
import java.util.ArrayList;

public class level5 extends level {
    private static final double LOCK_LOSS_COSINE = 0.70710678;

    static final double SPLIT_X = 960;
    static final double GRID_X = 30;
    static final double GRID_Y = 40;
    static final int GRID_COLS = 18;
    static final int GRID_ROWS = 20;
    static final int CELL = 50;

    static final double BATTLE_X = 1080;
    static final double BATTLE_START_Y = 500;
    static final double BOSS_X = 1700;
    static final double BOSS_Y = 350;
    static final double BOSS_W = 160;
    static final double BOSS_H = 260;
    static final double BOSS_DRAW_X = 1660;
    static final double BOSS_DRAW_Y = 350;
    static final double BOSS_DRAW_W = 220;
    static final double BOSS_DRAW_H = 220;
    static final double BOSS_MOUTH_X = BOSS_DRAW_X + BOSS_DRAW_W * 0.20;
    static final double BOSS_MOUTH_Y = BOSS_DRAW_Y + BOSS_DRAW_H * 0.62;
    static final int MAX_LIVES = 3;
    static final int MAX_MISSILES = 5;
    static final double MAX_BOSS_HP = 1200;
    static final double STAGE3_INTRO_DURATION = 3.0;
    static final String STAGE3_MESSAGE = "The Stage3 is coming, all controls will be reserved.";
    static final double BOSS_EXPLOSION_DURATION = 3.0;
    private static final double BOSS_ATTACK_FRAME_TIME = 0.14;
    private static final int BOSS_ATTACK_FRAME_COUNT = 5;
    private static final int BOSS_ATTACK_MISSILE_FRAME = 3;
    private static final double BOSS_ATTACK_MISSILE_DELAY = BOSS_ATTACK_FRAME_TIME * BOSS_ATTACK_MISSILE_FRAME;

    enum ItemType {
        MISSILE,
        CURSED_MISSILE,
        HEAL,
        SHIELD,
        RAGE
    }

    static class GridPoint {
        int col;
        int row;

        GridPoint(int col, int row) {
            this.col = col;
            this.row = row;
        }

        boolean sameCell(GridPoint other) {
            return other != null && col == other.col && row == other.row;
        }
    }

    static class SupplyItem {
        ItemType type;
        int col;
        int row;

        SupplyItem(ItemType type, int col, int row) {
            this.type = type;
            this.col = col;
            this.row = row;
        }

        double x() {
            return GRID_X + col * CELL;
        }

        double y() {
            return GRID_Y + row * CELL;
        }
    }

    static class Shot {
        double x, y, width, height;
        double vx, vy;
        double damage;
        boolean missile;
        boolean locked = true;
        boolean selfTargeting = false;
        boolean looping = false;
        double age = 0;
        double loopCenterX = 0;
        double loopCenterY = 0;
        double loopRadius = 55;
        double loopDuration = 0.85;
        double loopStartAngle = 0;

        Shot(double x, double y, double width, double height, double vx, double vy, double damage, boolean missile) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.vx = vx;
            this.vy = vy;
            this.damage = damage;
            this.missile = missile;
        }

        void update(double dt) {
            x += vx * dt;
            y += vy * dt;
        }
    }

    static class FlyingEnemy {
        double x, y;
        double width = 50;
        double height = 50;

        FlyingEnemy(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    static class BossMissile {
        double x, y;
        double width = 50;
        double height = 50;
        double vx = -180;
        double vy = 0;
        double elapsed = 0;
        int phase;
        boolean locked = true;

        BossMissile(double x, double y, int phase) {
            this.x = x;
            this.y = y;
            this.phase = phase;
        }
    }

    static class BossExplosionParticle {
        double x, y;
        double vx, vy;
        double radius;
        double age = 0;
        double life;
        double delay;
        int colorIndex;

        BossExplosionParticle(double x, double y, double vx, double vy, double radius,
                              double life, double delay, int colorIndex) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.radius = radius;
            this.life = life;
            this.delay = delay;
            this.colorIndex = colorIndex;
        }

        void update(double dt) {
            age += dt;

            if (age < delay) {
                return;
            }

            x += vx * dt;
            y += vy * dt;
            vx *= 0.98;
            vy = vy * 0.98 + 120 * dt;
        }

        double alphaRatio() {
            if (age < delay) {
                return 0;
            }

            double activeAge = age - delay;
            if (activeAge >= life) {
                return 0;
            }

            return 1.0 - activeAge / life;
        }
    }

    ArrayList<GridPoint> greedyPig = new ArrayList<>();
    ArrayList<SupplyItem> supplyItems = new ArrayList<>();
    ArrayList<Shot> knives = new ArrayList<>();
    ArrayList<Shot> playerMissiles = new ArrayList<>();
    ArrayList<Shot> cursedMissiles = new ArrayList<>();
    ArrayList<FlyingEnemy> flyingEnemies = new ArrayList<>();
    ArrayList<BossMissile> bossMissiles = new ArrayList<>();
    ArrayList<Boolean> missileInventory = new ArrayList<>();
    ArrayList<BossExplosionParticle> bossExplosionParticles = new ArrayList<>();

    double bossHp = MAX_BOSS_HP;
    int missileAmmo = 0;
    int battleLives = MAX_LIVES;
    boolean shield = false;
    double rageTimer = 0;
    double greedyStunTimer = 0;
    double battleY = BATTLE_START_Y;
    double battleVelocityY = 0;
    double invincibleTimer = 0;
    double noFireTimer = 0;
    boolean jetpackPressed = false;
    boolean gameOver = false;
    boolean complete = false;

    private Image knifeImage;
    private Image[] enemyIdleFrames;
    private int dirCol = 1;
    private int dirRow = 0;
    private int nextDirCol = 1;
    private int nextDirRow = 0;
    private double greedyMoveTimer = 0;
    private double supplySpawnTimer = 0;
    private double knifeFireTimer = 0;
    private double missileCooldownTimer = 0;
    private double enemySpawnTimer = 0;
    private double bossMissileTimer = 0;
    private boolean stage3IntroStarted = false;
    private boolean stage3IntroActive = false;
    private double stage3IntroTimer = 0;
    private boolean stage3SoundRequested = false;
    private boolean bossAttackAnimating = false;
    private double bossAttackAnimationTimer = 0;
    private int bossAttackFrame = 0;
    private boolean bossAttackMissileFired = false;
    private boolean bossExplosionActive = false;
    private double bossExplosionTimer = 0;
    private boolean enemyDiedThisFrame = false;
    private int enemyKillsThisFrame = 0;
    private int lifeLossesThisFrame = 0;

    public level5(Image backgroundImage) {
        super(backgroundImage);
    }

    @Override
    public void load(
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
    ) {
        this.player1 = player1;
        this.player2 = player2;
        this.knifeImage = knifeImage;
        this.enemyIdleFrames = enemyIdleFrames;

        clearLevelObjects();
        gate = null;
        resetBossFight();
    }

    private void resetBossFight() {
        bossHp = MAX_BOSS_HP;
        missileAmmo = 0;
        battleLives = MAX_LIVES;
        shield = false;
        rageTimer = 0;
        greedyStunTimer = 0;
        battleY = BATTLE_START_Y;
        battleVelocityY = 0;
        invincibleTimer = 0;
        noFireTimer = 0;
        jetpackPressed = false;
        gameOver = false;
        complete = false;
        dirCol = 1;
        dirRow = 0;
        nextDirCol = 1;
        nextDirRow = 0;
        greedyMoveTimer = 0;
        supplySpawnTimer = 0;
        knifeFireTimer = 0;
        missileCooldownTimer = 0;
        enemySpawnTimer = 0;
        bossMissileTimer = 0;
        stage3IntroStarted = false;
        stage3IntroActive = false;
        stage3IntroTimer = 0;
        stage3SoundRequested = false;
        bossAttackAnimating = false;
        bossAttackAnimationTimer = 0;
        bossAttackFrame = 0;
        bossAttackMissileFired = false;
        bossExplosionActive = false;
        bossExplosionTimer = 0;

        supplyItems.clear();
        knives.clear();
        playerMissiles.clear();
        cursedMissiles.clear();
        flyingEnemies.clear();
        bossMissiles.clear();
        missileInventory.clear();
        bossExplosionParticles.clear();

        resetGreedyPig();
        spawnMissingSupplies();
        placePlayers();
    }

    private void placePlayers() {
        if (player1 != null) {
            GridPoint head = greedyPig.isEmpty() ? new GridPoint(8, 10) : greedyPig.get(0);
            player1.x = GRID_X + head.col * CELL;
            player1.y = GRID_Y + head.row * CELL;
            player1.velocityX = 0;
            player1.velocityY = 0;
            player1.dead = false;
            player1.reachedGate = false;
            player1.trappedInFakeGate = false;
            player1.onGround = true;
            player1.faceRight = dirCol >= 0;
        }

        if (player2 != null) {
            player2.x = BATTLE_X;
            player2.y = battleY;
            player2.velocityX = 0;
            player2.velocityY = 0;
            player2.dead = false;
            player2.reachedGate = false;
            player2.trappedInFakeGate = false;
            player2.onGround = false;
            player2.faceRight = true;
        }
    }

    @Override
    public void update(double dt) {
        enemyDiedThisFrame = false;
        enemyKillsThisFrame = 0;
        lifeLossesThisFrame = 0;

        if (gameOver || complete) {
            return;
        }

        if (bossExplosionActive) {
            updateBossExplosion(dt);
            jetpackPressed = false;
            placePlayers();
            return;
        }

        if (stage3IntroActive) {
            stage3IntroTimer = Math.max(0, stage3IntroTimer - dt);
            jetpackPressed = false;
            placePlayers();

            if (stage3IntroTimer <= 0) {
                stage3IntroActive = false;
            }

            return;
        }

        if (rageTimer > 0) {
            rageTimer = Math.max(0, rageTimer - dt);
        }

        if (invincibleTimer > 0) {
            invincibleTimer = Math.max(0, invincibleTimer - dt);
        }

        if (noFireTimer > 0) {
            noFireTimer = Math.max(0, noFireTimer - dt);
        }

        if (missileCooldownTimer > 0) {
            missileCooldownTimer = Math.max(0, missileCooldownTimer - dt);
        }

        updateGreedyPig(dt);
        updateBattlePig(dt);
        updateBossAttacks(dt);
        updatePlayerWeapons(dt);
        updateFlyingEnemies(dt);
        updateBossMissiles(dt);
        handleWeaponCollisions();
        handleBattlePigHazards();

        if (bossHp <= 0) {
            bossHp = 0;
            startBossExplosion();
            placePlayers();
            return;
        }

        if (phase() == 3 && !stage3IntroStarted) {
            startStage3Intro();
        }

        placePlayers();
    }

    private void updateGreedyPig(double dt) {
        if (greedyStunTimer > 0) {
            greedyStunTimer = Math.max(0, greedyStunTimer - dt);
            return;
        }

        supplySpawnTimer += dt;

        if (supplySpawnTimer >= 2.0) {
            supplySpawnTimer = 0;
            spawnMissingSupplies();
        }

        greedyMoveTimer += dt;

        if (greedyMoveTimer < 0.18) {
            return;
        }

        greedyMoveTimer = 0;
        dirCol = nextDirCol;
        dirRow = nextDirRow;

        GridPoint head = greedyPig.get(0);
        GridPoint next = new GridPoint(head.col + dirCol, head.row + dirRow);

        if (isWallHit(next) || isSelfHit(next)) {
            punishGreedyPig();
            return;
        }

        SupplyItem eaten = itemAt(next);
        greedyPig.add(0, next);

        if (eaten != null) {
            applySupply(eaten.type);
            supplyItems.remove(eaten);
            spawnMissingSupplies();
        } else {
            greedyPig.remove(greedyPig.size() - 1);
        }
    }

    private void updateBattlePig(double dt) {
        double acceleration = jetpackPressed ? -1300 : 900;
        battleVelocityY += acceleration * dt;

        if (battleVelocityY > 500) {
            battleVelocityY = 500;
        }

        if (battleVelocityY < -450) {
            battleVelocityY = -450;
        }

        battleY += battleVelocityY * dt;

        if (battleY < 80) {
            battleY = 80;
            battleVelocityY = 0;
        }

        if (battleY > 980) {
            battleY = 980;
            battleVelocityY = 0;
        }
    }

    private void updateBossAttacks(double dt) {
        enemySpawnTimer += dt;
        bossMissileTimer += dt;

        if (enemySpawnTimer >= enemySpawnInterval()) {
            enemySpawnTimer = 0;
            flyingEnemies.add(new FlyingEnemy(1650, 120 + Math.random() * 780));
        }

        updateBossAttackAnimation(dt);

        if (bossAttackAnimating) {
            return;
        }

        double windupStartTime = Math.max(0, bossMissileInterval() - BOSS_ATTACK_MISSILE_DELAY);

        if (bossMissileTimer >= windupStartTime) {
            startBossMissileAnimation();
        }
    }

    private void startBossMissileAnimation() {
        bossAttackAnimating = true;
        bossAttackAnimationTimer = 0;
        bossAttackFrame = 0;
        bossAttackMissileFired = false;
    }

    private void updateBossAttackAnimation(double dt) {
        if (!bossAttackAnimating) {
            bossAttackFrame = 0;
            return;
        }

        bossAttackAnimationTimer += dt;
        bossAttackFrame = Math.min(
                BOSS_ATTACK_FRAME_COUNT - 1,
                (int)(bossAttackAnimationTimer / BOSS_ATTACK_FRAME_TIME)
        );

        if (!bossAttackMissileFired && bossAttackFrame >= BOSS_ATTACK_MISSILE_FRAME) {
            bossMissiles.add(new BossMissile(
                    BOSS_MOUTH_X - 25,
                    BOSS_MOUTH_Y - 25,
                    phase()
            ));
            bossMissileTimer = 0;
            bossAttackMissileFired = true;
        }

        if (bossAttackAnimationTimer >= BOSS_ATTACK_FRAME_TIME * BOSS_ATTACK_FRAME_COUNT) {
            bossAttackAnimating = false;
            bossAttackAnimationTimer = 0;
            bossAttackFrame = 0;
            bossAttackMissileFired = false;
        }
    }

    private void startBossExplosion() {
        if (bossExplosionActive) {
            return;
        }

        bossExplosionActive = true;
        bossExplosionTimer = BOSS_EXPLOSION_DURATION;
        bossAttackAnimating = false;
        bossAttackAnimationTimer = 0;
        bossAttackFrame = 0;
        bossAttackMissileFired = false;
        knives.clear();
        playerMissiles.clear();
        cursedMissiles.clear();
        flyingEnemies.clear();
        bossMissiles.clear();
        bossExplosionParticles.clear();
        spawnBossExplosionParticles();
    }

    private void spawnBossExplosionParticles() {
        double centerX = BOSS_DRAW_X + BOSS_DRAW_W / 2.0;
        double centerY = BOSS_DRAW_Y + BOSS_DRAW_H / 2.0;

        for (int i = 0; i < 90; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 80 + Math.random() * 380;
            double offset = Math.random() * 55;
            double x = centerX + Math.cos(angle) * offset;
            double y = centerY + Math.sin(angle) * offset;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed - 80;
            double radius = 8 + Math.random() * 28;
            double life = 0.65 + Math.random() * 1.05;
            double delay = Math.random() * 1.6;
            bossExplosionParticles.add(new BossExplosionParticle(x, y, vx, vy, radius, life, delay, i % 4));
        }
    }

    private void updateBossExplosion(double dt) {
        bossExplosionTimer = Math.max(0, bossExplosionTimer - dt);

        for (BossExplosionParticle particle : bossExplosionParticles) {
            particle.update(dt);
        }

        if (bossExplosionTimer <= 0) {
            bossExplosionActive = false;
            bossExplosionParticles.clear();
            complete = true;
        }
    }

    private void updatePlayerWeapons(double dt) {
        knifeFireTimer += dt;

        if (knifeFireTimer >= knifeInterval()) {
            knifeFireTimer = 0;
            knives.add(new Shot(1130, battleY, 50, 50, 900, 0, isRaging() ? 15 : 10, false));
        }

        for (int i = knives.size() - 1; i >= 0; i--) {
            Shot shot = knives.get(i);
            shot.update(dt);

            if (shot.x > 1920) {
                knives.remove(i);
            }
        }

        for (int i = playerMissiles.size() - 1; i >= 0; i--) {
            Shot shot = playerMissiles.get(i);
            updatePlayerMissileTracking(shot);
            shot.update(dt);

            if (shot.x > 1940 || shot.x < 940 || shot.y < -80 || shot.y > 1160) {
                playerMissiles.remove(i);
            }
        }

        for (int i = cursedMissiles.size() - 1; i >= 0; i--) {
            Shot missile = cursedMissiles.get(i);
            updateCursedMissile(missile, dt);

            if (missile.x > 1940 || missile.x < 940 || missile.y < -80 || missile.y > 1160) {
                cursedMissiles.remove(i);
            }
        }
    }

    private void updateCursedMissile(Shot missile, double dt) {
        missile.age += dt;

        if (missile.looping) {
            double previousCenterX = missile.x + missile.width / 2.0;
            double previousCenterY = missile.y + missile.height / 2.0;
            double ratio = Math.min(1.0, missile.age / missile.loopDuration);
            double angle = missile.loopStartAngle + ratio * Math.PI * 2.0;
            double centerX = missile.loopCenterX + Math.cos(angle) * missile.loopRadius;
            double centerY = missile.loopCenterY + Math.sin(angle) * missile.loopRadius;

            missile.vx = (centerX - previousCenterX) / Math.max(0.001, dt);
            missile.vy = (centerY - previousCenterY) / Math.max(0.001, dt);
            missile.x = centerX - missile.width / 2.0;
            missile.y = centerY - missile.height / 2.0;

            if (ratio >= 1.0) {
                missile.looping = false;
                missile.age = 0;
                setMissileVelocityTowardBattlePig(missile, 700);
            }

            return;
        }

        if (missile.locked) {
            double targetX = BATTLE_X + 25;
            double targetY = battleY + 25;
            double centerX = missile.x + missile.width / 2.0;
            double centerY = missile.y + missile.height / 2.0;
            double dx = targetX - centerX;
            double dy = targetY - centerY;

            if (isLockAngleExceeded(missile.vx, missile.vy, dx, dy)) {
                missile.locked = false;
            } else {
                setMissileVelocity(missile, dx, dy, 700);
            }
        }

        missile.update(dt);
    }

    private void updatePlayerMissileTracking(Shot missile) {
        if (!missile.locked) {
            return;
        }

        double targetX;
        double targetY;
        BossMissile targetMissile = nearestBossMissile(missile);

        if (targetMissile != null) {
            targetX = targetMissile.x + targetMissile.width / 2.0;
            targetY = targetMissile.y + targetMissile.height / 2.0;
        } else {
            targetX = BOSS_X + BOSS_W / 2.0;
            targetY = BOSS_Y + BOSS_H / 2.0;
        }

        double centerX = missile.x + missile.width / 2.0;
        double centerY = missile.y + missile.height / 2.0;
        double dx = targetX - centerX;
        double dy = targetY - centerY;
        double distance = Math.max(1, Math.sqrt(dx * dx + dy * dy));

        if (isLockAngleExceeded(missile.vx, missile.vy, dx, dy)) {
            missile.locked = false;
            return;
        }

        missile.vx = dx / distance * 700;
        missile.vy = dy / distance * 700;
    }

    private BossMissile nearestBossMissile(Shot missile) {
        BossMissile nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        double missileCenterX = missile.x + missile.width / 2.0;
        double missileCenterY = missile.y + missile.height / 2.0;

        for (BossMissile bossMissile : bossMissiles) {
            double bossMissileCenterX = bossMissile.x + bossMissile.width / 2.0;
            double bossMissileCenterY = bossMissile.y + bossMissile.height / 2.0;
            double dx = bossMissileCenterX - missileCenterX;
            double dy = bossMissileCenterY - missileCenterY;
            double distance = dx * dx + dy * dy;

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = bossMissile;
            }
        }

        return nearest;
    }

    private void setMissileVelocityTowardBattlePig(Shot missile, double speed) {
        double centerX = missile.x + missile.width / 2.0;
        double centerY = missile.y + missile.height / 2.0;
        double dx = BATTLE_X + 25 - centerX;
        double dy = battleY + 25 - centerY;

        setMissileVelocity(missile, dx, dy, speed);
    }

    private void setMissileVelocity(Shot missile, double dx, double dy, double speed) {
        double distance = Math.max(1, Math.sqrt(dx * dx + dy * dy));
        missile.vx = dx / distance * speed;
        missile.vy = dy / distance * speed;
    }

    private void updateFlyingEnemies(double dt) {
        double targetX = BATTLE_X + 25;
        double targetY = battleY + 25;

        for (FlyingEnemy enemy : flyingEnemies) {
            double centerX = enemy.x + enemy.width / 2.0;
            double centerY = enemy.y + enemy.height / 2.0;
            double dx = targetX - centerX;
            double dy = targetY - centerY;
            double distance = Math.max(1, Math.sqrt(dx * dx + dy * dy));
            double speed = enemySpeed();

            enemy.x += dx / distance * speed * dt;
            enemy.y += dy / distance * speed * dt;
        }
    }

    private void updateBossMissiles(double dt) {
        double targetX = BATTLE_X + 25;
        double targetY = battleY + 25;

        for (BossMissile missile : bossMissiles) {
            missile.elapsed += dt;

            double centerX = missile.x + missile.width / 2.0;
            double centerY = missile.y + missile.height / 2.0;
            double dx = targetX - centerX;
            double dy = targetY - centerY;
            double distance = Math.max(1, Math.sqrt(dx * dx + dy * dy));
            double targetDirX = dx / distance;
            double targetDirY = dy / distance;

            if (missile.locked && isLockAngleExceeded(missile.vx, missile.vy, dx, dy)) {
                missile.locked = false;
            }

            if (!missile.locked) {
                missile.x += missile.vx * dt;
                missile.y += missile.vy * dt;
                continue;
            }

            if (missile.elapsed <= 1.0) {
                double startSpeed = missile.phase == 3 ? 240 : 180;
                double speed = startSpeed + (600 - startSpeed) * missile.elapsed;
                missile.vx = targetDirX * speed;
                missile.vy = targetDirY * speed;
            } else {
                double currentSpeed = Math.max(1, Math.sqrt(missile.vx * missile.vx + missile.vy * missile.vy));
                double currentDirX = missile.vx / currentSpeed;
                double currentDirY = missile.vy / currentSpeed;
                double dirX = currentDirX * 0.92 + targetDirX * 0.08;
                double dirY = currentDirY * 0.92 + targetDirY * 0.08;
                double dirLength = Math.max(1, Math.sqrt(dirX * dirX + dirY * dirY));

                missile.vx = dirX / dirLength * 600;
                missile.vy = dirY / dirLength * 600;
            }

            missile.x += missile.vx * dt;
            missile.y += missile.vy * dt;
        }

        for (int i = bossMissiles.size() - 1; i >= 0; i--) {
            BossMissile missile = bossMissiles.get(i);

            if (missile.x < 940 || missile.x > 1940 || missile.y < -80 || missile.y > 1160) {
                bossMissiles.remove(i);
            }
        }
    }

    private void handleWeaponCollisions() {
        handleKnifeCollisions();
        handlePlayerMissileCollisions();
    }

    private boolean isLockAngleExceeded(double vx, double vy, double targetDx, double targetDy) {
        double speed = Math.sqrt(vx * vx + vy * vy);
        double targetDistance = Math.sqrt(targetDx * targetDx + targetDy * targetDy);

        if (speed <= 0.01 || targetDistance <= 0.01) {
            return false;
        }

        double dot = vx * targetDx + vy * targetDy;
        double cosine = dot / (speed * targetDistance);

        return cosine < LOCK_LOSS_COSINE;
    }

    private void handleKnifeCollisions() {
        for (int i = knives.size() - 1; i >= 0; i--) {
            Shot knife = knives.get(i);
            boolean removed = false;

            for (int j = flyingEnemies.size() - 1; j >= 0; j--) {
                FlyingEnemy enemy = flyingEnemies.get(j);

                if (rectsOverlap(knife.x, knife.y, knife.width, knife.height, enemy.x, enemy.y, enemy.width, enemy.height)) {
                    flyingEnemies.remove(j);
                    knives.remove(i);
                    markEnemyKilled();
                    removed = true;
                    break;
                }
            }

            if (removed) {
                continue;
            }

            if (rectsOverlap(knife.x, knife.y, knife.width, knife.height, BOSS_X, BOSS_Y, BOSS_W, BOSS_H)) {
                bossHp -= knife.damage;
                knives.remove(i);
            }
        }
    }

    private void handlePlayerMissileCollisions() {
        for (int i = playerMissiles.size() - 1; i >= 0; i--) {
            Shot missile = playerMissiles.get(i);
            boolean removed = false;

            for (int j = bossMissiles.size() - 1; j >= 0; j--) {
                BossMissile bossMissile = bossMissiles.get(j);

                if (rectsOverlap(missile.x, missile.y, missile.width, missile.height,
                        bossMissile.x, bossMissile.y, bossMissile.width, bossMissile.height)) {
                    bossMissiles.remove(j);
                    playerMissiles.remove(i);
                    removed = true;
                    break;
                }
            }

            if (removed) {
                continue;
            }

            for (int j = flyingEnemies.size() - 1; j >= 0; j--) {
                FlyingEnemy enemy = flyingEnemies.get(j);

                if (rectsOverlap(missile.x, missile.y, missile.width, missile.height, enemy.x, enemy.y, enemy.width, enemy.height)) {
                    flyingEnemies.remove(j);
                    playerMissiles.remove(i);
                    markEnemyKilled();
                    removed = true;
                    break;
                }
            }

            if (removed) {
                continue;
            }

            if (rectsOverlap(missile.x, missile.y, missile.width, missile.height, BOSS_X, BOSS_Y, BOSS_W, BOSS_H)) {
                bossHp -= missile.damage;
                playerMissiles.remove(i);
            }
        }
    }

    private void handleBattlePigHazards() {
        if (invincibleTimer > 0) {
            return;
        }

        for (int i = flyingEnemies.size() - 1; i >= 0; i--) {
            FlyingEnemy enemy = flyingEnemies.get(i);

            if (rectsOverlap(BATTLE_X, battleY, 50, 50, enemy.x, enemy.y, enemy.width, enemy.height)) {
                flyingEnemies.remove(i);
                damageBattlePig();
                return;
            }
        }

        for (int i = bossMissiles.size() - 1; i >= 0; i--) {
            BossMissile missile = bossMissiles.get(i);

            if (rectsOverlap(BATTLE_X, battleY, 50, 50, missile.x, missile.y, missile.width, missile.height)) {
                bossMissiles.remove(i);
                damageBattlePig();
                return;
            }
        }

        for (int i = cursedMissiles.size() - 1; i >= 0; i--) {
            Shot missile = cursedMissiles.get(i);

            if (missile.looping) {
                continue;
            }

            if (rectsOverlap(BATTLE_X, battleY, 50, 50, missile.x, missile.y, missile.width, missile.height)) {
                cursedMissiles.remove(i);
                damageBattlePig();
                return;
            }
        }
    }

    private void damageBattlePig() {
        if (shield) {
            shield = false;
            invincibleTimer = 0.4;
            return;
        }

        battleLives--;
        lifeLossesThisFrame++;

        if (battleLives <= 0) {
            battleLives = 0;
            gameOver = true;
            return;
        }

        battleY = BATTLE_START_Y;
        battleVelocityY = 0;
        invincibleTimer = 2.0;
        noFireTimer = 0.5;
        flyingEnemies.clear();
        bossMissiles.clear();
        cursedMissiles.clear();
    }

    private void punishGreedyPig() {
        greedyStunTimer = 2.0;
        if (!missileInventory.isEmpty()) {
            missileInventory.remove(missileInventory.size() - 1);
        }

        missileAmmo = missileInventory.size();
        resetGreedyPig();
    }

    private void resetGreedyPig() {
        greedyPig.clear();
        greedyPig.add(new GridPoint(8, 10));
        greedyPig.add(new GridPoint(7, 10));
        greedyPig.add(new GridPoint(6, 10));
        dirCol = 1;
        dirRow = 0;
        nextDirCol = 1;
        nextDirRow = 0;
    }

    private void spawnMissingSupplies() {
        while (countCommonItems() < 2) {
            spawnSupply(randomCommonType());
        }

        if (countRageItems() < 1) {
            spawnSupply(ItemType.RAGE);
        }
    }

    private void spawnSupply(ItemType type) {
        for (int attempt = 0; attempt < 200; attempt++) {
            int col = (int)(Math.random() * GRID_COLS);
            int row = (int)(Math.random() * GRID_ROWS);

            if (!isCellBlocked(col, row)) {
                supplyItems.add(new SupplyItem(type, col, row));
                return;
            }
        }
    }

    private ItemType randomCommonType() {
        int index = (int)(Math.random() * 4);

        if (index == 0) {
            return ItemType.MISSILE;
        }

        if (index == 1) {
            return ItemType.CURSED_MISSILE;
        }

        if (index == 2) {
            return ItemType.HEAL;
        }

        return ItemType.SHIELD;
    }

    private int countCommonItems() {
        int count = 0;

        for (SupplyItem item : supplyItems) {
            if (item.type != ItemType.RAGE) {
                count++;
            }
        }

        return count;
    }

    private int countRageItems() {
        int count = 0;

        for (SupplyItem item : supplyItems) {
            if (item.type == ItemType.RAGE) {
                count++;
            }
        }

        return count;
    }

    private boolean isCellBlocked(int col, int row) {
        for (GridPoint point : greedyPig) {
            if (point.col == col && point.row == row) {
                return true;
            }
        }

        for (SupplyItem item : supplyItems) {
            if (item.col == col && item.row == row) {
                return true;
            }
        }

        return false;
    }

    private SupplyItem itemAt(GridPoint point) {
        for (SupplyItem item : supplyItems) {
            if (item.col == point.col && item.row == point.row) {
                return item;
            }
        }

        return null;
    }

    private void applySupply(ItemType type) {
        if (type == ItemType.MISSILE) {
            addMissileToInventory(false);
        } else if (type == ItemType.CURSED_MISSILE) {
            addMissileToInventory(true);
        } else if (type == ItemType.HEAL) {
            if (battleLives < MAX_LIVES) {
                battleLives++;
            } else if (!shield) {
                shield = true;
            } else {
                addMissileToInventory(false);
            }
        } else if (type == ItemType.SHIELD) {
            shield = true;
        } else if (type == ItemType.RAGE) {
            rageTimer = 8.0;
        }
    }

    private void addMissileToInventory(boolean cursed) {
        if (missileInventory.size() < MAX_MISSILES) {
            missileInventory.add(cursed);
            missileAmmo = missileInventory.size();
        } else {
            bossHp -= 20;
        }
    }

    private void launchCursedMissile() {
        Shot missile = new Shot(1130, battleY, 50, 50, 700, 0, 0, true);
        missile.selfTargeting = true;
        missile.looping = true;
        missile.locked = true;
        missile.loopRadius = 75;
        missile.loopDuration = 1.0;
        missile.loopStartAngle = Math.PI;

        double centerX = missile.x + missile.width / 2.0;
        double centerY = missile.y + missile.height / 2.0;
        missile.loopCenterX = centerX + missile.loopRadius;
        missile.loopCenterY = centerY;

        cursedMissiles.add(missile);
    }

    public void setGreedyDirection(int col, int row) {
        if (stage3IntroActive || bossExplosionActive) {
            return;
        }

        if (col == 0 && row == 0) {
            return;
        }

        if (col == -dirCol && row == -dirRow) {
            return;
        }

        nextDirCol = col;
        nextDirRow = row;
    }

    public void setJetpackPressed(boolean pressed) {
        if (stage3IntroActive || bossExplosionActive) {
            return;
        }

        jetpackPressed = pressed;
    }

    public void firePlayerMissile() {
        if (missileInventory.isEmpty() || missileCooldownTimer > 0 || noFireTimer > 0 ||
                stage3IntroActive || bossExplosionActive || gameOver || complete) {
            return;
        }

        boolean cursed = missileInventory.remove(0);
        missileAmmo = missileInventory.size();
        missileCooldownTimer = 0.5;

        if (cursed) {
            launchCursedMissile();
        } else {
            playerMissiles.add(new Shot(1130, battleY, 50, 50, 700, 0, missileDamage(), true));
        }
    }

    private double missileDamage() {
        return knifeDamage() * 10;
    }

    private double knifeDamage() {
        return isRaging() ? 15 : 10;
    }

    private boolean isWallHit(GridPoint point) {
        return point.col < 0 || point.col >= GRID_COLS || point.row < 0 || point.row >= GRID_ROWS;
    }

    private boolean isSelfHit(GridPoint point) {
        for (GridPoint body : greedyPig) {
            if (body.sameCell(point)) {
                return true;
            }
        }

        return false;
    }

    private void markEnemyKilled() {
        enemyDiedThisFrame = true;
        enemyKillsThisFrame++;
    }

    private int phase() {
        if (bossHp > 840) {
            return 1;
        }

        if (bossHp > 420) {
            return 2;
        }

        return 3;
    }

    private double enemySpawnInterval() {
        int phase = phase();
        return phase == 1 ? 3.0 : phase == 2 ? 2.5 : 2.0;
    }

    private double bossMissileInterval() {
        int phase = phase();
        return phase == 1 ? 10.0 : phase == 2 ? 8.0 : 6.0;
    }

    private void startStage3Intro() {
        stage3IntroStarted = true;
        stage3IntroActive = true;
        stage3IntroTimer = STAGE3_INTRO_DURATION;
        stage3SoundRequested = true;
        jetpackPressed = false;
    }

    private double enemySpeed() {
        int phase = phase();
        double speed = phase == 1 ? 180 : phase == 2 ? 220 : 260;

        if (isRaging()) {
            speed *= 0.8;
        }

        return speed;
    }

    private double knifeInterval() {
        return isRaging() ? 0.18 : 0.35;
    }

    boolean isRaging() {
        return rageTimer > 0;
    }

    int phaseNumber() {
        return phase();
    }

    boolean isBattlePigInvincible() {
        return invincibleTimer > 0;
    }

    boolean isBossFightGameOver() {
        return gameOver;
    }

    boolean isStage3IntroActive() {
        return stage3IntroActive;
    }

    boolean isBossExplosionActive() {
        return bossExplosionActive;
    }

    double getBossExplosionProgress() {
        if (!bossExplosionActive) {
            return 0;
        }

        return 1.0 - bossExplosionTimer / BOSS_EXPLOSION_DURATION;
    }

    double getStage3IntroTimer() {
        return stage3IntroTimer;
    }

    boolean areControlsReversed() {
        return stage3IntroStarted && !stage3IntroActive && !bossExplosionActive &&
                phase() == 3 && !complete && !gameOver;
    }

    boolean consumeStage3SoundRequest() {
        if (!stage3SoundRequested) {
            return false;
        }

        stage3SoundRequested = false;
        return true;
    }

    int getBossFrameIndex() {
        return bossAttackAnimating ? bossAttackFrame : 0;
    }

    boolean hasLockedBossMissile() {
        for (BossMissile missile : bossMissiles) {
            if (missile.locked && distanceToBattlePig(missile) > 50) {
                return true;
            }
        }

        return false;
    }

    private double distanceToBattlePig(BossMissile missile) {
        double missileCenterX = missile.x + missile.width / 2.0;
        double missileCenterY = missile.y + missile.height / 2.0;
        double battlePigCenterX = BATTLE_X + 25;
        double battlePigCenterY = battleY + 25;
        double dx = missileCenterX - battlePigCenterX;
        double dy = missileCenterY - battlePigCenterY;

        return Math.sqrt(dx * dx + dy * dy);
    }

    int getLifeLossesThisFrame() {
        return lifeLossesThisFrame;
    }

    Image getKnifeImage() {
        return knifeImage;
    }

    Image getEnemyImage() {
        if (enemyIdleFrames == null || enemyIdleFrames.length == 0) {
            return null;
        }

        return enemyIdleFrames[0];
    }

    @Override
    public boolean isLevelComplete() {
        return complete;
    }

    @Override
    public boolean didEnemyDie() {
        return enemyDiedThisFrame;
    }

    @Override
    public int getEnemyKillsForPlayer(int playerNumber) {
        return playerNumber == 2 ? enemyKillsThisFrame : 0;
    }

    private boolean rectsOverlap(double x1, double y1, double w1, double h1,
                                 double x2, double y2, double w2, double h2) {
        return x1 < x2 + w2 &&
                x1 + w1 > x2 &&
                y1 < y2 + h2 &&
                y1 + h1 > y2;
    }
}
