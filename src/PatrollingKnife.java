import java.awt.*;

public class PatrollingKnife extends Trap {

    private final double startX;
    private final double startY;
    private final double endX;
    private final double endY;
    private final double speed;

    private boolean movingToEnd = true;

    public PatrollingKnife(double x,
                           double y,
                           double width,
                           double height,
                           double endX,
                           double endY,
                           double speed,
                           Image image) {
        this(x, y, width, height, x, y, endX, endY, speed, image);
    }

    public PatrollingKnife(double x,
                           double y,
                           double width,
                           double height,
                           double startX,
                           double startY,
                           double endX,
                           double endY,
                           double speed,
                           Image image) {
        super(x, y, width, height, image);
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.speed = speed;
    }

    @Override
    public void update(double dt, player p1, player p2) {
        double targetX = movingToEnd ? endX : startX;
        double targetY = movingToEnd ? endY : startY;

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 2) {
            movingToEnd = !movingToEnd;
            return;
        }

        x += dx / distance * speed * dt;
        y += dy / distance * speed * dt;
    }

    @Override
    public boolean checkCollision(player p) {
        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                x, y, width, height
        );
    }
}
