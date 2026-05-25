import java.awt.*;

public class MovingPit extends Platform {

    double targetX, targetY;
    double speed;
    double triggerDistance = 50;

    boolean moving = false;
    boolean finished = false;

    public MovingPit(double x, double y, double width, double height, double openTime, double closeTime, Image image) {
        this(x, y, width, height, x, y + 80, 220, image);
    }

    public MovingPit(double x, double y, double width, double height,
                     double targetX, double targetY, double speed,
                     Image image) {

        super(x, y, width, height, image);

        this.targetX = targetX;
        this.targetY = targetY;
        this.speed = speed;
    }

    public void update(double dt, player p1, player p2) {
        if (!moving && !finished && (isPlayerNear(p1) || isPlayerNear(p2))) {
            moving = true;
        }

        if (!moving) {
            return;
        }

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= speed * dt || distance == 0) {
            x = targetX;
            y = targetY;
            moving = false;
            finished = true;
            return;
        }

        x += dx / distance * speed * dt;
        y += dy / distance * speed * dt;
    }

    private boolean isPlayerNear(player p) {
        if (p == null || p.dead || p.reachedGate) {
            return false;
        }

        return distanceToPlayer(p) <= triggerDistance;
    }

    private double distanceToPlayer(player p) {
        double dx = 0;
        double dy = 0;

        if (p.x + p.width < x) {
            dx = x - (p.x + p.width);
        } else if (p.x > x + width) {
            dx = p.x - (x + width);
        }

        if (p.y + p.height < y) {
            dy = y - (p.y + p.height);
        } else if (p.y > y + height) {
            dy = p.y - (y + height);
        }

        return Math.sqrt(dx * dx + dy * dy);
    }

    public boolean isMoving() {
        return moving;
    }
}
