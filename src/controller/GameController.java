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
 *  - Maintenir la sélection courante (troupe sélectionnée, point de clic)
 */
public class GameController {

    // ── Modèle ──────────────────────────────────────────────────────────────
    private final Partie partie;

    // ── Vue ─────────────────────────────────────────────────────────────────
    private Affichage affichage;

    // ── État de sélection (appartient au contrôleur, pas à la vue) ──────────
    private Troupe troupeSelectionnee = null;

    // ── Timers ──────────────────────────────────────────────────────────────
    private final Timer timerJeu;    // boucle 40 ms : déplace les troupes + redessine
    private Timer timerChrono = null;

    // ── Constantes de hit-test ───────────────────────────────────────────────
    private static final int TROUPE_SIZE  = 40;
    private static final int AVATAR_SIZE  = 50;
    private static final int AVATAR_SPACING = 80;
    private static final int AVATAR_START_X = 20;
    private static final int DEF_SIZE    = 30;
    private static final int HOTEL_SIZE  = 50;

    // ────────────────────────────────────────────────────────────────────────

    public GameController() {

        // 1. Crée la partie et les troupes initiales
        partie = new Partie();
        partie.ajouterTroupe(new Barbare(50,  500));
        partie.ajouterTroupe(new Sorcier(120, 500));
        partie.ajouterTroupe(new Pekka(190,   500));

        // 2. Boucle de jeu (40 ms ≈ 25 fps)
        timerJeu = new Timer(40, e -> {
            partie.update();
            if (affichage != null) affichage.repaint();
        });

        // 3. Chronomètre (1 tick/s)
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

    // ── Accesseurs pour la vue 

    public Partie getPartie() {
        return partie;
    }

    public Troupe getTroupeSelectionnee() {
        return troupeSelectionnee;
    }

    // ── Gestion des clics 

    private void handleClick(int mx, int my) {

        // 1. Clic sur un avatar dans la barre du bas → sélection
        Troupe depuisBar = getTroupeFromBar(mx, my);
        if (depuisBar != null) {
            troupeSelectionnee = depuisBar;
            affichage.repaint();
            return;
        }

        // 2. Clic sur une troupe sur la carte → sélection
        Troupe surCarte = getTroupeAtPosition(mx, my);
        if (surCarte != null) {
            troupeSelectionnee = surCarte;
            affichage.repaint();
            return;
        }

        // 3. Une troupe est sélectionnée : on interprète le clic sur le terrain
        if (troupeSelectionnee != null) {

            // 3a. Pekka : clic sur le sol → déplacement manuel
            if (troupeSelectionnee instanceof Pekka) {
                Batiment cible = getBatimentAtPosition(mx, my);
                if (cible == null) {
                    ((Pekka) troupeSelectionnee).setDestination(mx, my);
                    affichage.repaint();
                    return;
                }
            }

            // 3b. Clic sur un bâtiment → assigner une cible
            Batiment cible = getBatimentAtPosition(mx, my);
            if (cible != null) {
                troupeSelectionnee.setCible(cible);
                affichage.repaint();
                return;
            }
        }

        // 4. Clic libre sur le sol → test de portée des défenses
        List<Defense> enPortee = partie.getDefensesEnPortee(mx, my);
        affichage.setClickInfo(mx, my, enPortee);
        affichage.repaint();
    }

    // ── Hit-tests (logique de position) 

    private Troupe getTroupeFromBar(int mx, int my) {
        int panelHeight = affichage.getMapPanel().getHeight();
        int barY = panelHeight - 80;
        int index = 0;
        for (Troupe t : partie.getTroupes()) {
            int x = AVATAR_START_X + index * AVATAR_SPACING;
            if (mx >= x && mx <= x + AVATAR_SIZE && my >= barY && my <= barY + AVATAR_SIZE) {
                return t;
            }
            index++;
        }
        return null;
    }

    private Troupe getTroupeAtPosition(int mx, int my) {
        for (Troupe t : partie.getTroupes()) {
            if (mx >= t.getX() && mx <= t.getX() + TROUPE_SIZE
                    && my >= t.getY() && my <= t.getY() + TROUPE_SIZE) {
                return t;
            }
        }
        return null;
    }

    private Batiment getBatimentAtPosition(int mx, int my) {
        for (Defense d : partie.getDefenses()) {
            int bx = d.getX() - DEF_SIZE / 2;
            int by = d.getY() - DEF_SIZE / 2;
            if (mx >= bx && mx <= bx + DEF_SIZE && my >= by && my <= by + DEF_SIZE) {
                return d;
            }
        }
        Batiment hdv = partie.getHotelDeVille();
        int bx = hdv.getX() - HOTEL_SIZE / 2;
        int by = hdv.getY() - HOTEL_SIZE / 2;
        if (mx >= bx && mx <= bx + HOTEL_SIZE && my >= by && my <= by + HOTEL_SIZE) {
            return hdv;
        }
        return null;
    }
}
