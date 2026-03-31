package controller;

import model.*;
import view.Affichage;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Contrôleur principal du jeu.
 *
 * Responsabilités :
 *  - Posséder la Partie (état du jeu)
 *  - Gérer les timers (boucle de jeu 40ms + chronomètre 1s)
 *  - Interpréter les événements souris et les traduire en actions sur le modèle
 *  - Maintenir le type de troupe sélectionné dans la barre du bas
 */
public class GameController {

    private final Partie partie;

    private Affichage affichage;

    private final Timer timerJeu;    // boucle 40 ms : déplace les troupes + redessine
    private Timer       timerChrono; // tick 1 s : décrémente le chrono

    private static final int AVATAR_SIZE    = 50;
    private static final int AVATAR_SPACING = 80;
    private static final int AVATAR_START_X = 20;
    private static final int DEF_SIZE       = 30;
    private static final int HOTEL_SIZE     = 50;
    private static final int BAT_SIZE       = 35;


    // Type de troupe sélectionné dans la barre ("Barbare", "Sorcier", "Pekka")
    // null si aucun type n'est sélectionné
    private String typeSelectionne = null;


    /**
     * Initialise le contrôleur avec une partie existante.
     * Crée les deux timers sans les démarrer.
     *
     * @param partie  La partie à contrôler
     */
    public GameController(Partie partie) {
        this.partie = partie;

        // Boucle de jeu (40 ms ≈ 25 fps)
        // Fait avancer le jeu et redessine l'écran à chaque tick
        timerJeu = new Timer(40, e -> {
            partie.update();
            if (affichage != null) affichage.repaint();
        });

        // Chronomètre (1 tick/s)
        // Décrémente le temps restant et arrête quand le temps est écoulé
        timerChrono = new Timer(1000, e -> {
            if (!partie.tempsEcoule()) {
                partie.decrementerTemps();
                if (affichage != null) affichage.repaint();
            } else {
                timerChrono.stop();
                if (affichage != null) affichage.repaint();
            }
        });
    }


    /**
     * Lie la vue au contrôleur et installe le listener souris.
     * Appelé par Main une fois la fenêtre construite.
     *
     * @param affichage  La vue principale du jeu
     */
    public void setAffichage(Affichage affichage) {
        this.affichage = affichage;
        affichage.getMapPanel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    /** Démarre les deux timers. Appelé par Main après setAffichage(). */
    public void demarrer() {
        timerJeu.start();
        timerChrono.start();
    }


    /** Retourne la partie en cours. */
    public Partie getPartie() { return partie; }

    /** Retourne le type de troupe sélectionné dans la barre (ou null). */
    public String getTypeSelectionne() { return typeSelectionne; }


    /**
     * Interprète un clic souris selon le contexte :
     * 1. Clic sur un avatar dans la barre → sélectionner le type
     * 2. Type sélectionné + clic sur la carte → demander la quantité et déployer
     * 3. Clic libre → test de portée des défenses
     *
     * @param mx  Coordonnée X du clic
     * @param my  Coordonnée Y du clic
     */
    private void handleClick(int mx, int my) {

        // 1. Clic sur un avatar dans la barre → sélectionner le TYPE
        String type = getTypeFromBar(mx, my);
        if (type != null) {
            typeSelectionne = type;
            affichage.repaint();
            return;
        }

        // 2. Type sélectionné + clic sur la carte → déploiement
        if (typeSelectionne != null) {
            int stock = getStock(typeSelectionne);

            // Vérification que le stock n'est pas vide
            if (stock <= 0) {
                JOptionPane.showMessageDialog(affichage,
                    "Plus de " + typeSelectionne + " disponible !",
                    "Stock vide", JOptionPane.WARNING_MESSAGE);
                typeSelectionne = null;
                return;
            }

            // Demande la quantité à déployer
            String input = JOptionPane.showInputDialog(
                affichage,
                "Combien de " + typeSelectionne + " voulez-vous envoyer ? (max " + stock + ")",
                "Déployer",
                JOptionPane.QUESTION_MESSAGE
            );

            if (input == null) return; // le joueur a annulé

            try {
                int quantite = Integer.parseInt(input.trim());
                if (quantite <= 0) return;
                partie.deployerTroupes(typeSelectionne, quantite, mx, my);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(affichage, "Entrez un nombre valide.");
            }

            typeSelectionne = null;
            affichage.repaint();
            return;
        }

        // 3. Clic libre → test de portée des défenses
        List<Defense> enPortee = partie.getDefensesEnPortee(mx, my);
        affichage.setClickInfo(mx, my, enPortee);
        affichage.repaint();
    }


    /**
     * Retourne le type de troupe cliqué dans la barre du bas, ou null.
     * Ordre des avatars : Pekka (0), Sorcier (1), Barbare (2).
     *
     * @param mx  Coordonnée X du clic
     * @param my  Coordonnée Y du clic
     * @return    "Pekka", "Sorcier", "Barbare", ou null
     */
    private String getTypeFromBar(int mx, int my) {
        int barY      = affichage.getMapPanel().getHeight() - 80;
        String[] noms = {"Pekka", "Sorcier", "Barbare"};

        for (int i = 0; i < 3; i++) {
            int x = AVATAR_START_X + i * AVATAR_SPACING;
            if (mx >= x && mx <= x + AVATAR_SIZE && my >= barY && my <= barY + AVATAR_SIZE)
                return noms[i];
        }
        return null;
    }

    /**
     * Retourne le stock disponible pour un type de troupe donné.
     *
     * @param type  "Barbare", "Sorcier" ou "Pekka"
     * @return      Nombre de troupes restantes dans le stock
     */
    private int getStock(String type) {
        switch (type) {
            case "Barbare": return partie.getStockBarbare();
            case "Sorcier": return partie.getStockSorcier();
            case "Pekka":   return partie.getStockPekka();
            default:        return 0;
        }
    }
}