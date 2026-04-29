package controller;

import model.*;
import view.Affichage;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Contrôleur principal du jeu.
 *
 * <p>Responsabilités :</p>
 * <ul>
 *   <li>Posséder la {@link Partie} (source de vérité de l'état du jeu).</li>
 *   <li>Interpréter les événements souris et les traduire en actions sur le modèle.</li>
 *   <li>Maintenir le type de troupe sélectionné dans la barre du bas.</li>
 * </ul>
 *
 * <p>Ce contrôleur ne gère aucun thread. La boucle de jeu et le chronomètre
 * sont délégués à {@link GameLoop}, instancié dans le constructeur.</p>
 */
public class GameController extends Thread {

    private final Partie   partie;
    private final GameLoop gameLoop;

    private Affichage affichage;

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
     * Initialise le contrôleur avec une partie existante et prépare la boucle de jeu.
     * Aucun thread n'est démarré ici — appeler {@link #demarrer()} après {@link #setAffichage}.
     *
     * @param partie  La partie à contrôler.
     */
    public GameController(Partie partie) {
        this.partie   = partie;
        this.gameLoop = new GameLoop(partie);
    }

    /**
     * Enregistre le callback exécuté sur le thread graphique à la fin de la partie.
     * Délégué à {@link GameLoop} — doit être appelé avant {@link #demarrer()}.
     *
     * @param callback  Action à déclencher (ex. : afficher l'écran de fin).
     */
    public void setFinPartieCallback(Runnable callback) {
        gameLoop.setFinPartieCallback(callback);
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
     * Calcule l'or gagné à l'issue de la partie : 30 or par étoile obtenue.
     *
     * @return Or à créditer dans la sauvegarde.
     */
    public int getOrGagne() {
        // 30 or par étoile — progression lente, améliorations coûteuses
        return getEtoiles() * 30;
    }

    /**
     * Indique si l'Hôtel de Ville a été détruit.
     * C'est la condition requise pour valider une victoire et débloquer le niveau suivant.
     *
     * @return {@code true} si l'Hôtel de Ville est à 0 PV.
     */
    public boolean hotelDeVilleDetruit() {
        return partie.getHotelDeVille().estDetruit();
    }


    /**
     * Lie la vue au contrôleur, la transmet à {@link GameLoop} et installe
     * le {@link java.awt.event.MouseListener} sur le panneau de la carte.
     * Doit être appelé avant {@link #demarrer()}.
     *
     * @param affichage  La vue principale du jeu.
     */
    public void setAffichage(Affichage affichage) {
        this.affichage = affichage;
        gameLoop.setAffichage(affichage);
        affichage.getMapPanel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    // Clic droit → déplacer tous les Pekkas déployés vers ce point
                    handleRightClick(e.getX(), e.getY());
                } else {
                    handleClick(e.getX(), e.getY());
                }
            }
        });
    }

    /**
     * Démarre la boucle de jeu en déléguant à {@link GameLoop}.
     * Doit être appelé après {@link #setAffichage}.
     */
    public void demarrer() {
        gameLoop.demarrer();
    }


    /** Retourne la partie en cours. */
    public Partie getPartie() { return partie; }

    /** Retourne le type de troupe sélectionné dans la barre (ou null). */
    public String getTypeSelectionne() { return typeSelectionne; }


    /**
     * Gère le clic gauche de la souris sur la carte.
     *
     * Cette méthode est le point d'entrée principal pour toutes les interactions
     * du joueur. Elle traite les actions dans un ordre de priorité fixe :
     *
     *   0. Clic sur le bouton "+" d'un avatar → achète une troupe avec l'or de combat.
     *      Si l'or est insuffisant, affiche un message d'erreur.
     *
     *   1. Clic sur un avatar dans la barre du bas → sélectionne le type de troupe
     *      à déployer (Pekka, Sorcier ou Barbare). Annule toute sélection de Pekka en cours.
     *
     *   2. Clic sur un Pekka déjà déployé sur la carte → sélectionne ce Pekka
     *      pour pouvoir lui donner un ordre de déplacement au prochain clic.
     *
     *   3. Un Pekka est sélectionné (étape 2) + clic sur la carte → envoie ce Pekka
     *      vers la position cliquée, puis désélectionne.
     *
     *   4. Un type de troupe est sélectionné (étape 1) + clic sur la carte → déploie
     *      une troupe de ce type à l'endroit cliqué. Si le stock est épuisé,
     *      affiche un message et annule la sélection.
     *
     *   5. Aucune sélection active → affiche les défenses ennemies à portée
     *      du point cliqué (mode inspection).
     *
     * @param mx  Coordonnée X du clic gauche (en pixels sur la carte)
     * @param my  Coordonnée Y du clic gauche (en pixels sur la carte)
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
     * Cherche un Pekka déployé et vivant dont la zone (40×40 px) contient le point (mx, my).
     *
     * @param mx  Coordonnée X du clic.
     * @param my  Coordonnée Y du clic.
     * @return    Le Pekka cliqué, ou {@code null} si aucun ne correspond.
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

    /**
     * Clic droit : envoie tous les Pekkas déployés du joueur vers (mx, my).
     */
    /**
     * Gère le clic droit de la souris sur la carte.
     *
     * Quand le joueur fait un clic droit à la position (mx, my),
     * cette méthode parcourt toutes les troupes de la partie et
     * donne un ordre de déplacement à chaque Pekka du joueur
     * qui est déjà déployé et encore en vie.
     *
     * Le Pekka se dirigera alors vers le point cliqué comme destination.
     * Si au moins un Pekka a reçu l'ordre, la carte est redessinée
     * pour refléter immédiatement le changement.
     *
     * @param mx  Coordonnée X du clic droit (en pixels sur la carte)
     * @param my  Coordonnée Y du clic droit (en pixels sur la carte)
     */
    private void handleRightClick(int mx, int my) {
        boolean moved = false;
        for (Troupe t : partie.getTroupes()) {
            if (t instanceof Pekka && t.getCamp() == Camp.JOUEUR
                    && t.isDeployee() && !t.estMorte()) {
                ((Pekka) t).setDestination(mx, my);
                moved = true;
            }
        }
        if (moved) affichage.repaint();
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

   
    /**
     * Détecte si le clic tombe sur le bouton "+" d'achat placé à droite de chaque avatar.
     *
     * Chaque avatar dans la barre du bas possède un petit bouton "+" de 22×22 pixels,
     * positionné juste à droite de l'image de l'avatar (décalage : AVATAR_SIZE + 3 px).
     * Cette méthode vérifie si les coordonnées du clic (mx, my) se trouvent
     * dans cette zone pour l'un des 3 types de troupes.
     *
     * Si le clic correspond, le joueur souhaite acheter une troupe de ce type
     * avec l'or de combat (distinct de l'or de sauvegarde).
     *
     * @param mx  Coordonnée X du clic (en pixels sur la carte)
     * @param my  Coordonnée Y du clic (en pixels sur la carte)
     * @return    "Pekka", "Sorcier" ou "Barbare" si le "+" correspondant est cliqué, sinon null
     */
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