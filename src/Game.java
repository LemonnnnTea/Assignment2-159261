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
    boolean showLevelSelect = false;
    boolean gamePaused = false;
    Image gameOverImage;
    Image victoryImage;

    boolean pauseReturnHover = false;
    boolean pauseHelpHover = false;
    boolean pauseQuitHover = false;
    boolean showPauseHelp = false;

    boolean pauseResumeHover = false;
    boolean pauseQuitLevelHover = false;
    boolean pauseMenuHover = false;

    double levelCompleteTimer = 0;
    boolean showAllLevelsComplete = false;
    double allLevelsCompleteTimer = 0;

    int maxUnlockedLevel = 1;

    class LevelButton {
        int levelNumber;
        double x, y, width, height;
        String levelName;
        boolean unlocked;

        LevelButton(int level, double x, double y, double w, double h, String name, boolean unlocked) {
            this.levelNumber = level;
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.levelName = name;
            this.unlocked = unlocked;
        }
    }

    LevelButton[] levelButtons;
    int hoveredLevelButton = -1;
    String levelDescription = "";

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

    private void drawLevelSelectScreen() {
        changeBackgroundColor(173, 216, 230);
        clearBackground(width(), height());

        changeColor(0, 0, 0);
        drawBoldText(280, 50, "SELECT LEVEL", "Arial", 36);

        for (int i = 0; i < levelButtons.length - 1; i++) {
            LevelButton current = levelButtons[i];
            LevelButton next = levelButtons[i + 1];

            double x1 = current.x + current.width / 2;
            double y1 = current.y + current.height / 2;
            double x2 = next.x + next.width / 2;
            double y2 = next.y + next.height / 2;

            changeColor(100, 100, 100);
            drawLine(x1, y1, x2, y2, 3);
        }

        for (LevelButton button : levelButtons) {
            if (button.unlocked) {
                changeColor(34, 139, 34);
            } else {
                changeColor(0, 0, 139);
            }

            drawSolidRectangle(button.x, button.y, button.width, button.height);

            changeColor(255, 255, 255);
            drawRectangle(button.x, button.y, button.width, button.height, 2);

            changeColor(255, 255, 255);
            String buttonText = button.unlocked ? "Level " + button.levelNumber : "Locked";
            drawBoldText(button.x + 15, button.y + 30, buttonText, "Arial", 18);
        }

        if (hoveredLevelButton >= 0 && hoveredLevelButton < levelButtons.length) {
            LevelButton button = levelButtons[hoveredLevelButton];

            double triangleX = button.x + button.width / 2;
            double triangleY = button.y - 15;
            double triangleSize = 15;

            changeColor(0, 0, 0);

            double[] xPoints = {triangleX - triangleSize / 2, triangleX + triangleSize / 2, triangleX};
            double[] yPoints = {triangleY - triangleSize, triangleY - triangleSize, triangleY};

            int[] xIntPoints = {(int)xPoints[0], (int)xPoints[1], (int)xPoints[2]};
            int[] yIntPoints = {(int)yPoints[0], (int)yPoints[1], (int)yPoints[2]};

            mGraphics.fillPolygon(xIntPoints, yIntPoints, 3);

            changeColor(0, 0, 0);
            drawSolidRectangle(250, 20, 300, 50);

            changeColor(255, 255, 255);
            drawRectangle(250, 20, 300, 50, 2);

            changeColor(255, 255, 255);
            drawBoldText(265, 50, button.levelName, "Arial", 18);
        } else {
            changeColor(0, 0, 0);
            drawSolidRectangle(250, 20, 300, 50);

            changeColor(255, 255, 255);
            drawRectangle(250, 20, 300, 50, 2);
        }

        changeColor(100, 100, 100);
        drawText(250, 420, "Click on unlocked levels to play | Press ESC to go back", "Arial", 16);
    }

    @Override
    public void update(double dt) {
        if (currentLevel >= 1 && currentLevel <= 5 && !gameOver && !levelComplete && !gamePaused) {
            level.update(dt);

            if (level.isLevelComplete()) {
                levelComplete = true;
                levelCompleteTimer = 0;

                if (currentLevel == 5) {
                    showAllLevelsComplete = true;
                    allLevelsCompleteTimer = 0;
                }
            } else if (player[0].dead || player[1].dead) {
                gameOver = true;
            }
        }

        if (levelComplete && currentLevel != 5) {
            levelCompleteTimer += dt;
        }

        if (showAllLevelsComplete) {
            allLevelsCompleteTimer += dt;
            if (allLevelsCompleteTimer >= 3.0) {
                showAllLevelsComplete = false;
                levelComplete = false;
                currentLevel = -1;
                showLevelSelect = true;
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
        else if (currentLevel == -1) {
            drawLevelSelectScreen();
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

            if (!gameOver && !levelComplete && !gamePaused) {
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

            if (gamePaused) {
                changeColor(0, 0, 0);
                drawSolidRectangle(0, 0, width(), height());

                changeColor(255, 255, 255);
                drawBoldText(320, 80, "PAUSED", "Arial", 42);

                drawMenuButton(280, 120, 240, 50, "RESUME", pauseResumeHover);
                drawMenuButton(280, 190, 240, 50, "QUIT", pauseQuitLevelHover);
                drawMenuButton(280, 260, 240, 50, "HELP", pauseHelpHover);
                drawMenuButton(280, 330, 240, 50, "MENU", pauseMenuHover);

                if (showPauseHelp) {
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
                    drawText(160, 365, "Press ESC to close help.", "Arial", 18);
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

                if (showAllLevelsComplete) {
                    changeColor(255, 215, 0);
                    drawBoldText(180, 150, "Congratulations!", "Arial", 42);

                    changeColor(255, 255, 255);
                    drawText(120, 220, "You have completed all the levels.", "Arial", 24);

                    int countdown = 3 - (int)allLevelsCompleteTimer;
                    if (countdown < 0) countdown = 0;

                    changeColor(200, 200, 200);
                    String countdownText = "Returning to level select... " + countdown;
                    drawText(260, 350, countdownText, "Arial", 18);
                } else {
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
                    drawText(200, 430, "Press ESC to return to level select", "Arial", 18);
                }
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


        sawFrames = new Image[2];

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

        initializeLevelButtons();
    }

    private void initializeLevelButtons() {
        levelButtons = new LevelButton[5];

        double buttonWidth = 100;
        double buttonHeight = 50;

        double startX = 100;
        double startY = 150;
        double spacingX = 180;
        double spacingY = 100;

        levelButtons[0] = new LevelButton(1, startX, startY, buttonWidth, buttonHeight, "Level 1: First Steps", true);
        levelButtons[1] = new LevelButton(2, startX + spacingX, startY, buttonWidth, buttonHeight, "Level 2: Double Trouble", false);
        levelButtons[2] = new LevelButton(3, startX + spacingX * 2, startY, buttonWidth, buttonHeight, "Level 3: Portal Jump", false);
        levelButtons[3] = new LevelButton(4, startX + spacingX * 2, startY + spacingY, buttonWidth, buttonHeight, "Level 4: Moving Danger", false);
        levelButtons[4] = new LevelButton(5, startX + spacingX, startY + spacingY, buttonWidth, buttonHeight, "Level 5: Boss Fight", false);
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
        player[0].reachedGate = false;
        player[1].reachedGate = false;
        player[0].leftPressed = false;
        player[0].rightPressed = false;
        player[0].jumpPressed = false;
        player[1].leftPressed = false;
        player[1].rightPressed = false;
        player[1].jumpPressed = false;
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

        if (currentLevel > maxUnlockedLevel) {
            maxUnlockedLevel = currentLevel;
            updateLevelButtons();
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
        player[0].reachedGate = false;
        player[1].reachedGate = false;
        player[0].leftPressed = false;
        player[0].rightPressed = false;
        player[0].jumpPressed = false;
        player[1].leftPressed = false;
        player[1].rightPressed = false;
        player[1].jumpPressed = false;
        player[0].velocityX = 0;
        player[0].velocityY = 0;
        player[1].velocityX = 0;
        player[1].velocityY = 0;


        if (level.getGate() != null) {
            level.getGate().reset();
        }

        level.load(player[0], player[1], platformImage, spikeImage, sawFrames, pitImage, knifeImage, portalImage, gateImage);
    }
    private void updateLevelButtons() {
        for (int i = 0; i < levelButtons.length; i++) {
            levelButtons[i].unlocked = (i + 1) <= maxUnlockedLevel;
        }
    }
    private void loadSelectedLevel(int levelNumber) {
        currentLevel = levelNumber;
        showLevelSelect = false;
        gameOver = false;
        levelComplete = false;

        if (levelNumber == 1) {
            level = new level1(loadImage("resources/bg1.png"));
        }
//         else if (levelNumber == 2) {
//            level = new level2(loadImage("resources/bg1.png"));
//        } else if (levelNumber == 3) {
//            level = new level3(loadImage("resources/bg1.png"));
//        } else if (levelNumber == 4) {
//            level = new level4(loadImage("resources/bg1.png"));
//        } else if (levelNumber == 5) {
//            level = new level5(loadImage("resources/bg1.png"));
//        }

        player[0].dead = false;
        player[1].dead = false;
        player[0].reachedGate = false;
        player[1].reachedGate = false;
        player[0].leftPressed = false;
        player[0].rightPressed = false;
        player[0].jumpPressed = false;
        player[1].leftPressed = false;
        player[1].rightPressed = false;
        player[1].jumpPressed = false;
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

    @Override
    public void keyPressed(KeyEvent event) {
        if (currentLevel == 0) {
            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                showLevelSelect = true;
                currentLevel = -1;
            }

            if (event.getKeyCode() == KeyEvent.VK_H) {
                showHelp = !showHelp;
            }

            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }

            return;
        }

        if (currentLevel == -1) {
            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                showLevelSelect = false;
                currentLevel = 0;
            }
            return;
        }


        else{
            if (gamePaused) {
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (showPauseHelp) {
                        showPauseHelp = false;
                    } else {
                        gamePaused = false;
                    }
                }
                return;
            }
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
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    levelComplete = false;
                    currentLevel = -1;
                    showLevelSelect = true;
                }
                return;
            }

            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                gamePaused = true;
                showPauseHelp = false;
                return;
            }

            if(event.getKeyCode() == KeyEvent.VK_LEFT){
                if (!player[1].reachedGate) {
                    player[1].leftPressed = true;
                }
            }

            if(event.getKeyCode() == KeyEvent.VK_RIGHT){
                if (!player[1].reachedGate) {
                    player[1].rightPressed = true;
                }
            }

            if(event.getKeyCode() == KeyEvent.VK_UP){
                if (!player[1].reachedGate) {
                    player[1].jumpPressed = true;
                }
            }

            if(event.getKeyCode() == KeyEvent.VK_A){
                if (!player[0].reachedGate) {
                    player[0].leftPressed = true;
                }
            }

            if(event.getKeyCode() == KeyEvent.VK_D){
                if (!player[0].reachedGate) {
                    player[0].rightPressed = true;
                }
            }

            if(event.getKeyCode() == KeyEvent.VK_W){
                if (!player[0].reachedGate) {
                    player[0].jumpPressed = true;
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.VK_LEFT) {
            if (!player[1].reachedGate) {
                player[1].leftPressed = false;
            }
        }

        if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (!player[1].reachedGate) {
                player[1].rightPressed = false;
            }
        }

        if (event.getKeyCode() == KeyEvent.VK_UP) {
            if (!player[1].reachedGate) {
                player[1].jumpPressed = false;
            }
        }
        if (event.getKeyCode() == KeyEvent.VK_A) {
            if (!player[0].reachedGate) {
                player[0].leftPressed = false;
            }
        }

        if (event.getKeyCode() == KeyEvent.VK_D) {
            if (!player[0].reachedGate) {
                player[0].rightPressed = false;
            }
        }

        if (event.getKeyCode() == KeyEvent.VK_W) {
            if (!player[0].reachedGate) {
                player[0].jumpPressed = false;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent event) {

    }

    @Override
    public void mouseMoved(MouseEvent event) {

        mouseX = event.getX();
        mouseY = event.getY();

        if (currentLevel == 0) {
            startHover = isMouseInside(460, 220, 220, 50);
            helpHover = isMouseInside(460, 295, 220, 50);
            quitHover = isMouseInside(460, 370, 220, 50);
        } else if (currentLevel == -1) {
            hoveredLevelButton = -1;
            for (int i = 0; i < levelButtons.length; i++) {
                if (isMouseInside(levelButtons[i].x, levelButtons[i].y, levelButtons[i].width, levelButtons[i].height)) {
                    hoveredLevelButton = i;
                    break;
                }
            }
        }  else if (currentLevel >= 1 && currentLevel <= 5 && gamePaused && !showPauseHelp) {
            pauseResumeHover = isMouseInside(280, 120, 240, 50);
            pauseQuitLevelHover = isMouseInside(280, 190, 240, 50);
            pauseHelpHover = isMouseInside(280, 260, 240, 50);
            pauseMenuHover = isMouseInside(280, 330, 240, 50);
        }
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
                showLevelSelect = true;
                currentLevel = -1;
            }

            if (isMouseInside(460, 295, 220, 50)) {
                showHelp = !showHelp;
            }

            if (isMouseInside(460, 370, 220, 50)) {
                System.exit(0);
            }
        } else if (currentLevel == -1) {
            if (hoveredLevelButton >= 0 && hoveredLevelButton < levelButtons.length) {
                if (levelButtons[hoveredLevelButton].unlocked) {
                    loadSelectedLevel(levelButtons[hoveredLevelButton].levelNumber);
                }
            }
        }
        else if (currentLevel >= 1 && currentLevel <= 5 && gamePaused && !showPauseHelp) {
            if (isMouseInside(280, 120, 240, 50)) {
                gamePaused = false;
            }

            if (isMouseInside(280, 190, 240, 50)) {
                gamePaused = false;
                showLevelSelect = true;
                currentLevel = -1;
            }

            if (isMouseInside(280, 260, 240, 50)) {
                showPauseHelp = true;
            }

            if (isMouseInside(280, 330, 240, 50)) {
                gamePaused = false;
                currentLevel = 0;
                showLevelSelect = false;
            }
        }
    }
}
