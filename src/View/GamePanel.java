package View;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

/**
 * VISTA PRINCIPAL - Maneja renderizado y entrada del usuario
 * Implementa patrón MVC: no accede directamente al Modelo
 */
public class GamePanel extends JPanel implements java.awt.event.KeyListener {
    
    // Interfaces MVC - Comunicación entre Vista y Controller
    
    /**
     * Proveedor de datos: Controller -> Vista (solo lectura)
     */
    public interface GameDataProvider {
        int getScore();                    // Current game score
        int getLives();                    // Remaining lives
        boolean isGameOver();              // Game over state
        boolean isPaused();                // Pause state
        boolean isInvincible();            // Ship invincibility state
        int getHighScore();                // Highest score achieved
        ShipData getShipData();            // Ship position and angle data
        List<AsteroidData> getAsteroidData(); // All asteroid data for rendering
        List<BulletData> getBulletData();      // All active bullet data
        int getWindowWidth();              // Game window width
        int getWindowHeight();             // Game window height
        int getInitialLives();             // Starting number of lives
    }
    
    /**
     * Escuchador de entrada: Vista -> Controller
     * Traduce teclas a comandos semánticos
     */
    public interface GameInputListener {
        void onMoveLeft(boolean pressed);     // Left movement control
        void onMoveRight(boolean pressed);    // Right movement control
        void onThrust(boolean pressed);       // Forward thrust control
        void onDecelerate(boolean pressed);   // Deceleration control
        void onShoot(boolean pressed);        // Shooting control
        void onRestart();                     // Restart game command
        void onStartGame();                   // Start new game command
        void onResume();                      // Resume from pause command
        void onSettings();                    // Open settings command
        void onExit();                        // Exit game command
    }
    
    // DTOs inmutables para transferir datos entre capas MVC
    
    // Datos de nave para renderizado
    public static class ShipData {
        public final double x, y, angle;    // Position and rotation angle
        public ShipData(double x, double y, double angle) {
            this.x = x; this.y = y; this.angle = angle;
        }
    }
    
    // Datos de asteroide: posición, velocidad y tamaño
    public static class AsteroidData {
        public final double x, y, velocityX, velocityY;
        public final int size;
        public AsteroidData(double x, double y, double velocityX, double velocityY, int size) {
            this.x = x; this.y = y; this.velocityX = velocityX; this.velocityY = velocityY; this.size = size;
        }
    }
    
    // Datos de bala: solo posición
    public static class BulletData {
        public final double x, y;
        public BulletData(double x, double y) {
            this.x = x; this.y = y;
        }
    }
    
    // Variables de comunicación MVC
    private GameDataProvider gameDataProvider;
    private GameInputListener gameInputListener;
    
    // Sistema de estrellas de fondo
    private static final int STAR_COUNT = 120;
    private final int[] starX = new int[STAR_COUNT];
    private final int[] starY = new int[STAR_COUNT];
    private final int[] starSize = new int[STAR_COUNT];

    /**
     * Initialize star positions for consistent starfield background.
     * Uses fixed seed for consistent star placement across game sessions.
     */
    private void initStars(int windowWidth, int windowHeight) {
        java.util.Random rand = new java.util.Random(42); // fixed seed for consistency
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = rand.nextInt(windowWidth);
            starY[i] = rand.nextInt(windowHeight);
            starSize[i] = 1 + rand.nextInt(2); // 1 or 2 px
        }
    }
    
    // UI state management
    private boolean showStartOverlay = true;  // Controls start screen visibility
    
    // Cached fonts for better performance
    private static final Font SCORE_FONT = new Font("Arial", Font.BOLD, 22);

    /**
     * Draw current score in top-left corner.
     * Part of the HUD (Heads-Up Display) system.
     */
    private void drawScore(Graphics g) {
        if (gameDataProvider == null) return;
        Graphics2D g2 = (Graphics2D) g;
        
        // Enable anti-aliasing for smoother text
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        g2.setColor(Color.WHITE);
        g2.setFont(SCORE_FONT);
        g2.drawString("Score: " + gameDataProvider.getScore(), 10, 30);
    }

    // Draw heart icons for lives
    private void drawHearts(Graphics g) {
        if (gameDataProvider == null) return;
        int heartY = 50;
        int heartX = 10;
        int heartSize = 24;
        int totalHearts = gameDataProvider.getInitialLives();
        int currentLives = gameDataProvider.getLives();
        for (int i = 0; i < totalHearts; i++) {
            Image heartImg = (i < currentLives)
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
    // Control de entrada - evita repetición de teclas
    private final Set<Integer> pressedKeys = new HashSet<>();

    // Procesa teclas presionadas y envía comandos al Controller
    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
        int code = e.getKeyCode();
        if (!pressedKeys.add(code)) return; // Evitar repeticiones
        
        if (gameInputListener != null) {
            // Allow restart with 'R' if game over
            if (gameDataProvider != null && gameDataProvider.isGameOver() && code == java.awt.event.KeyEvent.VK_R) {
                gameInputListener.onRestart();
                return;
            }
            
            // Don't process game controls if paused
            if (gameDataProvider != null && gameDataProvider.isPaused()) return;
            
            // Process ship controls
            switch (code) {
                case java.awt.event.KeyEvent.VK_A:
                    gameInputListener.onMoveLeft(true);
                    break;
                case java.awt.event.KeyEvent.VK_D:
                    gameInputListener.onMoveRight(true);
                    break;
                case java.awt.event.KeyEvent.VK_W:
                    gameInputListener.onThrust(true);
                    break;
                case java.awt.event.KeyEvent.VK_S:
                    gameInputListener.onDecelerate(true);
                    break;
                case java.awt.event.KeyEvent.VK_SPACE:
                    gameInputListener.onShoot(true);
                    break;
            }
        }
    }

    /**
     * Handle key release events.
     * Sends corresponding release commands to maintain proper input state.
     * Important for continuous actions like movement and shooting.
     */
    @Override
    public void keyReleased(java.awt.event.KeyEvent e) {
        int code = e.getKeyCode();
        pressedKeys.remove(code);
        
        if (gameInputListener != null) {
            // Process ship controls release
            switch (code) {
                case java.awt.event.KeyEvent.VK_A:
                    gameInputListener.onMoveLeft(false);
                    break;
                case java.awt.event.KeyEvent.VK_D:
                    gameInputListener.onMoveRight(false);
                    break;
                case java.awt.event.KeyEvent.VK_W:
                    gameInputListener.onThrust(false);
                    break;
                case java.awt.event.KeyEvent.VK_S:
                    gameInputListener.onDecelerate(false);
                    break;
                case java.awt.event.KeyEvent.VK_SPACE:
                    gameInputListener.onShoot(false);
                    break;
            }
        }
    }

    @Override
    public void keyTyped(java.awt.event.KeyEvent e) {
        // Not used
    }

    // Set up panel, input, and mouse handling
    public void setGameDataProvider(GameDataProvider provider) {
        this.gameDataProvider = provider;
    }
    
    public void setGameInputListener(GameInputListener listener) {
        this.gameInputListener = listener;
    }

    // Constructor: configura panel, input y eventos
    public GamePanel() {
        setBackground(Color.BLACK);  // Space-like black background
        setFocusable(true);          // Required for keyboard input
        addKeyListener(this);        // Register for key events
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
                        if (gameInputListener != null) gameInputListener.onStartGame();
                        repaint();
                        return;
                    }
                }
                // Handle pause menu clicks
                if (gameDataProvider != null && gameDataProvider.isPaused()) {
                    handlePauseMenuClick(e.getX(), e.getY());
                    return;
                }
            }
        });
    }
    
    // Debe ser llamado por el controller después de configurar providers
    public void initialize(int windowWidth, int windowHeight) {
        setPreferredSize(new Dimension(windowWidth, windowHeight));
        initStars(windowWidth, windowHeight);
    }

    // Método principal de renderizado - dibuja todo el juego
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameDataProvider == null) return;
        
        // Draw starfield background
        g.setColor(Color.WHITE);
        for (int i = 0; i < STAR_COUNT; i++) {
            g.fillRect(starX[i], starY[i], starSize[i], starSize[i]);
        }
        
        // Draw HUD first (less prone to flickering)
        drawScore(g);
        drawHearts(g);
        
        // Flicker effect for invincibility
        if (!gameDataProvider.isInvincible() || ((System.currentTimeMillis() / 100) % 2 == 0)) {
            drawShip(g, gameDataProvider.getShipData());
        }
        drawAsteroids(g, gameDataProvider.getAsteroidData());
        drawBullets(g, gameDataProvider.getBulletData());
        if (gameDataProvider.isGameOver()) drawGameOverOverlay(g);
        if (showStartOverlay) drawStartOverlay(g);
        
        // Draw pause menu if needed
        if (gameDataProvider.isPaused()) {
            drawPauseMenu(g);
        }
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
        String instr = "Use WASD to move. Space to shoot.";
        int instrWidth = g2.getFontMetrics().stringWidth(instr);
        g2.drawString(instr, (getWidth() - instrWidth) / 2, buttonY + 60);
    }

    // Game over and restart message Y positions
    private static final int GAME_OVER_Y = 220;
    private static final int RESTART_Y = 270;

    // Draw the game over message
    private void drawGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String msg = "GAME OVER";
        int msgWidth = g.getFontMetrics().stringWidth(msg);
        g.drawString(msg, (getWidth() - msgWidth) / 2, GAME_OVER_Y);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String restartMsg = "Press R to restart";
        int restartWidth = g.getFontMetrics().stringWidth(restartMsg);
        g.drawString(restartMsg, (getWidth() - restartWidth) / 2, RESTART_Y);
    }

    // Dibuja nave con rotación (puede parpadear si es invencible)
    private void drawShip(Graphics g, ShipData ship) {
        if (ship == null) return;
        Graphics2D g2 = (Graphics2D) g;
        int iconW = 32, iconH = 32;
        g2.translate(ship.x, ship.y);
        g2.rotate(ship.angle);
        g2.drawImage(SHIP_ICON, -iconW/2, -iconH/2, iconW, iconH, null);
        g2.rotate(-ship.angle);
        g2.translate(-ship.x, -ship.y);
    }

    // Dibuja asteroides con rotación según dirección de movimiento
    private void drawAsteroids(Graphics g, List<AsteroidData> asteroids) {
        if (asteroids == null) return;
        Graphics2D g2 = (Graphics2D) g;
        for (AsteroidData a : asteroids) {
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

    // Dibuja balas como círculos cian
    private void drawBullets(Graphics g, List<BulletData> bullets) {
        if (bullets == null) return;
        g.setColor(Color.CYAN);
        for (BulletData b : bullets) {
            g.fillOval((int) b.x - 2, (int) b.y - 2, 4, 4);
        }
    }
    
    // Maneja clics en menú de pausa
    private void handlePauseMenuClick(int x, int y) {
        if (gameInputListener == null) return;
        
        int centerX = getWidth() / 2;
        int buttonY = 320;
        
        // Resume button
        if (x >= centerX - 100 && x <= centerX + 100 && y >= buttonY - 30 && y <= buttonY + 10) {
            gameInputListener.onResume();
            return;
        }
        buttonY += 50;
        
        // Restart button
        if (x >= centerX - 100 && x <= centerX + 100 && y >= buttonY - 30 && y <= buttonY + 10) {
            gameInputListener.onRestart();
            return;
        }
        buttonY += 50;
        
        // Settings button
        if (x >= centerX - 100 && x <= centerX + 100 && y >= buttonY - 30 && y <= buttonY + 10) {
            gameInputListener.onSettings();
            return;
        }
        buttonY += 50;
        
        // Exit button
        if (x >= centerX - 100 && x <= centerX + 100 && y >= buttonY - 30 && y <= buttonY + 10) {
            gameInputListener.onExit();
        }
    }

    /**
     * Draw pause menu overlay with translucent background.
     * Creates a modal-like interface over the game without stopping the timer.
     * Shows current score, high score, and action buttons.
     */
    private void drawPauseMenu(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        // Draw translucent overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw pause title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        String pauseMsg = "PAUSED";
        int pauseWidth = g2.getFontMetrics().stringWidth(pauseMsg);
        g2.drawString(pauseMsg, (getWidth() - pauseWidth) / 2, 200);
        
        // Draw current score
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        String scoreMsg = "Score: " + gameDataProvider.getScore();
        int scoreWidth = g2.getFontMetrics().stringWidth(scoreMsg);
        g2.drawString(scoreMsg, (getWidth() - scoreWidth) / 2, 250);
        
        // Draw menu buttons
        drawPauseButton(g2, "Resume", 320);
        drawPauseButton(g2, "Restart", 370);
        drawPauseButton(g2, "Settings", 420);
        drawPauseButton(g2, "Exit", 470);
        
        // Draw high score at the bottom
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        String highScoreMsg = "Highscore: " + gameDataProvider.getHighScore();
        int highScoreWidth = g2.getFontMetrics().stringWidth(highScoreMsg);
        g2.setColor(Color.WHITE);
        g2.drawString(highScoreMsg, (getWidth() - highScoreWidth) / 2, getHeight() - 40);
    }
    
    /**
     * Draw a pause menu button with background and text.
     * Helper method to maintain consistent button appearance.
     * @param g2 Graphics context for drawing
     * @param text Button text to display
     * @param y Vertical position of the button
     */
    private void drawPauseButton(Graphics2D g2, String text, int y) {
        int textWidth = g2.getFontMetrics().stringWidth(text);
        
        // Draw button background
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRoundRect((getWidth() - 200) / 2, y - 30, 200, 40, 20, 20);
        
        // Draw button text
        g2.setColor(Color.BLACK);
        g2.drawString(text, (getWidth() - textWidth) / 2, y);
    }
}
