import java.awt.*;

public class player {
    private static final int WORLD_WIDTH = 1920;
    private static final int WORLD_HEIGHT = 1080;
    public static final double DEFAULT_JUMP_POWER = 550;

    double x, y;
    int height = 50, width = 50;
    Image[] jump, left, stay;
    boolean faceRight = true;


    int currentFrame = 0;
    double animationTimer = 0;
    double animationSpeed = 0.12;

    double velocityX, velocityY;

    double speed = 250;
    double jumpPower = DEFAULT_JUMP_POWER;
    double gravity = 1200;

    boolean leftPressed = false;
    boolean rightPressed = false;
    boolean jumpPressed = false;

    boolean onGround = false;

    boolean dead = false;
    double deadTimer = 0;

    boolean reachedGate = false;

    public player(Image[] stay, Image[] left, Image[] jump) {
        this.stay = stay;
        this.left = left;
        this.jump = jump;
    }

    public void setSprites(Image[] stay, Image[] left, Image[] jump) {
        this.stay = stay;
        this.left = left;
        this.jump = jump;
        currentFrame = 0;
        animationTimer = 0;
    }

    public player() {

    }

    public void updatePlayer(double dt) {

        if (dead) {
            velocityX = 0;
            velocityY = 0;
            leftPressed = false;
            rightPressed = false;
            jumpPressed = false;
            return;
        }

        velocityX = 0;

        if (leftPressed) {
            velocityX = -speed;
            faceRight = false;
        }

        if (rightPressed) {
            velocityX = speed;
            faceRight = true;
        }

        if (jumpPressed && onGround) {
            velocityY = -jumpPower;
            onGround = false;
        }

        velocityY += gravity * dt;

        x += velocityX * dt;
        y += velocityY * dt;

        updateAnimation(dt);


        if (y + height >= WORLD_HEIGHT) {
            y = WORLD_HEIGHT - height;
            velocityY = 0;
            onGround = true;
            die();
        }

        if(y - height < -50){
            y = 0;
            velocityY = 0;
            die();
        }


        if (x + width >= WORLD_WIDTH) {
            x = WORLD_WIDTH - width;
            velocityX = 0;
            die();
        }

        if (x - width < -50) {
            x = 0;
            velocityX = 0;
            die();
        }


        // Die when leaving the map.
        if (y < -50) {
            die();
        }

        if (y > WORLD_HEIGHT + 50) {
            die();
        }

        if (x < -50) {
            die();
        }

        if (x + width > WORLD_WIDTH + 50) {
            die();
        }
    }

    public void die() {
        if (dead) {
            return;
        }

        dead = true;
        deadTimer = 0;
        velocityX = 0;
        velocityY = 0;
        leftPressed = false;
        rightPressed = false;
        jumpPressed = false;
    }

    public void updateDeadTimer(double dt) {
        if (dead) {
            deadTimer += dt;
        }
    }

    public void respawn(double spawnX, double spawnY) {
        x = spawnX;
        y = spawnY;
        velocityX = 0;
        velocityY = 0;
        leftPressed = false;
        rightPressed = false;
        jumpPressed = false;
        onGround = false;
        dead = false;
        deadTimer = 0;
    }

    private void updateAnimation(double dt) {
        animationTimer += dt;

        if (animationTimer >= animationSpeed) {
            currentFrame++;
            currentFrame %= 5;
            animationTimer = 0;
        }
    }

    public Image getCurrentImage() {

        if (!onGround) {
            return jump[currentFrame];
        }

        if (leftPressed || rightPressed) {
            return left[currentFrame];
        }
        return stay[currentFrame];
    }
}
