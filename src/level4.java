import java.awt.*;

public class level4 extends level {
    private static final double BLOCK_SIZE = 50;
    private static final double TRAP_SIZE = 50;
    private static final double DOOR_SIZE = 50;
    private static final double KNIFE_SPEED = 800;
    private static final double KNIFE_COOLDOWN = 2.0;
    private static final double JUMP_POWER = 720;
    private static final double DEATH_Y = 1030;

    private FakeReturnDoorTrap fakeDoor1;
    private int raceWinner = 0;

    public level4(Image backgroundImage) {
        super(backgroundImage);
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        if (raceWinner == 0) {
            // Level 4 is a direct race, so touching the real door decides immediately.
            if (isTouchingTrueDoor(player1)) {
                finishRace(1);
            } else if (isTouchingTrueDoor(player2)) {
                finishRace(2);
            }
        }

        killIfBelowStage(player1);
        killIfBelowStage(player2);
    }

    @Override
    public boolean isLevelComplete() {
        return raceWinner != 0;
    }

    @Override
    public int getWinningPlayer() {
        return raceWinner;
    }

    public String getTrollMessage() {
        if (fakeDoor1 == null) {
            return "";
        }

        return fakeDoor1.getMessage();
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
        raceWinner = 0;

        clearLevelObjects();
        enemies.clear();

        spawnX1 = 100;
        spawnY1 = 880;
        spawnX2 = 1410;
        spawnY2 = 880;

        placePlayerAtSpawn(player1, spawnX1, spawnY1);
        placePlayerAtSpawn(player2, spawnX2, spawnY2);

        goalX = 950;
        goalY = 295;
        goalWidth = DOOR_SIZE;
        goalHeight = DOOR_SIZE;

        addPlatforms(platformImage);
        addPortals(portalImage);
        addKnives(knifeImage);
        addFakeDoors(gateImage);
        addSpikes(spikeImage);
        addHiddenSpikes(spikeImage);
        addSaws(sawFrames);

        gate = new Gate(goalX, goalY, goalWidth, goalHeight, gateImage);
        addEnemiesToHalfPlatforms(enemyIdleFrames, enemyLeftFrames);
    }

    private void addPlatforms(Image platformImage) {
        // Symmetric stacked lanes support a race format while fake doors disrupt obvious routes.
        addBlockRun(0, 930, 8, platformImage, 250);
        addBlockRun(500, 930, 7, platformImage, 600);
        addBlockRun(1000, 930, 6, platformImage, 1150);
        addBlockRun(1400, 930, 7, platformImage, 1550);

        addBlockRun(250, 735, 6, platformImage, 350);
        addBlockRun(800, 735, 6, platformImage, 900);
        addBlockRun(1350, 735, 6, platformImage, 1450);

        addBlockRun(100, 540, 6, platformImage, 250);
        addBlockRun(650, 540, 6, platformImage, 850);
        addBlockRun(1200, 540, 7, platformImage, 1350);

        addBlockRun(450, 345, 6, platformImage);
        addBlockRun(750, 345, 7, platformImage, 1000);
        addBlockRun(1250, 345, 6, platformImage, 1450);
    }

    private void addPortals(Image[] portalImage) {
        portals.add(new Portal(600, 880, TRAP_SIZE, TRAP_SIZE, 1300, 490, portalImage));
        portals.add(new Portal(1500, 685, TRAP_SIZE, TRAP_SIZE, 750, 295, portalImage));
    }

    private void addKnives(Image knifeImage) {
        traps.add(new FlyingKnife(450, 880, TRAP_SIZE, TRAP_SIZE, 1, 0, KNIFE_SPEED, 500, 830, 350, 150, 2100, KNIFE_COOLDOWN, knifeImage));
        traps.add(new FlyingKnife(850, 880, TRAP_SIZE, TRAP_SIZE, -1, 0, KNIFE_SPEED, 550, 830, 200, 150, 2100, KNIFE_COOLDOWN, knifeImage));
        traps.add(new FlyingKnife(1300, 880, TRAP_SIZE, TRAP_SIZE, -1, 0, KNIFE_SPEED, 950, 830, 300, 150, 2100, KNIFE_COOLDOWN, knifeImage));
        traps.add(new FlyingKnife(1150, 685, TRAP_SIZE, TRAP_SIZE, -1, 0, KNIFE_SPEED, 750, 635, 350, 120, 2100, KNIFE_COOLDOWN, knifeImage));
        traps.add(new FlyingKnife(1650, 490, TRAP_SIZE, TRAP_SIZE, -1, 0, KNIFE_SPEED, 1250, 440, 300, 120, 2100, KNIFE_COOLDOWN, knifeImage));
        traps.add(new FlyingKnife(1300, 295, TRAP_SIZE, TRAP_SIZE, -1, 0, KNIFE_SPEED, 750, 275, 500, 170, 2100, KNIFE_COOLDOWN, knifeImage));
    }

    private void addFakeDoors(Image[] gateImage) {
        fakeDoor1 = new FakeReturnDoorTrap(
                1550, 685,
                DOOR_SIZE, DOOR_SIZE,
                100, 880,
                "You thought this was the finish?",
                gateImage[0]
        );

        traps.add(fakeDoor1);

        traps.add(new FakeReturnDoorTrap(
                1350, 490,
                DOOR_SIZE, DOOR_SIZE,
                300, 490,
                gateImage[0]
        ));

        traps.add(new FakeReturnDoorTrap(
                1400, 295,
                DOOR_SIZE, DOOR_SIZE,
                1300, 490,
                gateImage[0]
        ));
    }

    private void addSpikes(Image spikeImage) {
        traps.add(new Spike(300, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(750, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1000, 880, TRAP_SIZE, TRAP_SIZE, spikeImage));

        traps.add(new Spike(900, 685, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1450, 685, TRAP_SIZE, TRAP_SIZE, spikeImage));

        traps.add(new Spike(900, 490, TRAP_SIZE, TRAP_SIZE, spikeImage));
        traps.add(new Spike(1300, 490, TRAP_SIZE, TRAP_SIZE, spikeImage));

        traps.add(new Spike(900, 295, TRAP_SIZE, TRAP_SIZE, spikeImage));
    }

    private void addHiddenSpikes(Image spikeImage) {
        traps.add(new HiddenSpike(650, 880, TRAP_SIZE, TRAP_SIZE, 600, 830, 150, 150, spikeImage));
        traps.add(new HiddenSpike(1150, 880, TRAP_SIZE, TRAP_SIZE, 1100, 830, 150, 150, spikeImage));
        traps.add(new HiddenSpike(1500, 295, TRAP_SIZE, TRAP_SIZE, 1450, 275, 150, 100, spikeImage));
    }

    private void addSaws(Image[] sawFrames) {
        traps.add(new Saw(700, 875, TRAP_SIZE, TRAP_SIZE, 700, 875, 700, 875, 0, sawFrames));
        traps.add(new Saw(1250, 490, TRAP_SIZE, TRAP_SIZE, 1250, 440, 1250, 590, 120, sawFrames));
        traps.add(new Saw(1400, 490, TRAP_SIZE, TRAP_SIZE, 1400, 490, 1400, 490, 0, sawFrames));
        traps.add(new Saw(1050, 295, TRAP_SIZE, TRAP_SIZE, 900, 295, 1150, 295, 130, sawFrames));
    }

    private void addBlockRun(double x, double y, int blocks, Image platformImage, double... pitXs) {
        registerEnemyPlatform(x, y, blocks);

        for (int i = 0; i < blocks; i++) {
            double blockX = x + i * BLOCK_SIZE;

            if (isPitBlock(blockX, x, blocks, pitXs)) {
                platforms.add(new BreakawayPitPlatform(blockX, y, platformImage, 0.1, 3.0));
            } else {
                platforms.add(new Platform(blockX, y, BLOCK_SIZE, BLOCK_SIZE, platformImage));
            }
        }
    }

    private boolean isPitBlock(double x, double runStartX, int blocks, double[] pitXs) {
        double runEndX = runStartX + (blocks - 1) * BLOCK_SIZE;

        for (double pitX : pitXs) {
            // A pit marker covers up to two adjacent tiles, clamped to the platform run.
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

    private boolean isTouchingTrueDoor(player p) {
        if (p == null || p.dead) {
            return false;
        }

        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                goalX, goalY, goalWidth, goalHeight
        );
    }

    private void placePlayerAtSpawn(player p, double spawnX, double spawnY) {
        p.x = spawnX;
        p.y = spawnY;
        p.velocityX = 0;
        p.velocityY = 0;
        p.leftPressed = false;
        p.rightPressed = false;
        p.jumpPressed = false;
        p.dead = false;
        p.deadTimer = 0;
        p.reachedGate = false;
        p.trappedInFakeGate = false;
        p.onGround = true;
    }

    private void finishRace(int playerNumber) {
        raceWinner = playerNumber;

        if (playerNumber == 1) {
            // Trigger the gate animation for feedback even though this level uses raceWinner as completion.
            if (!player1.reachedGate && gate != null) {
                gate.playerReach(1);
            }

            player1.reachedGate = true;
        } else {
            if (!player2.reachedGate && gate != null) {
                gate.playerReach(2);
            }

            player2.reachedGate = true;
        }
    }

    private void killIfBelowStage(player p) {
        if (p != null && !p.dead && !p.reachedGate && p.y > DEATH_Y) {
            p.die();
        }
    }
}
