package view;

import java.awt.*;
import javax.swing.*;
import java.util.function.IntConsumer;

public class StartPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final String[] NOMS_NIVEAUX = {
        "Goblin Village",
        "Camp Barbare",
        "Forteresse Sombre",
        "Donjon du Champion"
    };
    private static final String[] DESC_NIVEAUX = {
        "3 défenses — 2 min",
        "4 défenses — 2 min",
        "5 défenses — 2 min",
        "6 défenses — 1m45 — 2 Pekka"
    };
    private static final Color[] COULEURS_NIVEAUX = {
        new Color(70, 140, 70),
        new Color(180, 130, 30),
        new Color(180, 70, 30),
        new Color(130, 30, 140)
    };

    public StartPanel(int orTotal, int niveauDebloque, IntConsumer onNiveauChoisi) {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 45));
        setPreferredSize(new Dimension(700, 460));

        // --- En-tête ---
        JPanel header = new JPanel();
        header.setBackground(new Color(30, 30, 45));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(24, 0, 10, 0));

        JLabel titre = new JLabel("FreeFight", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 42));
        titre.setForeground(Color.WHITE);
        titre.setAlignmentX(CENTER_ALIGNMENT);

        JLabel orLabel = new JLabel("Or total : " + orTotal, SwingConstants.CENTER);
        orLabel.setFont(new Font("Arial", Font.BOLD, 18));
        orLabel.setForeground(new Color(255, 215, 0));
        orLabel.setAlignmentX(CENTER_ALIGNMENT);

        header.add(titre);
        header.add(Box.createVerticalStrut(8));
        header.add(orLabel);
        add(header, BorderLayout.NORTH);

        // --- Grille des niveaux ---
        JPanel grille = new JPanel(new GridLayout(2, 2, 15, 15));
        grille.setBackground(new Color(30, 30, 45));
        grille.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        for (int i = 0; i < 4; i++) {
            final int niveau = i + 1;
            boolean debloque = niveau <= niveauDebloque;

            JPanel carte = new JPanel(new BorderLayout(0, 6));
            carte.setBackground(debloque ? new Color(45, 45, 65) : new Color(35, 35, 45));
            carte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(debloque ? COULEURS_NIVEAUX[i] : new Color(60, 60, 60), 2),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
            ));

            JLabel nomLabel = new JLabel("Niv. " + niveau + " – " + NOMS_NIVEAUX[i]);
            nomLabel.setFont(new Font("Arial", Font.BOLD, 15));
            nomLabel.setForeground(debloque ? Color.WHITE : new Color(100, 100, 100));

            JLabel descLabel = new JLabel(debloque ? DESC_NIVEAUX[i] : "Terminez le niveau précédent");
            descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            descLabel.setForeground(debloque ? new Color(190, 190, 190) : new Color(80, 80, 80));

            JButton btn = new JButton(debloque ? "Jouer" : "Verrouillé");
            btn.setFont(new Font("Arial", Font.BOLD, 13));
            btn.setBackground(debloque ? COULEURS_NIVEAUX[i] : new Color(55, 55, 55));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setEnabled(debloque);
            if (debloque) {
                btn.addActionListener(e -> onNiveauChoisi.accept(niveau));
            }

            carte.add(nomLabel,  BorderLayout.NORTH);
            carte.add(descLabel, BorderLayout.CENTER);
            carte.add(btn,       BorderLayout.SOUTH);
            grille.add(carte);
        }

        add(grille, BorderLayout.CENTER);

        // --- Pied de page ---
        JPanel bas = new JPanel();
        bas.setBackground(new Color(30, 30, 45));
        bas.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        JLabel hint = new JLabel("Gagnez des étoiles pour débloquer les niveaux suivants");
        hint.setFont(new Font("Arial", Font.ITALIC, 13));
        hint.setForeground(new Color(140, 140, 140));
        bas.add(hint);
        add(bas, BorderLayout.SOUTH);
    }
}
