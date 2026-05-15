import java.awt.*;

public class Saw extends Trap {

    double startX, startY;
    double endX, endY;
    double speed;

    boolean movingToEnd = true;

    public Saw(double x, double y, double width, double height,
               double endX, double endY, double speed, Image image) {

        super(x, y, width, height, image);

        this.startX = x;
        this.startY = y;
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