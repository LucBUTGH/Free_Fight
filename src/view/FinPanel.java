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

        JPanel etoilesPanel = creerPanneauEtoiles(etoiles, 44, 10);
        etoilesPanel.setAlignmentX(CENTER_ALIGNMENT);
        centre.add(etoilesPanel);

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

    // Dessine 3 étoiles en Java2D — évite les problèmes de police avec ★/☆ sur Windows
    private JPanel creerPanneauEtoiles(int etoiles, int taille, int espacement) {
        int largeur = 3 * taille + 2 * espacement + 4;
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 0; i < 3; i++) {
                    int cx = i * (taille + espacement) + taille / 2 + 2;
                    int cy = taille / 2 + 2;
                    int[] xp = new int[10], yp = new int[10];
                    double outer = taille / 2.0, inner = outer * 0.42;
                    for (int j = 0; j < 10; j++) {
                        double angle = Math.PI / 2 + j * Math.PI / 5;
                        double r = (j % 2 == 0) ? outer : inner;
                        xp[j] = cx + (int)(Math.cos(angle) * r);
                        yp[j] = cy - (int)(Math.sin(angle) * r);
                    }
                    if (i < etoiles) {
                        g2.setColor(new Color(255, 215, 0));
                        g2.fillPolygon(xp, yp, 10);
                        g2.setColor(new Color(200, 160, 0));
                    } else {
                        g2.setColor(new Color(70, 70, 70));
                        g2.fillPolygon(xp, yp, 10);
                        g2.setColor(new Color(120, 120, 120));
                    }
                    g2.setStroke(new java.awt.BasicStroke(2));
                    g2.drawPolygon(xp, yp, 10);
                }
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(largeur, taille + 4));
        p.setMaximumSize(new Dimension(largeur, taille + 4));
        return p;
    }
}
