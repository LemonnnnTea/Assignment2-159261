import java.awt.*;

public class level4 extends level {
    private static final double BLOCK_SIZE = 50;
    private static final double TRAP_SIZE = 50;
    private static final double DOOR_SIZE = 100;
    private static final double JUMP_POWER = 650;
    private static final double DEATH_Y = 1030;

    public level4(Image backgroundImage) {
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
            Image[] gateImage
    ) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1.jumpPower = JUMP_POWER;
        this.player2.jumpPower = JUMP_POWER;

        platforms.clear();
        traps.clear();
        portals.clear();
        portalParticles.clear();
        windVents.clear();

        spawnX1 = 100;
        spawnY1 = 880;
        spawnX2 = 170;
        spawnY2 = 880;

        player1.x = spawnX1;
        player1.y = spawnY1;
        player2.x = spawnX2;
        player2.y = spawnY2;

        goalX = 1000;
        goalY = 80;
        goalWidth = DOOR_SIZE;
        goalHeight = DOOR_SIZE;

        addPlatforms(platformImage, pitImage);
        addPortals(portalImage);
        addFakeDoors(gateImage);
        addSpikes(spikeImage);
        addSaws(sawFrames);
        addKnives(knifeImage);

        gate = new Gate(goalX, goalY, goalWidth, goalHeight, gateImage);
    }

    private void addPlatforms(Image platformImage, Image pitImage) {
        addBlockRun(0, 930, 7, platformImage, pitImage, 250);
        addBlockRun(450, 930, 6, platformImage, pitImage, 550);
        addBlockRun(850, 930, 7, platformImage, pitImage, 950);
        addBlockRun(1350, 930, 8, platformImage, pitImage, 1600);

        addBlockRun(200, 780, 6, platformImage, pitImage, 300);
        addBlockRun(650, 780, 6, platformImage, pitImage, 750);
        addBlockRun(1100, 780, 6, platformImage, pitImage, 1200);
        addBlockRun(1500, 780, 6, platformImage, pitImage, 1650);

        addBlockRun(100, 630, 5, platformImage, pitImage);
        addBlockRun(500, 630, 7, platformImage, pitImage, 700);
        addBlockRun(1000, 630, 6, platformImage, pitImage, 1150);
        addBlockRun(1450, 630, 6, platformImage, pitImage);

        addBlockRun(300, 480, 6, platformImage, pitImage);
        addBlockRun(800, 480, 7, platformImage, pitImage, 900);
        addBlockRun(1300, 480, 6, platformImage, pitImage);

        addBlockRun(100, 330, 6, platformImage, pitImage);
        addBlockRun(600, 330, 7, platformImage, pitImage);
        addBlockRun(1150, 330, 6, platformImage, pitImage, 1250);
        addBlockRun(1550, 330, 5, platformImage, pitImage);

        addBlockRun(750, 180, 10, platformImage, pitImage);
    }

    private void addPortals(Image[] portalImage) {
        portals.add(new Portal(600, 880, TRAP_SIZE, TRAP_SIZE, 1650, 580, portalImage));
        portals.add(new Portal(1450, 880, TRAP_SIZE, TRAP_SIZE, 850, 130, portalImage));
    }

    private void addFakeDoors(Image[] gateImage) {
        traps.add(new FakeReturnDoorTrap(
                1550, 680,
                DOOR_SIZE, DOOR_SIZE,
                spawnX1, spawnY1,
                spawnX2, spawnY2,
                gateImage[0]
        ));

        traps.add(new FakeReturnDoorTrap(
                1350, 380,
                DOOR_SIZE, DOOR_SIZE,
                1050, 580,
                1200, 580,
                gateImage[0]
        ));
    }

    private void addSpikes(Image spikeImage) {
        traps.add(new Spike(300, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(700, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1050, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1650, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));

        traps.add(new Spike(800, 730, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1250, 730, TRAP_SIZE, TRAP_SIZE, spikeImage));

        traps.add(new Spike(700, 580, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1550, 580, TRAP_SIZE, TRAP_SIZE, spikeImage));

        traps.add(new Spike(400, 430, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1050, 430, TRAP_SIZE, TRAP_SIZE, spikeImage));

        traps.add(new Spike(700, 280, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1250, 280, TRAP_SIZE, TRAP_SIZE, spikeImage));

        traps.add(new Spike(1650, 580, TRAP_SIZE, TRAP_SIZE, spikeImage));
    }

    private void addSaws(Image[] sawFrames) {
        traps.add(new Saw(700, 875, TRAP_SIZE, TRAP_SIZE, 700, 875, 700, 875, 0, sawFrames));
        traps.add(new Saw(1250, 700, TRAP_SIZE, TRAP_SIZE, 1250, 650, 1250, 850, 120, sawFrames));
        traps.add(new Saw(1400, 430, TRAP_SIZE, TRAP_SIZE, 1400, 430, 1400, 430, 0, sawFrames));
        traps.add(new Saw(1050, 130, TRAP_SIZE, TRAP_SIZE, 900, 130, 1150, 130, 130, sawFrames));
    }

    private void addKnives(Image knifeImage) {
        traps.add(new FlyingKnife(350, 840, TRAP_SIZE, TRAP_SIZE, 1, 0, 850, 380, 830, 180, 150, 1700, knifeImage));
        traps.add(new FlyingKnife(950, 700, TRAP_SIZE, TRAP_SIZE, -1, 0, 850, 650, 700, 250, 120, 1700, knifeImage));
        traps.add(new FlyingKnife(450, 580, TRAP_SIZE, TRAP_SIZE, 1, 0, 850, 500, 560, 300, 120, 1700, knifeImage));
        traps.add(new FlyingKnife(1650, 430, TRAP_SIZE, TRAP_SIZE, -1, 0, 900, 1250, 360, 300, 150, 1700, knifeImage));
        traps.add(new FlyingKnife(1300, 130, TRAP_SIZE, TRAP_SIZE, -1, 0, 900, 850, 100, 400, 130, 1700, knifeImage));
    }

    private void addBlockRun(double x, double y, int blocks, Image platformImage, Image pitImage, double... pitXs) {
        for (int i = 0; i < blocks; i++) {
            double blockX = x + i * BLOCK_SIZE;

            if (isPitBlock(blockX, pitXs)) {
                platforms.add(new BreakawayPitPlatform(blockX, y, pitImage));
            } else {
                platforms.add(new Platform(blockX, y, BLOCK_SIZE, BLOCK_SIZE, platformImage));
            }
        }
    }

    private boolean isPitBlock(double x, double[] pitXs) {
        for (double pitX : pitXs) {
            if (Math.abs(x - pitX) < 0.01) {
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
