package Model;

/**
 * ENTIDAD NAVE - REPRESENTACIÓN DEL JUGADOR
 * ==========================================
 * 
 * La clase Ship modela la nave espacial que controla el jugador.
 * Implementa física realista con inercia y fricción.
 * 
 * CONCEPTOS DE FÍSICA IMPLEMENTADOS:
 * 
 * 1. INERCIA:
 *    - La nave mantiene velocidad cuando no se acelera
 *    - Simula movimiento en el espacio (sin fricción atmosférica)
 * 
 * 2. ACELERACIÓN VECTORIAL:
 *    - Thrust se aplica en la dirección que apunta la nave
 *    - Usa trigonometría para descomponer fuerza en componentes X,Y
 * 
 * 3. FRICCIÓN ESPACIAL:
 *    - Pequeña fricción (0.99) para gameplay jugable
 *    - Sin fricción, la nave sería imposible de controlar
 * 
 * 4. SCREEN WRAPPING:
 *    - La nave aparece del lado opuesto al salir de pantalla
 *    - Crea sensación de mundo infinito
 * 
 * VARIABLES DE ESTADO:
 * - x, y: Posición actual
 * - angle: Rotación (0 = apunta hacia arriba)
 * - velocityX, velocityY: Velocidad actual
 * - accelerating: Si está aplicando thrust
 * - decelerating: Si está frenando activamente
 */
public class Ship {
    // ESTADO FÍSICO DE LA NAVE
    public double x, y, angle;           // Posición y orientación
    public double velocityX, velocityY;   // Velocidad actual en cada eje
    public boolean accelerating;          // Si está acelerando (thrust)
    private boolean decelerating = false; // Si está desacelerando activamente
    /**
     * CONSTRUCTOR - Inicializa nave en posición dada
     * 
     * @param x Posición inicial X
     * @param y Posición inicial Y
     */
    public Ship(double x, double y) {
        this.x = x;
        this.y = y;
        this.angle = 0;        // Apunta hacia arriba (0 radianes)
        this.velocityX = 0;    // Empieza sin movimiento
        this.velocityY = 0;
        this.accelerating = false;
    }

    public void setDecelerating(boolean decelerating) {
        this.decelerating = decelerating;
    }
    /**
     * MÉTODO UPDATE - Actualiza física de la nave cada frame
     * 
     * PROCESO DE ACTUALIZACIÓN:
     * 1. Aplicar aceleración si está en thrust
     * 2. Aplicar desaceleración si está frenando
     * 3. Actualizar posición según velocidad
     * 4. Aplicar screen wrapping
     * 5. Aplicar fricción espacial
     */
    public void update() {
        // ACELERACIÓN - Aplicar thrust en dirección de la nave
        if (accelerating) {
            // La nave apunta hacia arriba, pero angle=0 es hacia la derecha en trigonometría
            // Por eso restamos PI/2 para corregir la orientación
            double forwardAngle = angle - Math.PI/2;
            velocityX += Math.cos(forwardAngle) * 0.1;  // Componente X del thrust
            velocityY += Math.sin(forwardAngle) * 0.1;  // Componente Y del thrust
        }
        
        // DESACELERACIÓN ACTIVA - Freno suave cuando se presiona S
        if (decelerating) {
            double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            if (speed > 0.05) {
                velocityX *= 0.96;  // Reduce velocidad gradualmente
                velocityY *= 0.96;
            } else {
                velocityX = 0;      // Detiene completamente si va muy lento
                velocityY = 0;
            }
        }
        
        // ACTUALIZAR POSICIÓN
        x += velocityX;
        y += velocityY;
        
        // SCREEN WRAPPING - Aparecer del otro lado al salir de pantalla
        if (x < 0) x += Model.GameState.Config.WINDOW_WIDTH;
        if (x > Model.GameState.Config.WINDOW_WIDTH) x -= Model.GameState.Config.WINDOW_WIDTH;
        if (y < 0) y += Model.GameState.Config.WINDOW_HEIGHT;
        if (y > Model.GameState.Config.WINDOW_HEIGHT) y -= Model.GameState.Config.WINDOW_HEIGHT;
        
        // FRICCIÓN ESPACIAL - Reduce ligeramente la velocidad
        // Sin esto, la nave sería imposible de controlar
        velocityX *= 0.99;
        velocityY *= 0.99;
    }
}
