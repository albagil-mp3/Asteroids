import javax.swing.*;
import Controller.GameController;

/**
 * PUNTO DE ENTRADA PRINCIPAL DEL JUEGO ASTEROIDS
 * ==============================================
 * 
 * Esta clase es el punto de entrada de la aplicación.
 * Su única responsabilidad es inicializar el sistema y limpiar recursos.
 * 
 * ARQUITECTURA:
 * - Sigue el patrón de "Single Responsibility" - solo inicialización
 * - Delega toda la lógica al GameController
 * - Maneja cleanup de recursos de manera segura
 * 
 * ELEMENTOS IMPORTANTES:
 * 
 * 1. SHUTDOWN HOOK:
 *    - Garantiza que los recursos se liberen correctamente
 *    - Detiene threads de asteroides y guarda high score
 *    - Se ejecuta incluso si el usuario cierra la ventana bruscamente
 * 
 * 2. SWINGUTILITIES.INVOKELATER:
 *    - Asegura que la UI se cree en el Event Dispatch Thread (EDT)
 *    - Requisito de Swing para thread safety
 *    - Evita problemas de concurrencia en la interfaz
 * 
 * FLUJO DE EJECUCIÓN:
 * 1. Se registra el shutdown hook para cleanup
 * 2. Se programa la creación de UI en el EDT
 * 3. El GameController toma control del juego
 */
public class App {
    // Referencia al controlador para cleanup
    private static GameController controller;
    
    /**
     * MÉTODO MAIN - PUNTO DE ENTRADA
     * 
     * Configura el sistema y transfiere control al GameController
     */
    public static void main(String[] args) {
        // CONFIGURACIÓN DE LIMPIEZA AUTOMÁTICA
        // Registra un "shutdown hook" que se ejecuta cuando la aplicación termina
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (controller != null) {
                // Limpia recursos: detiene threads, guarda high score
                controller.cleanupOnExit();
            }
        }));
        
        // INICIALIZACIÓN SEGURA DE UI
        // SwingUtilities.invokeLater asegura que la UI se cree en el EDT
        // (Event Dispatch Thread) - requisito fundamental de Swing
        SwingUtilities.invokeLater(() -> {
            // Crea el controlador - esto inicia todo el sistema MVC
            controller = new GameController();
        });
    }
}
