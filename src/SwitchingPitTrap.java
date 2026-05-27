import java.awt.*;

public class SwitchingPitTrap extends Trap {

    private static final double WARNING_TIME = 0.5;
    private static final double BLINK_TIME = 0.1;

    private final double ax;
    private final double ay;
    private final double bx;
    private final double by;
    private final double switchTime;

    private boolean atA = true;
    private double timer = 0;

    public SwitchingPitTrap(double ax,
                            double ay,
                            double width,
                            double height,
                            double bx,
                            double by,
                            double switchTime,
                            Image image) {
        super(ax, ay, width, height, image);
        this.ax = ax;
        this.ay = ay;
        this.bx = bx;
        this.by = by;
        this.switchTime = switchTime;
    }

    @Override
    public void update(double dt, player p1, player p2) {
        timer += dt;

        if (timer >= switchTime) {
            timer -= switchTime;
            atA = !atA;
        }

        if (isWarning()) {
            x = nextX();
            y = nextY();
        } else {
            x = currentX();
            y = currentY();
        }
    }

    @Override
    public boolean checkCollision(player p) {
        if (isWarning()) {
            return false;
        }

        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                x, y - height, width, height
        );
    }

    @Override
    public boolean isVisible() {
        if (!active) {
            return false;
        }

        if (!isWarning()) {
            return true;
        }

        return ((int)((timer - (switchTime - WARNING_TIME)) / BLINK_TIME)) % 2 == 0;
    }

    private boolean isWarning() {
        return timer >= switchTime - WARNING_TIME;
    }

    private double currentX() {
        return atA ? ax : bx;
    }

    private double currentY() {
        return atA ? ay : by;
    }

    private double nextX() {
        return atA ? bx : ax;
    }

    private double nextY() {
        return atA ? by : ay;
    }
}
