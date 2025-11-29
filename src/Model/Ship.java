package Model;

public class Ship {
    public double x, y, angle;
    public double velocityX, velocityY;
    public boolean accelerating;
    private boolean decelerating = false;
    public Ship(double x, double y) {
        this.x = x;
        this.y = y;
        this.angle = 0;
        this.velocityX = 0;
        this.velocityY = 0;
        this.accelerating = false;
    }

    public void setDecelerating(boolean decelerating) {
        this.decelerating = decelerating;
    }
    public void update() {
        if (accelerating) {
            // Move in the direction of the ship's "front" (top of the image)
            double forwardAngle = angle - Math.PI/2;
            velocityX += Math.cos(forwardAngle) * 0.1;
            velocityY += Math.sin(forwardAngle) * 0.1;
        }
        // Smooth deceleration if requested
        if (decelerating) {
            double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            if (speed > 0.05) {
                velocityX *= 0.96;
                velocityY *= 0.96;
            } else {
                velocityX = 0;
                velocityY = 0;
            }
        }
        x += velocityX;
        y += velocityY;
        // Screen wrap
        if (x < 0) x += Model.GameState.Config.WINDOW_WIDTH;
        if (x > Model.GameState.Config.WINDOW_WIDTH) x -= Model.GameState.Config.WINDOW_WIDTH;
        if (y < 0) y += Model.GameState.Config.WINDOW_HEIGHT;
        if (y > Model.GameState.Config.WINDOW_HEIGHT) y -= Model.GameState.Config.WINDOW_HEIGHT;
        velocityX *= 0.99;
        velocityY *= 0.99;
    }
}
