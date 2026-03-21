package view;

import java.awt.*;
import javax.swing.*;

public class StartPanel extends JPanel {
    private final JButton startButton;
    private final JLabel titleLabel;
    
    private static final long serialVersionUID = 1L;

    public StartPanel(Runnable onStart) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Taille de la fenêtre de démarrage
        setPreferredSize(new Dimension(600, 400));

        titleLabel = new JLabel("FreeFight", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        add(titleLabel, BorderLayout.CENTER);

        startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.PLAIN, 24));

        JPanel bottom = new JPanel();
        bottom.setBackground(Color.WHITE);

        // Ajouter une marge autour du bouton
        bottom.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        bottom.add(startButton);
        add(bottom, BorderLayout.SOUTH);

        startButton.addActionListener(e -> onStart.run());
    }
}
