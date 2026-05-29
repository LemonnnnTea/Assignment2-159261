import java.awt.*;

public class PlayerKnife {
    private static final double WORLD_WIDTH = 1920;
    private static final double WORLD_HEIGHT = 1080;
    private static final double SIZE = 42;
    private static final double SPEED = 950;

    double x;
    double y;
    double width = SIZE;
    double height = SIZE;
    double velocityX;
    double velocityY;
    int ownerPlayerNumber;
    Image image;

    public PlayerKnife(player owner, int ownerPlayerNumber, Image image) {
        this.ownerPlayerNumber = ownerPlayerNumber;
        this.image = image;

        int direction = owner.faceRight ? 1 : -1;
        x = owner.faceRight ? owner.x + owner.width : owner.x - width;
        y = owner.y + owner.height / 2.0 - height / 2.0;
        velocityX = direction * SPEED;
        velocityY = 0;
    }

    public void update(double dt) {
        x += velocityX * dt;
        y += velocityY * dt;
    }

    public boolean isOutsideWorld() {
        return x + width < 0 || x > WORLD_WIDTH || y + height < 0 || y > WORLD_HEIGHT;
    }

    public double getDirectionAngleDegrees() {
        return Math.toDegrees(Math.atan2(velocityY, velocityX));
    }
}
