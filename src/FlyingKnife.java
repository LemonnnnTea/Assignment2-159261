import java.awt.*;

public class FlyingKnife extends Trap {

    double startX, startY;

    double velocityX;
    double velocityY;

    double speed;
    double triggerDistance;
    double maxFlyDistance;
    double triggerX, triggerY, triggerWidth, triggerHeight;

    boolean triggered = false;
    boolean triggerByArea = false;
    boolean visibleBeforeTriggered = true;

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

    public FlyingKnife(double x, double y, double width, double height,
                       double directionX, double directionY,
                       double speed,
                       double triggerX, double triggerY, double triggerWidth, double triggerHeight,
                       double maxFlyDistance,
                       Image image) {

        this(x, y, width, height, directionX, directionY, speed, 0, maxFlyDistance, image);

        this.triggerX = triggerX;
        this.triggerY = triggerY;
        this.triggerWidth = triggerWidth;
        this.triggerHeight = triggerHeight;
        this.triggerByArea = true;
        this.visibleBeforeTriggered = false;
    }

    @Override
    public void update(double dt, player p1, player p2) {

        if (!active) {
            return;
        }

        if (!triggered) {
            if (isTriggeredBy(p1) || isTriggeredBy(p2)) {
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

    private boolean playerCanTrigger(player p) {
        return p != null && !p.dead && !p.reachedGate;
    }

    private boolean isTriggeredBy(player p) {
        if (!playerCanTrigger(p)) {
            return false;
        }

        if (triggerByArea) {
            return CollisionManager.rectCollision(
                    p.x, p.y, p.width, p.height,
                    triggerX, triggerY, triggerWidth, triggerHeight
            );
        }

        return CollisionManager.distance(x, y, p.x, p.y) <= triggerDistance;
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

    public boolean shouldFlipImage() {
        return velocityX > 0;
    }

    public double getDirectionAngleDegrees() {
        return Math.toDegrees(Math.atan2(velocityY, velocityX) - Math.PI);
    }

    @Override
    public boolean isVisible() {
        return active && (visibleBeforeTriggered || triggered);
    }
}
