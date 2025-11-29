package Model;

// Represents a bullet fired by the ship
public class Bullet {
    // Current position
    public double x, y;
    // Current velocity
    public double velocityX, velocityY;
    // True if bullet is still active (on screen)
    public boolean active = true;

    // Create a new bullet at (x, y) with given velocity
    public Bullet(double x, double y, double velocityX, double velocityY) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    // Update bullet position; deactivate if out of bounds
    public void update() {
        x += velocityX;
        y += velocityY;
        if (x < 0 || x > 800 || y < 0 || y > 600) {
            active = false;
        }
    }
}
