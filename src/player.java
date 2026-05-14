import java.awt.*;

public class player {
    double x, y;
    int height = 50, width = 50;
    Image img;

    double velocityX, velocityY;

    double speed = 250;
    double jumpPower = 550;
    double gravity = 1200;

    boolean leftPressed = false;
    boolean rightPressed = false;
    boolean jumpPressed = false;

    boolean onGround = false;


    public player(Image img){
        this.img = img;
    }

    public player(){

    }

    public void updatePlayer(double dt) {

        velocityX = 0;

        if (leftPressed) {
            velocityX = -speed;
        }

        if (rightPressed) {
            velocityX = speed;
        }

        if (jumpPressed && onGround) {
            velocityY = -jumpPower;
            onGround = false;
        }

        velocityY += gravity * dt;

        x += velocityX * dt;
        y += velocityY * dt;


        if (y + height >= 500) {
            y = 500 - height;
            velocityY = 0;
            onGround = true;
        }
    }


}
