import java.awt.*;

public class HiddenSpike extends Spike {
    private static final double POP_DELAY = 0.3;
    private static final double ACTIVE_TIME = 1.5;

    private final double triggerX;
    private final double triggerY;
    private final double triggerWidth;
    private final double triggerHeight;

    private int state = 0;
    private double timer = 0;

    public HiddenSpike(double x,
                       double y,
                       double width,
                       double height,
                       double triggerX,
                       double triggerY,
                       double triggerWidth,
                       double triggerHeight,
                       Image image) {
        super(x, y, width, height, image);
        this.triggerX = triggerX;
        this.triggerY = triggerY;
        this.triggerWidth = triggerWidth;
        this.triggerHeight = triggerHeight;
    }

    @Override
    public void update(double dt, player p1, player p2) {
        if (state == 0 && (isTriggeredBy(p1) || isTriggeredBy(p2))) {
            state = 1;
            timer = 0;
        }

        if (state == 0) {
            return;
        }

        timer += dt;

        if (state == 1 && timer >= POP_DELAY) {
            state = 2;
            timer = 0;
        } else if (state == 2 && timer >= ACTIVE_TIME) {
            state = 0;
            timer = 0;
        }
    }

    @Override
    public boolean checkCollision(player p) {
        return state == 2 && super.checkCollision(p);
    }

    @Override
    public boolean isVisible() {
        return active && state == 2;
    }

    private boolean isTriggeredBy(player p) {
        if (p == null || p.dead || p.reachedGate) {
            return false;
        }

        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                triggerX, triggerY, triggerWidth, triggerHeight
        );
    }
}
