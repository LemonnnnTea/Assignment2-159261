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

    boolean dead = false;


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


        if (y + height >= 450) {
            y = 450 - height;
            velocityY = 0;
            onGround = true;
            dead = true;
        }

        if(y - height < -50){
            y = 0;
            velocityY = 0;
            dead = true;
        }


        if (x + width >= 800) {
            x = 800 - width;
            velocityX = 0;
            dead = true;
        }

        if (x - width < -50) {
            x = 0;
            velocityX = 0;
            dead = true;
        }
    }


}
