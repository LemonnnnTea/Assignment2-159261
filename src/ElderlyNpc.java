import java.awt.*;

// Passive NPC used for short contextual hints without interrupting player control.
public class ElderlyNpc {
    static final double SIZE = 50;
    private static final double FRAME_TIME = 0.2;
    private static final double REST_TIME = 2.0;
    private static final double INTERACTION_RANGE = 150;
    private static final double MESSAGE_TIME = 1.0;

    double x;
    double y;

    private final Image[] frames;
    private int frameIndex = 0;
    private double animationTimer = 0;
    private double restTimer = 0;
    private String message = "";
    private double messageTimer = 0;

    public ElderlyNpc(double x, double y, Image[] frames) {
        this.x = x;
        this.y = y;
        this.frames = frames;
    }

    public void update(double dt) {
        updateAnimation(dt);

        if (messageTimer > 0) {
            messageTimer = Math.max(0, messageTimer - dt);
        }
    }

    private void updateAnimation(double dt) {
        if (restTimer > 0) {
            // Hold the first frame between cycles so the NPC reads as idle, not constantly twitching.
            restTimer = Math.max(0, restTimer - dt);
            frameIndex = 0;
            animationTimer = 0;
            return;
        }

        animationTimer += dt;

        if (animationTimer < FRAME_TIME) {
            return;
        }

        animationTimer -= FRAME_TIME;
        frameIndex++;

        if (frameIndex >= 5) {
            frameIndex = 0;
            restTimer = REST_TIME;
        }
    }

    public Image getCurrentImage() {
        if (frames == null || frames.length == 0) {
            return null;
        }

        return frames[Math.min(frameIndex, frames.length - 1)];
    }

    public boolean canInteract(player p) {
        if (p == null || p.dead || p.reachedGate || p.trappedInFakeGate) {
            return false;
        }

        // Interaction is distance-based so either player can talk from either side of the sprite.
        return distanceTo(p) <= INTERACTION_RANGE;
    }

    public double distanceTo(player p) {
        if (p == null) {
            return Double.MAX_VALUE;
        }

        double playerCenterX = p.x + p.width / 2.0;
        double playerCenterY = p.y + p.height / 2.0;
        double npcCenterX = x + SIZE / 2.0;
        double npcCenterY = y + SIZE / 2.0;

        return CollisionManager.distance(playerCenterX, playerCenterY, npcCenterX, npcCenterY);
    }

    public void showMessage(String message) {
        this.message = message;
        messageTimer = MESSAGE_TIME;
    }

    public boolean hasMessage() {
        return messageTimer > 0 && message != null && !message.isEmpty();
    }

    public String getMessage() {
        return message;
    }
}
