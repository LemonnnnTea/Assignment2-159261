import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

public class Platform {
    public static final int TILE_SIZE = 50;

    double x, y;
    double width, height;
    Image image;

    public Platform(double x, double y, double width, double height, Image image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
    }

    public void update(double dt, player p1, player p2) {
    }

    public boolean isSolid() {
        return true;
    }

    public boolean isVisible() {
        return true;
    }

    public List<Rectangle2D.Double> getCollisionBounds() {
        if (!isSolid()) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new Rectangle2D.Double(x, y, width, height));
    }

    public List<Rectangle2D.Double> getDrawBounds() {
        if (!isVisible()) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new Rectangle2D.Double(x, y, width, height));
    }

    public boolean checkCollision(player p) {
        for (Rectangle2D.Double bounds : getCollisionBounds()) {
            if (CollisionManager.rectCollision(
                    p.x, p.y, p.width, p.height,
                    bounds.x, bounds.y, bounds.width, bounds.height
            )) {
                return true;
            }
        }

        return false;
    }
}
