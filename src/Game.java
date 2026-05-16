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
    boolean gameOver = false;
    boolean levelComplete = false;
    Image gameOverImage;
    Image victoryImage;

    Image[] platformImage;
    Image spikeImage;
    Image[] sawFrames;
    Image pitImage;
    Image knifeImage;
    Image portalImage;
    Image[] gateImage;

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

    private void drawSinglePlayer(player p) {
        if (p.faceRight) {
            drawImage(p.getCurrentImage(), p.x + 50, p.y, -p.width, p.height);
        } else {
            drawImage(p.getCurrentImage(), p.x, p.y, p.width, p.height);
        }
    }

    private void drawGate(Gate gate) {
        drawImage(gate.getCurrentImage(), gate.x, gate.y, gate.width, gate.height);
    }

    @Override
    public void update(double dt) {
        if (currentLevel >= 1 && currentLevel <= 5 && !gameOver && !levelComplete) {
            level.update(dt);

            if (level.isLevelComplete()) {
                levelComplete = true;
            } else if (player[0].dead || player[1].dead) {
                gameOver = true;
            }
        }
    }

    @Override
    public void paintComponent() {
        if (currentLevel == 0) {

            changeBackgroundColor(25, 28, 40);
            clearBackground(width(), height());

            changeColor(255, 255, 255);
            drawBoldText(210, 100, "You jump,I jump", "Arial", 42);

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
        else if (currentLevel >= 1 && currentLevel <= 5){
            drawLevel(level);
            for(Platform p : level.platforms){

            }

            for(Trap t : level.traps){

            }

            if (level.getGate() != null) {
                drawGate(level.getGate());
            }

            if (!gameOver && !levelComplete) {
                boolean showPlayer1 = !level.getGate().hasPlayerReached(1);
                boolean showPlayer2 = !level.getGate().hasPlayerReached(2);

                if (showPlayer1 && showPlayer2) {
                    drawPlayer(player[0], player[1]);
                } else if (showPlayer1) {
                    drawSinglePlayer(player[0]);
                } else if (showPlayer2) {
                    drawSinglePlayer(player[1]);
                }
            }

            if (gameOver) {
                changeColor(0, 0, 0);
                drawSolidRectangle(0, 0, width(), height());

                if (gameOverImage != null) {
                    double imageWidth = 400;
                    double imageHeight = 300;
                    double imageX = (width() - imageWidth) / 2;
                    double imageY = (height() - imageHeight) / 2 - 50;
                    drawImage(gameOverImage, imageX, imageY, imageWidth, imageHeight);
                }


                changeColor(255, 255, 255);
                drawText(220, 370, "Press R to restart", "Arial", 24);
            }

            if (levelComplete) {
                changeColor(0, 0, 0);
                drawSolidRectangle(0, 0, width(), height());

                if (victoryImage != null) {
                    double imageWidth = 400;
                    double imageHeight = 300;
                    double imageX = (width() - imageWidth) / 2;
                    double imageY = (height() - imageHeight) / 2 - 50;
                    drawImage(victoryImage, imageX, imageY, imageWidth, imageHeight);
                }

                changeColor(255, 215, 0);
                drawBoldText(280, 320, "VICTORY!", "Arial", 42);

                changeColor(255, 255, 255);
                drawText(180, 370, "Press SPACE for next level", "Arial", 20);
                drawText(220, 400, "Press R to restart", "Arial", 20);
            }
        }
    }


    @Override
    public void init(){
        setWindowSize(800, 450);
        currentLevel = 0;

        level = new level1(loadImage("resources/bg1.png"));
        Image playerSheet = loadImage("resources/playerMale.png");
        Image platformSheet = loadImage("resources/platform.png");
        Image gateSheet = loadImage("resources/gate.png");
        Image sawSheet = loadImage("resources/saw.png");

        Image[] stay = new Image[5];
        Image[] left = new Image[5];
        Image[] jump = new Image[5];

        int spriteSize = 50;

        for (int i = 0; i < 5; i++) {
            stay[i] = subImage(playerSheet, i * spriteSize, 0, spriteSize, spriteSize);
        }

        for (int i = 0; i < 5; i++) {
            left[i] = subImage(playerSheet, i * spriteSize, spriteSize, spriteSize, spriteSize);
        }

        for (int i = 0; i < 5; i++) {
            jump[i] = subImage(playerSheet, i * spriteSize, spriteSize * 2, spriteSize, spriteSize);
        }

        player[0] = new player(stay, left, jump);
        player[1] = new player(stay, left, jump);

        platformImage = new Image[4];

        platformImage[0] = subImage(platformSheet, 0, 0, 100, 20);
        platformImage[1] = subImage(platformSheet, 100, 0, 100, 20);
        platformImage[2] = subImage(platformSheet, 0, 20, 100, 20);
        platformImage[3] = subImage(platformSheet, 100, 20, 100, 20);

        gateImage = new Image[5];
        for (int i = 0; i < 5; i++) {
            gateImage[i] = subImage(gateSheet, i * 50, 0, 50, 50);
        }

        spikeImage = loadImage("resources/spike.png");


        Image[] sawFrames = new Image[2];

        sawFrames[0] = subImage(sawSheet, 0, 0, 50, 50);
        sawFrames[1] = subImage(sawSheet, 50, 0, 50, 50);
        pitImage = loadImage("resources/pit.png");
        knifeImage = loadImage("resources/knife.png");
        portalImage = loadImage("resources/portal.png");
        gameOverImage = loadImage("resources/oneplayersurvive.png");
        victoryImage = loadImage("resources/victory.png");


        level = new level1(loadImage("resources/bg1.png"));

        level.load(
                player[0],
                player[1],
                platformImage,
                spikeImage,
                sawFrames,
                pitImage,
                knifeImage,
                portalImage,
                gateImage
        );

    }

    private void restartLevel() {
        gameOver = false;
        levelComplete = false;

        if (currentLevel == 1) {
            level = new level1(loadImage("resources/bg1.png"));
        }
//         else if (currentLevel == 2) {
//            level = new level2(loadImage("resources/bg1.png"));
//        } else if (currentLevel == 3) {
//            level = new level3(loadImage("resources/bg1.png"));
//        } else if (currentLevel == 4) {
//            level = new level4(loadImage("resources/bg1.png"));
//        } else if (currentLevel == 5) {
//            level = new level5(loadImage("resources/bg1.png"));
//        }

        player[0].dead = false;
        player[1].dead = false;
        player[0].velocityX = 0;
        player[0].velocityY = 0;
        player[1].velocityX = 0;
        player[1].velocityY = 0;

        if (level.getGate() != null) {
            level.getGate().reset();
        }

        level.load(
                player[0],
                player[1],
                platformImage,
                spikeImage,
                sawFrames,
                pitImage,
                knifeImage,
                portalImage,
                gateImage
        );
    }

    private void nextLevel() {
        gameOver = false;
        levelComplete = false;

        currentLevel++;

        if (currentLevel > 5) {
            currentLevel = 5;
            return;
        }

//        if (currentLevel == 2) {
//            level = new level2(loadImage("resources/bg1.png"));
//        } else if (currentLevel == 3) {
//            level = new level3(loadImage("resources/bg1.png"));
//        } else if (currentLevel == 4) {
//            level = new level4(loadImage("resources/bg1.png"));
//        } else if (currentLevel == 5) {
//            level = new level5(loadImage("resources/bg1.png"));
//        }

        player[0].dead = false;
        player[1].dead = false;
        player[0].velocityX = 0;
        player[0].velocityY = 0;
        player[1].velocityX = 0;
        player[1].velocityY = 0;


        if (level.getGate() != null) {
            level.getGate().reset();
        }

        level.load(player[0], player[1], platformImage, spikeImage, sawFrames, pitImage, knifeImage, portalImage, gateImage);
    }



    @Override
    public void keyPressed(KeyEvent event) {
        if (currentLevel == 0) {
            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                currentLevel = 1;
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
            if (gameOver) {
                if (event.getKeyCode() == KeyEvent.VK_R) {
                    restartLevel();
                }
                return;
            }
            if (levelComplete) {
                if (event.getKeyCode() == KeyEvent.VK_SPACE) {
                    nextLevel();
                }
                if (event.getKeyCode() == KeyEvent.VK_R) {
                    restartLevel();
                }
                return;
            }

            if(event.getKeyCode() == KeyEvent.VK_LEFT){
                player[1].leftPressed = true;
            }

            if(event.getKeyCode() == KeyEvent.VK_RIGHT){
                player[1].rightPressed = true;
            }

            if(event.getKeyCode() == KeyEvent.VK_UP){
                player[1].jumpPressed = true;
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
