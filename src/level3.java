import java.awt.*;

public class level3 extends level {
    private static final double TRAP_SIZE = 50;
    private static final double BLOCK_SIZE = 50;
    private static final double JUMP_POWER = 650;
    private static final double DEATH_Y = 1030;

    public level3(Image backgroundImage) {
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

        spawnX1 = 80;
        spawnY1 = 880;
        spawnX2 = 150;
        spawnY2 = 880;

        player1.x = spawnX1;
        player1.y = spawnY1;
        player2.x = spawnX2;
        player2.y = spawnY2;

        goalX = 500;
        goalY = 880;
        goalWidth = 50;
        goalHeight = 50;

        addPlatforms(platformImage, pitImage);
        addKnives(knifeImage);
        addFakeGate(gateImage, spikeImage);
        addPortals(portalImage);
        addSpikes(spikeImage);
        addSaws(sawFrames);

        gate = new Gate(goalX, goalY, goalWidth, goalHeight, gateImage);
        addEnemiesToHalfPlatforms(enemyIdleFrames, enemyLeftFrames);
    }

    private void addPlatforms(Image platformImage, Image pitImage) {
        addBlockRun(0, 930, 6, platformImage, pitImage);
        addBlockRun(380, 930, 5, platformImage, pitImage, 430);
        addBlockRun(720, 930, 6, platformImage, pitImage, 770, 870, 970);
        addBlockRun(1120, 930, 5, platformImage, pitImage, 1170, 1220, 1320);
        addBlockRun(1510, 930, 8, platformImage, pitImage, 1560);

        addBlockRun(320, 780, 4, platformImage, pitImage);
        addBlockRun(620, 690, 4, platformImage, pitImage);
        addBlockRun(920, 580, 5, platformImage, pitImage, 970, 1120);
        addBlockRun(1230, 690, 4, platformImage, pitImage);
        addBlockRun(1480, 800, 1, platformImage, pitImage);
        addBlockRun(1530, 800, 5, platformImage, pitImage, 1580, 1730);
        addBlockRun(1700, 700, 4, platformImage, pitImage);
        addBlockRun(1200, 520, 3, platformImage, pitImage);
        addBlockRun(1450, 470, 3, platformImage, pitImage);
        addBlockRun(1650, 450, 5, platformImage, pitImage);
    }

    private void addKnives(Image knifeImage) {
        traps.add(new FlyingKnife(-50, 840, TRAP_SIZE, TRAP_SIZE, 1, 0, 2600, 410, 880, 120, 100, 2100, knifeImage));
        traps.add(new FlyingKnife(-50, 630, TRAP_SIZE, TRAP_SIZE, 1, 0, 2750, 620, 640, 120, 90, 2100, knifeImage));
        traps.add(new FlyingKnife(-50, 520, TRAP_SIZE, TRAP_SIZE, 1, 0, 2700, 920, 530, 130, 90, 2100, knifeImage));
        traps.add(new FlyingKnife(1320, -50, TRAP_SIZE, TRAP_SIZE, 0, 1, 2300, 1230, 640, 170, 120, 1200, knifeImage));
        traps.add(new FlyingKnife(1920, 750, TRAP_SIZE, TRAP_SIZE, -1, 0, 2800, 1530, 750, 160, 100, 2100, knifeImage));
        traps.add(new FlyingKnife(1920, 410, TRAP_SIZE, TRAP_SIZE, -1, 0, 2800, 1600, 400, 180, 110, 2100, knifeImage));
    }

    private void addPortals(Image[] portalImage) {
        portals.add(new Portal(770, 640, TRAP_SIZE, TRAP_SIZE, 820, 880, portalImage));
        portals.add(new Portal(1700, 400, TRAP_SIZE, TRAP_SIZE, 1800, 400, portalImage));
    }

    private void addSpikes(Image spikeImage) {
        traps.add(new Spike(280, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(820, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(970, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1220, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1340, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1600, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1760, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1050, 530, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1280, 640, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1760, 650, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1500, 420, TRAP_SIZE, TRAP_SIZE, spikeImage));
    }

    private void addFakeGate(Image[] gateImage, Image spikeImage) {
        traps.add(new FakeGateTrap(
                1850, 400,
                TRAP_SIZE, TRAP_SIZE,
                gateImage
        ));
    }

    private void addSaws(Image[] sawFrames) {
        traps.add(new Saw(900, 875, TRAP_SIZE, TRAP_SIZE, 850, 875, 1000, 875, 100, sawFrames));
        traps.add(new Saw(1080, 650, TRAP_SIZE, TRAP_SIZE, 1080, 560, 1080, 760, 120, sawFrames));
        traps.add(new Saw(1380, 875, TRAP_SIZE, TRAP_SIZE, 1380, 875, 1380, 875, 0, sawFrames));
        traps.add(new Saw(1530, 750, TRAP_SIZE, TRAP_SIZE, 1530, 750, 1730, 750, 90, sawFrames));
        traps.add(new Saw(1800, 650, TRAP_SIZE, TRAP_SIZE, 1800, 650, 1800, 650, 0, sawFrames));
        traps.add(new Saw(1580, 420, TRAP_SIZE, TRAP_SIZE, 1500, 420, 1650, 420, 110, sawFrames));
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
