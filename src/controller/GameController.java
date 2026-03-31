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
 *  - Créer et posséder la Partie (état du jeu)
 *  - Gérer les timers (boucle de jeu + chronomètre)
 *  - Interpréter les événements souris et les traduire en actions sur le modèle
 *  - Maintenir la sélection courante (troupe sélectionnée)
 */
public class GameController {

    // ── Modèle 
    private final Partie partie;

    // ── Vue 
    private Affichage affichage;

    // ── État de sélection (appartient au contrôleur, pas à la vue) 
    private Troupe troupeSelectionnee = null;

    // ── Timers
    private final Timer timerJeu;    // boucle 40 ms : déplace les troupes + redessine
    private Timer timerChrono = null;

    // ── Constantes de hit-test 
    private static final int TROUPE_SIZE    = 40;
    private static final int AVATAR_SIZE    = 50;
    private static final int AVATAR_SPACING = 80;
    private static final int AVATAR_START_X = 20;
    private static final int DEF_SIZE       = 30;
    private static final int HOTEL_SIZE     = 50;
    
 // Type de troupe sélectionné dans la barre ("Barbare", "Sorcier", "Pekka")
    private String typeSelectionne = null;

    public GameController(Partie partie) {

        // 1. Crée la partie et les troupes initiales
    	this.partie = partie;

        // 2. Boucle de jeu (40 ms ≈ 25 fps)
        // Ce timer est pour :
        // - faire bouger les troupes
        // - faire attaquer
        // - mettre à jour l'état du jeu
        // - redessiner l'écran
        timerJeu = new Timer(40, e -> {
            partie.update();
            if (affichage != null) affichage.repaint();
        });

        // 3. Chronomètre (1 tick/s)
        // Décrémente le temps restant chaque seconde
        // et arrête automatiquement quand le temps est écoulé
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

    /** Démarre les deux timers (appelé par Main après setAffichage). */
    public void demarrer() {
        timerJeu.start();
        timerChrono.start();
    }

    public Partie getPartie() {
        return partie;
    }


    private void handleClick(int mx, int my) {

        // 1. Clic sur un avatar dans la barre → sélectionner le TYPE
        String type = getTypeFromBar(mx, my);
        if (type != null) {
            typeSelectionne = type;
            affichage.repaint();
            return;
        }

        // 2. Un type est sélectionné + clic sur la carte → demander la quantité puis déployer
        if (typeSelectionne != null) {
            int stock = getStock(typeSelectionne);
            if (stock <= 0) {
                JOptionPane.showMessageDialog(affichage,
                    "Plus de " + typeSelectionne + " disponible !",
                    "Stock vide", JOptionPane.WARNING_MESSAGE);
                typeSelectionne = null;
                return;
            }

            // Demande la quantité
            String input = JOptionPane.showInputDialog(
                affichage,
                "Combien de " + typeSelectionne + " voulez-vous envoyer ? (max " + stock + ")",
                "Déployer",
                JOptionPane.QUESTION_MESSAGE
            );

            if (input == null) return; // annulé

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

        // 3. Clic libre → test de portée
        List<Defense> enPortee = partie.getDefensesEnPortee(mx, my);
        affichage.setClickInfo(mx, my, enPortee);
        affichage.repaint();
    }

    /** Retourne le type de troupe cliqué dans la barre du bas, ou null. */
    private String getTypeFromBar(int mx, int my) {
        int barY     = affichage.getMapPanel().getHeight() - 80;
        int[] types  = {0, 1, 2}; // Pekka, Sorcier, Barbare (ordre des avatars)
        String[] noms = {"Pekka", "Sorcier", "Barbare"};

        for (int i = 0; i < 3; i++) {
            int x = AVATAR_START_X + i * AVATAR_SPACING;
            if (mx >= x && mx <= x + AVATAR_SIZE && my >= barY && my <= barY + AVATAR_SIZE)
                return noms[i];
        }
        return null;
    }

    /** Retourne le stock disponible pour un type donné. */
    private int getStock(String type) {
        switch (type) {
            case "Barbare": return partie.getStockBarbare();
            case "Sorcier": return partie.getStockSorcier();
            case "Pekka":   return partie.getStockPekka();
            default: return 0;
        }
    }

    /** Exposé pour la vue (contour jaune sur l'avatar sélectionné). */
    public String getTypeSelectionne() { return typeSelectionne; }


    /**
     * Retourne la troupe cliquée dans la barre du bas.
     * Parcourt la liste des troupes et vérifie si le clic tombe
     * dans la zone de l'avatar correspondant.
     */
    private Troupe getTroupeFromBar(int mx, int my) {
        int barY  = affichage.getMapPanel().getHeight() - 80;
        int index = 0;
        for (Troupe t : partie.getTroupes()) {
            int x = AVATAR_START_X + index * AVATAR_SPACING;
            if (mx >= x && mx <= x + AVATAR_SIZE && my >= barY && my <= barY + AVATAR_SIZE)
                return t;
            index++;
        }
        return null;
    }

    /**
     * Retourne la troupe située à la position du clic sur la carte.
     * Parcourt la liste des troupes et vérifie si les coordonnées
     * de la souris tombent dans le rectangle de la troupe.
     * Retourne null si aucune troupe n'est trouvée.
     */
    private Troupe getTroupeAtPosition(int mx, int my) {
        for (Troupe t : partie.getTroupes()) {
            if (mx >= t.getX() && mx <= t.getX() + TROUPE_SIZE
                    && my >= t.getY() && my <= t.getY() + TROUPE_SIZE)
                return t;
        }
        return null;
    }

    /**
     * Retourne le bâtiment situé sous le clic de la souris.
     * Vérifie d'abord les défenses, puis l'hôtel de ville.
     * Retourne null si aucun bâtiment n'est trouvé.
     */
    private Batiment getBatimentAtPosition(int mx, int my) {
        // Vérifie d'abord les défenses
        for (Defense d : partie.getDefenses()) {
            int bx = d.getX() - DEF_SIZE / 2;
            int by = d.getY() - DEF_SIZE / 2;
            if (mx >= bx && mx <= bx + DEF_SIZE && my >= by && my <= by + DEF_SIZE)
                return d;
        }

        // Vérifie les bâtiments normaux
        final int BAT_SIZE = 35;
        for (Batiment b : partie.getAutresBatiments()) {
            int bx = b.getX() - BAT_SIZE / 2;
            int by = b.getY() - BAT_SIZE / 2;
            if (mx >= bx && mx <= bx + BAT_SIZE && my >= by && my <= by + BAT_SIZE)
                return b;
        }

        // Vérifie ensuite l'hôtel de ville
        Batiment hdv = partie.getHotelDeVille();
        int bx = hdv.getX() - HOTEL_SIZE / 2;
        int by = hdv.getY() - HOTEL_SIZE / 2;
        if (mx >= bx && mx <= bx + HOTEL_SIZE && my >= by && my <= by + HOTEL_SIZE)
            return hdv;

        return null;
    }
    
    
    
    
    
    
}