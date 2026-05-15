import java.awt.*;

public class Portal {

    double x, y;
    double width, height;

    double targetX, targetY;

    Image image;

    public Portal(double x, double y, double width, double height,
                  double targetX, double targetY,
                  Image image) {

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.targetX = targetX;
        this.targetY = targetY;

        this.image = image;
    }

    public boolean checkCollision(player p) {
        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                x, y, width, height
        );
    }

    public void teleport(player p) {
        p.x = targetX;
        p.y = targetY;
        p.velocityX = 0;
        p.velocityY = 0;
    }
}