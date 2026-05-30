import java.awt.*;

// Shared trap contract: traps update themselves, report player hits, and can customize collision effects.
public abstract class Trap {

    double x, y;
    double width, height;
    Image image;

    boolean active = true;

    public Trap(double x, double y, double width, double height, Image image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
    }

    public abstract void update(double dt, player p1, player p2);

    public abstract boolean checkCollision(player p);

    public void onCollide(player p) {
        // Most traps simply kill; fake doors and return doors override this behavior.
        p.die();
    }

    public boolean isActive() {
        return active;
    }

    public boolean isVisible() {
        return active;
    }
}
