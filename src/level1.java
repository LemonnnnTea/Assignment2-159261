import java.awt.*;

public class level1 extends level {
    private static final double PLAYER_SIZE = 50;
    private static final double BLOCK_SIZE = 50;
    private static final double DEATH_Y = 1030;

    public level1(Image backgroundImage) {
        super(backgroundImage);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        killIfBelowStage(player1);
        killIfBelowStage(player2);
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
        this.player1.jumpPower = player.DEFAULT_JUMP_POWER;
        this.player2.jumpPower = player.DEFAULT_JUMP_POWER;

        clearLevelObjects();
        enemies.clear();

        spawnX1 = 100;
        spawnY1 = 880;
        spawnX2 = 170;
        spawnY2 = 880;

        player1.x = spawnX1;
        player1.y = spawnY1;
        player2.x = spawnX2;
        player2.y = spawnY2;

        goalX = 1800;
        goalY = 880;
        goalWidth = 50;
        goalHeight = 50;

        addPlatforms(platformImage);
        addPortals(portalImage);
        addTraps(spikeImage, sawFrames, knifeImage);
        addFakeGate(gateImage, spikeImage);
        addWindVent();

        gate = new Gate(goalX, goalY, goalWidth, goalHeight, gateImage);
        addEnemiesToHalfPlatforms(enemyIdleFrames, enemyLeftFrames);
    }

    private void addPlatforms(Image platformImage) {
        addBlockRun(0, 930, 7, platformImage);
        addBlockRun(450, 930, 5, platformImage);
        addBlockRun(750, 830, 3, platformImage);
        addBlock(950, 930, platformImage);

        addBlockRun(1050, 650, 4, platformImage);
        addBlockRun(1300, 760, 3, platformImage);
        addBlockRun(1450, 860, 4, platformImage);
        addBlockRun(1600, 930, 6, platformImage);
    }

    private void addTraps(Image spikeImage, Image[] sawFrames, Image knifeImage) {
        traps.add(new Spike(300, 880, BLOCK_SIZE, BLOCK_SIZE, spikeImage));
        traps.add(new Spike(800, 780, BLOCK_SIZE, BLOCK_SIZE, spikeImage));
        traps.add(new Spike(1200, 600, BLOCK_SIZE, BLOCK_SIZE, spikeImage));
        traps.add(new Spike(1500, 810, BLOCK_SIZE, BLOCK_SIZE, spikeImage));
        traps.add(new Spike(1650, 880, BLOCK_SIZE, BLOCK_SIZE, spikeImage));

        traps.add(new FlyingKnife(
                -50, 850,
                BLOCK_SIZE, BLOCK_SIZE,
                1, 0,
                2600,
                430, 880, 120, 80,
                2100,
                knifeImage
        ));

        traps.add(new FlyingKnife(
                1920, 700,
                BLOCK_SIZE, BLOCK_SIZE,
                -1, 0,
                2600,
                1320, 720, 120, 100,
                2100,
                knifeImage
        ));

        traps.add(new Saw(
                1420, 810,
                BLOCK_SIZE, BLOCK_SIZE,
                1360, 810,
                1510, 810,
                95,
                sawFrames
        ));
    }

    private void addWindVent() {
        windVents.add(new WindVent(
                950, 930,
                BLOCK_SIZE, BLOCK_SIZE,
                900, 590,
                150, 340
        ));
    }

    private void addPortals(Image[] portalImage) {
        portals.add(new Portal(1350, 710, BLOCK_SIZE, BLOCK_SIZE, 1200, 600, portalImage));
        portals.add(new Portal(1550, 810, BLOCK_SIZE, BLOCK_SIZE, 1700, 880, portalImage));
    }

    private void addFakeGate(Image[] gateImage, Image spikeImage) {
        traps.add(new FakeGateTrap(
                500, 880,
                BLOCK_SIZE, BLOCK_SIZE,
                gateImage
        ));
    }

    private void addBlockRun(double x, double y, int blocks, Image platformImage) {
        registerEnemyPlatform(x, y, blocks);

        for (int i = 0; i < blocks; i++) {
            addBlock(x + i * BLOCK_SIZE, y, platformImage);
        }
    }

    private void addBlock(double x, double y, Image platformImage) {
        platforms.add(new Platform(x, y, BLOCK_SIZE, BLOCK_SIZE, platformImage));
    }

    private void killIfBelowStage(player p) {
        if (p != null && !p.dead && !p.reachedGate && p.y > DEATH_Y) {
            p.die();
        }
    }
}
