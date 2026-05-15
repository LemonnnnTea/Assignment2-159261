import java.awt.*;

public class level1 extends level {

    public level1(Image backgroundImage) {
        super(backgroundImage);
    }

    @Override
    public void load(
            player player1,
            player player2,
            Image platformImage,
            Image spikeImage,
            Image sawImage,
            Image pitImage,
            Image knifeImage,
            Image portalImage,
            Image gateImage
    ) {
        this.player1 = player1;
        this.player2 = player2;

        platforms.clear();
        traps.clear();
        portals.clear();

        spawnX1 = 60;
        spawnY1 = 300;

        spawnX2 = 120;
        spawnY2 = 300;

        player1.x = spawnX1;
        player1.y = spawnY1;

        player2.x = spawnX2;
        player2.y = spawnY2;

        goalX = 700;
        goalY = 300;
        goalWidth = 70;
        goalHeight = 100;

        platforms.add(new Platform(0, 400, 220, 50, platformImage));
        platforms.add(new Platform(300, 360, 160, 40, platformImage));
        platforms.add(new Platform(540, 320, 240, 40, platformImage));

        traps.add(new Spike(230, 350, 50, 50, spikeImage));

        traps.add(new Saw(
                340, 310,
                50, 50,
                430, 310,
                120,
                sawImage
        ));

        traps.add(new MovingPit(
                500, 370,
                80, 30,
                1.5,
                1.5,
                pitImage
        ));

        traps.add(new FlyingKnife(
                760, 250,
                50, 20,
                -1, 0,
                350,
                230,
                600,
                knifeImage
        ));

        portals.add(new Portal(
                170, 350,
                40, 50,
                560, 260,
                portalImage
        ));
        gate = new Gate(750, 270, 50, 50, gateImage);
    }
}