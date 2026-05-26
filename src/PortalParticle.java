import java.awt.*;

public class PortalParticle {
    double x, y;
    double velocityX, velocityY;
    double radius;
    double life;
    double maxLife;
    Color color;

    public PortalParticle(double x, double y, double velocityX, double velocityY, double radius, double maxLife, Color color) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.radius = radius;
        this.maxLife = maxLife;
        this.life = maxLife;
        this.color = color;
    }

    public void update(double dt) {
        x += velocityX * dt;
        y += velocityY * dt;
        velocityX *= 0.96;
        velocityY *= 0.96;
        life -= dt;
    }

    public boolean isAlive() {
        return life > 0;
    }

    public double alphaRatio() {
        if (maxLife <= 0) {
            return 0;
        }

        return Math.max(0, life / maxLife);
    }
}
