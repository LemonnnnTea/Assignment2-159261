import java.awt.*;

// Minimal boss sprite holder kept for older level code that only needs position and image data.
public class boss {
    double x, y;
    Image img;

    public void updatePlayer(){

    }

    public boss(double x, double y, Image img){
        this.x = x;
        this.y = y;
        this.img = img;
    }
}
