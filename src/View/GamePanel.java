package View;

import Model.*;
import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

// Main game rendering panel and input handler
public class GamePanel extends JPanel implements java.awt.event.KeyListener {
    // Starfield background
    private static final int STAR_COUNT = 120;
    private final int[] starX = new int[STAR_COUNT];
    private final int[] starY = new int[STAR_COUNT];
    private final int[] starSize = new int[STAR_COUNT];

    // Initialize star positions
    private void initStars() {
        int w = Model.GameState.Config.WINDOW_WIDTH;
        int h = Model.GameState.Config.WINDOW_HEIGHT;
        java.util.Random rand = new java.util.Random(42); // fixed seed for consistency
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = rand.nextInt(w);
            starY[i] = rand.nextInt(h);
            starSize[i] = 1 + rand.nextInt(2); // 1 or 2 px
        }
    }
    // Callback to start the game (set by controller)
    private Runnable gameStartCallback;

    // Show the start overlay before the game begins
    private boolean showStartOverlay = true;

    // Draw current score
    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("Score: " + gameState.score, 10, 30);
    }

    // Draw heart icons for lives
    private void drawHearts(Graphics g) {
        int heartY = 50;
        int heartX = 10;
        int heartSize = 24;
        for (int i = 0; i < HEART_COUNT; i++) {
            Image heartImg = (i < gameState.lives)
                    ? HEART_ICON
                    : GrayFilter.createDisabledImage(HEART_ICON);
            g.drawImage(heartImg, heartX + i * (heartSize + 6), heartY, heartSize, heartSize, null);
        }
    }

    // Icon images
    private static final Image HEART_ICON;
    private static final Image SHIP_ICON;
    private static final Image ASTEROID_ICON;
    static {
        HEART_ICON = new ImageIcon("resources/icons/heart.png").getImage();
        SHIP_ICON = new ImageIcon("resources/icons/ship.png").getImage();
        ASTEROID_ICON = new ImageIcon("resources/icons/asteroid.png").getImage();
    }
    private static final int HEART_COUNT = Model.GameState.Config.INITIAL_LIVES;
    private static final int GAME_OVER_Y = 300;
    private static final int RESTART_Y = 350;
    private GameState gameState;
    private PauseMenuListener pauseMenuListener;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Map<Integer, Runnable> keyPressActions = new HashMap<>();
    private final Map<Integer, Runnable> keyReleaseActions = new HashMap<>();

    // Map keys to game actions
    private void setupKeyActions() {
        // Only WASD and space controls
        keyPressActions.put(java.awt.event.KeyEvent.VK_A, () -> gameState.left = true);
        keyPressActions.put(java.awt.event.KeyEvent.VK_D, () -> gameState.right = true);
        keyPressActions.put(java.awt.event.KeyEvent.VK_W, () -> gameState.up = true);
        keyPressActions.put(java.awt.event.KeyEvent.VK_SPACE, () -> gameState.shooting = true);
        keyPressActions.put(java.awt.event.KeyEvent.VK_S, () -> gameState.ship.setDecelerating(true));
        keyReleaseActions.put(java.awt.event.KeyEvent.VK_A, () -> gameState.left = false);
        keyReleaseActions.put(java.awt.event.KeyEvent.VK_D, () -> gameState.right = false);
        keyReleaseActions.put(java.awt.event.KeyEvent.VK_W, () -> gameState.up = false);
        keyReleaseActions.put(java.awt.event.KeyEvent.VK_SPACE, () -> gameState.shooting = false);
        keyReleaseActions.put(java.awt.event.KeyEvent.VK_S, () -> gameState.ship.setDecelerating(false));
    }

    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
        int code = e.getKeyCode();
        // Allow restart with 'R' if game over
        if (gameState.gameOver && code == java.awt.event.KeyEvent.VK_R) {
            if (pauseMenuListener != null) pauseMenuListener.onRestart();
            return;
        }
        if (gameState.paused) return;
        if (!pressedKeys.add(code)) return;
        Runnable action = keyPressActions.get(code);
        if (action != null) action.run();
    }

    @Override
    public void keyReleased(java.awt.event.KeyEvent e) {
        int code = e.getKeyCode();
        pressedKeys.remove(code);
        Runnable action = keyReleaseActions.get(code);
        if (action != null) action.run();
    }

    @Override
    public void keyTyped(java.awt.event.KeyEvent e) {
        // Not used
    }
    
    // Listener for pause menu actions
    public interface PauseMenuListener {
        void onResume();
        void onRestart();
        void onSettings();
    }
    public void setPauseMenuListener(PauseMenuListener listener) {
        this.pauseMenuListener = listener;
    }

    // Set up panel, input, and mouse handling
    public void setGameStartCallback(Runnable callback) {
        this.gameStartCallback = callback;
    }

    public GamePanel(GameState gameState) {
        this.gameState = gameState;
        setPreferredSize(new Dimension(Model.GameState.Config.WINDOW_WIDTH, Model.GameState.Config.WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        initStars();
        setFocusable(true);
        addKeyListener(this);
        setupKeyActions();
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
            }
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
                if (showStartOverlay) {
                    int mx = e.getX();
                    int my = e.getY();
                    int centerX = getWidth() / 2;
                    int buttonY = getHeight() / 2 + 60;
                    // Big START button
                    if (mx >= centerX - 120 && mx <= centerX + 120 && my >= buttonY - 40 && my <= buttonY + 20) {
                        showStartOverlay = false;
                        if (gameStartCallback != null) gameStartCallback.run();
                        repaint();
                        return;
                    }
                }
                if (gameState.paused && pauseMenuListener != null) {
                    int mx = e.getX();
                    int my = e.getY();
                    int centerX = getWidth() / 2;
                    int y = 320;
                    // Resume button
                    if (mx >= centerX - 100 && mx <= centerX + 100 && my >= y - 30 && my <= y + 10) {
                        pauseMenuListener.onResume();
                        return;
                    }
                    y += 50;
                    // Restart button
                    if (mx >= centerX - 100 && mx <= centerX + 100 && my >= y - 30 && my <= y + 10) {
                        pauseMenuListener.onRestart();
                        return;
                    }
                    y += 50;
                    // Settings button
                    if (mx >= centerX - 100 && mx <= centerX + 100 && my >= y - 30 && my <= y + 10) {
                        pauseMenuListener.onSettings();
                        return;
                    }
                }
            }
        });
    }

    // Main rendering method
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw starfield background
        g.setColor(Color.WHITE);
        for (int i = 0; i < STAR_COUNT; i++) {
            g.fillRect(starX[i], starY[i], starSize[i], starSize[i]);
        }
        // Flicker effect for invincibility
        if (!gameState.invincible || ((System.currentTimeMillis() / 100) % 2 == 0)) {
            drawShip(g, gameState.ship);
        }
        drawAsteroids(g, gameState.asteroids);
        drawBullets(g, gameState.bullets);
        drawScore(g);
        drawHearts(g);
        if (gameState.gameOver) drawGameOverOverlay(g);
        if (gameState.paused) drawPauseOverlay(g);
        if (showStartOverlay) drawStartOverlay(g);
    }

    // Draw a blurry/translucent overlay for GAME OVER
    private void drawGameOverOverlay(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // Blurry/translucent overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());
        drawGameOver(g);
    }

    // Draw the start overlay with blur and big START button
    private void drawStartOverlay(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // Blurry/translucent overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());
        // Title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 56));
        String title = "ASTEROIDS";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (getWidth() - titleWidth) / 2, getHeight() / 2 - 40);
        // Big START button
        int buttonY = getHeight() / 2 + 60;
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRoundRect((getWidth() - 240) / 2, buttonY - 40, 240, 60, 30, 30);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        String startMsg = "START";
        int startWidth = g2.getFontMetrics().stringWidth(startMsg);
        g2.drawString(startMsg, (getWidth() - startWidth) / 2, buttonY);
        // Instructions
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.setColor(Color.WHITE);
        String instr = "Use arrow keys or WASD to move. Space to shoot.";
        int instrWidth = g2.getFontMetrics().stringWidth(instr);
        g2.drawString(instr, (getWidth() - instrWidth) / 2, buttonY + 60);
    }

    // Draw the pause menu overlay
    private void drawPauseOverlay(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // Draw translucent overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());
        // Draw pause menu
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        String pauseMsg = "PAUSED";
        int pauseWidth = g2.getFontMetrics().stringWidth(pauseMsg);
        g2.drawString(pauseMsg, (getWidth() - pauseWidth) / 2, 200);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        String scoreMsg = "Score: " + gameState.score;
        int scoreWidth = g2.getFontMetrics().stringWidth(scoreMsg);
        g2.drawString(scoreMsg, (getWidth() - scoreWidth) / 2, 250);

        // Draw buttons (visual only, not interactive here)
        String resumeMsg = "Resume";
        String restartMsg = "Restart";
        String settingsMsg = "Settings";
        int y = 320;
        int resumeWidth = g2.getFontMetrics().stringWidth(resumeMsg);
        int restartWidth = g2.getFontMetrics().stringWidth(restartMsg);
        int settingsWidth = g2.getFontMetrics().stringWidth(settingsMsg);
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRoundRect((getWidth() - 200) / 2, y - 30, 200, 40, 20, 20);
        g2.setColor(Color.BLACK);
        g2.drawString(resumeMsg, (getWidth() - resumeWidth) / 2, y);
        y += 50;
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRoundRect((getWidth() - 200) / 2, y - 30, 200, 40, 20, 20);
        g2.setColor(Color.BLACK);
        g2.drawString(restartMsg, (getWidth() - restartWidth) / 2, y);
        y += 50;
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRoundRect((getWidth() - 200) / 2, y - 30, 200, 40, 20, 20);
        g2.setColor(Color.BLACK);
        g2.drawString(settingsMsg, (getWidth() - settingsWidth) / 2, y);

        // Draw high score at the bottom
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        String highScoreMsg = "Highscore: " + gameState.getHighScore();
        int highScoreWidth = g2.getFontMetrics().stringWidth(highScoreMsg);
        g2.setColor(Color.WHITE);
        g2.drawString(highScoreMsg, (getWidth() - highScoreWidth) / 2, getHeight() - 40);
    }

    // Draw the game over message
    private void drawGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String msg = "GAME OVER";
        int msgWidth = g.getFontMetrics().stringWidth(msg);
        g.drawString(msg, (Model.GameState.Config.WINDOW_WIDTH - msgWidth) / 2, GAME_OVER_Y);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String restartMsg = "Press R to restart";
        int restartWidth = g.getFontMetrics().stringWidth(restartMsg);
        g.drawString(restartMsg, (Model.GameState.Config.WINDOW_WIDTH - restartWidth) / 2, RESTART_Y);
    }

    // Draw the ship icon at its position and angle
    private void drawShip(Graphics g, Ship ship) {
        Graphics2D g2 = (Graphics2D) g;
        int iconW = 32, iconH = 32;
        g2.translate(ship.x, ship.y);
        g2.rotate(ship.angle);
        g2.drawImage(SHIP_ICON, -iconW/2, -iconH/2, iconW, iconH, null);
        g2.rotate(-ship.angle);
        g2.translate(-ship.x, -ship.y);
    }

    // Draw all asteroids
    private void drawAsteroids(Graphics g, List<Asteroid> asteroids) {
        Graphics2D g2 = (Graphics2D) g;
        for (Asteroid a : asteroids) {
            int iconW = a.size * 2;
            int iconH = a.size * 2;
            double angle = Math.atan2(a.velocityY, a.velocityX);
            g2.translate(a.x, a.y);
            g2.rotate(angle);
            g2.drawImage(ASTEROID_ICON, -iconW/2, -iconH/2, iconW, iconH, null);
            g2.rotate(-angle);
            g2.translate(-a.x, -a.y);
        }
    }

    // Draw all bullets
    private void drawBullets(Graphics g, List<Bullet> bullets) {
        g.setColor(Color.MAGENTA);
        for (Bullet b : bullets) {
            g.fillOval((int) b.x - 2, (int) b.y - 2, 4, 4);
        }
    }
}
