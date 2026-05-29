import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class Game extends GameEngine{

    private static final int WINDOW_WIDTH = 1920;
    private static final int WINDOW_HEIGHT = 1080;
    private static final int LAST_IMPLEMENTED_LEVEL = 5;
    private static final double MAX_PHYSICS_DT = 1.0 / 30.0;
    private static final int GATE_WIN_SCORE = 10;
    private static final int MIN_POWER_LEVEL = 1;
    private static final int MAX_POWER_LEVEL = 5;
    private static final float BGM_VOLUME_OFFSET = -6.0206f;

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
    AudioClip eatSound;
    AudioClip enemyDeadSound;
    AudioClip missileLockSound;
    AudioClip stage3Sound;
    AudioClip menuBgm;
    AudioClip[] levelBgms = new AudioClip[6];
    AudioClip currentBgm;
    Clip missileLockClip;
    boolean missileLockLoopPlaying = false;

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
    int[] playerPowerLevels = {MIN_POWER_LEVEL, MIN_POWER_LEVEL};
    int[] levelDeaths = new int[2];
    int winningPlayer = 0;

    class LevelButton {
        int levelNumber;
        double x, y, width, height;
        String levelName;
        String levelDescription;
        boolean unlocked;

        LevelButton(int level, double x, double y, double w, double h, String name, String description, boolean unlocked) {
            this.levelNumber = level;
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.levelName = name;
            this.levelDescription = description;
            this.unlocked = unlocked;
        }
    }

    LevelButton[] levelButtons;
    int hoveredLevelButton = -1;

    Image platformImage;
    Image spikeImage;
    Image[] sawFrames;
    Image pitImage;
    Image knifeImage;
    Image missileImage;
    Image[] bossFrames;
    Image[] portalImage;
    Image[] gateImage;

    Image[] maleStay;
    Image[] maleLeft;
    Image[] maleJump;
    Image[] femaleStay;
    Image[] femaleLeft;
    Image[] femaleJump;
    Image[] enemyIdleFrames;
    Image[] enemyLeftFrames;

    int characterChoice = 0;
    int hoveredCharacterChoice = -1;
    int masterVolume = 80;
    float masterVolumeGain = 0.0f;
    boolean volumeMinusHover = false;
    boolean volumePlusHover = false;
    boolean volumeBarHover = false;
    boolean settingsCloseHover = false;
    boolean[] fakeGateDeathSequenceActive = new boolean[2];
    boolean[] fakeGateDeathHidePlayer = new boolean[2];
    double[] fakeGateDeathSequenceTimer = new double[2];
    int[] fakeGateDeathSequenceDeadPlays = new int[2];

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

        for (Enemy enemy : Level.getEnemies()) {
            if (enemy.isActive()) {
                drawEnemy(enemy);
            }
        }

        for (PlayerKnife knife : Level.getPlayerKnives()) {
            drawPlayerKnife(knife);
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
        int fittedSize = size;
        int style = bold ? Font.BOLD : Font.PLAIN;
        Font drawFont = new Font(font, style, fittedSize);
        mGraphics.setFont(drawFont);
        FontMetrics metrics = mGraphics.getFontMetrics();

        while (fittedSize > 14 && metrics.stringWidth(text) > w - 24) {
            fittedSize--;
            drawFont = new Font(font, style, fittedSize);
            mGraphics.setFont(drawFont);
            metrics = mGraphics.getFontMetrics();
        }

        double textWidth = metrics.stringWidth(text);
        double textX = x + (w - textWidth) / 2;

        mGraphics.drawString(text, (int)textX, (int)y);
    }

    private void drawButtonLabel(double x, double y, double w, double h, String text, int size, Color color) {
        changeColor(color);
        int fittedSize = size;
        Font drawFont = new Font("Arial", Font.BOLD, fittedSize);
        mGraphics.setFont(drawFont);
        FontMetrics metrics = mGraphics.getFontMetrics();

        while (fittedSize > 12 && metrics.stringWidth(text) > w - 18) {
            fittedSize--;
            drawFont = new Font("Arial", Font.BOLD, fittedSize);
            mGraphics.setFont(drawFont);
            metrics = mGraphics.getFontMetrics();
        }

        double textWidth = metrics.stringWidth(text);
        double textX = x + (w - textWidth) / 2;
        double textY = y + (h - metrics.getHeight()) / 2 + metrics.getAscent();
        mGraphics.drawString(text, (int)textX, (int)textY);
    }

    private void drawWrappedText(double x, double y, double w, String text, String font, int size, Color color) {
        changeColor(color);
        Font drawFont = new Font(font, Font.PLAIN, size);
        mGraphics.setFont(drawFont);
        FontMetrics metrics = mGraphics.getFontMetrics();

        String[] words = text.split(" ");
        String line = "";
        double lineY = y;
        double lineHeight = metrics.getHeight() + 4;

        for (String word : words) {
            String candidate = line.isEmpty() ? word : line + " " + word;

            if (!line.isEmpty() && metrics.stringWidth(candidate) > w) {
                mGraphics.drawString(line, (int)x, (int)lineY);
                line = word;
                lineY += lineHeight;
            } else {
                line = candidate;
            }
        }

        if (!line.isEmpty()) {
            mGraphics.drawString(line, (int)x, (int)lineY);
        }
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

    private Image[] sliceEnemyRow(Image sheet, int row) {
        int spriteSize = 100;
        Image[] frames = new Image[5];

        for (int i = 0; i < frames.length; i++) {
            frames[i] = subImage(sheet, i * spriteSize, row * spriteSize, spriteSize, spriteSize);
        }

        return frames;
    }

    private Image[] sliceBossFrames(Image sheet) {
        int spriteSize = 220;
        Image[] frames = new Image[5];

        for (int i = 0; i < frames.length; i++) {
            frames[i] = subImage(sheet, i * spriteSize, 0, spriteSize, spriteSize);
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
        drawPanel(520, 230, 880, 620);

        changeColor(COLOR_TEXT);
        drawBoldText(610, 320, "HELP", "Arial", 48);

        changeColor(COLOR_MUTED_TEXT);
        drawText(610, 390, "This is a two-player platform game.", "Arial", 24);
        drawText(610, 435, "The first pig to enter the true gate scores 10.", "Arial", 24);
        drawText(610, 480, "Use portals and timing to race past hazards.", "Arial", 24);
        drawText(610, 525, "Player 1 uses W A D and throws knives with F.", "Arial", 24);
        drawText(610, 570, "Player 2 uses arrow keys and throws knives with Enter.", "Arial", 24);
        drawText(610, 615, "In levels 1-4, each winner gains +1 Level, up to Level 5.", "Arial", 24);
        drawText(610, 660, "Each Level grants +1% speed and one more knife slot.", "Arial", 24);
        drawText(610, 705, "Knives defeat enemies, then return when used or off-screen.", "Arial", 24);

        changeColor(COLOR_ACCENT_2);
        drawText(610, 790, closeText, "Arial", 24);
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

    private void drawPlayerKnife(PlayerKnife knife) {
        saveCurrentTransform();
        translate(knife.x + knife.width / 2, knife.y + knife.height / 2);
        rotate(knife.getDirectionAngleDegrees());
        drawImage(knife.image, -knife.width / 2, -knife.height / 2, knife.width, knife.height);
        restoreLastTransform();
    }

    private void drawFakeGateTrap(FakeGateTrap fakeGate) {
        drawImage(fakeGate.getDoorImage(), fakeGate.x, fakeGate.y, fakeGate.width, fakeGate.height);
    }

    private void drawEnemy(Enemy enemy) {
        if (enemy.isFallingAfterDeath()) {
            saveCurrentTransform();
            translate(enemy.x + enemy.width / 2, enemy.y + enemy.height / 2);
            rotate(180);
            drawImage(enemy.getCurrentImage(), -enemy.width / 2, -enemy.height / 2, enemy.width, enemy.height);
            restoreLastTransform();
            return;
        }

        if (enemy.isFacingLeft()) {
            drawImage(enemy.getCurrentImage(), enemy.x, enemy.y, enemy.width, enemy.height);
        } else {
            drawImage(enemy.getCurrentImage(), enemy.x + enemy.width, enemy.y, -enemy.width, enemy.height);
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
        if (p.trappedInFakeGate) {
            return;
        }

        if ((p == player[0] && fakeGateDeathHidePlayer[0]) ||
                (p == player[1] && fakeGateDeathHidePlayer[1])) {
            return;
        }

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

        drawLevelInfoPanel();
    }

    private void drawLevelInfoPanel() {
        double panelX = 500;
        double panelY = 290;
        double panelW = 920;
        double panelH = 190;
        double contentX = panelX + 46;
        double contentW = panelW - 92;

        drawPanel(panelX, panelY, panelW, panelH);

        if (hoveredLevelButton >= 0 && hoveredLevelButton < levelButtons.length) {
            LevelButton button = levelButtons[hoveredLevelButton];
            Color accent = button.unlocked ? COLOR_ACCENT : COLOR_LOCKED;

            changeColor(accent);
            drawSolidRectangle(panelX, panelY, 8, panelH);
            drawSolidRectangle(button.x + button.width / 2 - 8, button.y - 30, 16, 16);

            changeColor(COLOR_TEXT);
            drawBoldText(contentX, panelY + 58, button.levelName, "Arial", 30);

            fillRoundRect(panelX + panelW - 160, panelY + 30, 110, 38, 8, button.unlocked ? COLOR_GOOD : COLOR_LOCKED);
            drawButtonLabel(panelX + panelW - 160, panelY + 30, 110, 38, button.unlocked ? "READY" : "LOCKED", 17, COLOR_TEXT);

            String description = button.unlocked ? button.levelDescription : "Complete the previous level to unlock this stage.";
            drawWrappedText(contentX, panelY + 104, contentW, description, "Arial", 22, COLOR_MUTED_TEXT);

            if (button.levelNumber < LAST_IMPLEMENTED_LEVEL) {
                drawCenteredText(panelX + panelW - 220, panelY + 155, 170, "+10 true gate", "Arial", 18, true, COLOR_ACCENT_2);
            }
        } else {
            changeColor(COLOR_ACCENT);
            drawSolidRectangle(panelX, panelY, 8, panelH);

            changeColor(COLOR_TEXT);
            drawBoldText(contentX, panelY + 64, "Choose a stage", "Arial", 34);
            drawWrappedText(
                    contentX,
                    panelY + 112,
                    contentW,
                    "Move the cursor over a level to preview its route, hazards, and scoring target.",
                    "Arial",
                    22,
                    COLOR_MUTED_TEXT
            );
        }
    }

    private void drawInGameHud() {
        fillRoundRect(36, 32, 520, 70, 8, new Color(12, 18, 28, 190));
        drawRoundRect(36, 32, 520, 70, 8, 2, new Color(105, 126, 160));

        changeColor(COLOR_TEXT);
        drawBoldText(62, 77, "Level " + currentLevel, "Arial", 26);

        changeColor(COLOR_MUTED_TEXT);
        drawText(186, 77, "Score  P1: " + playerScores[0] + "    P2: " + playerScores[1], "Arial", 22);

        if (currentLevel >= 1 && currentLevel <= 4) {
            drawPowerLevelHud();
        }
    }

    private void drawPowerLevelHud() {
        double panelW = 560;
        double panelH = 70;
        double panelX = width() - panelW - 36;
        double panelY = 32;

        fillRoundRect(panelX, panelY, panelW, panelH, 8, new Color(12, 18, 28, 190));
        drawRoundRect(panelX, panelY, panelW, panelH, 8, 2, new Color(105, 126, 160));

        drawPowerHudPlayer(panelX + 24, panelY + 45, 1, COLOR_ACCENT);
        drawPowerHudPlayer(panelX + 292, panelY + 45, 2, COLOR_ACCENT_2);
    }

    private void drawPowerHudPlayer(double x, double y, int playerNumber, Color color) {
        int playerIndex = playerNumber - 1;
        int powerLevel = playerPowerLevels[playerIndex];
        int maxKnives = getMaxKnivesForPowerLevel(powerLevel);
        int activeKnives = 0;

        if (level != null) {
            activeKnives = level.getActivePlayerKnifeCount(playerNumber);
        }

        changeColor(color);
        drawBoldText(x, y, "P" + playerNumber + " Level " + powerLevel, "Arial", 22);
        changeColor(COLOR_MUTED_TEXT);
        drawText(x + 138, y, "Knives " + activeKnives + "/" + maxKnives, "Arial", 20);
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
        updateBackgroundMusic();

        if (!(currentLevel == 5 && level instanceof level5 && !gameOver && !levelComplete && !gamePaused)) {
            updateMissileLockSound(false, dt);
        }

        if (currentLevel >= 1 && currentLevel <= 5 && !gameOver && !levelComplete && !gamePaused) {
            boolean player1WasDead = player[0].dead;
            boolean player2WasDead = player[1].dead;

            level.update(dt);

            if (level.didTeleport()) {
                playSound(transSound);
            }

            if (level.didEnemyDie()) {
                playSound(enemyDeadSound);
            }

            playerScores[0] += level.getEnemyKillsForPlayer(1);
            playerScores[1] += level.getEnemyKillsForPlayer(2);

            if (level instanceof level5) {
                level5 bossLevel = (level5)level;

                if (bossLevel.getLifeLossesThisFrame() > 0) {
                    levelDeaths[1] += bossLevel.getLifeLossesThisFrame();
                    playSound(deadSound);
                }

                if (bossLevel.consumeStage3SoundRequest()) {
                    playSound(stage3Sound);
                }

                if (bossLevel.isBossFightGameOver()) {
                    gameOver = true;
                }

                updateMissileLockSound(!gameOver && !bossLevel.isLevelComplete() &&
                        !bossLevel.isStage3IntroActive() && bossLevel.hasLockedBossMissile(), dt);

                if (bossLevel.isLevelComplete()) {
                    updateMissileLockSound(false, dt);
                    completeLevel(1);
                }

                return;
            }

            if (!player1WasDead && player[0].dead) {
                levelDeaths[0]++;
                if (level.wasPlayerEatenByFakeGate(1)) {
                    startFakeGateDeathSequence(0);
                } else {
                    playSound(level.wasPlayerEaten(1) ? eatSound : deadSound);
                }
            }

            if (!player2WasDead && player[1].dead) {
                levelDeaths[1]++;
                if (level.wasPlayerEatenByFakeGate(2)) {
                    startFakeGateDeathSequence(1);
                } else {
                    playSound(level.wasPlayerEaten(2) ? eatSound : deadSound);
                }
            }

            int reachedPlayer = getWinningPlayer();
            if (level.isLevelComplete() && reachedPlayer != 0) {
                completeLevel(reachedPlayer);
            }
        }

        if (levelComplete && currentLevel != 5) {
            levelCompleteTimer += dt;
        }

        updateFakeGateDeathSequences(dt);

        if (showAllLevelsComplete) {
            allLevelsCompleteTimer += dt;
        }
    }

    private int getWinningPlayer() {
        if (level != null) {
            int winningPlayer = level.getWinningPlayer();
            if (winningPlayer != 0) {
                return winningPlayer;
            }
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

        if (currentLevel == 5) {
            winningPlayer = getFinalWinner();
        } else {
            playerScores[playerNumber - 1] += GATE_WIN_SCORE;
            if (currentLevel >= 1 && currentLevel <= 4) {
                awardPowerLevel(playerNumber);
            }
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

    private void awardPowerLevel(int playerNumber) {
        if (playerNumber < 1 || playerNumber > 2) {
            return;
        }

        int index = playerNumber - 1;
        if (playerPowerLevels[index] < MAX_POWER_LEVEL) {
            playerPowerLevels[index]++;
        }

        applyPlayerPowerLevels();
    }

    private void applyPlayerPowerLevels() {
        for (int i = 0; i < player.length; i++) {
            if (player[i] != null) {
                player[i].setPowerLevel(playerPowerLevels[i]);
            }
        }
    }

    private int getMaxKnivesForPowerLevel(int powerLevel) {
        if (powerLevel <= MIN_POWER_LEVEL) {
            return 0;
        }

        return Math.min(MAX_POWER_LEVEL - 1, powerLevel - MIN_POWER_LEVEL);
    }

    private void firePlayerKnife(int playerNumber) {
        if (level == null || currentLevel < 1 || currentLevel > 4) {
            return;
        }

        level.firePlayerKnife(playerNumber, knifeImage, playerPowerLevels[playerNumber - 1]);
    }

    private void startFakeGateDeathSequence(int playerIndex) {
        fakeGateDeathSequenceActive[playerIndex] = true;
        fakeGateDeathHidePlayer[playerIndex] = true;
        fakeGateDeathSequenceTimer[playerIndex] = 0;
        fakeGateDeathSequenceDeadPlays[playerIndex] = 0;

        playSound(eatSound);

        double sequenceDuration = audioDuration(eatSound) + audioDuration(deadSound) * 3.0;
        player[playerIndex].deadTimer = -Math.max(0, sequenceDuration - 1.0);
    }

    private void updateFakeGateDeathSequences(double dt) {
        for (int i = 0; i < fakeGateDeathSequenceActive.length; i++) {
            if (!fakeGateDeathSequenceActive[i]) {
                continue;
            }

            fakeGateDeathSequenceTimer[i] += dt;

            double eatDuration = audioDuration(eatSound);
            double deadDuration = audioDuration(deadSound);

            while (fakeGateDeathSequenceDeadPlays[i] < 3 &&
                    fakeGateDeathSequenceTimer[i] >= eatDuration + fakeGateDeathSequenceDeadPlays[i] * deadDuration) {
                playSound(deadSound);
                fakeGateDeathSequenceDeadPlays[i]++;
            }

            if (fakeGateDeathSequenceTimer[i] >= eatDuration + deadDuration * 3.0) {
                fakeGateDeathSequenceActive[i] = false;
                fakeGateDeathHidePlayer[i] = false;
            }
        }
    }

    private void resetFakeGateDeathSequences() {
        for (int i = 0; i < fakeGateDeathSequenceActive.length; i++) {
            fakeGateDeathSequenceActive[i] = false;
            fakeGateDeathHidePlayer[i] = false;
            fakeGateDeathSequenceTimer[i] = 0;
            fakeGateDeathSequenceDeadPlays[i] = 0;
        }
    }

    private double audioDuration(AudioClip clip) {
        if (clip == null || clip.getAudioFormat() == null ||
                clip.getAudioFormat().getFrameSize() <= 0 ||
                clip.getAudioFormat().getFrameRate() <= 0) {
            return 1.0;
        }

        return clip.getBufferSize() /
                (clip.getAudioFormat().getFrameSize() * clip.getAudioFormat().getFrameRate());
    }

    private String completionSummaryText() {
        String deaths = "P1 deaths: " + levelDeaths[0] + "    P2 deaths: " + levelDeaths[1];

        if (winningPlayer == 0) {
            return "Co-op clear    " + deaths;
        }

        return "Winner: P" + winningPlayer + "  +" + GATE_WIN_SCORE + "    " + deaths;
    }

    private int getFinalWinner() {
        if (playerScores[0] == playerScores[1]) {
            return 0;
        }

        return playerScores[0] > playerScores[1] ? 1 : 2;
    }

    private String finalWinnerText() {
        int finalWinner = getFinalWinner();

        if (finalWinner == 0) {
            return "Final Result: Draw";
        }

        return "Final Winner: P" + finalWinner;
    }

    private String finalReasonText() {
        int finalWinner = getFinalWinner();

        if (finalWinner == 0) {
            return "Both players finished with the same score.";
        }

        return "P" + finalWinner + " has the higher score.";
    }

    private void returnToLevelSelect() {
        showAllLevelsComplete = false;
        levelComplete = false;
        currentLevel = -1;
        showLevelSelect = true;
    }

    private void playSound(AudioClip sound) {
        playAudio(sound, masterVolumeGain);
    }

    private AudioClip getBgmForCurrentScreen() {
        if (currentLevel >= 1 && currentLevel <= 5) {
            return levelBgms[currentLevel];
        }

        return menuBgm;
    }

    private void updateBackgroundMusic() {
        AudioClip nextBgm = getBgmForCurrentScreen();

        if (nextBgm == currentBgm) {
            ensureBackgroundMusicPlaying();
            updateLoopVolume(currentBgm);
            return;
        }

        if (currentBgm != null) {
            stopAudioLoop(currentBgm);
        }

        currentBgm = nextBgm;
        startBackgroundMusicLoop(currentBgm);
    }

    private void ensureBackgroundMusicPlaying() {
        if (currentBgm == null) {
            return;
        }

        Clip clip = currentBgm.getLoopClip();
        if (clip == null || !clip.isRunning()) {
            startBackgroundMusicLoop(currentBgm);
        }
    }

    private void startBackgroundMusicLoop(AudioClip music) {
        if (music == null) {
            return;
        }

        try {
            Clip clip = music.getLoopClip();

            if (clip == null) {
                clip = AudioSystem.getClip();
                clip.open(
                        music.getAudioFormat(),
                        music.getData(),
                        0,
                        (int)music.getBufferSize()
                );
                music.setLoopClip(clip);
            }

            updateLoopVolume(music);
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (Exception exception) {
            System.out.println("Error playing background music\n");
        }
    }

    private void updateLoopVolume(AudioClip music) {
        if (music == null || music.getLoopClip() == null) {
            return;
        }

        updateClipVolume(music.getLoopClip(), masterVolumeGain + BGM_VOLUME_OFFSET);
    }

    private void updateClipVolume(Clip clip) {
        updateClipVolume(clip, masterVolumeGain);
    }

    private void updateClipVolume(Clip clip, float volumeGain) {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            float volume = Math.max(control.getMinimum(), Math.min(control.getMaximum(), volumeGain));
            control.setValue(volume);
        }
    }

    private void updateMissileLockSound(boolean shouldPlay, double dt) {
        if (!shouldPlay) {
            stopMissileLockClip();
            return;
        }

        if (!missileLockLoopPlaying || missileLockClip == null || !missileLockClip.isRunning()) {
            startMissileLockClip();
        }
    }

    private void startMissileLockClip() {
        if (missileLockSound == null) {
            return;
        }

        try {
            if (missileLockClip == null) {
                missileLockClip = AudioSystem.getClip();
                missileLockClip.open(
                        missileLockSound.getAudioFormat(),
                        missileLockSound.getData(),
                        0,
                        (int)missileLockSound.getBufferSize()
                );
            }

            updateClipVolume(missileLockClip);

            missileLockClip.setFramePosition(0);
            missileLockClip.loop(Clip.LOOP_CONTINUOUSLY);
            missileLockClip.start();
            missileLockLoopPlaying = true;
        } catch (Exception exception) {
            System.out.println("Error playing missile lock Audio Clip\n");
        }
    }

    private void stopMissileLockClip() {
        if (missileLockClip != null) {
            missileLockClip.stop();
            missileLockClip.setFramePosition(0);
        }

        missileLockLoopPlaying = false;
    }

    private void drawLevel5(level5 bossLevel) {
        changeColor(new Color(12, 20, 34));
        drawSolidRectangle(0, 0, 960, 1080);
        changeColor(new Color(16, 18, 28));
        drawSolidRectangle(960, 0, 960, 1080);
        changeColor(new Color(255, 255, 255, 80));
        drawSolidRectangle(958, 0, 4, 1080);

        drawLevel5LeftPanel(bossLevel);
        drawLevel5RightPanel(bossLevel);

        if (bossLevel.isStage3IntroActive()) {
            drawStage3IntroOverlay(bossLevel);
        }
    }

    private void drawLevel5LeftPanel(level5 bossLevel) {
        changeColor(new Color(20, 34, 52));
        drawSolidRectangle(level5.GRID_X, level5.GRID_Y, level5.GRID_COLS * level5.CELL, level5.GRID_ROWS * level5.CELL);

        changeColor(new Color(56, 78, 104, 160));
        for (int col = 0; col <= level5.GRID_COLS; col++) {
            double x = level5.GRID_X + col * level5.CELL;
            drawLine(x, level5.GRID_Y, x, level5.GRID_Y + level5.GRID_ROWS * level5.CELL);
        }

        for (int row = 0; row <= level5.GRID_ROWS; row++) {
            double y = level5.GRID_Y + row * level5.CELL;
            drawLine(level5.GRID_X, y, level5.GRID_X + level5.GRID_COLS * level5.CELL, y);
        }

        for (level5.SupplyItem item : bossLevel.supplyItems) {
            drawSupplyItem(item);
        }

        for (int i = bossLevel.greedyPig.size() - 1; i >= 0; i--) {
            level5.GridPoint part = bossLevel.greedyPig.get(i);
            double x = level5.GRID_X + part.col * level5.CELL;
            double y = level5.GRID_Y + part.row * level5.CELL;

            if (i == 0 && player[0] != null) {
                drawImage(player[0].getCurrentImage(), x, y, 50, 50);
            } else {
                changeColor(new Color(74, 190, 95));
                drawSolidRectangle(x + 6, y + 6, 38, 38);
                changeColor(new Color(180, 255, 190));
                drawRectangle(x + 6, y + 6, 38, 38, 2);
            }
        }

        changeColor(COLOR_TEXT);
        drawBoldText(50, 32, "Supply Pig", "Arial", 24);
        changeColor(COLOR_MUTED_TEXT);
        drawText(210, 32, "Missiles " + bossLevel.missileAmmo + "/" + level5.MAX_MISSILES, "Arial", 20);
        drawText(380, 32, "Shield " + (bossLevel.shield ? "ON" : "OFF"), "Arial", 20);
        drawText(520, 32, "Rage " + String.format("%.1f", bossLevel.rageTimer), "Arial", 20);

        if (bossLevel.greedyStunTimer > 0) {
            drawCenteredText(260, 560, 440, "STUN " + String.format("%.1f", bossLevel.greedyStunTimer), "Arial", 42, true, COLOR_ACCENT_2);
        }
    }

    private void drawSupplyItem(level5.SupplyItem item) {
        Color color;
        String label;

        if (item.type == level5.ItemType.MISSILE || item.type == level5.ItemType.CURSED_MISSILE) {
            color = new Color(255, 140, 45);
            label = "M";
        } else if (item.type == level5.ItemType.HEAL) {
            color = new Color(80, 220, 120);
            label = "+";
        } else if (item.type == level5.ItemType.SHIELD) {
            color = new Color(80, 190, 255);
            label = "S";
        } else {
            color = new Color(245, 70, 65);
            label = "R";
        }

        changeColor(color);
        drawSolidCircle(item.x() + 25, item.y() + 25, 20);
        changeColor(new Color(255, 255, 255, 220));
        drawCircle(item.x() + 25, item.y() + 25, 20, 3);
        drawCenteredText(item.x(), item.y() + 34, 50, label, "Arial", 24, true, Color.WHITE);
    }

    private void drawLevel5RightPanel(level5 bossLevel) {
        if (bossLevel.isRaging()) {
            changeColor(new Color(180, 35, 30, 50));
            drawSolidRectangle(960, 0, 960, 1080);
        }

        drawBossHealthBar(bossLevel);
        drawBattleHud(bossLevel);
        drawBattlePig(bossLevel);
        if (!bossLevel.isBossExplosionActive() && bossLevel.bossHp > 0) {
            drawBoss(bossLevel);
        }
        drawLevel5WeaponsAndHazards(bossLevel);

        if (bossLevel.isBossExplosionActive()) {
            drawBossExplosion(bossLevel);
        }
    }

    private void drawBossHealthBar(level5 bossLevel) {
        double ratio = bossLevel.bossHp / level5.MAX_BOSS_HP;
        if (ratio < 0) ratio = 0;

        changeColor(new Color(36, 42, 56));
        drawSolidRectangle(1080, 40, 700, 30);
        changeColor(ratio > 0.7 ? new Color(80, 210, 110) : ratio > 0.35 ? new Color(255, 190, 70) : new Color(235, 70, 70));
        drawSolidRectangle(1080, 40, 700 * ratio, 30);
        changeColor(Color.WHITE);
        drawRectangle(1080, 40, 700, 30, 2);
        drawText(1080, 32, "BOSS HP  Phase " + bossLevel.phaseNumber(), "Arial", 22);

        if (bossLevel.isRaging()) {
            changeColor(new Color(255, 90, 70));
            drawBoldText(1620, 32, "RAGE " + String.format("%.1f", bossLevel.rageTimer) + "s", "Arial", 22);
        }
    }

    private void drawBattleHud(level5 bossLevel) {
        changeColor(COLOR_TEXT);
        drawText(980, 58, "Lives: " + bossLevel.battleLives, "Arial", 22);
        drawText(980, 105, "Missiles: " + bossLevel.missileAmmo + "/" + level5.MAX_MISSILES, "Arial", 22);
        drawText(980, 145, "Shield: " + (bossLevel.shield ? "ON" : "OFF"), "Arial", 22);
        drawText(980, 185, "Rage: " + String.format("%.1f", bossLevel.rageTimer), "Arial", 22);

        if (!bossLevel.bossMissiles.isEmpty()) {
            drawCenteredText(1280, 105, 360, "WARNING!", "Arial", 30, true, new Color(255, 80, 60));
        }
    }

    private void drawBattlePig(level5 bossLevel) {
        if (bossLevel.isBattlePigInvincible() && ((int)(System.currentTimeMillis() / 120) % 2 == 0)) {
            return;
        }

        if (bossLevel.shield) {
            changeColor(new Color(80, 190, 255, 80));
            drawSolidCircle(level5.BATTLE_X + 25, bossLevel.battleY + 25, 42);
            changeColor(new Color(150, 225, 255, 220));
            drawCircle(level5.BATTLE_X + 25, bossLevel.battleY + 25, 42, 3);
        }

        drawSinglePlayer(player[1]);
    }

    private void drawBoss(level5 bossLevel) {
        if (bossFrames != null && bossFrames.length > 0) {
            int frameIndex = bossLevel.getBossFrameIndex();
            if (frameIndex < 0 || frameIndex >= bossFrames.length) {
                frameIndex = 0;
            }

            drawImage(
                    bossFrames[frameIndex],
                    level5.BOSS_DRAW_X,
                    level5.BOSS_DRAW_Y,
                    level5.BOSS_DRAW_W,
                    level5.BOSS_DRAW_H
            );
            return;
        }

        changeColor(new Color(130, 35, 48));
        drawSolidRectangle(level5.BOSS_X, level5.BOSS_Y, level5.BOSS_W, level5.BOSS_H);
        changeColor(new Color(210, 70, 80));
        drawSolidRectangle(level5.BOSS_X + 18, level5.BOSS_Y + 30, level5.BOSS_W - 36, 56);
        changeColor(Color.WHITE);
        drawSolidCircle(level5.BOSS_X + 48, level5.BOSS_Y + 80, 12);
        drawSolidCircle(level5.BOSS_X + 112, level5.BOSS_Y + 80, 12);
        changeColor(new Color(20, 20, 24));
        drawSolidCircle(level5.BOSS_X + 52, level5.BOSS_Y + 82, 6);
        drawSolidCircle(level5.BOSS_X + 116, level5.BOSS_Y + 82, 6);
        changeColor(new Color(255, 190, 80));
        drawRectangle(level5.BOSS_X, level5.BOSS_Y, level5.BOSS_W, level5.BOSS_H, 4);
    }

    private void drawStage3IntroOverlay(level5 bossLevel) {
        drawScrim();
        drawPanel(390, 420, 1140, 240);
        drawCenteredText(390, 525, 1140, level5.STAGE3_MESSAGE, "Arial", 32, true, COLOR_ACCENT_2);
        drawCenteredText(
                390,
                595,
                1140,
                String.format("%.1f", bossLevel.getStage3IntroTimer()),
                "Arial",
                34,
                true,
                COLOR_TEXT
        );
    }

    private void drawBossExplosion(level5 bossLevel) {
        double centerX = level5.BOSS_DRAW_X + level5.BOSS_DRAW_W / 2.0;
        double centerY = level5.BOSS_DRAW_Y + level5.BOSS_DRAW_H / 2.0;
        double progress = bossLevel.getBossExplosionProgress();
        double fade = Math.max(0, 1.0 - progress * 0.75);
        int coreAlpha = (int)(220 * fade);
        int blastAlpha = (int)(180 * fade);
        int smokeAlpha = (int)(120 * fade);

        changeColor(new Color(255, 245, 185, coreAlpha));
        drawSolidCircle(centerX, centerY, 58 + progress * 50);
        changeColor(new Color(255, 120, 30, blastAlpha));
        drawSolidCircle(centerX, centerY, 96 + progress * 85);
        changeColor(new Color(170, 30, 25, smokeAlpha));
        drawSolidCircle(centerX, centerY, 140 + progress * 130);

        for (level5.BossExplosionParticle particle : bossLevel.bossExplosionParticles) {
            double alphaRatio = particle.alphaRatio();
            if (alphaRatio <= 0) {
                continue;
            }

            int alpha = (int)(230 * alphaRatio);
            Color color;
            if (particle.colorIndex == 0) {
                color = new Color(255, 235, 150, alpha);
            } else if (particle.colorIndex == 1) {
                color = new Color(255, 140, 30, alpha);
            } else if (particle.colorIndex == 2) {
                color = new Color(230, 55, 35, alpha);
            } else {
                color = new Color(80, 70, 70, Math.min(190, alpha));
            }

            changeColor(color);
            drawSolidCircle(particle.x, particle.y, particle.radius * alphaRatio);
        }
    }

    private void drawLevel5WeaponsAndHazards(level5 bossLevel) {
        for (level5.Shot knife : bossLevel.knives) {
            if (bossLevel.getKnifeImage() != null) {
                drawImage(bossLevel.getKnifeImage(), knife.x, knife.y, knife.width, knife.height);
            } else {
                changeColor(new Color(220, 225, 235));
                drawSolidRectangle(knife.x, knife.y + 20, knife.width, 10);
            }
        }

        for (level5.Shot missile : bossLevel.playerMissiles) {
            drawPlayerMissile(missile);
        }

        for (level5.Shot missile : bossLevel.cursedMissiles) {
            drawPlayerMissile(missile);
        }

        Image enemyImage = bossLevel.getEnemyImage();
        for (level5.FlyingEnemy enemy : bossLevel.flyingEnemies) {
            if (enemyImage != null) {
                drawImage(enemyImage, enemy.x, enemy.y, enemy.width, enemy.height);
            } else {
                changeColor(new Color(150, 80, 220));
                drawSolidCircle(enemy.x + 25, enemy.y + 25, 24);
            }
        }

        for (level5.BossMissile missile : bossLevel.bossMissiles) {
            drawBossMissile(missile);
        }
    }

    private void drawPlayerMissile(level5.Shot missile) {
        drawMissileWithFlame(missile.x, missile.y, missile.width, missile.height, missile.vx, missile.vy);
    }

    private void drawBossMissile(level5.BossMissile missile) {
        drawMissileWithFlame(missile.x, missile.y, missile.width, missile.height, missile.vx, missile.vy);
    }

    private void drawMissileWithFlame(double x, double y, double w, double h, double vx, double vy) {
        double centerX = x + w / 2.0;
        double centerY = y + h / 2.0;
        double speed = Math.max(1, Math.sqrt(vx * vx + vy * vy));
        double dirX = vx / speed;
        double dirY = vy / speed;
        double tailX = centerX - dirX * h * 0.48;
        double tailY = centerY - dirY * h * 0.48;

        drawMissileFlame(tailX, tailY, dirX, dirY);

        if (missileImage != null) {
            saveCurrentTransform();
            translate(centerX, centerY);
            rotate(Math.toDegrees(Math.atan2(-dirX, dirY)));
            drawImage(missileImage, -w / 2.0, -h / 2.0, w, h);
            restoreLastTransform();
            return;
        }

        changeColor(new Color(230, 60, 50));
        drawSolidRectangle(x + 8, y + 14, w - 16, h - 28);
        drawSolidCircle(centerX + dirX * 14, centerY + dirY * 14, 10);
    }

    private void drawMissileFlame(double tailX, double tailY, double dirX, double dirY) {
        double flameX = tailX - dirX * 12;
        double flameY = tailY - dirY * 12;

        changeColor(new Color(255, 80, 25, 190));
        drawSolidCircle(flameX, flameY, 13);
        changeColor(new Color(255, 185, 45, 220));
        drawSolidCircle(flameX - dirX * 5, flameY - dirY * 5, 9);
        changeColor(new Color(255, 240, 120, 235));
        drawSolidCircle(flameX - dirX * 10, flameY - dirY * 10, 5);
    }

    private void drawFinalSettlementScreen() {
        drawCenteredText(520, 310, 880, "FINAL RESULT", "Arial", 58, true, new Color(255, 215, 0));
        drawCenteredText(520, 385, 880, finalWinnerText(), "Arial", 40, true, COLOR_TEXT);
        drawCenteredText(520, 440, 880, finalReasonText(), "Arial", 26, false, COLOR_MUTED_TEXT);

        fillRoundRect(650, 500, 280, 120, 8, new Color(24, 32, 48));
        drawRoundRect(650, 500, 280, 120, 8, 2, new Color(105, 126, 160));
        fillRoundRect(990, 500, 280, 120, 8, new Color(24, 32, 48));
        drawRoundRect(990, 500, 280, 120, 8, 2, new Color(105, 126, 160));

        drawCenteredText(650, 545, 280, "P1", "Arial", 28, true, COLOR_TEXT);
        drawCenteredText(650, 595, 280, String.valueOf(playerScores[0]), "Arial", 40, true, COLOR_ACCENT);
        drawCenteredText(990, 545, 280, "P2", "Arial", 28, true, COLOR_TEXT);
        drawCenteredText(990, 595, 280, String.valueOf(playerScores[1]), "Arial", 40, true, COLOR_ACCENT_2);

        drawCenteredText(520, 690, 880, "Boss cleared. Higher score wins.", "Arial", 26, false, COLOR_TEXT);
        drawCenteredText(520, 750, 880, "SPACE: level select    R: replay boss    ESC: level select", "Arial", 24, false, COLOR_TEXT);
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
            drawText(210, 635, "First pig into the true gate scores 10.", "Arial", 24);

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
            if (currentLevel == 5 && level instanceof level5) {
                drawLevel5((level5)level);
            } else {
                drawLevel(level);

                if (level.getGate() != null) {
                    drawGate(level.getGate());
                }

                if (!gameOver && !levelComplete && !gamePaused) {
                    boolean showPlayer1 = !level.getGate().hasPlayerReached(1);
                    boolean showPlayer2 = !level.getGate().hasPlayerReached(2);
                    showPlayer1 = showPlayer1 && !player[0].trappedInFakeGate;
                    showPlayer2 = showPlayer2 && !player[1].trappedInFakeGate;

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
            }

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
                    drawFinalSettlementScreen();
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
        Image enemySheet = loadImage("resources/enemy.png");
        Image bossSheet = loadImage("resources/boss.png");
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
        enemyIdleFrames = sliceEnemyRow(enemySheet, 0);
        enemyLeftFrames = sliceEnemyRow(enemySheet, 1);
        bossFrames = sliceBossFrames(bossSheet);

        player[0] = new player(maleStay, maleLeft, maleJump);
        player[1] = new player(maleStay, maleLeft, maleJump);
        applyCharacterSelection();
        applyPlayerPowerLevels();

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
        missileImage = loadImage("resources/missile.png");

        gameOverImage = loadImage("resources/oneplayersurvive.png");
        victoryImage = loadImage("resources/victory.png");
        deadPigImage = loadImage("resources/deadPig.png");
        deadSound = loadAudio("resources/dead.wav");
        winSound = loadAudio("resources/win.wav");
        transSound = loadAudio("resources/trans.wav");
        eatSound = loadAudio("resources/eat.wav");
        enemyDeadSound = loadAudio("resources/enemyDead.wav");
        missileLockSound = loadAudio("resources/missileLock.wav");
        stage3Sound = loadAudio("resources/stage3.wav");
        menuBgm = loadAudio("resources/bgmStart.wav");
        levelBgms[1] = loadAudio("resources/bgm1.wav");
        levelBgms[2] = loadAudio("resources/bgm2.wav");
        levelBgms[3] = loadAudio("resources/bgm3.wav");
        levelBgms[4] = loadAudio("resources/bgm4.wav");
        levelBgms[5] = loadAudio("resources/bgm5.wav");


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
                gateImage,
                enemyIdleFrames,
                enemyLeftFrames
        );

        initializeLevelButtons();
        updateBackgroundMusic();
    }

    private void initializeLevelButtons() {
        levelButtons = new LevelButton[5];

        double buttonWidth = 170;
        double buttonHeight = 86;

        levelButtons[0] = new LevelButton(
                1,
                260,
                720,
                buttonWidth,
                buttonHeight,
                "Level 1: Wind Gate Sprint",
                "Avoid the fake gate, ride the wind vent, use portals, and sprint to the far-right true gate.",
                true
        );
        levelButtons[1] = new LevelButton(
                2,
                620,
                560,
                buttonWidth,
                buttonHeight,
                "Level 2: Breakaway Corridor",
                "Choose a route over collapsing floor blocks while flying knives, spikes, and saws block the exit.",
                true
        );
        levelButtons[2] = new LevelButton(
                3,
                980,
                700,
                buttonWidth,
                buttonHeight,
                "Level 3: Upper Route Gauntlet",
                "Climb through dense spikes and moving saws, use portal shortcuts, and ignore the far-right fake gate.",
                true
        );
        levelButtons[3] = new LevelButton(
                4,
                1320,
                500,
                buttonWidth,
                buttonHeight,
                "Level 4: Fake Door Tower",
                "Start apart, climb through return doors and hidden spikes, then touch the top-center true gate first.",
                true
        );
        levelButtons[4] = new LevelButton(
                5,
                1500,
                760,
                buttonWidth,
                buttonHeight,
                "Level 5: Split-Screen Boss",
                "P1 collects supplies on the grid while P2 dodges missiles and fires weapons at the boss.",
                true
        );
    }

    private void restartLevel() {
        gameOver = false;
        levelComplete = false;
        showAllLevelsComplete = false;
        resetLevelStats();
        resetFakeGateDeathSequences();

        if (currentLevel == 1) {
            level = new level1(loadImage("resources/bg1.png"));
        } else if (currentLevel == 2) {
            level = new level2(loadImage("resources/bg1.png"));
        } else if (currentLevel == 3) {
            level = new level3(loadImage("resources/bg1.png"));
        } else if (currentLevel == 4) {
            level = new level4(loadImage("resources/bg1.png"));
        }
        else if (currentLevel == 5) {
            level = new level5(loadImage("resources/bg1.png"));
        }

        player[0].dead = false;
        player[1].dead = false;
        player[0].trappedInFakeGate = false;
        player[1].trappedInFakeGate = false;
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
        applyPlayerPowerLevels();

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
                gateImage,
                enemyIdleFrames,
                enemyLeftFrames
        );
    }

    private void nextLevel() {
        gameOver = false;
        levelComplete = false;
        showAllLevelsComplete = false;
        resetLevelStats();
        resetFakeGateDeathSequences();

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
        else if (currentLevel == 5) {
            level = new level5(loadImage("resources/bg1.png"));
        }

        player[0].dead = false;
        player[1].dead = false;
        player[0].trappedInFakeGate = false;
        player[1].trappedInFakeGate = false;
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
        applyPlayerPowerLevels();


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
                gateImage,
                enemyIdleFrames,
                enemyLeftFrames
        );
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
        resetFakeGateDeathSequences();

        if (levelNumber == 1) {
            level = new level1(loadImage("resources/bg1.png"));
        } else if (levelNumber == 2) {
            level = new level2(loadImage("resources/bg1.png"));
        } else if (levelNumber == 3) {
            level = new level3(loadImage("resources/bg1.png"));
        } else if (levelNumber == 4) {
            level = new level4(loadImage("resources/bg1.png"));
        }
        else if (levelNumber == 5) {
            level = new level5(loadImage("resources/bg1.png"));
        }

        player[0].dead = false;
        player[1].dead = false;
        player[0].trappedInFakeGate = false;
        player[1].trappedInFakeGate = false;
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
        applyPlayerPowerLevels();

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
                gateImage,
                enemyIdleFrames,
                enemyLeftFrames
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
                if (showAllLevelsComplete) {
                    if (event.getKeyCode() == KeyEvent.VK_SPACE ||
                            event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        returnToLevelSelect();
                    }

                    if (event.getKeyCode() == KeyEvent.VK_R) {
                        restartLevel();
                    }

                    return;
                }

                if (event.getKeyCode() == KeyEvent.VK_SPACE) {
                    nextLevel();
                }
                if (event.getKeyCode() == KeyEvent.VK_R) {
                    restartLevel();
                }
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    returnToLevelSelect();
                }
                return;
            }

            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                gamePaused = true;
                showPauseHelp = false;
                return;
            }

            if (currentLevel == 5 && level instanceof level5) {
                level5 bossLevel = (level5)level;
                boolean controlsReversed = bossLevel.areControlsReversed();

                if (bossLevel.isStage3IntroActive()) {
                    return;
                }

                if (event.getKeyCode() == KeyEvent.VK_W) {
                    bossLevel.setGreedyDirection(0, controlsReversed ? 1 : -1);
                }

                if (event.getKeyCode() == KeyEvent.VK_A) {
                    bossLevel.setGreedyDirection(controlsReversed ? 1 : -1, 0);
                }

                if (event.getKeyCode() == KeyEvent.VK_S) {
                    bossLevel.setGreedyDirection(0, controlsReversed ? -1 : 1);
                }

                if (event.getKeyCode() == KeyEvent.VK_D) {
                    bossLevel.setGreedyDirection(controlsReversed ? -1 : 1, 0);
                }

                if ((!controlsReversed && event.getKeyCode() == KeyEvent.VK_UP) ||
                        (controlsReversed && event.getKeyCode() == KeyEvent.VK_DOWN)) {
                    bossLevel.setJetpackPressed(true);
                }

                if (event.getKeyCode() == KeyEvent.VK_SPACE) {
                    bossLevel.firePlayerMissile();
                }

                return;
            }

            if (currentLevel >= 1 && currentLevel <= 4) {
                if (event.getKeyCode() == KeyEvent.VK_F) {
                    firePlayerKnife(1);
                }

                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    firePlayerKnife(2);
                }
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

        if (currentLevel == 5 && level instanceof level5) {
            level5 bossLevel = (level5)level;

            if (bossLevel.isStage3IntroActive()) {
                return;
            }

            if ((!bossLevel.areControlsReversed() && event.getKeyCode() == KeyEvent.VK_UP) ||
                    (bossLevel.areControlsReversed() && event.getKeyCode() == KeyEvent.VK_DOWN)) {
                bossLevel.setJetpackPressed(false);
            }

            return;
        }

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

        updateLoopVolume(currentBgm);
        updateClipVolume(missileLockClip);
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
        } else if (currentLevel == 5 && level instanceof level5 && !gameOver && !levelComplete && !gamePaused) {
            ((level5)level).setJetpackPressed(true);
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        if (currentLevel == 5 && level instanceof level5) {
            ((level5)level).setJetpackPressed(false);
        }
    }
}
