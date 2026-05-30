import java.awt.*;

public class player {
    private static final int WORLD_WIDTH = 1920;
    private static final int WORLD_HEIGHT = 1080;
    public static final double DEFAULT_SPEED = 250;
    public static final double DEFAULT_JUMP_POWER = 550;

    double x, y;
    double previousX, previousY;
    int height = 50, width = 50;
    Image[] jump, left, stay;
    boolean faceRight = true;


    int currentFrame = 0;
    double animationTimer = 0;
    double animationSpeed = 0.12;

    double velocityX, velocityY;

    double speed = DEFAULT_SPEED;
    double jumpPower = DEFAULT_JUMP_POWER;
    double gravity = 1200;
    private int powerLevel = 1;
    private double speedMultiplier = 1.0;

    boolean leftPressed = false;
    boolean rightPressed = false;
    boolean jumpPressed = false;

    boolean onGround = false;

    boolean dead = false;
    double deadTimer = 0;

    boolean reachedGate = false;
    boolean trappedInFakeGate = false;

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

    public void setPowerLevel(int level) {
        if (level < 1) {
            level = 1;
        }

        if (level > 10) {
            level = 10;
        }

        powerLevel = level;
        updateSpeed();
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public void setSpeedMultiplier(double multiplier) {
        speedMultiplier = multiplier;
        updateSpeed();
    }

    private void updateSpeed() {
        // Power levels stay as small movement boosts so progression matters without breaking jumps.
        speed = DEFAULT_SPEED * (1.0 + (powerLevel - 1) * 0.01) * speedMultiplier;
    }

    public player() {

    }

    public void updatePlayer(double dt) {

        // Dead and trapped players are frozen until the level system respawns or releases them.
        if (dead) {
            velocityX = 0;
            velocityY = 0;
            leftPressed = false;
            rightPressed = false;
            jumpPressed = false;
            return;
        }

        if (trappedInFakeGate) {
            velocityX = 0;
            velocityY = 0;
            leftPressed = false;
            rightPressed = false;
            jumpPressed = false;
            return;
        }

        // Previous position is used by swept collision checks such as enemy stomp detection.
        previousX = x;
        previousY = y;

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


        // Leaving the playfield counts as death so players cannot bypass the level layout.
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
        trappedInFakeGate = false;
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
        previousX = spawnX;
        previousY = spawnY;
        velocityX = 0;
        velocityY = 0;
        // Clear held inputs so respawn never immediately walks or jumps into a hazard.
        leftPressed = false;
        rightPressed = false;
        jumpPressed = false;
        onGround = false;
        dead = false;
        deadTimer = 0;
        trappedInFakeGate = false;
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
