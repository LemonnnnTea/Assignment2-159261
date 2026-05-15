import java.awt.*;
import java.awt.image.BufferedImage;

public class Gate {

    double x, y;
    double width, height;
    Image[] frames;
    int totalFrames;

    int currentFrame = 0;
    double animationTimer = 0;
    double framesPerSecond = 10.0;

    boolean player1Reached = false;
    boolean player2Reached = false;
    boolean isOpening = false;
    boolean isFullyOpen = false;

    public Gate(double x, double y, double width, double height, Image spritesheet) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        int frameWidth = (int)width;
        int frameHeight = (int)height;

        BufferedImage buffered = (BufferedImage)spritesheet;
        int sheetWidth = buffered.getWidth();
        int sheetHeight = buffered.getHeight();

        int framesPerRow = sheetWidth / frameWidth;
        int totalRows = sheetHeight / frameHeight;

        totalFrames = framesPerRow * totalRows;
        frames = new Image[totalFrames];

        int frameIndex = 0;
        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < framesPerRow; col++) {
                if (frameIndex < totalFrames) {
                    frames[frameIndex] = buffered.getSubimage(
                            col * frameWidth,
                            row * frameHeight,
                            frameWidth,
                            frameHeight
                    );
                    frameIndex++;
                }
            }
        }
    }

    public void update(double dt) {
        if (isOpening && !isFullyOpen) {
            animationTimer += dt;

            double timePerFrame = 1.0 / framesPerSecond;

            if (animationTimer >= timePerFrame) {
                currentFrame++;
                animationTimer = 0;

                if (currentFrame >= totalFrames) {
                    currentFrame = totalFrames - 1;
                    isFullyOpen = true;
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

    public void playerReach(int playerNum) {
        if (playerNum == 1) {
            player1Reached = true;
        } else if (playerNum == 2) {
            player2Reached = true;
        }

        if (!isOpening) {
            isOpening = true;
        }
    }

    public Image getCurrentImage() {
        if (frames == null || frames.length == 0) {
            return null;
        }

        if (currentFrame >= frames.length) {
            return frames[frames.length - 1];
        }
        return frames[currentFrame];
    }

    public boolean isCompleted() {
        return player1Reached && player2Reached && isFullyOpen;
    }

    public boolean hasPlayerReached(int playerNum) {
        if (playerNum == 1) {
            return player1Reached;
        } else if (playerNum == 2) {
            return player2Reached;
        }
        return false;
    }

    public void reset() {
        player1Reached = false;
        player2Reached = false;
        isOpening = false;
        isFullyOpen = false;
        currentFrame = 0;
        animationTimer = 0;
    }
}
