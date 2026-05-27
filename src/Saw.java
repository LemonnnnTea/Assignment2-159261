import java.awt.*;

public class Saw extends Trap {

    double startX, startY;
    double endX, endY;
    double speed;

    boolean movingToEnd = true;

    Image[] frames;
    int currentFrame = 0;

    double animationTimer = 0;
    double animationSpeed = 0.1;

    public Saw(double x, double y,
               double width, double height,
               double endX, double endY,
               double speed,
               Image[] frames) {
        this(x, y, width, height, x, y, endX, endY, speed, frames);
    }

    public Saw(double x, double y,
               double width, double height,
               double startX, double startY,
               double endX, double endY,
               double speed,
               Image[] frames) {

        super(x, y, width, height, frames[0]);

        this.frames = frames;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.speed = speed;
    }

    @Override
    public void update(double dt, player p1, player p2) {
        updateAnimation(dt);

        double targetX = movingToEnd ? endX : startX;
        double targetY = movingToEnd ? endY : startY;

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 2) {
            movingToEnd = !movingToEnd;
            return;
        }

        x += dx / distance * speed * dt;
        y += dy / distance * speed * dt;
    }

    private void updateAnimation(double dt) {
        animationTimer += dt;

        if (animationTimer >= animationSpeed) {
            animationTimer = 0;
            currentFrame++;

            if (currentFrame >= frames.length) {
                currentFrame = 0;
            }

            image = frames[currentFrame];
        }
    }

    @Override
    public boolean checkCollision(player p) {
        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                x, y, width, height
        );
    }
}
