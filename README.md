# Juego Asteroids

Un clásico juego de Asteroids implementado en Java usando Swing para la interfaz gráfica. ¡Destruye asteroides, evita colisiones y consigue la puntuación más alta!

## Características

### Pantalla de Inicio
- Al iniciar, el juego muestra una pantalla de inicio con el título "ASTEROIDS", un botón "START" grande e instrucciones.
- Haz clic en "START" para comenzar el juego.

### Jugabilidad
- Controla una nave espacial en el espacio con un fondo de campo de estrellas.
- Dispara balas cian para destruir asteroides.
- Recoge puntos por cada asteroide destruido.
- Las vidas están representadas por iconos de corazones; pierde todas las vidas y el juego termina.

### Controles
- **WASD**: Mueve la nave (W: adelante, A: izquierda, D: derecha, S: frenar).
- **Espacio**: Disparar balas.
- **Botón Pausa**: Botón flotante en la esquina superior derecha para pausar el juego.

### Menú de Pausa
- Cuando está pausado, aparece una pantalla con opciones:
  - **Reanudar**: Continuar el juego.
  - **Reiniciar**: Resetear el estado del juego y empezar de nuevo.
  - **Configuración**: Ajustar rangos de tamaño y velocidad de asteroides.
  - **Salir**: Cerrar el juego completamente.

### Game Over
- Cuando se pierden todas las vidas, aparece una pantalla "GAME OVER" con fondo borroso.
- Presiona **R** para reiniciar el juego.

### Puntuación Máxima
- El juego rastrea y muestra la puntuación máxima.
- Las puntuaciones máximas se guardan en un archivo.

### Elementos Visuales
- Fondo de campo de estrellas con puntos blancos.
- Iconos para la nave, asteroides y corazones.
- Pantallas superpuestas para inicio, pausa y game over.

## Arquitectura MVC (Model-View-Controller)

Este juego implementa el patrón de arquitectura MVC para separar claramente las responsabilidades del código:

### Model (Modelo) - `src/Model/`
**Responsabilidad**: Contiene toda la lógica del juego y el estado de datos.

- **`GameState.java`**: Clase central que gestiona el estado completo del juego
  - Almacena puntuación, vidas, estado de pausa, game over
  - Mantiene listas de entidades (nave, asteroides, balas)
  - Implementa la lógica de spawn de asteroides y detección de colisiones
  - Gestiona la persistencia del high score
  
- **`Ship.java`**: Representa la nave del jugador
  - Física de movimiento con aceleración e inercia
  - Sistema de rotación automática hacia la dirección de movimiento
  - Control de invencibilidad temporal tras recibir daño
  
- **`Asteroid.java`**: Define el comportamiento de los asteroides
  - Movimiento lineal con wrapping en pantalla
  - Diferentes tamaños que afectan velocidad y puntuación
  
- **`Bullet.java`**: Proyectiles disparados por la nave
  - Movimiento linear simple con tiempo de vida limitado

### View (Vista) - `src/View/`
**Responsabilidad**: Maneja toda la interfaz de usuario y renderizado.

- **`GamePanel.java`**: Panel principal del juego
  - **Renderizado**: Dibuja todos los elementos (nave, asteroides, balas, HUD)
  - **Input Handling**: Captura eventos de teclado y ratón
  - **Overlays**: Gestiona pantallas de inicio, pausa y game over
  - **Comunicación con Controller**: Usa interfaces para mantener separación MVC

- **`ControlPanel.java`**: Panel de configuración
  - Permite ajustar parámetros de asteroides (tamaño, velocidad)
  - Interfaz para modificar configuración del juego

### Controller (Controlador) - `src/Controller/`
**Responsabilidad**: Coordina la comunicación entre Model y View.

- **`GameController.java`**: Controlador principal
  - **Game Loop**: Gestiona el timer principal (60 FPS)
  - **Event Handling**: Procesa eventos de UI (botones, pausas)
  - **State Coordination**: Sincroniza cambios entre Model y View
  - **Interface Implementation**: Implementa `GameDataProvider` y `GameInputListener`

### Comunicación MVC y Sistema de Controles

#### Flujo de Input (Controles):
1. **View → Controller**: `GamePanel` detecta teclas presionadas (KeyListener)
2. **Controller → Model**: `GameController` traduce input a comandos semánticos
3. **Model**: `GameState` y `Ship` procesan los comandos y actualizan física
4. **Model → Controller → View**: Datos se transfieren vía DTOs (Data Transfer Objects) a través del Controller

#### Listener de Controles:
```java
// En GamePanel.java - Captura input
public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
        case KeyEvent.VK_W: gameInputListener.onThrust(true); break;
        case KeyEvent.VK_A: gameInputListener.onRotateLeft(true); break;
        case KeyEvent.VK_D: gameInputListener.onRotateRight(true); break;
        case KeyEvent.VK_SPACE: gameInputListener.onShoot(); break;
    }
}

// En GameController.java - Procesa comandos
public void onThrust(boolean active) {
    gameState.getShip().setThrusting(active);
}
```

#### Data Transfer Objects (DTOs):
Para mantener la separación MVC, se usan DTOs inmutables:
- **`ShipData`**: Posición, ángulo, estado de thrust
- **`AsteroidData`**: Posición, velocidad, tamaño  
- **`BulletData`**: Posición de proyectiles

#### Interfaces de Comunicación:
- **`GameDataProvider`**: El Controller provee datos del Model a la View
- **`GameInputListener`**: La View envía eventos de input al Controller

**Flujo de datos completo:**
```
Input: View → Controller → Model
Output: Model → Controller → View (vía DTOs)
```

El Controller actúa como intermediario obligatorio - la View nunca accede directamente al Model. Esto mantiene la separación MVC estricta donde cada capa tiene responsabilidades bien definidas.

## Estructura de Carpetas

- `src/`: Código fuente
  - `App.java`: Punto de entrada principal
  - `Controller/GameController.java`: Gestiona el bucle del juego y la UI
  - `Model/`: Estado del juego y entidades (Ship, Bullet, Asteroid, GameState)
  - `View/`: Componentes de UI (GamePanel, ControlPanel)
- `bin/`: Clases compiladas
- `resources/`: Iconos y archivo de puntuación máxima

## Descripción de Archivos

- `README.md`: Este archivo de documentación.
- `src/App.java`: La clase principal que inicializa e inicia el juego creando el GameController.
- `src/Controller/GameController.java`: Gestiona el flujo general del juego, incluyendo el timer del bucle del juego, cambio de paneles de UI, lógica de pausa/reinicio, y manejo de eventos para el botón de pausa y configuración.
- `src/Model/Asteroid.java`: Define la clase Asteroid, manejando movimiento, rotación y detección de colisiones de asteroides.
- `src/Model/Bullet.java`: Define la clase Bullet para entidades proyectil, incluyendo posición y velocidad.
- `src/Model/GameState.java`: Gestiona el estado del juego, incluyendo puntuación, vidas, nave, balas, asteroides, estado de pausa y persistencia de puntuación máxima.
- `src/Model/Ship.java`: Define la clase Ship, controlando movimiento del jugador, disparo e invencibilidad.
- `src/View/ControlPanel.java`: Proporciona un panel de configuración para ajustar rangos de tamaño y velocidad de asteroides.
- `src/View/GamePanel.java`: El panel principal del juego para renderizado de gráficos, manejo de input de teclado/ratón, y mostrar overlays (inicio, pausa, game over).
- `resources/highscore.txt`: Archivo que almacena la puntuación máxima.
- `resources/icons/`: Directorio que contiene iconos de imagen para nave, asteroide y corazón.

## Cómo Funciona el Programa

### Generación de Asteroides (Asteroid Spawning)
Los asteroides se generan aleatoriamente en los bordes de la pantalla usando `Math.random()`. El sistema:
- Elige un borde aleatorio (arriba, abajo, izquierda, derecha)
- Asigna una posición aleatoria en ese borde
- Les da una velocidad aleatoria dirigida hacia el centro de la pantalla
- Cada asteroide tiene tamaño y velocidad aleatorios dentro de rangos configurables

### Movimiento de la Nave (Ship Movement)
El movimiento de la nave utiliza física realista:
- **Aceleración**: Las teclas WASD aplican fuerzas de aceleración
- **Inercia**: La nave mantiene su velocidad cuando no se presionan teclas
- **Rotación**: La nave rota gradualmente hacia la dirección de movimiento
- **Límites de velocidad**: La velocidad máxima está limitada para evitar que se vuelva incontrolable
- **Wrapping**: Cuando la nave sale por un lado de la pantalla, aparece por el lado opuesto

### Detección de Colisiones (Collision Detection)
El sistema utiliza detección de colisión circular:
- **Balas vs Asteroides**: Calcula la distancia entre el centro de la bala y el centro del asteroide
- **Nave vs Asteroides**: Verifica si la distancia es menor que la suma de sus radios
- Cuando una bala impacta un asteroide, ambos se eliminan y se suma puntuación
- Cuando un asteroide toca la nave, se pierde una vida y la nave se vuelve temporalmente invencible

### Bucle Principal del Juego (Game Loop)
El juego funciona con un Timer de Swing que ejecuta 60 veces por segundo:
1. **Input Processing**: Lee las teclas presionadas y actualiza el estado de la nave
2. **Physics Update**: Actualiza posiciones de nave, balas y asteroides
3. **Collision Detection**: Verifica todas las colisiones posibles
4. **State Management**: Actualiza puntuación, vidas, y estado del juego
5. **Rendering**: Redibuja todos los elementos en pantalla

### Gestión de Hilos (Thread Management)
El juego implementa un sistema de hilos para manejar múltiples aspectos concurrentemente:

#### Hilos Principales:
1. **Event Dispatch Thread (EDT)**: 
   - Hilo principal de Swing para la interfaz de usuario
   - Maneja eventos de ratón, teclado y renderizado
   - Ejecuta `paintComponent()` y actualiza la UI

2. **Game Loop Timer Thread**:
   - Timer de Swing (`javax.swing.Timer`) que ejecuta a 60 FPS
   - Llama a `GameController.actionPerformed()` cada 16ms
   - Actualiza la lógica del juego y repinta la pantalla

3. **Asteroid Threads**:
   - Cada asteroide (`Asteroid.java`) extiende `Thread`
   - Ejecuta su propio bucle de actualización independiente
   - Permite movimiento autónomo de asteroides

#### Creación y Destrucción de Hilos:

**Creación de Hilos de Asteroides:**
```java
// En GameState.java - spawnAsteroids()
Asteroid asteroid = new Asteroid(x, y, vx, vy, size);
asteroid.start(); // Inicia el hilo del asteroide
asteroids.add(asteroid);
```

**Gestión del Ciclo de Vida:**
- **Inicio**: Los asteroides se crean cuando el juego comienza o cuando se destruyen asteroides grandes
- **Pausa**: Todos los hilos de asteroides se pausan via `setPaused(boolean)`
- **Destrucción**: Los hilos se detienen cuando:
  - El asteroide es destruido por una bala
  - El juego termina (Game Over)
  - La aplicación se cierra

**Limpieza Segura de Hilos:**
```java
// En App.java - Shutdown Hook
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    if (controller != null) {
        controller.cleanupOnExit(); // Detiene todos los hilos de asteroides
    }
}));

// En GameController.java - cleanupOnExit()
for (Model.Asteroid a : gameState.asteroids) {
    a.stopAsteroid(); // Termina el hilo del asteroide de forma segura
}
```

#### Sincronización:
- **Thread-Safe Collections**: Se usa `synchronized` en listas de asteroides cuando es necesario
- **Volatile Variables**: Estados como `paused` y `running` están marcados como `volatile`
- **Safe Shutdown**: Shutdown hooks garantizan limpieza de recursos al cerrar

### Sistema de Coordenadas (Coordinate System)
- Origen (0,0) en la esquina superior izquierda
- X aumenta hacia la derecha, Y aumenta hacia abajo
- Las entidades que salen de la pantalla "envuelven" al lado opuesto
- Todas las posiciones se manejan como números decimales para movimiento suave

### Gestión de Estado (State Management)
La clase `GameState` mantiene:
- Posiciones y velocidades de todas las entidades
- Puntuación actual y récord histórico
- Número de vidas restantes
- Estados de pausa, game over, e invencibilidad
- Lista de balas y asteroides activos

### Pipeline de Renderizado (Rendering Pipeline)
El orden de dibujo en `paintComponent()`:
1. Fondo negro con estrellas blancas
2. Nave (con efecto de parpadeo si es invencible)
3. Asteroides con rotación
4. Balas cian
5. Interfaz de usuario (puntuación, vidas)
6. Overlays (inicio, pausa, game over) si están activos
