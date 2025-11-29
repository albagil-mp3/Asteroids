package View;

import javax.swing.*;
import java.awt.*;

// Panel for adjusting asteroid size and speed settings
public class ControlPanel extends JPanel {
    public JSpinner minSizeSpinner, maxSizeSpinner, minSpeedSpinner, maxSpeedSpinner;
    public JButton applyButton;

    public ControlPanel(int minSize, int maxSize, double minSpeed, double maxSpeed) {
        setLayout(new FlowLayout());
        // Size controls
        add(new JLabel("Min Size:"));
        minSizeSpinner = new JSpinner(new SpinnerNumberModel(minSize, 5, 200, 1));
        add(minSizeSpinner);
        add(new JLabel("Max Size:"));
        maxSizeSpinner = new JSpinner(new SpinnerNumberModel(maxSize, 5, 200, 1));
        add(maxSizeSpinner);
        // Speed controls
        add(new JLabel("Min Speed:"));
        minSpeedSpinner = new JSpinner(new SpinnerNumberModel(minSpeed, 0.1, 10.0, 0.1));
        add(minSpeedSpinner);
        add(new JLabel("Max Speed:"));
        maxSpeedSpinner = new JSpinner(new SpinnerNumberModel(maxSpeed, 0.1, 10.0, 0.1));
        add(maxSpeedSpinner);
        // Apply button
        applyButton = new JButton("Apply");
        add(applyButton);
    }

    // Getters for current spinner values
    public int getMinSize() { return (int) minSizeSpinner.getValue(); }
    public int getMaxSize() { return (int) maxSizeSpinner.getValue(); }
    public double getMinSpeed() { return (double) minSpeedSpinner.getValue(); }
    public double getMaxSpeed() { return (double) maxSpeedSpinner.getValue(); }
}
