import java.awt.*;

public class MovingPit extends Trap {

    double timer = 0;
    double openTime;
    double closeTime;

    boolean open = false;

    public MovingPit(double x, double y, double width, double height,
                     double openTime, double closeTime, Image image) {

        super(x, y, width, height, image);

        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    @Override
    public void update(double dt, player p1, player p2) {
        timer += dt;

        if (open && timer >= openTime) {
            open = false;
            timer = 0;
        }

        if (!open && timer >= closeTime) {
            open = true;
            timer = 0;
        }
    }

    @Override
    public boolean checkCollision(player p) {

        if (!open) {
            return false;
        }

        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                x, y, width, height
        );
    }

    public boolean isOpen() {
        return open;
    }
}