import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class MovingPit extends Platform {
    private static final double RESET_DELAY = 1.0;

    private final double pitWidth;
    private final double startPitX;
    private final double targetPitX;
    private final double triggerDistance;
    private final int approachDirection;
    private final double speed;

    private double pitX;
    private boolean moving = false;
    private boolean finished = false;
    private boolean resetting = false;
    private double resetTimer = 0;
    private player triggerPlayer;

    public MovingPit(double regionX,
                     double regionY,
                     double regionWidth,
                     double height,
                     double pitX,
                     double pitWidth,
                     double moveDistance,
                     int moveDirection,
                     double triggerDistance,
                     int approachDirection,
                     double speed,
                     Image image) {
        super(regionX, regionY, regionWidth, height, image);
        this.pitX = pitX;
        this.startPitX = pitX;
        this.pitWidth = pitWidth;
        this.targetPitX = pitX + moveDistance * moveDirection;
        this.triggerDistance = triggerDistance;
        this.approachDirection = approachDirection;
        this.speed = speed;
    }

    @Override
    public void update(double dt, player p1, player p2) {
        if (!moving && !finished && !resetting) {
            triggerPlayer = getTriggerPlayer(p1, p2);

            if (triggerPlayer != null) {
                moving = true;
            }
        }

        if (moving) {
            movePit(dt);
        }

        updateResetTimer(dt);
    }

    private void movePit(double dt) {
        double dx = targetPitX - pitX;
        double maxStep = speed * dt;

        if (Math.abs(dx) <= maxStep) {
            pitX = targetPitX;
            moving = false;
            finished = true;
            return;
        }

        pitX += Math.signum(dx) * maxStep;
    }

    private void updateResetTimer(double dt) {
        if (!resetting && shouldStartReset()) {
            resetting = true;
            resetTimer = 0;
        }

        if (!resetting) {
            return;
        }

        resetTimer += dt;

        if (resetTimer >= RESET_DELAY && !moving) {
            reset();
        }
    }

    private void reset() {
        pitX = startPitX;
        moving = false;
        finished = false;
        resetting = false;
        resetTimer = 0;
        triggerPlayer = null;
    }

    private boolean shouldStartReset() {
        if (triggerPlayer == null) {
            return false;
        }

        return triggerPlayer.dead || hasPlayerPassed(triggerPlayer);
    }

    private player getTriggerPlayer(player p1, player p2) {
        if (isApproaching(p1)) {
            return p1;
        }

        if (isApproaching(p2)) {
            return p2;
        }

        return null;
    }

    private boolean hasPlayerPassed(player p) {
        if (p == null) {
            return false;
        }

        if (approachDirection > 0) {
            return p.x > x + width;
        }

        return p.x + p.width < x;
    }

    @Override
    public List<Rectangle2D.Double> getCollisionBounds() {
        return getSolidBounds();
    }

    @Override
    public List<Rectangle2D.Double> getDrawBounds() {
        return getSolidBounds();
    }

    private List<Rectangle2D.Double> getSolidBounds() {
        ArrayList<Rectangle2D.Double> bounds = new ArrayList<>();

        double regionLeft = x;
        double regionRight = x + width;
        double pitLeft = Math.max(regionLeft, pitX);
        double pitRight = Math.min(regionRight, pitX + pitWidth);

        addSolidBounds(bounds, regionLeft, pitLeft);
        addSolidBounds(bounds, pitRight, regionRight);

        return bounds;
    }

    private void addSolidBounds(ArrayList<Rectangle2D.Double> bounds, double left, double right) {
        double segmentWidth = right - left;

        if (segmentWidth > 0) {
            bounds.add(new Rectangle2D.Double(left, y, segmentWidth, height));
        }
    }

    private boolean isApproaching(player p) {
        if (!canTrigger(p) || !isVerticallyNear(p)) {
            return false;
        }

        if (approachDirection > 0) {
            double gap = startPitX - (p.x + p.width);
            return gap >= 0 && gap <= triggerDistance;
        }

        double gap = p.x - (startPitX + pitWidth);
        return gap >= 0 && gap <= triggerDistance;
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

    private boolean canTrigger(player p) {
        return p != null && !p.dead && !p.reachedGate;
    }

    public boolean isMoving() {
        return moving;
    }
}
