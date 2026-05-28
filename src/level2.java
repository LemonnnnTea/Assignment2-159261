import java.awt.*;

public class level2 extends level {
    private static final double TRAP_SIZE = 50;
    private static final double BLOCK_SIZE = 50;
    private static final double JUMP_POWER = 650;
    private static final double DEATH_Y = 1030;

    public level2(Image backgroundImage) {
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
        this.player1.jumpPower = JUMP_POWER;
        this.player2.jumpPower = JUMP_POWER;

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

        goalX = 1300;
        goalY = 880;
        goalWidth = 50;
        goalHeight = 50;

        addPlatforms(platformImage, pitImage);
        addPortals(portalImage);
        addKnives(knifeImage);
        addFakeGate(gateImage, spikeImage);
        addSpikes(spikeImage);
        addSaws(sawFrames);

        gate = new Gate(goalX, goalY, goalWidth, goalHeight, gateImage);
        addEnemiesToHalfPlatforms(enemyIdleFrames, enemyLeftFrames);
    }

    private void addPlatforms(Image platformImage, Image pitImage) {
        addBlockRun(0, 930, 7, platformImage, pitImage);
        addBlockRun(450, 930, 5, platformImage, pitImage, 500);
        addBlockRun(800, 930, 6, platformImage, pitImage, 850, 950, 1050);
        addBlockRun(1150, 930, 1, platformImage, pitImage);
        addBlockRun(1200, 930, 5, platformImage, pitImage, 1200, 1400);
        addBlockRun(1550, 930, 7, platformImage, pitImage);

        addBlockRun(400, 780, 4, platformImage, pitImage);
        addBlockRun(700, 680, 5, platformImage, pitImage);
        addBlockRun(1050, 600, 5, platformImage, pitImage, 1100, 1250);
        addBlockRun(1400, 710, 5, platformImage, pitImage, 1450, 1600);
        addBlockRun(1650, 830, 4, platformImage, pitImage);
    }

    private void addPortals(Image[] portalImage) {
        portals.add(new Portal(1200, 880, TRAP_SIZE, TRAP_SIZE, 900, 880, portalImage));
        portals.add(new Portal(1150, 550, TRAP_SIZE, TRAP_SIZE, 1700, 780, portalImage));
    }

    private void addKnives(Image knifeImage) {
        traps.add(new FlyingKnife(-50, 850, TRAP_SIZE, TRAP_SIZE, 1, 0, 2600, 430, 880, 110, 100, 2100, knifeImage));
        traps.add(new FlyingKnife(1920, 620, TRAP_SIZE, TRAP_SIZE, -1, 0, 2700, 730, 630, 120, 90, 2100, knifeImage));
        traps.add(new FlyingKnife(-50, 520, TRAP_SIZE, TRAP_SIZE, 1, 0, 2600, 1080, 550, 120, 90, 2100, knifeImage));
        traps.add(new FlyingKnife(1920, 830, TRAP_SIZE, TRAP_SIZE, -1, 0, 2700, 1470, 840, 140, 100, 2100, knifeImage));
    }

    private void addSpikes(Image spikeImage) {
        traps.add(new Spike(300, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(600, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(900, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1250, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1600, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1750, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(750, 630, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1500, 660, TRAP_SIZE, TRAP_SIZE, spikeImage));
    }

    private void addFakeGate(Image[] gateImage, Image spikeImage) {
        traps.add(new FakeGateTrap(
                1800, 780,
                TRAP_SIZE, TRAP_SIZE,
                gateImage
        ));
    }

    private void addSaws(Image[] sawFrames) {
        traps.add(new Saw(875, 875, TRAP_SIZE, TRAP_SIZE, 825, 875, 1025, 875, 90, sawFrames));
        traps.add(new Saw(1260, 730, TRAP_SIZE, TRAP_SIZE, 1260, 650, 1260, 850, 100, sawFrames));
    }

    private void addBlockRun(double x, double y, int blocks, Image platformImage, Image pitImage, double... pitXs) {
        registerEnemyPlatform(x, y, blocks);

        for (int i = 0; i < blocks; i++) {
            double blockX = x + i * BLOCK_SIZE;

            if (isPitBlock(blockX, x, blocks, pitXs)) {
                platforms.add(new BreakawayPitPlatform(blockX, y, pitImage));
            } else {
                platforms.add(new Platform(blockX, y, BLOCK_SIZE, BLOCK_SIZE, platformImage));
            }
        }
    }

    private boolean isPitBlock(double x, double runStartX, int blocks, double[] pitXs) {
        double runEndX = runStartX + (blocks - 1) * BLOCK_SIZE;

        for (double pitX : pitXs) {
            boolean pitStartsHere = Math.abs(x - pitX) < 0.01;
            boolean pitContinuesRight = pitX + BLOCK_SIZE <= runEndX &&
                    Math.abs(x - (pitX + BLOCK_SIZE)) < 0.01;
            boolean pitContinuesLeftAtRunEnd = pitX + BLOCK_SIZE > runEndX &&
                    Math.abs(x - (pitX - BLOCK_SIZE)) < 0.01;

            if (pitStartsHere || pitContinuesRight || pitContinuesLeftAtRunEnd) {
                return true;
            }
        }

        return false;
    }

    private void killIfBelowStage(player p) {
        if (p != null && !p.dead && !p.reachedGate && p.y > DEATH_Y) {
            p.die();
        }
    }
}
