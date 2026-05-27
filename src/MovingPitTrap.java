import java.awt.*;

public class MovingPitTrap extends Trap {

    private final double startX;
    private final double endX;
    private final double speed;

    private boolean movingToEnd = true;

    public MovingPitTrap(double x,
                         double y,
                         double width,
                         double height,
                         double endX,
                         double speed,
                         Image image) {
        super(x, y, width, height, image);
        this.startX = x;
        this.endX = endX;
        this.speed = speed;
    }

    @Override
    public void update(double dt, player p1, player p2) {
        double targetX = movingToEnd ? endX : startX;
        double dx = targetX - x;
        double maxStep = speed * dt;

        if (Math.abs(dx) <= maxStep) {
            x = targetX;
            movingToEnd = !movingToEnd;
            return;
        }

        x += Math.signum(dx) * maxStep;
    }

    @Override
    public boolean checkCollision(player p) {
        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                x, y - height, width, height
        );
    }
}
