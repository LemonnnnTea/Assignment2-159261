import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WindVent {
    private static final double TARGET_LIFT_SPEED = -180;
    private static final double LIFT_ACCELERATION = 2600;
    private static final double PARTICLE_INTERVAL = 0.025;

    double x, y;
    double width, height;
    double zoneX, zoneY;
    double zoneWidth, zoneHeight;

    private double particleTimer = 0;
    private final ArrayList<WindParticle> particles = new ArrayList<>();

    public WindVent(double x,
                    double y,
                    double width,
                    double height,
                    double zoneX,
                    double zoneY,
                    double zoneWidth,
                    double zoneHeight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.zoneX = zoneX;
        this.zoneY = zoneY;
        this.zoneWidth = zoneWidth;
        this.zoneHeight = zoneHeight;
    }

    public void update(double dt, player p1, player p2) {
        applyLift(p1, dt);
        applyLift(p2, dt);
        spawnParticles(dt);
        updateParticles(dt);
    }

    private void applyLift(player p, double dt) {
        if (!canAffect(p) || !isInsideWind(p)) {
            return;
        }

        if (p.velocityY > TARGET_LIFT_SPEED) {
            p.velocityY -= LIFT_ACCELERATION * dt;
        }

        if (p.velocityY < TARGET_LIFT_SPEED) {
            p.velocityY = TARGET_LIFT_SPEED;
        }

        p.onGround = false;
    }

    private boolean isInsideWind(player p) {
        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                zoneX, zoneY, zoneWidth, zoneHeight
        );
    }

    private boolean canAffect(player p) {
        return p != null && !p.dead && !p.reachedGate;
    }

    private void spawnParticles(double dt) {
        particleTimer += dt;

        while (particleTimer >= PARTICLE_INTERVAL) {
            particleTimer -= PARTICLE_INTERVAL;

            double particleX = zoneX + Math.random() * zoneWidth;
            double particleY = y - 5 + Math.random() * 12;
            double velocityX = -18 + Math.random() * 36;
            double velocityY = -110 - Math.random() * 90;
            double radius = 3 + Math.random() * 4;
            double life = 0.7 + Math.random() * 0.45;

            particles.add(new WindParticle(
                    particleX,
                    particleY,
                    velocityX,
                    velocityY,
                    radius,
                    life,
                    new Color(170, 235, 255)
            ));
        }
    }

    private void updateParticles(double dt) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            WindParticle particle = particles.get(i);
            particle.update(dt);

            if (!particle.isAlive() || particle.y < zoneY - 30) {
                particles.remove(i);
            }
        }
    }

    public List<WindParticle> getParticles() {
        return particles;
    }
}
