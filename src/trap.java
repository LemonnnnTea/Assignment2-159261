import java.awt.*;

public abstract class trap {
    double x, y;
    double width, height;
    Image img;

    public trap(double x, double y, double width, double height, Image img) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = img;
    }

    public trap(){

    }

    public abstract void updateTrap();
}