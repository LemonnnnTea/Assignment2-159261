import java.awt.event.KeyEvent;

public class Game extends GameEngine{

    level level;

    public static void main(String[] args) {
        createGame(new Game(), 60);
    }

    public void drawLevel(level Level){
        drawImage(level.backgroundImage, 0, 0);
    }

    @Override
    public void update(double dt) {

    }

    @Override
    public void paintComponent() {
        drawLevel(level);
        for(platform p : level.platforms){

        }

        for(trap t : level.traps){

        }
    }

    @Override
    public void init(){
        setWindowSize(800, 450);
        level = new level1(loadImage("resources/bg1.png"));
    }

    @Override
    public void keyPressed(KeyEvent event) {

    }

    @Override
    public void keyReleased(KeyEvent event) {

    }

    @Override
    public void keyTyped(KeyEvent event) {

    }
}