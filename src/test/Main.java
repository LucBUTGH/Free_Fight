package test;
import controller.GameController;
import view.Affichage;
import view.StartPanel;

import javax.swing.*;
import java.awt.*;

public class Main {

    private static final String CARTE_START = "start";
    private static final String CARTE_JEU   = "jeu";

    private final JFrame     fenetre;
    private final CardLayout cards;
    private final JPanel     root;

    public Main() {
        fenetre = new JFrame("FreeFight");
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenetre.setResizable(false);

        cards = new CardLayout();
        root  = new JPanel(cards);

        StartPanel startPanel = new StartPanel(this::lancerJeu);
        root.add(startPanel, CARTE_START);

        fenetre.add(root);
        fenetre.pack();
        fenetre.setLocationRelativeTo(null);
        fenetre.setVisible(true);
    }

    private void lancerJeu() {
        GameController controller = new GameController();
        Affichage affichage = new Affichage(controller.getPartie(), controller);
        controller.setAffichage(affichage);

        root.add(affichage, CARTE_JEU);
        cards.show(root, CARTE_JEU);
        fenetre.pack();

        controller.demarrer();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}