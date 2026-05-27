import java.awt.*;

public class DisappearingPlatform extends Platform {

    private final double triggerDistance;
    private final int approachDirection;
    private boolean disappeared = false;

    public DisappearingPlatform(double x,
                                double y,
                                double width,
                                double height,
                                double triggerDistance,
                                int approachDirection,
                                Image image) {
        super(x, y, width, height, image);
        this.triggerDistance = triggerDistance;
        this.approachDirection = approachDirection;
    }

    @Override
    public void update(double dt, player p1, player p2) {
        if (disappeared) {
            if (hasCleared(p1) && hasCleared(p2)) {
                disappeared = false;
            }
            return;
        }

        if (isApproaching(p1) || isApproaching(p2)) {
            disappeared = true;
        }
    }

    @Override
    public boolean isSolid() {
        return !disappeared;
    }

    @Override
    public boolean isVisible() {
        return !disappeared;
    }

    private boolean isApproaching(player p) {
        if (!canAffect(p) || !isVerticallyNear(p)) {
            return false;
        }

        if (approachDirection > 0) {
            double gap = x - (p.x + p.width);
            return gap >= 0 && gap <= triggerDistance;
        }

        double gap = p.x - (x + width);
        return gap >= 0 && gap <= triggerDistance;
    }

    private boolean hasCleared(player p) {
        if (!canAffect(p)) {
            return true;
        }

        double playerCenterX = p.x + p.width / 2.0;

        if (approachDirection > 0) {
            return playerCenterX > x + width;
        }

        return playerCenterX < x;
    }

    private boolean isVerticallyNear(player p) {
        double dy = 0;

        if (p.y + p.height < y) {
            dy = y - (p.y + p.height);
        } else if (p.y > y + height) {
            dy = p.y - (y + height);
        }

        return dy <= triggerDistance;
    }

    private boolean canAffect(player p) {
        return p != null && !p.dead && !p.reachedGate;
    }
}
