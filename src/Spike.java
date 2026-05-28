import java.awt.*;

public class Spike extends Trap {
    private static final double COLLISION_WIDTH = 50;
    private static final double COLLISION_HEIGHT = 10;

    public Spike(double x, double y, double width, double height, Image image) {
        super(x, y, width, height, image);
    }

    @Override
    public void update(double dt, player p1, player p2) {
        // Spikes do not move.
    }

    @Override
    public boolean checkCollision(player p) {
        double collisionX = x + (width - COLLISION_WIDTH) / 2;
        double collisionY = y + height - COLLISION_HEIGHT;

        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                collisionX, collisionY, COLLISION_WIDTH, COLLISION_HEIGHT
        );
    }
}
