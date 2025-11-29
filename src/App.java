import javax.swing.*;
import Controller.GameController;

public class App {
    private static GameController controller;
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (controller != null) {
                controller.cleanupOnExit();
            }
        }));
        SwingUtilities.invokeLater(() -> {
            controller = new GameController();
        });
    }
}
