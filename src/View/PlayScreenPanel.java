package View;

import javax.swing.*;
import java.awt.*;

// Simple play/start screen panel with a Play button
public class PlayScreenPanel extends JPanel {
    public JButton playButton;
    public PlayScreenPanel() {
        setLayout(null);
        setBackground(Color.BLACK);
        JLabel title = new JLabel("ASTEROIDS");
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        title.setBounds(200, 100, 400, 60);
        add(title);
        playButton = new JButton("Play");
        playButton.setFont(new Font("Arial", Font.BOLD, 32));
        playButton.setBounds(300, 250, 200, 70);
        add(playButton);
    }
}
