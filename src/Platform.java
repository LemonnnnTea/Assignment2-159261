import java.awt.*;

public class Platform {

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

    public boolean checkCollision(player p) {
        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                x, y, width, height
        );
    }
}