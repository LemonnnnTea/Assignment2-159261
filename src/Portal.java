import java.awt.*;

public class Portal {

    double x, y;
    double width, height;

    double targetX, targetY;

    Image image;
    Image[] frames;

    int currentFrame = 0;

    double animationTimer = 0;
    double animationSpeed = 0.12;

    public Portal(double x, double y, double width, double height,
                  double targetX, double targetY,
                  Image[] frames) {

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.targetX = targetX;
        this.targetY = targetY;

        this.frames = frames;
        if (frames != null && frames.length > 0) {
            this.image = frames[0];
        }
    }

    public Portal(double x, double y, double width, double height,
                  double targetX, double targetY,
                  Image image) {

        this(x, y, width, height, targetX, targetY, new Image[]{image});
    }

    public void update(double dt) {

        if (frames == null || frames.length == 0) {
            return;
        }

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

    public boolean checkCollision(player p) {
        return p.x <= x + width &&
                p.x + p.width >= x &&
                p.y <= y + height &&
                p.y + p.height >= y;
    }

    public void teleport(player p) {
        p.x = targetX;
        p.y = targetY;
        p.velocityX = 0;
        p.velocityY = 0;
    }
}
