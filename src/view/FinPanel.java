package view;

import javax.swing.*;
import java.awt.*;

public class FinPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public FinPanel(int score, int etoiles, int tempsRestant, int orGagne,
                    int niveauActuel, boolean niveauSuivantDisponible,
                    Runnable onRejouer, Runnable onNiveauSuivant, Runnable onMenu) {

        setLayout(new BorderLayout());
        setBackground(new Color(40, 44, 52));
        setPreferredSize(new Dimension(600, 420));

        // --- Titre ---
        String titreTexte = etoiles > 0 ? "Victoire !" : "Défaite...";
        Color couleurTitre = etoiles > 0 ? new Color(100, 220, 100) : new Color(220, 80, 80);
        JLabel titre = new JLabel(titreTexte, SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 36));
        titre.setForeground(couleurTitre);
        titre.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        add(titre, BorderLayout.NORTH);

        // --- Résultats ---
        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setOpaque(false);
        centre.add(Box.createVerticalStrut(16));

        JLabel niveauLabel = new JLabel("Niveau " + niveauActuel, SwingConstants.CENTER);
        niveauLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        niveauLabel.setForeground(new Color(160, 160, 160));
        niveauLabel.setAlignmentX(CENTER_ALIGNMENT);
        centre.add(niveauLabel);

        centre.add(Box.createVerticalStrut(10));

        JLabel etoilesLabel = new JLabel(buildEtoiles(etoiles), SwingConstants.CENTER);
        etoilesLabel.setFont(new Font("Arial", Font.BOLD, 48));
        etoilesLabel.setForeground(new Color(255, 215, 0));
        etoilesLabel.setAlignmentX(CENTER_ALIGNMENT);
        centre.add(etoilesLabel);

        centre.add(Box.createVerticalStrut(12));

        JLabel scoreLabel = new JLabel("Score : " + score, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 22));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setAlignmentX(CENTER_ALIGNMENT);
        centre.add(scoreLabel);

        centre.add(Box.createVerticalStrut(4));

        JLabel tempsLabel = new JLabel("Temps restant : " + tempsRestant + "s", SwingConstants.CENTER);
        tempsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        tempsLabel.setForeground(new Color(180, 180, 180));
        tempsLabel.setAlignmentX(CENTER_ALIGNMENT);
        centre.add(tempsLabel);

        centre.add(Box.createVerticalStrut(8));

        JLabel orLabel = new JLabel("+ " + orGagne + " or", SwingConstants.CENTER);
        orLabel.setFont(new Font("Arial", Font.BOLD, 22));
        orLabel.setForeground(new Color(255, 215, 0));
        orLabel.setAlignmentX(CENTER_ALIGNMENT);
        centre.add(orLabel);

        if (niveauSuivantDisponible) {
            centre.add(Box.createVerticalStrut(6));
            JLabel unlockLabel = new JLabel("Niveau " + (niveauActuel + 1) + " débloqué !", SwingConstants.CENTER);
            unlockLabel.setFont(new Font("Arial", Font.BOLD, 15));
            unlockLabel.setForeground(new Color(100, 220, 100));
            unlockLabel.setAlignmentX(CENTER_ALIGNMENT);
            centre.add(unlockLabel);
        }

        add(centre, BorderLayout.CENTER);

        // --- Boutons ---
        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 14));
        boutons.setOpaque(false);

        if (niveauSuivantDisponible) {
            JButton btnSuivant = creerBouton("Niveau suivant", new Color(60, 160, 60));
            btnSuivant.addActionListener(e -> onNiveauSuivant.run());
            boutons.add(btnSuivant);
        }

        JButton btnRejouer = creerBouton("Rejouer", new Color(70, 130, 180));
        btnRejouer.addActionListener(e -> onRejouer.run());
        boutons.add(btnRejouer);

        JButton btnMenu = creerBouton("Menu", new Color(90, 90, 90));
        btnMenu.addActionListener(e -> onMenu.run());
        boutons.add(btnMenu);

        add(boutons, BorderLayout.SOUTH);
    }

    private JButton creerBouton(String texte, Color fond) {
        JButton btn = new JButton(texte);
        btn.setFont(new Font("Arial", Font.BOLD, 17));
        btn.setPreferredSize(new Dimension(160, 46));
        btn.setBackground(fond);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        return btn;
    }

    private String buildEtoiles(int etoiles) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(i < etoiles ? "★" : "☆").append(" ");
        }
        return sb.toString().trim();
    }
}
