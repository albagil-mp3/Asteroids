package Controller;

import Model.GameState;
import javax.swing.*;
import java.awt.event.*;

import View.GamePanel;
import View.ControlPanel;
import java.awt.*;

/**
 * GameController manages the main game loop, UI, and user interactions for the Asteroids game.
 * It wires together the Model (GameState), View (GamePanel, ControlPanel), and handles events.
 */
public class GameController implements ActionListener {
    // Remove PlayScreenPanel, use overlay in GamePanel
    // Core game state (model)
    private GameState gameState;
    // Main game display (view)
    private GamePanel panel;
    // Control panel for asteroid settings (view)
    private ControlPanel controlPanel;
    // Main application window
    private JFrame frame;
    // Game loop timer (~60 FPS)
    public Timer timer;
    // Callback to trigger repaint
    private Runnable repaintCallback;
    // Pause button in the UI
    private JButton pauseButton;

    // Initial asteroid settings (defaults)
    private static final int INIT_MIN_ASTEROID_SIZE = 20;
    private static final int INIT_MAX_ASTEROID_SIZE = 60;
    private static final double INIT_MIN_ASTEROID_SPEED = 0.5;
    private static final double INIT_MAX_ASTEROID_SPEED = 2.5;
    // Color for pause button (semi-transparent white)
    private static final java.awt.Color PAUSE_BUTTON_COLOR = new java.awt.Color(255,255,255,200);

    /**
     * Constructs the game controller, sets up the game window, panels, and event handlers.
     */
    public GameController() {
        // Initialize model and views
        gameState = new GameState();
        panel = new GamePanel(gameState);
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
        pauseButton.setBounds(Model.GameState.Config.WINDOW_WIDTH - btnWidth - 10, 10, btnWidth, btnHeight);
        panel.add(pauseButton);

        // Use a container panel for the game (game panel + control panel)
        JPanel gameContainer = new JPanel(new BorderLayout());
        gameContainer.add(panel, BorderLayout.CENTER);
        gameContainer.add(controlPanel, BorderLayout.SOUTH);


        // Use a container panel for the game (game panel + control panel)
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

        // Pass a callback to GamePanel to start the game after clicking START overlay
        panel.setGameStartCallback(() -> {
            timer.start();
            gameState.startAsteroidSpawner();
            panel.requestFocusInWindow();
        });

        // Hide pause button when paused
        gameState.paused = false;
        panel.addPropertyChangeListener(evt -> {
            if ("paused".equals(evt.getPropertyName())) {
                boolean paused = (boolean) evt.getNewValue();
                pauseButton.setVisible(!paused);
            }
        });

        // Pause button event: pause the game
        pauseButton.addActionListener(e -> {
            timer.stop();
            gameState.setPaused(true);
            pauseButton.setVisible(false);
            panel.repaint();
        });

        // Set up pause menu actions (resume, restart, settings)
        panel.setPauseMenuListener(new View.GamePanel.PauseMenuListener() {
            @Override
            public void onResume() {
                // Resume the game from pause
                controlPanel.setVisible(false);
                gameState.setPaused(false);
                pauseButton.setVisible(true);
                timer.start();
                panel.repaint();
            }
            @Override
            public void onRestart() {
                // Reset game state to initial values
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
                // Show the asteroid settings control panel
                controlPanel.setVisible(true);
                panel.requestFocusInWindow();
            }
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
