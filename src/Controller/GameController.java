package Controller;

import Model.GameState;
import javax.swing.*;
import java.awt.event.*;

import View.GamePanel;
import View.ControlPanel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PATRÓN MVC - CONTROLADOR PRINCIPAL DEL JUEGO
 * ============================================
 */
public class GameController implements ActionListener, GamePanel.GameDataProvider, GamePanel.GameInputListener {
    // ========================================================================
    // COMPONENTES DEL PATRÓN MVC
    // ========================================================================
    
    // MODELO - Contiene toda la lógica y estado del juego
    private GameState gameState;
    
    // VISTAS - Componentes de interfaz de usuario
    private GamePanel panel;          // Pantalla principal del juego
    private ControlPanel controlPanel; // Panel de configuración de asteroides
    
    // INFRAESTRUCTURA DE UI
    private JFrame frame;              // Ventana principal de la aplicación
    private JButton pauseButton;       // Botón flotante de pausa
    
    // SISTEMA DE GAME LOOP
    public Timer timer;                // Timer para 60 FPS (~16ms por frame)
    private Runnable repaintCallback;  // Callback para redibujar la pantalla

    // ========================================================================
    // CONFIGURACIÓN INICIAL DEL JUEGO
    // ========================================================================
    
    // VALORES POR DEFECTO PARA ASTEROIDES
    // Estos valores se pueden cambiar durante el juego a través del ControlPanel
    private static final int INIT_MIN_ASTEROID_SIZE = 20;      // Tamaño mínimo de asteroide
    private static final int INIT_MAX_ASTEROID_SIZE = 60;      // Tamaño máximo de asteroide
    private static final double INIT_MIN_ASTEROID_SPEED = 0.5; // Velocidad mínima
    private static final double INIT_MAX_ASTEROID_SPEED = 2.5; // Velocidad máxima
    
    // ESTILO VISUAL
    // Color semi-transparente para el botón de pausa flotante
    private static final java.awt.Color PAUSE_BUTTON_COLOR = new java.awt.Color(255,255,255,200);

    /**
     * Constructs the game controller, sets up the game window, panels, and event handlers.
     */
    public GameController() {
        // Initialize model and views
        gameState = new GameState();
        panel = new GamePanel();
        panel.setGameDataProvider(this);
        panel.setGameInputListener(this);
        panel.initialize(gameState.getWindowWidth(), gameState.getWindowHeight());
        
        panel.setLayout(null); // Set absolute layout before adding components
        controlPanel = new ControlPanel(INIT_MIN_ASTEROID_SIZE, INIT_MAX_ASTEROID_SIZE, INIT_MIN_ASTEROID_SPEED, INIT_MAX_ASTEROID_SPEED);
        controlPanel.setVisible(false);

        // Set up main window
        frame = new JFrame("Asteroids MVC");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        // Add Pause button as a floating transparent button in the top right corner
        pauseButton = new JButton("||");
        pauseButton.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
        pauseButton.setOpaque(false);
        pauseButton.setContentAreaFilled(false);
        pauseButton.setBorderPainted(false);
        pauseButton.setFocusPainted(false);
        pauseButton.setForeground(PAUSE_BUTTON_COLOR);
        pauseButton.setFocusable(false);
        // Place in top right, accounting for button size and panel width
        int btnWidth = 60, btnHeight = 40;
        pauseButton.setBounds(gameState.getWindowWidth() - btnWidth - 10, 10, btnWidth, btnHeight);
        panel.add(pauseButton);

        // Use a container panel for the game (game panel + control panel)
        JPanel gameContainer = new JPanel(new BorderLayout());
        gameContainer.add(panel, BorderLayout.CENTER);
        gameContainer.add(controlPanel, BorderLayout.SOUTH);

        frame.setContentPane(gameContainer);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Set up repaint callback and game loop timer
        repaintCallback = panel::repaint;
        timer = new Timer(16, this); // ~60 FPS
        panel.setFocusable(true);
        panel.requestFocusInWindow();

        // Hide pause button when paused
        gameState.paused = false;

        // Pause button event: pause the game
        pauseButton.addActionListener(e -> {
            timer.stop();
            gameState.setPaused(true);
            pauseButton.setVisible(false);
            panel.repaint();
        });

        // Apply new asteroid settings from the control panel
        controlPanel.applyButton.addActionListener(e -> {
            int minSize = controlPanel.getMinSize();
            int maxSize = controlPanel.getMaxSize();
            double minSpeed = controlPanel.getMinSpeed();
            double maxSpeed = controlPanel.getMaxSpeed();
            // Ensure min <= max
            if (minSize > maxSize) {
                int tmp = minSize; minSize = maxSize; maxSize = tmp;
            }
            if (minSpeed > maxSpeed) {
                double tmp = minSpeed; minSpeed = maxSpeed; maxSpeed = tmp;
            }
            gameState.setAsteroidSizeRange(minSize, maxSize);
            gameState.setAsteroidSpeedRange(minSpeed, maxSpeed);
            gameState.spawnAsteroids();
            panel.repaint();
            panel.requestFocusInWindow();
            controlPanel.setVisible(false);
        });
    }
    
    // Implementación de GameDataProvider
    @Override
    public int getScore() { return gameState.score; }
    
    @Override
    public int getLives() { return gameState.lives; }
    
    @Override
    public boolean isGameOver() { return gameState.gameOver; }
    
    @Override
    public boolean isPaused() { return gameState.paused; }
    
    @Override
    public boolean isInvincible() { return gameState.invincible; }
    
    @Override
    public int getHighScore() { return gameState.getHighScore(); }
    
    @Override
    public GamePanel.ShipData getShipData() {
        return new GamePanel.ShipData(gameState.ship.x, gameState.ship.y, gameState.ship.angle);
    }
    
    @Override
    public List<GamePanel.AsteroidData> getAsteroidData() {
        return gameState.asteroids.stream()
            .map(a -> new GamePanel.AsteroidData(a.x, a.y, a.velocityX, a.velocityY, a.size))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<GamePanel.BulletData> getBulletData() {
        return gameState.bullets.stream()
            .filter(b -> b.active)
            .map(b -> new GamePanel.BulletData(b.x, b.y))
            .collect(Collectors.toList());
    }
    
    @Override
    public int getWindowWidth() { return Model.GameState.Config.WINDOW_WIDTH; }
    
    @Override
    public int getWindowHeight() { return Model.GameState.Config.WINDOW_HEIGHT; }
    
    @Override
    public int getInitialLives() { return Model.GameState.Config.INITIAL_LIVES; }
    
    // Implementación de GameInputListener
    @Override
    public void onMoveLeft(boolean pressed) {
        gameState.left = pressed;
    }
    
    @Override
    public void onMoveRight(boolean pressed) {
        gameState.right = pressed;
    }
    
    @Override
    public void onThrust(boolean pressed) {
        gameState.up = pressed;
    }
    
    @Override
    public void onDecelerate(boolean pressed) {
        gameState.ship.setDecelerating(pressed);
    }
    
    @Override
    public void onShoot(boolean pressed) {
        gameState.shooting = pressed;
    }
    
    @Override
    public void onStartGame() {
        timer.start();
        gameState.startAsteroidSpawner();
        panel.requestFocusInWindow();
    }
    
    @Override
    public void onResume() {
        controlPanel.setVisible(false);
        gameState.setPaused(false);
        pauseButton.setVisible(true);
        timer.start();
        panel.repaint();
    }
    
    @Override
    public void onRestart() {
        controlPanel.setVisible(false);
        gameState.ship.x = Model.GameState.Config.SHIP_START_X;
        gameState.ship.y = Model.GameState.Config.SHIP_START_Y;
        gameState.ship.velocityX = 0;
        gameState.ship.velocityY = 0;
        gameState.lives = Model.GameState.Config.INITIAL_LIVES;
        gameState.score = 0;
        gameState.gameOver = false;
        gameState.invincible = false;
        gameState.bullets.clear();
        gameState.spawnAsteroids();
        gameState.startAsteroidSpawner();
        gameState.setPaused(false);
        pauseButton.setVisible(true);
        timer.start();
        panel.repaint();
    }
    
    @Override
    public void onSettings() {
        controlPanel.setVisible(true);
        panel.requestFocusInWindow();
    }
    
    @Override
    public void onExit() {
        cleanupOnExit();
        System.exit(0);
    }

    /**
     * Cleanup method to stop all asteroid threads and save the high score on exit.
     */
    public void cleanupOnExit() {
        if (gameState != null && gameState.asteroids != null) {
            for (Model.Asteroid a : gameState.asteroids) {
                a.stopAsteroid();
            }
        }
        // Save high score directly if method is public, otherwise ignore
        try {
            gameState.getClass().getMethod("saveHighScore").invoke(gameState);
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Main game loop: called by the timer every frame.
     * Handles shooting, updates game state, and repaints the view.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState.shooting) {
            gameState.shoot();
            gameState.shooting = false;
        }
        gameState.update();
        repaintCallback.run();
    }
}
