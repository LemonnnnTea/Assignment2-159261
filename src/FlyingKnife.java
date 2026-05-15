import java.awt.*;

public class FlyingKnife extends Trap {

    double startX, startY;

    double velocityX;
    double velocityY;

    double speed;
    double triggerDistance;
    double maxFlyDistance;

    boolean triggered = false;

    public FlyingKnife(double x, double y, double width, double height,
                       double directionX, double directionY,
                       double speed, double triggerDistance,
                       double maxFlyDistance,
                       Image image) {

        super(x, y, width, height, image);

        this.startX = x;
        this.startY = y;

        this.speed = speed;
        this.triggerDistance = triggerDistance;
        this.maxFlyDistance = maxFlyDistance;

        double length = Math.sqrt(directionX * directionX + directionY * directionY);

        if (length != 0) {
            velocityX = directionX / length * speed;
            velocityY = directionY / length * speed;
        }
    }

    @Override
    public void update(double dt, player p1, player p2) {

        if (!active) {
            return;
        }

        if (!triggered) {
            double d1 = CollisionManager.distance(x, y, p1.x, p1.y);
            double d2 = CollisionManager.distance(x, y, p2.x, p2.y);

            if (d1 <= triggerDistance || d2 <= triggerDistance) {
                triggered = true;
            }
        }

        if (triggered) {
            x += velocityX * dt;
            y += velocityY * dt;

            double flyDistance = CollisionManager.distance(startX, startY, x, y);

            if (flyDistance >= maxFlyDistance) {
                active = false;
            }
        }
    }

    @Override
    public boolean checkCollision(player p) {

        if (!active || !triggered) {
            return false;
        }

        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                x, y, width, height
        );
    }

    public boolean isTriggered() {
        return triggered;
    }
}