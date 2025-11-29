# Asteroids Game

A classic Asteroids game implemented in Java using Swing for the GUI. Destroy asteroids, avoid collisions, and achieve the highest score!

## Features

### Start Screen
- Upon launching, the game displays a start overlay with the title "ASTEROIDS", a big "START" button, and instructions.
- Click the "START" button to begin the game.

### Gameplay
- Control a spaceship in space with a starfield background.
- Shoot green bullets to destroy asteroids.
- Collect points for each asteroid destroyed.
- Lives are represented by heart icons; lose all lives and it's game over.

### Controls
- **WASD**: Move the ship (W: forward, A: left, D: right, S: decelerate).
- **Space**: Shoot bullets.
- **Pause Button**: Top-right floating button to pause the game.

### Pause Menu
- When paused, an overlay appears with options:
  - **Resume**: Continue the game.
  - **Restart**: Reset the game state and start over.
  - **Settings**: Adjust asteroid size and speed ranges.

### Game Over
- When all lives are lost, a "GAME OVER" screen appears with a blurry background.
- Press **R** to restart the game.

### High Score
- The game tracks and displays the high score.
- High scores are saved to a file.

### Visuals
- Starfield background with white dots.
- Icons for ship, asteroids, and hearts.
- Overlays for start, pause, and game over screens.

## Folder Structure

- `src/`: Source code
  - `App.java`: Main entry point
  - `Controller/GameController.java`: Manages game loop and UI
  - `Model/`: Game state and entities (Ship, Bullet, Asteroid, GameState)
  - `View/`: UI components (GamePanel, ControlPanel)
- `lib/`: Dependencies (if any)
- `bin/`: Compiled classes
- `resources/`: Icons and high score file

## File Descriptions

- `README.md`: This documentation file.
- `src/App.java`: The main class that initializes and starts the game by creating the GameController.
- `src/Controller/GameController.java`: Manages the overall game flow, including the game loop timer, UI panel switching, pause/restart logic, and event handling for the pause button and settings.
- `src/Model/Asteroid.java`: Defines the Asteroid class, handling asteroid movement, rotation, and collision detection.
- `src/Model/Bullet.java`: Defines the Bullet class for projectile entities, including position and velocity.
- `src/Model/GameState.java`: Manages the game's state, including score, lives, ship, bullets, asteroids, pause status, and high score persistence.
- `src/Model/Ship.java`: Defines the Ship class, controlling player movement, shooting, and invincibility.
- `src/View/ControlPanel.java`: Provides a settings panel for adjusting asteroid size and speed ranges.
- `src/View/GamePanel.java`: The main game panel for rendering graphics, handling keyboard/mouse input, and displaying overlays (start, pause, game over).
- `resources/highscore.txt`: File storing the high score.
- `resources/icons/`: Directory containing image icons for ship, asteroid, and heart.

## Dependencies

- Java Swing for GUI
- No external libraries required
