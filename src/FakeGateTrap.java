import java.awt.*;

public class FakeGateTrap extends Trap {
    private static final double RESET_DELAY = 1.0;
    private static final double TRIGGER_DISTANCE = 5.0;

    private final double startX;
    private final double startY;
    private final double retreatDistance;
    private final Image doorImage;
    private final Image spikeImage;

    private boolean triggered = false;
    private double timer = 0;

    public FakeGateTrap(double x,
                        double y,
                        double width,
                        double height,
                        double retreatDistance,
                        Image doorImage,
                        Image spikeImage) {
        super(x, y, width, height, doorImage);
        this.startX = x;
        this.startY = y;
        this.retreatDistance = retreatDistance;
        this.doorImage = doorImage;
        this.spikeImage = spikeImage;
    }

    @Override
    public void update(double dt, player p1, player p2) {
        if (!triggered && (isNearDoor(p1) || isNearDoor(p2))) {
            triggered = true;
            timer = 0;
        }

        if (!triggered) {
            x = startX;
            y = startY;
            return;
        }

        timer += dt;
        double ratio = Math.min(1.0, timer / RESET_DELAY);
        x = startX + retreatDistance * ratio;
        y = startY;

        if (timer >= RESET_DELAY) {
            triggered = false;
            timer = 0;
            x = startX;
            y = startY;
        }
    }

    @Override
    public boolean checkCollision(player p) {
        if (!triggered || p == null || p.dead || p.reachedGate) {
            return false;
        }

        double playerCenterX = p.x + p.width / 2.0;
        double playerBottom = p.y + p.height;

        return playerCenterX >= startX &&
                playerCenterX <= startX + width &&
                playerBottom >= startY &&
                playerBottom <= startY + height + 5 &&
                p.velocityY >= 0;
    }

    private boolean isNearDoor(player p) {
        if (p == null || p.dead || p.reachedGate) {
            return false;
        }

        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                startX - TRIGGER_DISTANCE,
                startY - TRIGGER_DISTANCE,
                width + TRIGGER_DISTANCE * 2,
                height + TRIGGER_DISTANCE * 2
        );
    }

    public boolean isSpikeVisible() {
        return triggered;
    }

    public Image getDoorImage() {
        return doorImage;
    }

    public Image getSpikeImage() {
        return spikeImage;
    }

    public double getSpikeX() {
        return startX;
    }

    public double getSpikeY() {
        return startY;
    }
}
