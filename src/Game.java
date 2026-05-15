import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class Game extends GameEngine{

    level level;
    int currentLevel;

    player[] player = new player[2];
    Image spritesheet;

    int mouseX = 0;
    int mouseY = 0;

    boolean startHover = false;
    boolean helpHover = false;
    boolean quitHover = false;

    boolean showHelp = false;

    public static void main(String[] args) {
        createGame(new Game(), 60);
    }

    public void drawLevel(level Level) {
        drawImage(Level.backgroundImage, 0, 0, width(), height());

        for (Platform platform : Level.getPlatforms()) {
            drawPlatform(platform);
        }

        for (Portal portal : Level.getPortals()) {
            drawPortal(portal);
        }

        for (Trap trap : Level.getTraps()) {
            if (trap.isActive()) {
                drawTrap(trap);
            }
        }
    }

    public void drawMenuButton(double x, double y, double w, double h, String text, boolean hover) {

        if (hover) {
            changeColor(100, 180, 255);
        } else {
            changeColor(60, 70, 95);
        }

        drawSolidRectangle(x, y, w, h);

        changeColor(255, 255, 255);
        drawRectangle(x, y, w, h, 2);

        changeColor(255, 255, 255);
        drawBoldText(x + 35, y + 34, text, "Arial", 20);
    }

    public void drawPlayer(player player1, player player2){

        if(player1.faceRight){
            drawImage(player1.getCurrentImage(), player1.x + 50, player1.y, -player1.width, player1.height);
        }else{
            drawImage(player1.getCurrentImage(), player1.x, player1.y, player1.width, player1.height);

        }

        if(player2.faceRight){
            drawImage(player2.getCurrentImage(), player2.x + 50, player2.y, -player2.width, player2.height);
        }else{
            drawImage(player2.getCurrentImage(), player2.x, player2.y, player2.width, player2.height);
        }

    }

    private void drawTrap(Trap trap) {
        drawImage(trap.image, trap.x, trap.y, trap.width, trap.height);
    }

    private void drawPlatform(Platform platform) {
        drawImage(
                platform.image,
                platform.x,
                platform.y,
                platform.width,
                platform.height
        );
    }

    private void drawPortal(Portal portal) {
        drawImage(
                portal.image,
                portal.x,
                portal.y,
                portal.width,
                portal.height
        );
    }

    @Override
    public void update(double dt) {
        if (currentLevel == 1) {
            level.update(dt);
        }
    }

    @Override
    public void paintComponent() {
        if (currentLevel == 0) {

            changeBackgroundColor(25, 28, 40);
            clearBackground(width(), height());

            changeColor(255, 255, 255);
            drawBoldText(210, 100, "DOUBLE I WANNA", "Arial", 42);

            changeColor(180, 180, 180);
            drawText(265, 140, "Two Players Challenge Game", "Arial", 18);

            changeColor(100, 180, 255);
            drawSolidRectangle(200, 160, 400, 4);

            changeColor(45, 50, 70);
            drawSolidRectangle(70, 210, 260, 250);

            changeColor(255, 255, 255);
            drawBoldText(95, 245, "Controls", "Arial", 24);

            changeColor(220, 220, 220);
            drawText(95, 285, "Player 1:  W A D", "Arial", 18);
            drawText(95, 315, "Player 2:  Arrow Keys", "Arial", 18);
            drawText(95, 345, "Avoid traps", "Arial", 18);
            drawText(95, 375, "Reach the goal together", "Arial", 18);
            drawText(95, 405, "Defeat the final boss", "Arial", 18);

            drawMenuButton(460, 220, 220, 50, "START GAME", startHover);
            drawMenuButton(460, 295, 220, 50, "HELP", helpHover);
            drawMenuButton(460, 370, 220, 50, "QUIT", quitHover);

            changeColor(160, 160, 160);
            drawText(230, 530, "Press ENTER to start | Press H for help | Press ESC to quit", "Arial", 16);

            if (showHelp) {
                changeColor(0, 0, 0);
                drawSolidRectangle(120, 120, 560, 330);

                changeColor(255, 255, 255);
                drawRectangle(120, 120, 560, 330, 3);

                drawBoldText(285, 165, "HELP", "Arial", 34);

                changeColor(220, 220, 220);
                drawText(160, 210, "This is a two-player platform game.", "Arial", 18);
                drawText(160, 245, "Both players must survive traps.", "Arial", 18);
                drawText(160, 280, "Some levels require cooperation.", "Arial", 18);
                drawText(160, 315, "Final level contains a boss fight.", "Arial", 18);
                drawText(160, 365, "Press H or click HELP again to close.", "Arial", 18);
            }
        }
        else{
            drawLevel(level);
            for(Platform p : level.platforms){

            }

            for(Trap t : level.traps){

            }

            drawPlayer(player[0], player[1]);
        }
    }

    @Override
    public void init(){
        setWindowSize(800, 450);
        currentLevel = 0;

        level = new level1(loadImage("resources/bg1.png"));
        Image spritesheet = loadImage("resources/playerMale.png");

        Image[] stay = new Image[5];
        Image[] left = new Image[5];
        Image[] jump = new Image[5];

        int spriteSize = 50;

        for (int i = 0; i < 5; i++) {
            stay[i] = subImage(spritesheet, i * spriteSize, 0, spriteSize, spriteSize);
        }

        for (int i = 0; i < 5; i++) {
            left[i] = subImage(spritesheet, i * spriteSize, spriteSize, spriteSize, spriteSize);
        }

        for (int i = 0; i < 5; i++) {
            jump[i] = subImage(spritesheet, i * spriteSize, spriteSize * 2, spriteSize, spriteSize);
        }

        player[0] = new player(stay, left, jump);
        player[1] = new player(stay, left, jump);

        Image platformImage = loadImage("resources/platform.png");
        Image spikeImage = loadImage("resources/spike.png");
        Image sawImage = loadImage("resources/saw.png");
        Image pitImage = loadImage("resources/pit.png");
        Image knifeImage = loadImage("resources/knife.png");
        Image portalImage = loadImage("resources/portal.png");

        level = new level1(loadImage("resources/bg1.png"));

        level.load(
                player[0],
                player[1],
                platformImage,
                spikeImage,
                sawImage,
                pitImage,
                knifeImage,
                portalImage
        );

    }

    @Override
    public void keyPressed(KeyEvent event) {


        if (currentLevel == 0) {

            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                currentLevel = 1;
                // loadLevel(1);
            }

            if (event.getKeyCode() == KeyEvent.VK_H) {
                showHelp = !showHelp;
            }

            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }

            return;
        }

        else{

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

    @Override
    public void mouseMoved(MouseEvent event) {

        mouseX = event.getX();
        mouseY = event.getY();

        startHover = isMouseInside(460, 220, 220, 50);
        helpHover = isMouseInside(460, 295, 220, 50);
        quitHover = isMouseInside(460, 370, 220, 50);
    }

    public boolean isMouseInside(double x, double y, double w, double h) {
        return mouseX >= x &&
                mouseX <= x + w &&
                mouseY >= y &&
                mouseY <= y + h;
    }

    @Override
    public void mousePressed(MouseEvent event) {

        mouseX = event.getX();
        mouseY = event.getY();

        if (currentLevel == 0) {

            if (isMouseInside(460, 220, 220, 50)) {
                currentLevel = 1;
            }

            if (isMouseInside(460, 295, 220, 50)) {
                showHelp = !showHelp;
            }

            if (isMouseInside(460, 370, 220, 50)) {
                System.exit(0);
            }
        }
    }
}