import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

// Platform tile that disappears after contact and later resets for repeated attempts.
public class BreakawayPitPlatform extends Platform {
    private static final double TRIGGER_TOLERANCE = 12;
    private static final double DEFAULT_FALL_DELAY = 0.0;
    private static final double DEFAULT_RESET_DELAY = 1.0;

    private final double fallDelay;
    private final double resetDelay;
    private boolean triggered = false;
    private boolean disappeared = false;
    private double timer = 0;

    public BreakawayPitPlatform(double x, double y, Image image) {
        this(x, y, image, DEFAULT_FALL_DELAY, DEFAULT_RESET_DELAY);
    }

    public BreakawayPitPlatform(double x, double y, Image image, double fallDelay, double resetDelay) {
        super(x, y, TILE_SIZE, TILE_SIZE, image);
        this.fallDelay = fallDelay;
        this.resetDelay = resetDelay;
    }

    @Override
    public void update(double dt, player p1, player p2) {
        if (disappeared) {
            timer += dt;

            if (timer >= resetDelay) {
                triggered = false;
                disappeared = false;
                timer = 0;
            }

            return;
        }

        if (!triggered && (isTriggeredBy(p1) || isTriggeredBy(p2))) {
            // Trigger from standing or overlapping so edge landings still break the tile.
            triggered = true;
            timer = 0;
        }

        if (triggered) {
            timer += dt;

            if (timer >= fallDelay) {
                disappeared = true;
                timer = 0;
            }
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
