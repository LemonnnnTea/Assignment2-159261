import java.awt.*;

public class Spike extends Trap {
    private static final double COLLISION_HORIZONTAL_INSET = 5;
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
        // Use only the sharp tip area so brushing the sprite sides is not unfairly lethal.
        double collisionX = x + COLLISION_HORIZONTAL_INSET;
        double collisionY = y + height - COLLISION_HEIGHT;
        double collisionWidth = width - COLLISION_HORIZONTAL_INSET * 2;

        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                collisionX, collisionY, collisionWidth, COLLISION_HEIGHT
        );
    }
}
