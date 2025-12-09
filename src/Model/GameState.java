package Model;
import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * PATRÓN MVC - MODELO DEL JUEGO
 * =============================
 */
public class GameState {
    // --- Helper methods for collision and respawn logic ---
    // Handle bullet-asteroid collisions, scoring, and asteroid splitting
    private void handleBulletAsteroidCollisions() {
            java.util.List<Asteroid> newAsteroids = new java.util.ArrayList<>();
            for (Bullet b : bullets) {
                if (!b.active) continue;
                for (Asteroid a : asteroids) {
                    double dx = b.x - a.x;
                    double dy = b.y - a.y;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < a.size) {
                        b.active = false;
                        a.stopAsteroid();
                        score += 100;
                        if (score > highScore) {
                            highScore = score;
                            saveHighScore();
                        }
                        // If asteroid is big enough, split into two smaller
                        if (a.size > Config.ASTEROID_SPLIT_THRESHOLD) {
                            int newSize = a.size / 2;
                            double baseAngle = Math.random() * 2 * Math.PI;
                            double speed = Math.sqrt(a.velocityX * a.velocityX + a.velocityY * a.velocityY);
                            double offset = newSize + 2;
                            double angle1 = baseAngle;
                            double angle2 = baseAngle + Math.PI;
                            double x1 = a.x + Math.cos(angle1) * offset;
                            double y1 = a.y + Math.sin(angle1) * offset;
                            double x2 = a.x + Math.cos(angle2) * offset;
                            double y2 = a.y + Math.sin(angle2) * offset;
                            Asteroid a1 = new Asteroid(x1, y1, Math.cos(angle1) * speed, Math.sin(angle1) * speed, newSize);
                            Asteroid a2 = new Asteroid(x2, y2, Math.cos(angle2) * speed, Math.sin(angle2) * speed, newSize);
                            a1.start();
                            a2.start();
                            newAsteroids.add(a1);
                            newAsteroids.add(a2);
                        }
                        a.size = -1; // Mark for removal
                        break;
                    }
                }
            }
            asteroids.removeIf(a -> a.size == -1);
            asteroids.addAll(newAsteroids);
        }

    // Handle ship-asteroid collision, respawn, and life decrement
    private boolean handleShipAsteroidCollisionAndRespawn() {
            if (!invincible) {
                for (Asteroid a : asteroids) {
                    double dx = ship.x - a.x;
                    double dy = ship.y - a.y;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < a.size + Config.SHIP_RADIUS) {
                        lives--;
                        if (lives <= 0) {
                            gameOver = true;
                        }
                        respawnShip();
                        return true;
                    }
                }
            }
            return false;
        }

    // Reset ship to center and make invincible
    private void respawnShip() {
            ship.x = Config.SHIP_START_X;
            ship.y = Config.SHIP_START_Y;
            ship.velocityX = 0;
            ship.velocityY = 0;
            setInvincible();
        }

    // Set ship to invincible for a duration
    private void setInvincible() {
            invincible = true;
            invincibleEndTime = System.currentTimeMillis() + Config.INVINCIBILITY_DURATION_MS;
        }
    // Game configuration constants
    public static class Config {
        public static final int WINDOW_WIDTH = 800;
        public static final int WINDOW_HEIGHT = 600;
        public static final int INITIAL_LIVES = 3;
        public static final int SHIP_START_X = WINDOW_WIDTH / 2;
        public static final int SHIP_START_Y = WINDOW_HEIGHT / 2;
        public static final int SHIP_RADIUS = 10;
        public static final int ASTEROID_COUNT = 5;
        public static final int ASTEROID_MIN_OVERLAP_DIST = 10;
        public static final int ASTEROID_SPLIT_THRESHOLD = 25;
        public static final double SHIP_TURN_SPEED = 0.07;
        public static final int INVINCIBILITY_DURATION_MS = 2000;
    }

    // High score and file path
    private int highScore = 0;
    private static final java.nio.file.Path HIGH_SCORE_FILE = java.nio.file.Paths.get("resources", "highscore.txt");

    // Game objects and state
    public Ship ship;
    public List<Asteroid> asteroids = new ArrayList<>();
    public List<Bullet> bullets = new ArrayList<>();
    public boolean left, right, up, shooting;
    public int lives = Config.INITIAL_LIVES;
    public boolean gameOver = false;
    public boolean invincible = false;
    public boolean paused = false;
    public int score = 0;
    private long invincibleEndTime = 0;
    private final Random random = new Random();

    // Asteroid spawn parameters
    private int minAsteroidSize = 20;
    private int maxAsteroidSize = 60;
    private double minAsteroidSpeed = 0.5;
    private double maxAsteroidSpeed = 2.5;

    // Pause or resume all asteroids
    public void setPaused(boolean paused) {
        this.paused = paused;
        for (Asteroid a : asteroids) {
            a.setPaused(paused);
        }
    }

    // Initialize game state
    public GameState() {
        ship = new Ship(Config.SHIP_START_X, Config.SHIP_START_Y);
        spawnAsteroids();
        loadHighScore();
    }

    // Set asteroid size range
    public void setAsteroidSizeRange(int min, int max) {
        this.minAsteroidSize = min;
        this.maxAsteroidSize = max;
    }

    // Set asteroid speed range
    public void setAsteroidSpeedRange(double min, double max) {
        this.minAsteroidSpeed = min;
        this.maxAsteroidSpeed = max;
    }

    // Spawn initial asteroids
    public void spawnAsteroids() {
        for (Asteroid a : asteroids) a.stopAsteroid();
        asteroids.clear();
        int attempts;
        for (int i = 0; i < Config.ASTEROID_COUNT; i++) {
            int size = minAsteroidSize + random.nextInt(maxAsteroidSize - minAsteroidSize + 1);
            double speed = minAsteroidSpeed + random.nextDouble() * (maxAsteroidSpeed - minAsteroidSpeed);
            double angle = random.nextDouble() * 2 * Math.PI;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            double x, y;
            boolean overlap;
            attempts = 0;
            do {
                x = random.nextInt(Config.WINDOW_WIDTH);
                y = random.nextInt(Config.WINDOW_HEIGHT);
                overlap = false;
                for (Asteroid other : asteroids) {
                    double dx = x - other.x;
                    double dy = y - other.y;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < size + other.size + Config.ASTEROID_MIN_OVERLAP_DIST) {
                        overlap = true;
                        break;
                    }
                }
                attempts++;
            } while (overlap && attempts < 100);
            Asteroid asteroid = new Asteroid(x, y, vx, vy, size);
            asteroid.start();
            asteroids.add(asteroid);
        }
    }

    // Continuously spawn asteroids if needed
    private Thread asteroidSpawnerThread = null;
    public void startAsteroidSpawner() {
        if (asteroidSpawnerThread != null && asteroidSpawnerThread.isAlive()) return;
        asteroidSpawnerThread = new Thread(() -> {
            while (!gameOver) {
                if (asteroids.size() < Config.ASTEROID_COUNT) {
                    int size = minAsteroidSize + random.nextInt(maxAsteroidSize - minAsteroidSize + 1);
                    double speed = minAsteroidSpeed + random.nextDouble() * (maxAsteroidSpeed - minAsteroidSpeed);
                    double angle = random.nextDouble() * 2 * Math.PI;
                    double vx = Math.cos(angle) * speed;
                    double vy = Math.sin(angle) * speed;
                    double x, y;
                    boolean overlap;
                    int attempts = 0;
                    do {
                        x = random.nextInt(Config.WINDOW_WIDTH);
                        y = random.nextInt(Config.WINDOW_HEIGHT);
                        overlap = false;
                        for (Asteroid other : asteroids) {
                            double dx = x - other.x;
                            double dy = y - other.y;
                            double dist = Math.sqrt(dx * dx + dy * dy);
                            if (dist < size + other.size + Config.ASTEROID_MIN_OVERLAP_DIST) {
                                overlap = true;
                                break;
                            }
                        }
                        attempts++;
                    } while (overlap && attempts < 100);
                    Asteroid asteroid = new Asteroid(x, y, vx, vy, size);
                    asteroid.start();
                    synchronized (asteroids) {
                        asteroids.add(asteroid);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        asteroidSpawnerThread.start();
    }

    // Main game update: ship, bullets, collisions, invincibility
    public void update() {
        if (gameOver) return;
        if (left) ship.angle -= Config.SHIP_TURN_SPEED;
        if (right) ship.angle += Config.SHIP_TURN_SPEED;
        ship.accelerating = up;
        ship.update();
        for (Bullet b : bullets) b.update();
        bullets.removeIf(b -> !b.active);
        handleBulletAsteroidCollisions();
        if (invincible && System.currentTimeMillis() > invincibleEndTime) invincible = false;
        handleShipAsteroidCollisionAndRespawn();
    }

    // Get current high score
    public int getHighScore() {
        return highScore;
    }
    
    // Get window dimensions for MVC communication
    public int getWindowWidth() {
        return Config.WINDOW_WIDTH;
    }
    
    public int getWindowHeight() {
        return Config.WINDOW_HEIGHT;
    }

    // Load high score from file
    private void loadHighScore() {
        try (BufferedReader reader = java.nio.file.Files.newBufferedReader(HIGH_SCORE_FILE)) {
            String line = reader.readLine();
            if (line != null) highScore = Integer.parseInt(line.trim());
        } catch (Exception e) {
            highScore = 0;
        }
    }

    // Save high score to file
    private void saveHighScore() {
        try (BufferedWriter writer = java.nio.file.Files.newBufferedWriter(HIGH_SCORE_FILE)) {
            writer.write(Integer.toString(highScore));
        } catch (Exception e) {
            // ignore
        }
    }

    // Fire a bullet from the ship's tip
    public void shoot() {
        double iconHalf = 16;
        double bulletAngle = ship.angle - Math.PI/2;
        double tipX = ship.x + Math.cos(bulletAngle) * iconHalf;
        double tipY = ship.y + Math.sin(bulletAngle) * iconHalf;
        double bvx = Math.cos(bulletAngle) * 5 + ship.velocityX;
        double bvy = Math.sin(bulletAngle) * 5 + ship.velocityY;
        bullets.add(new Bullet(tipX, tipY, bvx, bvy));
    }
}
