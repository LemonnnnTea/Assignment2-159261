import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

public class BreakawayPitPlatform extends Platform {
    private static final double TRIGGER_TOLERANCE = 12;
    private static final double RESET_DELAY = 1.0;

    private boolean disappeared = false;
    private double resetTimer = 0;

    public BreakawayPitPlatform(double x, double y, Image image) {
        super(x, y, TILE_SIZE, TILE_SIZE, image);
    }

    @Override
    public void update(double dt, player p1, player p2) {
        if (disappeared) {
            resetTimer += dt;

            if (resetTimer >= RESET_DELAY) {
                disappeared = false;
                resetTimer = 0;
            }

            return;
        }

        if (isTriggeredBy(p1) || isTriggeredBy(p2)) {
            disappeared = true;
            resetTimer = 0;
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

    @Override
    public List<Rectangle2D.Double> getCollisionBounds() {
        if (disappeared) {
            return Collections.emptyList();
        }

        return super.getCollisionBounds();
    }

    private boolean isTriggeredBy(player p) {
        if (p == null || p.dead || p.reachedGate) {
            return false;
        }

        boolean horizontalOverlap = p.x < x + width && p.x + p.width > x;
        double playerBottom = p.y + p.height;
        boolean touchingTop = playerBottom >= y - 1 && playerBottom <= y + TRIGGER_TOLERANCE;

        return (horizontalOverlap && touchingTop) ||
                CollisionManager.rectCollision(p.x, p.y, p.width, p.height, x, y, width, height);
    }
}
