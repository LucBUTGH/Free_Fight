package view;

import java.awt.*;
import javax.swing.*;

/**
 * Écran de démarrage du jeu.
 *
 * Reçoit un simple Runnable au lieu de dépendre de la classe Main,
 * ce qui découple complètement la vue du point d'entrée.
 * Le bouton "Start Game" appelle onStart.run() → lancerJeu() dans Main.
 */
public class StartPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Crée l'écran de démarrage.
     *
     * @param onStart  Action exécutée quand le joueur clique sur "Start Game"
     */
    public StartPanel(Runnable onStart) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Taille de la fenêtre de démarrage
        setPreferredSize(new Dimension(600, 400));

        // Titre centré
        JLabel titleLabel = new JLabel("FreeFight", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        add(titleLabel, BorderLayout.CENTER);

        // Bouton de démarrage
        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.PLAIN, 24));

        // Au clic → déclenche lancerJeu() dans Main
        startButton.addActionListener(e -> onStart.run());

        // Panneau du bas avec marge autour du bouton
        JPanel bottom = new JPanel();
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        bottom.add(startButton);
        add(bottom, BorderLayout.SOUTH);
    }
}