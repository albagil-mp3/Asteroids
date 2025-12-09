package Model;

/**
 * ENTIDAD ASTEROIDE - OBSTÁCULO MÓVIL DEL JUEGO
 * ==============================================
 */
public class Asteroid extends Thread {
    public double x, y;
    public double velocityX, velocityY;
    public int size;
    private volatile boolean running = true;
    private volatile boolean paused = false;

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public Asteroid(double x, double y, double velocityX, double velocityY, int size) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.size = size;
    }

    public void update() {
        x += velocityX;
        y += velocityY;
        if (x < 0) x += Model.GameState.Config.WINDOW_WIDTH;
        if (x > Model.GameState.Config.WINDOW_WIDTH) x -= Model.GameState.Config.WINDOW_WIDTH;
        if (y < 0) y += Model.GameState.Config.WINDOW_HEIGHT;
        if (y > Model.GameState.Config.WINDOW_HEIGHT) y -= Model.GameState.Config.WINDOW_HEIGHT;
    }

    public void stopAsteroid() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            if (!paused) update();
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
