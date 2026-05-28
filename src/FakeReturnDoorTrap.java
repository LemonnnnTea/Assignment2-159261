import java.awt.*;

public class FakeReturnDoorTrap extends Trap {
    private static final double COOLDOWN = 0.35;
    private static final double MESSAGE_TIME = 2.0;

    private final double returnX1;
    private final double returnY1;
    private final double returnX2;
    private final double returnY2;
    private final String message;
    private double timer = 0;
    private double messageTimer = 0;

    public FakeReturnDoorTrap(double x,
                              double y,
                              double width,
                              double height,
                              double returnX,
                              double returnY,
                              Image image) {
        this(x, y, width, height, returnX, returnY, returnX, returnY, "", image);
    }

    public FakeReturnDoorTrap(double x,
                              double y,
                              double width,
                              double height,
                              double returnX1,
                              double returnY1,
                              double returnX2,
                              double returnY2,
                              Image image) {
        this(x, y, width, height, returnX1, returnY1, returnX2, returnY2, "", image);
    }

    public FakeReturnDoorTrap(double x,
                              double y,
                              double width,
                              double height,
                              double returnX,
                              double returnY,
                              String message,
                              Image image) {
        this(x, y, width, height, returnX, returnY, returnX, returnY, message, image);
    }

    public FakeReturnDoorTrap(double x,
                              double y,
                              double width,
                              double height,
                              double returnX1,
                              double returnY1,
                              double returnX2,
                              double returnY2,
                              String message,
                              Image image) {
        super(x, y, width, height, image);
        this.returnX1 = returnX1;
        this.returnY1 = returnY1;
        this.returnX2 = returnX2;
        this.returnY2 = returnY2;
        this.message = message;
    }

    @Override
    public void update(double dt, player p1, player p2) {
        if (messageTimer > 0) {
            messageTimer -= dt;
        }

        if (timer > 0) {
            timer -= dt;
            return;
        }

        boolean returned = false;
        returned = tryReturnPlayer(p1, returnX1, returnY1) || returned;
        returned = tryReturnPlayer(p2, returnX2, returnY2) || returned;

        if (returned) {
            timer = COOLDOWN;
            messageTimer = MESSAGE_TIME;
        }
    }

    @Override
    public boolean checkCollision(player p) {
        return false;
    }

    public String getMessage() {
        if (messageTimer <= 0 || message == null) {
            return "";
        }

        return message;
    }

    private boolean tryReturnPlayer(player p, double targetX, double targetY) {
        if (p == null || p.dead || p.reachedGate) {
            return false;
        }

        if (!CollisionManager.rectCollision(p.x, p.y, p.width, p.height, x, y, width, height)) {
            return false;
        }

        p.x = targetX;
        p.y = targetY;
        p.velocityX = 0;
        p.velocityY = 0;
        p.leftPressed = false;
        p.rightPressed = false;
        p.jumpPressed = false;
        p.onGround = false;
        return true;
    }
}
