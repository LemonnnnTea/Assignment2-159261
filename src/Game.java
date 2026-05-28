import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class Game extends GameEngine{

    private static final int WINDOW_WIDTH = 1920;
    private static final int WINDOW_HEIGHT = 1080;
    private static final int LAST_IMPLEMENTED_LEVEL = 4;
    private static final double MAX_PHYSICS_DT = 1.0 / 30.0;

    private static final double MENU_BUTTON_X = 1320;
    private static final double MENU_BUTTON_Y = 400;
    private static final double MENU_BUTTON_W = 360;
    private static final double MENU_BUTTON_H = 72;
    private static final double MENU_BUTTON_GAP = 24;

    private static final double SETTINGS_X = 500;
    private static final double SETTINGS_Y = 210;
    private static final double SETTINGS_W = 920;
    private static final double SETTINGS_H = 560;
    private static final double SETTINGS_CLOSE_X = 1350;
    private static final double SETTINGS_CLOSE_Y = 250;
    private static final double SETTINGS_CLOSE_SIZE = 46;
    private static final double CHARACTER_BUTTON_Y = 410;
    private static final double CHARACTER_BUTTON_W = 220;
    private static final double CHARACTER_BUTTON_H = 64;
    private static final double CHARACTER_BUTTON_1_X = 580;
    private static final double CHARACTER_BUTTON_2_X = 850;
    private static final double CHARACTER_BUTTON_3_X = 1120;
    private static final double VOLUME_MINUS_X = 620;
    private static final double VOLUME_BUTTON_Y = 610;
    private static final double VOLUME_BUTTON_SIZE = 54;
    private static final double VOLUME_BAR_X = 720;
    private static final double VOLUME_BAR_Y = 630;
    private static final double VOLUME_BAR_W = 440;
    private static final double VOLUME_BAR_H = 14;
    private static final double VOLUME_PLUS_X = 1210;

    private static final double PAUSE_BUTTON_X = 780;
    private static final double PAUSE_BUTTON_Y = 420;
    private static final double PAUSE_BUTTON_W = 360;
    private static final double PAUSE_BUTTON_H = 72;
    private static final double PAUSE_BUTTON_GAP = 24;

    private static final Color COLOR_PANEL = new Color(18, 24, 38, 225);
    private static final Color COLOR_PANEL_SOLID = new Color(18, 24, 38);
    private static final Color COLOR_PANEL_LIGHT = new Color(35, 45, 66);
    private static final Color COLOR_ACCENT = new Color(86, 190, 255);
    private static final Color COLOR_ACCENT_2 = new Color(255, 190, 85);
    private static final Color COLOR_TEXT = new Color(245, 248, 255);
    private static final Color COLOR_MUTED_TEXT = new Color(184, 194, 214);
    private static final Color COLOR_GOOD = new Color(50, 170, 100);
    private static final Color COLOR_LOCKED = new Color(70, 78, 96);

    level level;
    int currentLevel;

    player[] player = new player[2];
    Image spritesheet;

    int mouseX = 0;
    int mouseY = 0;

    boolean startHover = false;
    boolean helpHover = false;
    boolean settingsHover = false;
    boolean quitHover = false;

    boolean showHelp = false;
    boolean showSettings = false;
    boolean gameOver = false;
    boolean levelComplete = false;
    boolean showLevelSelect = false;
    boolean gamePaused = false;
    Image gameOverImage;
    Image victoryImage;
    Image deadPigImage;
    AudioClip deadSound;
    AudioClip winSound;
    AudioClip transSound;

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

    int maxUnlockedLevel = LAST_IMPLEMENTED_LEVEL;
    int[] playerScores = new int[2];
    int[] levelDeaths = new int[2];
    int winningPlayer = 0;

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

    Image platformImage;
    Image spikeImage;
    Image[] sawFrames;
    Image pitImage;
    Image knifeImage;
    Image[] portalImage;
    Image[] gateImage;

    Image[] maleStay;
    Image[] maleLeft;
    Image[] maleJump;
    Image[] femaleStay;
    Image[] femaleLeft;
    Image[] femaleJump;

    int characterChoice = 0;
    int hoveredCharacterChoice = -1;
    int masterVolume = 80;
    float masterVolumeGain = 0.0f;
    boolean volumeMinusHover = false;
    boolean volumePlusHover = false;
    boolean volumeBarHover = false;
    boolean settingsCloseHover = false;

    public static void main(String[] args) {
        createGame(new Game(), 60);
    }

    public void drawLevel(level Level) {
        drawImage(Level.backgroundImage, 0, 0, width(), height());

        for (Platform platform : Level.getPlatforms()) {
            drawPlatform(platform);
        }

        for (WindVent windVent : Level.getWindVents()) {
            drawWindVent(windVent);
        }

        for (Portal portal : Level.getPortals()) {
            drawPortal(portal);
        }

        for (PortalParticle particle : Level.getPortalParticles()) {
            drawPortalParticle(particle);
        }

        for (Trap trap : Level.getTraps()) {
            if (trap.isVisible()) {
                drawTrap(trap);
            }
        }
    }

    private double menuButtonY(int index) {
        return MENU_BUTTON_Y + index * (MENU_BUTTON_H + MENU_BUTTON_GAP);
    }

    private double pauseButtonY(int index) {
        return PAUSE_BUTTON_Y + index * (PAUSE_BUTTON_H + PAUSE_BUTTON_GAP);
    }

    private void fillRoundRect(double x, double y, double w, double h, double arc, Color color) {
        changeColor(color);
        drawSolidRectangle(x, y, w, h);
    }

    private void drawRoundRect(double x, double y, double w, double h, double arc, double stroke, Color color) {
        changeColor(color);
        drawRectangle(x, y, w, h, stroke);
    }

    private void drawCenteredText(double x, double y, double w, String text, String font, int size, boolean bold, Color color) {
        changeColor(color);
        double textWidth = text.length() * size * 0.55;
        double textX = x + (w - textWidth) / 2;

        if (bold) {
            drawBoldText(textX, y, text, font, size);
        } else {
            drawText(textX, y, text, font, size);
        }
    }

    private void drawButtonLabel(double x, double y, double w, double h, String text, int size, Color color) {
        changeColor(color);
        double textWidth = text.length() * size * 0.58;
        double textX = x + (w - textWidth) / 2;
        double textY = y + (h + size * 0.7) / 2;
        drawBoldText(textX, textY, text, "Arial", size);
    }

    private void drawPanel(double x, double y, double w, double h) {
        fillRoundRect(x, y, w, h, 8, COLOR_PANEL);
        drawRoundRect(x, y, w, h, 8, 2, new Color(115, 135, 165));
    }

    private void drawScrim() {
        changeColor(new Color(0, 0, 0, 165));
        drawSolidRectangle(0, 0, width(), height());
    }

    private void drawMenuBackground() {
        if (level != null && level.backgroundImage != null) {
            drawImage(level.backgroundImage, 0, 0, width(), height());
        } else {
            changeBackgroundColor(20, 26, 38);
            clearBackground(width(), height());
        }

        changeColor(new Color(10, 14, 24, 190));
        drawSolidRectangle(0, 0, width(), height());

        changeColor(new Color(30, 45, 70, 180));
        drawSolidRectangle(0, 760, width(), 320);

        changeColor(new Color(58, 76, 105, 180));
        drawSolidRectangle(0, 840, width(), 8);
        drawSolidRectangle(0, 1010, width(), 8);
    }

    public void drawMenuButton(double x, double y, double w, double h, String text, boolean hover) {
        Color fill = hover ? COLOR_ACCENT : COLOR_PANEL_LIGHT;
        Color border = hover ? new Color(210, 240, 255) : new Color(105, 126, 160);

        fillRoundRect(x, y, w, h, 8, fill);
        drawRoundRect(x, y, w, h, 8, hover ? 3 : 2, border);
        drawButtonLabel(x, y, w, h, text, 24, COLOR_TEXT);
    }

    private Image[] slicePlayerRow(Image sheet, int row) {
        int spriteSize = 50;
        Image[] frames = new Image[5];

        for (int i = 0; i < frames.length; i++) {
            frames[i] = subImage(sheet, i * spriteSize, row * spriteSize, spriteSize, spriteSize);
        }

        return frames;
    }

    private void applyCharacterSelection() {
        if (player[0] == null || player[1] == null) {
            return;
        }

        setPlayerSprites(player[0], isFemalePlayer(0));
        setPlayerSprites(player[1], isFemalePlayer(1));
    }

    private void setPlayerSprites(player p, boolean female) {
        if (female) {
            p.setSprites(femaleStay, femaleLeft, femaleJump);
        } else {
            p.setSprites(maleStay, maleLeft, maleJump);
        }
    }

    private boolean isFemalePlayer(int playerIndex) {
        if (characterChoice == 1) {
            return true;
        }

        if (characterChoice == 2) {
            return playerIndex == 1;
        }

        return false;
    }

    private Image getPreviewImage(int playerIndex) {
        if (isFemalePlayer(playerIndex)) {
            return femaleStay[0];
        }

        return maleStay[0];
    }

    private void drawSelectedCharacters(double x, double y) {
        changeColor(COLOR_TEXT);
        drawBoldText(x, y, "Selected", "Arial", 26);

        fillRoundRect(x, y + 28, 210, 150, 8, new Color(24, 32, 48));
        drawRoundRect(x, y + 28, 210, 150, 8, 2, new Color(90, 110, 145));

        drawImage(getPreviewImage(0), x + 38, y + 52, 70, 70);
        drawImage(getPreviewImage(1), x + 122, y + 52, 70, 70);

        changeColor(COLOR_MUTED_TEXT);
        drawText(x + 57, y + 145, "P1", "Arial", 18);
        drawText(x + 142, y + 145, "P2", "Arial", 18);
    }

    private void drawHelpPanel(String closeText) {
        drawScrim();
        drawPanel(520, 270, 880, 500);

        changeColor(COLOR_TEXT);
        drawBoldText(610, 360, "HELP", "Arial", 48);

        changeColor(COLOR_MUTED_TEXT);
        drawText(610, 430, "This is a two-player platform game.", "Arial", 26);
        drawText(610, 480, "The first pig to enter the gate wins the level.", "Arial", 26);
        drawText(610, 530, "Use portals and timing to race past hazards.", "Arial", 26);
        drawText(610, 580, "Player 1 uses W A D. Player 2 uses arrow keys.", "Arial", 26);

        changeColor(COLOR_ACCENT_2);
        drawText(610, 680, closeText, "Arial", 24);
    }

    private void drawSettingsPanel() {
        drawScrim();
        drawPanel(SETTINGS_X, SETTINGS_Y, SETTINGS_W, SETTINGS_H);

        changeColor(COLOR_TEXT);
        drawBoldText(SETTINGS_X + 80, SETTINGS_Y + 90, "SETTINGS", "Arial", 48);

        fillRoundRect(
                SETTINGS_CLOSE_X,
                SETTINGS_CLOSE_Y,
                SETTINGS_CLOSE_SIZE,
                SETTINGS_CLOSE_SIZE,
                8,
                settingsCloseHover ? COLOR_ACCENT : COLOR_PANEL_LIGHT
        );
        drawRoundRect(SETTINGS_CLOSE_X, SETTINGS_CLOSE_Y, SETTINGS_CLOSE_SIZE, SETTINGS_CLOSE_SIZE, 8, 2, new Color(135, 155, 190));
        drawButtonLabel(SETTINGS_CLOSE_X, SETTINGS_CLOSE_Y, SETTINGS_CLOSE_SIZE, SETTINGS_CLOSE_SIZE, "X", 22, COLOR_TEXT);

        changeColor(COLOR_MUTED_TEXT);
        drawText(SETTINGS_X + 80, SETTINGS_Y + 165, "Characters", "Arial", 24);

        drawCharacterChoiceButton(CHARACTER_BUTTON_1_X, CHARACTER_BUTTON_Y, CHARACTER_BUTTON_W, CHARACTER_BUTTON_H, "Two Male", 0);
        drawCharacterChoiceButton(CHARACTER_BUTTON_2_X, CHARACTER_BUTTON_Y, CHARACTER_BUTTON_W, CHARACTER_BUTTON_H, "Two Female", 1);
        drawCharacterChoiceButton(CHARACTER_BUTTON_3_X, CHARACTER_BUTTON_Y, CHARACTER_BUTTON_W, CHARACTER_BUTTON_H, "Mixed", 2);

        changeColor(COLOR_MUTED_TEXT);
        drawText(SETTINGS_X + 80, SETTINGS_Y + 345, "Volume", "Arial", 24);

        drawVolumeControl();
    }

    private void drawCharacterChoiceButton(double x, double y, double w, double h, String text, int choice) {
        Color fill;
        if (characterChoice == choice) {
            fill = COLOR_GOOD;
        } else if (hoveredCharacterChoice == choice) {
            fill = COLOR_ACCENT;
        } else {
            fill = COLOR_PANEL_LIGHT;
        }

        fillRoundRect(x, y, w, h, 8, fill);
        drawRoundRect(x, y, w, h, 8, 2, new Color(125, 145, 178));
        drawButtonLabel(x, y, w, h, text, 22, COLOR_TEXT);
    }

    private void drawVolumeControl() {
        fillRoundRect(
                VOLUME_MINUS_X,
                VOLUME_BUTTON_Y,
                VOLUME_BUTTON_SIZE,
                VOLUME_BUTTON_SIZE,
                8,
                volumeMinusHover ? COLOR_ACCENT : COLOR_PANEL_LIGHT
        );
        drawRoundRect(VOLUME_MINUS_X, VOLUME_BUTTON_Y, VOLUME_BUTTON_SIZE, VOLUME_BUTTON_SIZE, 8, 2, new Color(125, 145, 178));
        drawButtonLabel(VOLUME_MINUS_X, VOLUME_BUTTON_Y, VOLUME_BUTTON_SIZE, VOLUME_BUTTON_SIZE, "-", 28, COLOR_TEXT);

        fillRoundRect(VOLUME_BAR_X, VOLUME_BAR_Y, VOLUME_BAR_W, VOLUME_BAR_H, 8, new Color(70, 78, 94));
        fillRoundRect(VOLUME_BAR_X, VOLUME_BAR_Y, VOLUME_BAR_W * masterVolume / 100.0, VOLUME_BAR_H, 8, COLOR_ACCENT);

        if (volumeBarHover) {
            drawRoundRect(VOLUME_BAR_X - 4, VOLUME_BAR_Y - 8, VOLUME_BAR_W + 8, VOLUME_BAR_H + 16, 8, 2, COLOR_TEXT);
        }

        drawCenteredText(VOLUME_BAR_X, VOLUME_BAR_Y + 58, VOLUME_BAR_W, masterVolume + "%", "Arial", 22, false, COLOR_TEXT);

        fillRoundRect(
                VOLUME_PLUS_X,
                VOLUME_BUTTON_Y,
                VOLUME_BUTTON_SIZE,
                VOLUME_BUTTON_SIZE,
                8,
                volumePlusHover ? COLOR_ACCENT : COLOR_PANEL_LIGHT
        );
        drawRoundRect(VOLUME_PLUS_X, VOLUME_BUTTON_Y, VOLUME_BUTTON_SIZE, VOLUME_BUTTON_SIZE, 8, 2, new Color(125, 145, 178));
        drawButtonLabel(VOLUME_PLUS_X, VOLUME_BUTTON_Y, VOLUME_BUTTON_SIZE, VOLUME_BUTTON_SIZE, "+", 28, COLOR_TEXT);
    }

    public void drawPlayer(player player1, player player2){

        drawSinglePlayer(player1);
        drawSinglePlayer(player2);

    }

    private void drawTrap(Trap trap) {
        if (trap instanceof FlyingKnife) {
            drawFlyingKnife((FlyingKnife)trap);
            return;
        }

        if (trap instanceof FakeGateTrap) {
            drawFakeGateTrap((FakeGateTrap)trap);
            return;
        }

        drawImage(trap.image, trap.x, trap.y, trap.width, trap.height);
    }

    private void drawFlyingKnife(FlyingKnife knife) {
        saveCurrentTransform();
        translate(knife.x + knife.width / 2, knife.y + knife.height / 2);
        rotate(knife.getDirectionAngleDegrees());
        drawImage(knife.image, -knife.width / 2, -knife.height / 2, knife.width, knife.height);
        restoreLastTransform();
    }

    private void drawFakeGateTrap(FakeGateTrap fakeGate) {
        drawImage(fakeGate.getDoorImage(), fakeGate.x, fakeGate.y, fakeGate.width, fakeGate.height);

        if (fakeGate.isSpikeVisible()) {
            drawImage(
                    fakeGate.getSpikeImage(),
                    fakeGate.getSpikeX(),
                    fakeGate.getSpikeY(),
                    fakeGate.width,
                    fakeGate.height
            );
        }
    }

    private void drawWindVent(WindVent windVent) {
        changeColor(new Color(95, 220, 255, 35));
        drawSolidRectangle(windVent.zoneX, windVent.zoneY, windVent.zoneWidth, windVent.zoneHeight);

        changeColor(new Color(55, 165, 210, 210));
        drawSolidRectangle(windVent.x + 8, windVent.y + 10, windVent.width - 16, 6);
        drawSolidRectangle(windVent.x + 8, windVent.y + 22, windVent.width - 16, 6);
        drawSolidRectangle(windVent.x + 8, windVent.y + 34, windVent.width - 16, 6);

        for (WindParticle particle : windVent.getParticles()) {
            int alpha = (int)(190 * particle.alphaRatio());

            if (alpha <= 0) {
                continue;
            }

            changeColor(new Color(
                    particle.color.getRed(),
                    particle.color.getGreen(),
                    particle.color.getBlue(),
                    alpha
            ));
            drawSolidCircle(particle.x, particle.y, particle.radius);
        }
    }

    private void drawPlatform(Platform platform) {
        for (java.awt.geom.Rectangle2D.Double bounds : platform.getDrawBounds()) {
            if (bounds.width <= 0 || bounds.height <= 0) {
                continue;
            }

            drawImage(
                    platform.image,
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height
            );
        }
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

    private void drawPortalParticle(PortalParticle particle) {
        int alpha = (int)(220 * particle.alphaRatio());
        if (alpha <= 0) {
            return;
        }

        Color color = new Color(
                particle.color.getRed(),
                particle.color.getGreen(),
                particle.color.getBlue(),
                alpha
        );

        changeColor(color);
        drawSolidCircle(particle.x, particle.y, particle.radius);
    }

    private void drawSinglePlayer(player p) {
        if (p.dead && deadPigImage != null) {
            drawImage(deadPigImage, p.x, p.y, p.width, p.height);
            return;
        }

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
        drawMenuBackground();

        changeColor(COLOR_TEXT);
        drawBoldText(160, 150, "SELECT LEVEL", "Arial", 64);

        changeColor(COLOR_MUTED_TEXT);
        drawText(164, 205, "Unlocked stages are highlighted. Press ESC to return.", "Arial", 24);

        drawPanel(1420, 110, 340, 120);
        drawCenteredText(1420, 160, 340, "SCORE", "Arial", 28, true, COLOR_TEXT);
        drawCenteredText(1420, 205, 340, "P1: " + playerScores[0] + "    P2: " + playerScores[1], "Arial", 24, false, COLOR_TEXT);

        for (int i = 0; i < levelButtons.length - 1; i++) {
            LevelButton current = levelButtons[i];
            LevelButton next = levelButtons[i + 1];

            double x1 = current.x + current.width / 2;
            double y1 = current.y + current.height / 2;
            double x2 = next.x + next.width / 2;
            double y2 = next.y + next.height / 2;

            changeColor(120, 140, 170);
            drawLine(x1, y1, x2, y2, 5);
        }

        for (LevelButton button : levelButtons) {
            Color fill = button.unlocked ? COLOR_GOOD : COLOR_LOCKED;
            if (hoveredLevelButton >= 0 && levelButtons[hoveredLevelButton] == button && button.unlocked) {
                fill = COLOR_ACCENT;
            }

            fillRoundRect(button.x, button.y, button.width, button.height, 8, fill);
            drawRoundRect(button.x, button.y, button.width, button.height, 8, 3, new Color(220, 235, 255));

            String buttonText = button.unlocked ? "Level " + button.levelNumber : "Locked";
            drawButtonLabel(button.x, button.y, button.width, button.height, buttonText, 24, COLOR_TEXT);
        }

        drawPanel(560, 310, 800, 150);

        if (hoveredLevelButton >= 0 && hoveredLevelButton < levelButtons.length) {
            LevelButton button = levelButtons[hoveredLevelButton];

            changeColor(COLOR_TEXT);
            drawSolidRectangle(button.x + button.width / 2 - 8, button.y - 30, 16, 16);

            drawCenteredText(560, 365, 800, button.levelName, "Arial", 30, true, COLOR_TEXT);
            String stateText = button.unlocked ? "Ready to play" : "Complete the previous level to unlock";
            drawCenteredText(560, 410, 800, stateText, "Arial", 20, false, COLOR_MUTED_TEXT);
        } else {
            drawCenteredText(560, 365, 800, "Choose a stage", "Arial", 30, true, COLOR_TEXT);
            drawCenteredText(560, 410, 800, "Level 1 has been rebuilt for the 1920 x 1080 map.", "Arial", 20, false, COLOR_MUTED_TEXT);
        }
    }

    private void drawInGameHud() {
        fillRoundRect(36, 32, 520, 70, 8, new Color(12, 18, 28, 190));
        drawRoundRect(36, 32, 520, 70, 8, 2, new Color(105, 126, 160));

        changeColor(COLOR_TEXT);
        drawBoldText(62, 77, "Level " + currentLevel, "Arial", 26);

        changeColor(COLOR_MUTED_TEXT);
        drawText(186, 77, "Score  P1: " + playerScores[0] + "    P2: " + playerScores[1], "Arial", 22);
    }

    private void drawLevel4Message() {
        if (!(level instanceof level4)) {
            return;
        }

        String message = ((level4)level).getTrollMessage();

        if (message == null || message.isEmpty()) {
            return;
        }

        fillRoundRect(690, 112, 540, 72, 8, new Color(12, 18, 28, 210));
        drawRoundRect(690, 112, 540, 72, 8, 2, new Color(255, 190, 85));
        drawCenteredText(690, 158, 540, message, "Dialog", 32, true, COLOR_ACCENT_2);
    }

    @Override
    public void update(double dt) {
        dt = Math.min(dt, MAX_PHYSICS_DT);

        if (currentLevel >= 1 && currentLevel <= 5 && !gameOver && !levelComplete && !gamePaused) {
            boolean player1WasDead = player[0].dead;
            boolean player2WasDead = player[1].dead;

            level.update(dt);

            if (level.didTeleport()) {
                playSound(transSound);
            }

            if (!player1WasDead && player[0].dead) {
                levelDeaths[0]++;
                playSound(deadSound);
            }

            if (!player2WasDead && player[1].dead) {
                levelDeaths[1]++;
                playSound(deadSound);
            }

            int reachedPlayer = getReachedGatePlayer();
            if (reachedPlayer != 0 && level.isLevelComplete()) {
                completeLevel(reachedPlayer);
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

    private int getReachedGatePlayer() {
        if (currentLevel == 3) {
            if (player[0].reachedGate && player[1].reachedGate) {
                return 1;
            }

            return 0;
        }

        if (player[0].reachedGate) {
            return 1;
        }

        if (player[1].reachedGate) {
            return 2;
        }

        return 0;
    }

    private void completeLevel(int playerNumber) {
        winningPlayer = playerNumber;

        if (currentLevel == 3) {
            winningPlayer = 0;
            playerScores[0]++;
            playerScores[1]++;
        } else {
            playerScores[playerNumber - 1]++;
        }

        levelComplete = true;
        levelCompleteTimer = 0;
        playSound(winSound);

        if (currentLevel < LAST_IMPLEMENTED_LEVEL && currentLevel + 1 > maxUnlockedLevel) {
            maxUnlockedLevel = currentLevel + 1;
            updateLevelButtons();
        }

        if (currentLevel == LAST_IMPLEMENTED_LEVEL) {
            showAllLevelsComplete = true;
            allLevelsCompleteTimer = 0;
        }
    }

    private void resetLevelStats() {
        levelDeaths[0] = 0;
        levelDeaths[1] = 0;
        winningPlayer = 0;
    }

    private String completionSummaryText() {
        String deaths = "P1 deaths: " + levelDeaths[0] + "    P2 deaths: " + levelDeaths[1];

        if (winningPlayer == 0) {
            return "Co-op clear    " + deaths;
        }

        return "Winner: P" + winningPlayer + "    " + deaths;
    }

    private void playSound(AudioClip sound) {
        playAudio(sound, masterVolumeGain);
    }

    @Override
    public void paintComponent() {
        if (currentLevel == 0) {
            drawMenuBackground();

            changeColor(COLOR_TEXT);
            drawBoldText(160, 210, "You jump, I jump", "Arial", 82);

            changeColor(COLOR_MUTED_TEXT);
            drawText(166, 270, "Two players. One exit. Move together.", "Arial", 28);

            changeColor(COLOR_ACCENT);
            drawSolidRectangle(166, 308, 520, 6);

            drawPanel(160, 390, 660, 420);

            changeColor(COLOR_TEXT);
            drawBoldText(210, 465, "Team Setup", "Arial", 36);

            changeColor(COLOR_MUTED_TEXT);
            drawText(210, 525, "Player 1", "Arial", 24);
            drawText(360, 525, "W  A  D", "Arial", 26);
            drawText(210, 575, "Player 2", "Arial", 24);
            drawText(360, 575, "Arrow keys", "Arial", 26);
            drawText(210, 635, "First pig into the gate scores.", "Arial", 24);

            drawSelectedCharacters(210, 705);

            drawMenuButton(MENU_BUTTON_X, menuButtonY(0), MENU_BUTTON_W, MENU_BUTTON_H, "START GAME", startHover);
            drawMenuButton(MENU_BUTTON_X, menuButtonY(1), MENU_BUTTON_W, MENU_BUTTON_H, "HELP", helpHover);
            drawMenuButton(MENU_BUTTON_X, menuButtonY(2), MENU_BUTTON_W, MENU_BUTTON_H, "SETTINGS", settingsHover);
            drawMenuButton(MENU_BUTTON_X, menuButtonY(3), MENU_BUTTON_W, MENU_BUTTON_H, "QUIT", quitHover);

            changeColor(COLOR_MUTED_TEXT);
            drawText(MENU_BUTTON_X, 830, "ENTER: start    H: help    ESC: quit", "Arial", 22);

            if (showHelp) {
                drawHelpPanel("Press H or click HELP again to close.");
            }

            if (showSettings) {
                drawSettingsPanel();
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

            drawInGameHud();
            drawLevel4Message();

            if (gamePaused) {
                drawScrim();
                drawPanel(660, 250, 600, 610);

                drawCenteredText(660, 360, 600, "PAUSED", "Arial", 58, true, COLOR_TEXT);

                drawMenuButton(PAUSE_BUTTON_X, pauseButtonY(0), PAUSE_BUTTON_W, PAUSE_BUTTON_H, "RESUME", pauseResumeHover);
                drawMenuButton(PAUSE_BUTTON_X, pauseButtonY(1), PAUSE_BUTTON_W, PAUSE_BUTTON_H, "QUIT LEVEL", pauseQuitLevelHover);
                drawMenuButton(PAUSE_BUTTON_X, pauseButtonY(2), PAUSE_BUTTON_W, PAUSE_BUTTON_H, "HELP", pauseHelpHover);
                drawMenuButton(PAUSE_BUTTON_X, pauseButtonY(3), PAUSE_BUTTON_W, PAUSE_BUTTON_H, "MAIN MENU", pauseMenuHover);

                if (showPauseHelp) {
                    drawHelpPanel("Press ESC to close help.");
                }
            }


            if (gameOver) {
                drawScrim();
                drawPanel(560, 210, 800, 650);

                if (gameOverImage != null) {
                    double imageWidth = 520;
                    double imageHeight = 360;
                    double imageX = (width() - imageWidth) / 2;
                    double imageY = 300;
                    drawImage(gameOverImage, imageX, imageY, imageWidth, imageHeight);
                }


                drawCenteredText(560, 735, 800, "Press R to restart", "Arial", 30, false, COLOR_TEXT);
            }

            if (levelComplete) {
                drawScrim();
                drawPanel(520, 190, 880, 700);

                if (showAllLevelsComplete) {
                    drawCenteredText(520, 340, 880, "Congratulations!", "Arial", 58, true, new Color(255, 215, 0));
                    drawCenteredText(520, 425, 880, "You have completed all the levels.", "Arial", 30, false, COLOR_TEXT);
                    drawCenteredText(520, 485, 880, completionSummaryText(), "Arial", 24, false, COLOR_TEXT);
                    drawCenteredText(520, 530, 880, "Score  P1: " + playerScores[0] + "    P2: " + playerScores[1], "Arial", 24, false, COLOR_TEXT);

                    int countdown = 3 - (int)allLevelsCompleteTimer;
                    if (countdown < 0) countdown = 0;

                    String countdownText = "Returning to level select... " + countdown;
                    drawCenteredText(520, 610, 880, countdownText, "Arial", 24, false, COLOR_MUTED_TEXT);
                } else {
                    if (victoryImage != null) {
                        double imageWidth = 520;
                        double imageHeight = 360;
                        double imageX = (width() - imageWidth) / 2;
                        double imageY = 260;
                        drawImage(victoryImage, imageX, imageY, imageWidth, imageHeight);
                    }

                    drawCenteredText(520, 675, 880, "VICTORY!", "Arial", 58, true, new Color(255, 215, 0));
                    drawCenteredText(520, 715, 880, completionSummaryText(), "Arial", 24, false, COLOR_TEXT);
                    drawCenteredText(520, 750, 880, "Score  P1: " + playerScores[0] + "    P2: " + playerScores[1], "Arial", 24, false, COLOR_TEXT);
                    drawCenteredText(520, 790, 880, "SPACE: next level    R: restart    ESC: level select", "Arial", 24, false, COLOR_TEXT);
                }
            }
        }
    }


    @Override
    public void init(){
        setWindowSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        currentLevel = 0;
        updateMasterVolumeGain();

        level = new level1(loadImage("resources/bg1.png"));
        Image malePlayerSheet = loadImage("resources/playerMale.png");
        Image femalePlayerSheet = loadImage("resources/playerFamale.png");
        platformImage = loadImage("resources/platform.png");
        Image gateSheet = loadImage("resources/gate.png");
        Image sawSheet = loadImage("resources/saw.png");
        Image portalSheet = loadImage("resources/portal.png");

        maleStay = slicePlayerRow(malePlayerSheet, 0);
        maleLeft = slicePlayerRow(malePlayerSheet, 1);
        maleJump = slicePlayerRow(malePlayerSheet, 2);
        femaleStay = slicePlayerRow(femalePlayerSheet, 0);
        femaleLeft = slicePlayerRow(femalePlayerSheet, 1);
        femaleJump = slicePlayerRow(femalePlayerSheet, 2);

        player[0] = new player(maleStay, maleLeft, maleJump);
        player[1] = new player(maleStay, maleLeft, maleJump);
        applyCharacterSelection();

        portalImage = new Image[4];
        portalImage[0] = subImage(portalSheet, 0, 0, 50, 50);
        portalImage[1] = subImage(portalSheet, 50, 0, 50, 50);
        portalImage[2] = subImage(portalSheet, 100, 0, 50, 50);
        portalImage[3] = subImage(portalSheet, 150, 0, 50, 50);

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

        gameOverImage = loadImage("resources/oneplayersurvive.png");
        victoryImage = loadImage("resources/victory.png");
        deadPigImage = loadImage("resources/deadPig.png");
        deadSound = loadAudio("resources/dead.wav");
        winSound = loadAudio("resources/win.wav");
        transSound = loadAudio("resources/trans.wav");


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

        double buttonWidth = 170;
        double buttonHeight = 86;

        levelButtons[0] = new LevelButton(1, 260, 720, buttonWidth, buttonHeight, "Level 1: Skybridge Run", true);
        levelButtons[1] = new LevelButton(2, 620, 560, buttonWidth, buttonHeight, "Level 2: Trap Corridor", true);
        levelButtons[2] = new LevelButton(3, 980, 700, buttonWidth, buttonHeight, "Level 3: Windmill Valley", true);
        levelButtons[3] = new LevelButton(4, 1320, 500, buttonWidth, buttonHeight, "Level 4: Piggy Rage Tower", true);
        levelButtons[4] = new LevelButton(5, 1500, 760, buttonWidth, buttonHeight, "Level 5: Boss Fight", false);
    }

    private void restartLevel() {
        gameOver = false;
        levelComplete = false;
        showAllLevelsComplete = false;
        resetLevelStats();

        if (currentLevel == 1) {
            level = new level1(loadImage("resources/bg1.png"));
        } else if (currentLevel == 2) {
            level = new level2(loadImage("resources/bg1.png"));
        } else if (currentLevel == 3) {
            level = new level3(loadImage("resources/bg1.png"));
        } else if (currentLevel == 4) {
            level = new level4(loadImage("resources/bg1.png"));
        }
//        else if (currentLevel == 5) {
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
        showAllLevelsComplete = false;
        resetLevelStats();

        if (currentLevel >= LAST_IMPLEMENTED_LEVEL) {
            levelComplete = false;
            showAllLevelsComplete = false;
            currentLevel = -1;
            showLevelSelect = true;
            return;
        }

        currentLevel++;

        if (currentLevel > LAST_IMPLEMENTED_LEVEL) {
            currentLevel = LAST_IMPLEMENTED_LEVEL;
            return;
        }

        if (currentLevel > maxUnlockedLevel) {
            maxUnlockedLevel = currentLevel;
            updateLevelButtons();
        }

        if (currentLevel == 2) {
            level = new level2(loadImage("resources/bg1.png"));
        } else if (currentLevel == 3) {
            level = new level3(loadImage("resources/bg1.png"));
        } else if (currentLevel == 4) {
            level = new level4(loadImage("resources/bg1.png"));
        }
//        else if (currentLevel == 5) {
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
            levelButtons[i].unlocked = (i + 1) <= LAST_IMPLEMENTED_LEVEL;
        }
    }
    private void loadSelectedLevel(int levelNumber) {
        currentLevel = levelNumber;
        showLevelSelect = false;
        gameOver = false;
        levelComplete = false;
        showAllLevelsComplete = false;
        resetLevelStats();

        if (levelNumber == 1) {
            level = new level1(loadImage("resources/bg1.png"));
        } else if (levelNumber == 2) {
            level = new level2(loadImage("resources/bg1.png"));
        } else if (levelNumber == 3) {
            level = new level3(loadImage("resources/bg1.png"));
        } else if (levelNumber == 4) {
            level = new level4(loadImage("resources/bg1.png"));
        }
//        else if (levelNumber == 5) {
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
            if (showSettings) {
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    showSettings = false;
                }

                if (event.getKeyCode() == KeyEvent.VK_1) {
                    characterChoice = 0;
                    applyCharacterSelection();
                }

                if (event.getKeyCode() == KeyEvent.VK_2) {
                    characterChoice = 1;
                    applyCharacterSelection();
                }

                if (event.getKeyCode() == KeyEvent.VK_3) {
                    characterChoice = 2;
                    applyCharacterSelection();
                }

                if (event.getKeyCode() == KeyEvent.VK_LEFT) {
                    setMasterVolume(masterVolume - 10);
                }

                if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
                    setMasterVolume(masterVolume + 10);
                }

                return;
            }

            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                showLevelSelect = true;
                currentLevel = -1;
            }

            if (event.getKeyCode() == KeyEvent.VK_H) {
                showHelp = !showHelp;
                showSettings = false;
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
                if (canControlPlayer(player[1])) {
                    player[1].leftPressed = true;
                }
            }

            if(event.getKeyCode() == KeyEvent.VK_RIGHT){
                if (canControlPlayer(player[1])) {
                    player[1].rightPressed = true;
                }
            }

            if(event.getKeyCode() == KeyEvent.VK_UP){
                if (canControlPlayer(player[1])) {
                    player[1].jumpPressed = true;
                }
            }

            if(event.getKeyCode() == KeyEvent.VK_A){
                if (canControlPlayer(player[0])) {
                    player[0].leftPressed = true;
                }
            }

            if(event.getKeyCode() == KeyEvent.VK_D){
                if (canControlPlayer(player[0])) {
                    player[0].rightPressed = true;
                }
            }

            if(event.getKeyCode() == KeyEvent.VK_W){
                if (canControlPlayer(player[0])) {
                    player[0].jumpPressed = true;
                }
            }
        }
    }

    private boolean canControlPlayer(player p) {
        return p != null && !p.dead && !p.reachedGate;
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

    private void updateSettingsHover() {
        hoveredCharacterChoice = -1;
        volumeMinusHover = false;
        volumePlusHover = false;
        volumeBarHover = false;
        settingsCloseHover = false;

        if (!showSettings) {
            return;
        }

        if (isMouseInside(CHARACTER_BUTTON_1_X, CHARACTER_BUTTON_Y, CHARACTER_BUTTON_W, CHARACTER_BUTTON_H)) {
            hoveredCharacterChoice = 0;
        } else if (isMouseInside(CHARACTER_BUTTON_2_X, CHARACTER_BUTTON_Y, CHARACTER_BUTTON_W, CHARACTER_BUTTON_H)) {
            hoveredCharacterChoice = 1;
        } else if (isMouseInside(CHARACTER_BUTTON_3_X, CHARACTER_BUTTON_Y, CHARACTER_BUTTON_W, CHARACTER_BUTTON_H)) {
            hoveredCharacterChoice = 2;
        }

        volumeMinusHover = isMouseInside(VOLUME_MINUS_X, VOLUME_BUTTON_Y, VOLUME_BUTTON_SIZE, VOLUME_BUTTON_SIZE);
        volumePlusHover = isMouseInside(VOLUME_PLUS_X, VOLUME_BUTTON_Y, VOLUME_BUTTON_SIZE, VOLUME_BUTTON_SIZE);
        volumeBarHover = isMouseInside(VOLUME_BAR_X, VOLUME_BAR_Y - 12, VOLUME_BAR_W, VOLUME_BAR_H + 24);
        settingsCloseHover = isMouseInside(SETTINGS_CLOSE_X, SETTINGS_CLOSE_Y, SETTINGS_CLOSE_SIZE, SETTINGS_CLOSE_SIZE);
    }

    private void handleSettingsClick() {
        if (settingsCloseHover) {
            showSettings = false;
            updateSettingsHover();
            return;
        }

        if (hoveredCharacterChoice >= 0) {
            characterChoice = hoveredCharacterChoice;
            applyCharacterSelection();
            return;
        }

        if (volumeMinusHover) {
            setMasterVolume(masterVolume - 10);
            return;
        }

        if (volumePlusHover) {
            setMasterVolume(masterVolume + 10);
            return;
        }

        if (volumeBarHover) {
            int selectedVolume = (int)Math.round((mouseX - VOLUME_BAR_X) / VOLUME_BAR_W * 100);
            setMasterVolume(selectedVolume);
        }
    }

    private void setMasterVolume(int volume) {
        if (volume < 0) {
            volume = 0;
        }

        if (volume > 100) {
            volume = 100;
        }

        masterVolume = volume;
        updateMasterVolumeGain();
    }

    private void updateMasterVolumeGain() {
        if (masterVolume == 0) {
            masterVolumeGain = -80.0f;
        } else {
            masterVolumeGain = (float)(20.0 * Math.log10(masterVolume / 100.0));
        }
    }

    @Override
    public void mouseMoved(MouseEvent event) {

        mouseX = event.getX();
        mouseY = event.getY();

        if (currentLevel == 0) {
            startHover = !showSettings && isMouseInside(MENU_BUTTON_X, menuButtonY(0), MENU_BUTTON_W, MENU_BUTTON_H);
            helpHover = !showSettings && isMouseInside(MENU_BUTTON_X, menuButtonY(1), MENU_BUTTON_W, MENU_BUTTON_H);
            settingsHover = !showSettings && isMouseInside(MENU_BUTTON_X, menuButtonY(2), MENU_BUTTON_W, MENU_BUTTON_H);
            quitHover = !showSettings && isMouseInside(MENU_BUTTON_X, menuButtonY(3), MENU_BUTTON_W, MENU_BUTTON_H);
            updateSettingsHover();
        } else if (currentLevel == -1) {
            hoveredLevelButton = -1;
            for (int i = 0; i < levelButtons.length; i++) {
                if (isMouseInside(levelButtons[i].x, levelButtons[i].y, levelButtons[i].width, levelButtons[i].height)) {
                    hoveredLevelButton = i;
                    break;
                }
            }
        }  else if (currentLevel >= 1 && currentLevel <= 5 && gamePaused && !showPauseHelp) {
            pauseResumeHover = isMouseInside(PAUSE_BUTTON_X, pauseButtonY(0), PAUSE_BUTTON_W, PAUSE_BUTTON_H);
            pauseQuitLevelHover = isMouseInside(PAUSE_BUTTON_X, pauseButtonY(1), PAUSE_BUTTON_W, PAUSE_BUTTON_H);
            pauseHelpHover = isMouseInside(PAUSE_BUTTON_X, pauseButtonY(2), PAUSE_BUTTON_W, PAUSE_BUTTON_H);
            pauseMenuHover = isMouseInside(PAUSE_BUTTON_X, pauseButtonY(3), PAUSE_BUTTON_W, PAUSE_BUTTON_H);
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
            updateSettingsHover();

            if (showSettings) {
                handleSettingsClick();
                return;
            }

            if (isMouseInside(MENU_BUTTON_X, menuButtonY(0), MENU_BUTTON_W, MENU_BUTTON_H)) {
                showLevelSelect = true;
                currentLevel = -1;
            }

            if (isMouseInside(MENU_BUTTON_X, menuButtonY(1), MENU_BUTTON_W, MENU_BUTTON_H)) {
                showHelp = !showHelp;
                showSettings = false;
            }

            if (isMouseInside(MENU_BUTTON_X, menuButtonY(2), MENU_BUTTON_W, MENU_BUTTON_H)) {
                showSettings = true;
                showHelp = false;
                updateSettingsHover();
            }

            if (isMouseInside(MENU_BUTTON_X, menuButtonY(3), MENU_BUTTON_W, MENU_BUTTON_H)) {
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
            if (isMouseInside(PAUSE_BUTTON_X, pauseButtonY(0), PAUSE_BUTTON_W, PAUSE_BUTTON_H)) {
                gamePaused = false;
            }

            if (isMouseInside(PAUSE_BUTTON_X, pauseButtonY(1), PAUSE_BUTTON_W, PAUSE_BUTTON_H)) {
                gamePaused = false;
                showLevelSelect = true;
                currentLevel = -1;
            }

            if (isMouseInside(PAUSE_BUTTON_X, pauseButtonY(2), PAUSE_BUTTON_W, PAUSE_BUTTON_H)) {
                showPauseHelp = true;
            }

            if (isMouseInside(PAUSE_BUTTON_X, pauseButtonY(3), PAUSE_BUTTON_W, PAUSE_BUTTON_H)) {
                gamePaused = false;
                currentLevel = 0;
                showLevelSelect = false;
            }
        }
    }
}
