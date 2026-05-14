import java.awt.event.KeyEvent;

public class Game extends GameEngine{

    level level;
    int currentLevel;

    player[] player = new player[2];

    public static void main(String[] args) {
        createGame(new Game(), 60);
    }

    public void drawLevel(level Level){
        drawImage(level.backgroundImage, 0, 0);
    }

    public void drawPlayer(player player1, player player2){
        drawImage(player1.img, player1.x, player1.y);
        drawImage(player2.img,   player2.x, player2.y);
    }

    @Override
    public void update(double dt) {
        player[0].updatePlayer(dt);
        player[1].updatePlayer(dt);
    }

    @Override
    public void paintComponent() {
        drawLevel(level);
        for(platform p : level.platforms){

        }

        for(trap t : level.traps){

        }

        drawPlayer(player[0], player[1]);
    }

    @Override
    public void init(){
        setWindowSize(800, 450);
        currentLevel = 1;

        level = new level1(loadImage("resources/bg1.png"));
        player[0] = new player(loadImage("resources/playerMale.png"));
        player[1] = new player(loadImage("resources/playerMale.png"));


    }

    @Override
    public void keyPressed(KeyEvent event) {


        if(event.getKeyCode() == KeyEvent.VK_LEFT){
            player[1].leftPressed = true;
        }

        if(event.getKeyCode() == KeyEvent.VK_RIGHT){
            player[1].rightPressed = true;
        }

        if(event.getKeyCode() == KeyEvent.VK_UP){
            player[1].jumpPressed = true;
        }

        if(event.getKeyCode() == KeyEvent.VK_DOWN){

        }


        if(event.getKeyCode() == KeyEvent.VK_A){
            player[0].leftPressed = true;
        }

        if(event.getKeyCode() == KeyEvent.VK_D){
            player[0].rightPressed = true;
        }

        if(event.getKeyCode() == KeyEvent.VK_W){
            player[0].jumpPressed = true;
        }

        if(event.getKeyCode() == KeyEvent.VK_S){

        }
    }

    @Override
    public void keyReleased(KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.VK_LEFT) {
            player[1].leftPressed = false;
        }

        if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
            player[1].rightPressed = false;
        }

        if (event.getKeyCode() == KeyEvent.VK_UP) {
            player[1].jumpPressed = false;
        }
        if (event.getKeyCode() == KeyEvent.VK_A) {
            player[0].leftPressed = false;
        }

        if (event.getKeyCode() == KeyEvent.VK_D) {
            player[0].rightPressed = false;
        }

        if (event.getKeyCode() == KeyEvent.VK_W) {
            player[0].jumpPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent event) {

    }
}