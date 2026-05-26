import java.awt.*;

public class level1 extends level {

    public level1(Image backgroundImage) {
        super(backgroundImage);
    }

    @Override
    public void load(
            player player1,
            player player2,
            Image[] platformImage,
            Image spikeImage,
            Image[] sawFrames,
            Image pitImage,
            Image knifeImage,
            Image[] portalImage,
            Image[] gateImage
    ) {
        this.player1 = player1;
        this.player2 = player2;

        platforms.clear();
        traps.clear();
        portals.clear();
        portalParticles.clear();

        spawnX1 = 120;
        spawnY1 = 880;

        spawnX2 = 190;
        spawnY2 = 880;

        player1.x = spawnX1;
        player1.y = spawnY1;

        player2.x = spawnX2;
        player2.y = spawnY2;

        goalX = 1800;
        goalY = 850;
        goalWidth = 50;
        goalHeight = 50;

        platforms.add(new Platform(0, 930, 430, 70, platformImage[(int)(Math.random() * 4)]));
        platforms.add(new Platform(500, 880, 230, 45, platformImage[(int)(Math.random() * 4)]));
        platforms.add(new Platform(800, 820, 260, 45, platformImage[(int)(Math.random() * 4)]));
        platforms.add(new Platform(1110, 820, 260, 45, platformImage[(int)(Math.random() * 4)]));
        platforms.add(new Platform(1640, 900, 280, 65, platformImage[(int)(Math.random() * 4)]));

        traps.add(new Spike(430, 880, 55, 50, spikeImage));
        traps.add(new Spike(1210, 770, 55, 50, spikeImage));

        traps.add(new Saw(
                930, 765,
                60, 60,
                1030, 765,
                155,
                sawFrames
        ));

        platforms.add(new MovingPit(
                1410, 850,
                170, 45,
                1410, 930,
                140,
                platformImage[(int)(Math.random() * 4)]
        ));

        traps.add(new FlyingKnife(
                1840, 790,
                70, 28,
                -1, 0,
                470,
                280,
                720,
                knifeImage
        ));

        portals.add(new Portal(
                350, 865,
                55, 65,
                815, 770,
                portalImage
        ));

        portals.add(new Portal(
                1320, 755,
                55, 65,
                1660, 850,
                portalImage
        ));

        gate = new Gate(1800, 850, 50, 50, gateImage);
    }
}
