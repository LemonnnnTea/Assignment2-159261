import java.awt.*;

public class Spike extends Trap {

    public Spike(double x, double y, double width, double height, Image image) {
        super(x, y, width, height, image);
    }

    @Override
    public void update(double dt, player p1, player p2) {
        // 地刺不动
    }

    @Override
    public boolean checkCollision(player p) {
        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                x, y, width, height
        );
    }
}