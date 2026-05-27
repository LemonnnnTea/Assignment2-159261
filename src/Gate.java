import java.awt.*;

public class Gate {

    double x, y;
    double width, height;

    Image[] images;

    boolean player1Reached = false;
    boolean player2Reached = false;

    boolean completed = false;

    enum GateState {
        CLOSED,
        OPENING,
        CLOSING
    }

    GateState state = GateState.CLOSED;

    int currentFrame = 0;

    double timer = 0;

    double animationTime = 1.0;
    double frameTime;

    public Gate(double x,
                double y,
                double width,
                double height,
                Image[] images) {

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.images = images;

        frameTime = animationTime / images.length;
    }

    public void update(double dt) {

        if (state == GateState.CLOSED) {
            return;
        }

        timer += dt;

        if (timer >= frameTime) {

            timer = 0;

            // 开门
            if (state == GateState.OPENING) {

                currentFrame++;

                if (currentFrame >= images.length - 1) {

                    currentFrame = images.length - 1;

                    state = GateState.CLOSING;
                }
            }

            // 关门
            else if (state == GateState.CLOSING) {

                currentFrame--;

                if (currentFrame <= 0) {

                    currentFrame = 0;

                    state = GateState.CLOSED;

                    // 第二个人动画结束后才完成
                    if (player1Reached || player2Reached) {
                        completed = true;
                    }
                }
            }
        }
    }

    public boolean checkCollision(player p) {

        return CollisionManager.rectCollision(
                p.x, p.y, p.width, p.height,
                x, y, width, height
        );
    }

    public void playerReach(int playerNumber) {

        // 防止重复进入
        if (playerNumber == 1 && player1Reached) {
            return;
        }

        if (playerNumber == 2 && player2Reached) {
            return;
        }

        // 标记进入
        if (playerNumber == 1) {
            player1Reached = true;
        }

        if (playerNumber == 2) {
            player2Reached = true;
        }

        startAnimation();
    }

    private void startAnimation() {

        state = GateState.OPENING;

        currentFrame = 0;

        timer = 0;
    }

    public boolean hasPlayerReached(int playerNumber) {

        if (playerNumber == 1) {
            return player1Reached;
        }

        if (playerNumber == 2) {
            return player2Reached;
        }

        return false;
    }

    public boolean isCompleted() {

        return completed;
    }

    public Image getCurrentImage() {

        return images[currentFrame];
    }

    public void reset() {

        player1Reached = false;
        player2Reached = false;

        completed = false;

        state = GateState.CLOSED;

        currentFrame = 0;

        timer = 0;
    }
}
