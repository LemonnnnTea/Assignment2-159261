import java.awt.*;

public class FakeGateTrap extends Trap {
    private static final double ANIMATION_TIME = 1.0;

    private final Image[] doorFrames;
    private final double frameTime;

    private GateState state = GateState.CLOSED;
    private int currentFrame = 0;
    private double timer = 0;
    private player trappedPlayer;
    private int trappedPlayerNumber = 0;
    private boolean player1EatenThisFrame = false;
    private boolean player2EatenThisFrame = false;

    enum GateState {
        CLOSED,
        OPENING,
        CLOSING
    }

    public FakeGateTrap(double x,
                        double y,
                        double width,
                        double height,
                        Image[] doorFrames) {
        super(x, y, width, height, doorFrames[0]);
        this.doorFrames = doorFrames;

        int animationSteps = Math.max(1, (doorFrames.length - 1) * 2);
        frameTime = ANIMATION_TIME / animationSteps;
    }

    @Override
    public void update(double dt, player p1, player p2) {
        player1EatenThisFrame = false;
        player2EatenThisFrame = false;

        if (state == GateState.CLOSED) {
            image = doorFrames[0];
            return;
        }

        timer += dt;

        if (timer < frameTime) {
            return;
        }

        timer = 0;

        if (state == GateState.OPENING) {
            currentFrame++;

            if (currentFrame >= doorFrames.length - 1) {
                currentFrame = doorFrames.length - 1;
                state = GateState.CLOSING;
            }
        } else if (state == GateState.CLOSING) {
            currentFrame--;

            if (currentFrame <= 0) {
                currentFrame = 0;
                finishEatingPlayer();
            }
        }

        image = doorFrames[currentFrame];
    }

    @Override
    public boolean checkCollision(player p) {
        if (state != GateState.CLOSED || p == null || p.dead || p.reachedGate || p.trappedInFakeGate) {
            return false;
        }

        double playerCenterX = p.x + p.width / 2.0;
        double playerCenterY = p.y + p.height / 2.0;

        return playerCenterX > x &&
                playerCenterX < x + width &&
                playerCenterY > y &&
                playerCenterY < y + height;
    }

    @Override
    public void onCollide(player p) {
        if (state != GateState.CLOSED || p == null || p.dead || p.reachedGate || p.trappedInFakeGate) {
            return;
        }

        trappedPlayer = p;
        trappedPlayerNumber = 0;
        p.trappedInFakeGate = true;
        p.velocityX = 0;
        p.velocityY = 0;
        p.leftPressed = false;
        p.rightPressed = false;
        p.jumpPressed = false;

        startAnimation();
    }

    public void setTrappedPlayerNumber(int playerNumber) {
        if (trappedPlayer != null && trappedPlayerNumber == 0) {
            trappedPlayerNumber = playerNumber;
        }
    }

    private void startAnimation() {
        state = GateState.OPENING;
        currentFrame = 0;
        timer = 0;
        image = doorFrames[0];
    }

    private void finishEatingPlayer() {
        state = GateState.CLOSED;

        if (trappedPlayer != null) {
            trappedPlayer.trappedInFakeGate = false;
            trappedPlayer.die();

            if (trappedPlayerNumber == 1) {
                player1EatenThisFrame = true;
            } else if (trappedPlayerNumber == 2) {
                player2EatenThisFrame = true;
            }
        }

        trappedPlayer = null;
        trappedPlayerNumber = 0;
        timer = 0;
    }

    public boolean didEatPlayer(int playerNumber) {
        if (playerNumber == 1) {
            return player1EatenThisFrame;
        }

        if (playerNumber == 2) {
            return player2EatenThisFrame;
        }

        return false;
    }

    public Image getDoorImage() {
        return doorFrames[currentFrame];
    }
}
