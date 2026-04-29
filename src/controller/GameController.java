package controller;

import model.*;
import view.Affichage;

import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Contrôleur principal du jeu.
 *
 * Responsabilités :
 *  - Posséder la Partie (état du jeu)
 *  - Gérer les threads (boucle de jeu 40ms + chronomètre 1s)
 *  - Interpréter les événements souris et les traduire en actions sur le modèle
 *  - Maintenir le type de troupe sélectionné dans la barre du bas
 */
public class GameController extends Thread {

    private final Partie partie;

    private Affichage affichage;

    private Thread threadJeu;    // boucle 40 ms : déplace les troupes + redessine
    private Thread threadChrono; // tick 1 s : décrémente le chrono
    private volatile boolean running = false; // partagé entre les deux threads

    private static final int AVATAR_SIZE    = 50;
    private static final int AVATAR_SPACING = 80;
    private static final int AVATAR_START_X = 20;
    //private static final int DEF_SIZE       = 30;
    //private static final int HOTEL_SIZE     = 50;
    //private static final int BAT_SIZE       = 35;


    // Type de troupe sélectionné dans la barre ("Barbare", "Sorcier", "Pekka")
    // null si aucun type n'est sélectionné
    private String typeSelectionne = null;

    // Pekka déployé sélectionné par le joueur pour le déplacer manuellement
    // null si aucun Pekka n'est sélectionné
    private Pekka pekkaActive = null;


    /**
     * Initialise le contrôleur avec une partie existante.
     * Crée les deux timers sans les démarrer.
     *
     * @param partie  La partie à contrôler
     */
    // Garde pour ne pas appeler notifierFinPartie() deux fois
    private volatile boolean finNotifiee = false;

    public GameController(Partie partie) {
        this.partie = partie;

        threadJeu = new Thread(() -> {
            while (running) {
                partie.update();
                SwingUtilities.invokeLater(() -> {
                    if (affichage != null) affichage.repaint();
                });
                if (!finNotifiee && partie.estTerminee()) {
                    arreterThreads();
                    SwingUtilities.invokeLater(this::notifierFinPartie);
                    return;
                }
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "thread-jeu");

        threadChrono = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (!partie.tempsEcoule()) {
                    partie.decrementerTemps();
                    SwingUtilities.invokeLater(() -> {
                        if (affichage != null) affichage.repaint();
                    });
                } else if (!finNotifiee) {
                    arreterThreads();
                    SwingUtilities.invokeLater(this::notifierFinPartie);
                    return;
                }
            }
        }, "thread-chrono");
    }

    private void arreterThreads() {
        running = false;
        threadJeu.interrupt();
        threadChrono.interrupt();
        SwingUtilities.invokeLater(() -> {
            if (affichage != null) affichage.repaint();
        });
    }

    /**
     * Callback appelé quand la partie se termine (temps écoulé).
     * Implémenté par Main pour afficher l'écran de fin.
     */
    private Runnable finPartieCallback;

    /**
     * Définit le callback de fin de partie.
     * @param callback Runnable à exécuter quand la partie se termine
     */
    public void setFinPartieCallback(Runnable callback) {
        this.finPartieCallback = callback;
    }

    /**
     * Notifie la fin de la partie et appelle le callback si défini.
     */
    private void notifierFinPartie() {
        finNotifiee = true;
        if (finPartieCallback != null) {
            finPartieCallback.run();
        }
    }

    /**
     * Retourne le nombre d'étoiles gagnées.
     * @return Nombre d'étoiles (0 à 3)
     */
    public int getEtoiles() {
        return partie.getEtoiles();
    }

    /**
     * Retourne le score final.
     * @return Score de la partie
     */
    public int getScore() {
        return partie.getScore();
    }

    /**
     * Retourne le temps restant en secondes.
     * @return Secondes restantes
     */
    public int getTempsRestant() {
        return partie.getSecondesRestantes();
    }

    /**
     * Calcule l'or gagné pendant la partie.
     * Base : 100 par étoile + bonus selon le score.
     * @return Or gagné
     */
    public int getOrGagne() {
        // 30 or par étoile — progression lente, améliorations coûteuses
        return getEtoiles() * 30;
    }

    // Vrai uniquement si l'Hôtel de Ville a été détruit (condition réelle de victoire)
    public boolean hotelDeVilleDetruit() {
        return partie.getHotelDeVille().estDetruit();
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

    /** Démarre les deux threads. Appelé par Main après setAffichage(). */
    public void demarrer() {
        running = true;
        threadJeu.start();
        threadChrono.start();
    }


    /** Retourne la partie en cours. */
    public Partie getPartie() { return partie; }

    /** Retourne le type de troupe sélectionné dans la barre (ou null). */
    public String getTypeSelectionne() { return typeSelectionne; }


    /**
     * Interprète un clic souris selon le contexte :
     * 1. Clic sur un avatar dans la barre → sélectionner le type
     * 2. Type sélectionné + clic sur la carte 
     * 3. Clic libre → test de portée des défenses
     *
     * @param mx  Coordonnée X du clic
     * @param my  Coordonnée Y du clic
     */
    private void handleClick(int mx, int my) {

        // 0. Clic sur le bouton "+" → acheter une troupe avec l'or de combat
        String typeAchat = getAchatFromBar(mx, my);
        if (typeAchat != null) {
            boolean ok = partie.acheterTroupe(typeAchat);
            if (!ok) affichage.setMessageStock("Pas assez d'or ! (" + Partie.getPrixTroupe(typeAchat) + " or)");
            affichage.repaint();
            return;
        }

        // 1. Clic sur un avatar dans la barre → sélectionner le TYPE
        String type = getTypeFromBar(mx, my);
        if (type != null) {
            typeSelectionne = type;
            pekkaActive = null; // annule la sélection d'un Pekka en cours
            affichage.repaint();
            return;
        }

        // 2. Clic sur un Pekka déployé sur la carte → le sélectionner pour le déplacer
        Pekka pekkaClique = getPekkaAt(mx, my);
        if (pekkaClique != null) {
            pekkaActive = pekkaClique;
            typeSelectionne = null; // on ne déploie plus, on déplace
            affichage.repaint();
            return;
        }

        // 3. Un Pekka est sélectionné → lui donner la destination du clic
        if (pekkaActive != null) {
            pekkaActive.setDestination(mx, my);
            pekkaActive = null;
            affichage.repaint();
            return;
        }

        // 4. Type sélectionné + clic sur la carte → déployer UNE troupe
        if (typeSelectionne != null) {
            int stock = getStock(typeSelectionne);

            // Stock épuisé → afficher un message à l'écran et désélectionner
            if (stock <= 0) {
                affichage.setMessageStock("Plus de " + typeSelectionne + " disponible !");
                typeSelectionne = null;
                affichage.repaint();
                return;
            }

            // Déployer une seule troupe à chaque clic
            partie.deployerTroupes(typeSelectionne, 1, mx, my);

            // Si le stock est maintenant vide on affiche le message
            if (getStock(typeSelectionne) <= 0) {
                affichage.setMessageStock("Plus de " + typeSelectionne + " disponible !");
                typeSelectionne = null;
            }

            affichage.repaint();
            return;
        }

        // 5. Clic libre → test de portée des défenses
        List<Defense> enPortee = partie.getDefensesEnPortee(mx, my);
        affichage.setClickInfo(mx, my, enPortee);
        affichage.repaint();
    }

    /**
     * Cherche un Pekka déployé dont la zone (40x40px) contient le point (mx, my).
     * Retourne null si aucun Pekka n'est cliqué.
     */
    private Pekka getPekkaAt(int mx, int my) {
        for (Troupe t : partie.getTroupes()) {
            if (!(t instanceof Pekka)) continue;
            if (t.estMorte() || !t.isDeployee()) continue;
            if (mx >= t.getX() && mx <= t.getX() + 40
             && my >= t.getY() && my <= t.getY() + 40) {
                return (Pekka) t;
            }
        }
        return null;
    }

    /** Retourne le Pekka actuellement sélectionné pour le déplacement (ou null). */
    public Pekka getPekkaActive() { return pekkaActive; }



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

    // Bouton "+" placé à droite de chaque avatar (zone 22x22px)
    private String getAchatFromBar(int mx, int my) {
        int barY      = affichage.getMapPanel().getHeight() - 80;
        String[] noms = {"Pekka", "Sorcier", "Barbare"};

        for (int i = 0; i < 3; i++) {
            int bx = AVATAR_START_X + i * AVATAR_SPACING + AVATAR_SIZE + 3;
            int by = barY;
            if (mx >= bx && mx <= bx + 22 && my >= by && my <= by + 22)
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