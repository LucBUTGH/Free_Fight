package view;

import java.awt.*;
import javax.swing.*;

/**
 * Écran de démarrage du jeu.
 *
 * Reçoit un simple Runnable (au lieu de dépendre de test.Test),
 * ce qui découple complètement la vue du point d'entrée.
 */
public class StartPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public StartPanel(Runnable onStart) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 400));

        JLabel titleLabel = new JLabel("FreeFight", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        add(titleLabel, BorderLayout.CENTER);

        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.PLAIN, 24));
        startButton.addActionListener(e -> onStart.run());

        JPanel bottom = new JPanel();
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        bottom.add(startButton);
        add(bottom, BorderLayout.SOUTH);
    }
}
