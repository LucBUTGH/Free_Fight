package model;

import java.util.List;
import static java.awt.geom.Point2D.distance;

/**
 * Classe abstraite représentant une troupe d'attaque ou de défense.
 *
 * Une troupe est une unité mobile qui se déplace sur la carte, attaque les
 * bâtiments ennemis ou d'autres troupes, et peut mourir si elle reçoit
 * trop de dégâts.
 *
 * Cette classe est abstraite car on ne crée jamais une "Troupe" générique :
 * on crée toujours un Barbare, un Sorcier ou un Pekka.
 *
 * Cycle de vie d'une troupe :
 * 1. Créée dans le stock (deployee = false)
 * 2. Déployée par le joueur sur la carte (deployee = true)
 * 3. Se déplace et attaque automatiquement
 * 4. Meurt si ses PV tombent à 0 (mortVisuelle = true)
 * 5. Animation de mort pendant DUREE_MORT ticks, puis supprimée
 */
public abstract class Troupe {


    protected int x;          // Position X actuelle sur la carte
    protected int y;          // Position Y actuelle sur la carte
    protected int health;     // Points de vie actuels
    protected int damage;     // Dégâts infligés à chaque attaque
    protected int speed;      // Vitesse de déplacement en pixels par tick

    // Bâtiment actuellement ciblé par la troupe
    protected Batiment cible;

    // Troupe ennemie ciblée (combat troupe vs troupe)
    // Prioritaire sur la cible bâtiment si une troupe ennemie est présente
    protected Troupe cibleTroupe;

    // Camp de la troupe : JOUEUR ou ENNEMI
    // Empêche les troupes du même camp de s'attaquer entre elles
    private Camp camp;


    // Mémorisé à la création pour calculer le ratio de la barre de vie.
    // final car les PV max ne changent jamais pendant la partie.
    private final int healthMax;


    // Tant que deployee est false, la troupe n'est pas dessinée sur la carte
    // et n'est pas mise à jour dans la boucle de jeu.
    private boolean deployee = false;


    // Quand une troupe meurt, on affiche une croix rouge pendant DUREE_MORT
    // ticks avant de la supprimer de la liste.
    private boolean mortVisuelle = false; // true dès que les PV tombent à 0
    private int ticksMort = 0;            // compteur de ticks depuis la mort
    private static final int DUREE_MORT = 8; // durée de l'animation en ticks


    /**
     * Initialise une troupe avec ses caractéristiques de base.
     * Par défaut, toute troupe est du camp JOUEUR.
     *
     * @param x       Position X de départ
     * @param y       Position Y de départ
     * @param health  Points de vie initiaux (aussi utilisés comme maximum)
     * @param damage  Dégâts infligés par attaque
     * @param speed   Vitesse de déplacement (pixels par tick)
     */
    public Troupe(int x, int y, int health, int damage, int speed) {
        this.x         = x;
        this.y         = y;
        this.health    = health;
        this.healthMax = health;
        this.damage    = damage;
        this.speed     = speed;
        this.camp      = Camp.JOUEUR; // par défaut toute troupe est côté joueur
    }


    /**
     * Déplace la troupe d'un pas vers la position (targetX, targetY).
     *
     * On avance de 'speed' pixels par tick sur chaque axe.
     * Le clamp évite de dépasser la destination.
     *
     * Exemple : x=10, targetX=15, speed=3 → x devient 13
     *           x=13, targetX=15, speed=3 → x devient 15 (pas 16)
     *
     * @param targetX  Coordonnée X de destination
     * @param targetY  Coordonnée Y de destination
     */
    public void moveTo(int targetX, int targetY) {
        if (x < targetX) { x += speed; if (x > targetX) x = targetX; }
        else if (x > targetX) { x -= speed; if (x < targetX) x = targetX; }

        if (y < targetY) { y += speed; if (y > targetY) y = targetY; }
        else if (y > targetY) { y -= speed; if (y < targetY) y = targetY; }
    }

    /**
     * Vérifie si la troupe est exactement à la position cible.
     * Toujours atteignable grâce au clamp dans moveTo().
     *
     * @return true si la troupe est arrivée
     */
    public boolean isArrived(int targetX, int targetY) {
        return x == targetX && y == targetY;
    }


    public int getX()               { return x;            }
    public int getY()               { return y;            }
    public int getHealth()          { return health;       }
    public int getHealthMax()       { return healthMax;    }
    public boolean isDeployee()     { return deployee;     }
    public boolean isMortVisuelle() { return mortVisuelle; }
    public Batiment getCible()      { return cible;        }
    public Troupe getCibleTroupe()  { return cibleTroupe;  }
    public Camp getCamp()           { return camp;         }


    /** Définit le bâtiment cible à attaquer. */
    public void setCible(Batiment cible)     { this.cible       = cible; }

    /** Définit la troupe ennemie à attaquer en priorité. */
    public void setCibleTroupe(Troupe t)     { this.cibleTroupe = t;     }

    /** Définit le camp de la troupe (JOUEUR ou ENNEMI). */
    public void setCamp(Camp camp)           { this.camp        = camp;  }


    /**
     * Déploie la troupe à une position sur la carte.
     * Une fois deployee = true, elle apparaît et est mise à jour.
     *
     * @param x  Position X de déploiement
     * @param y  Position Y de déploiement
     */
    public void deployer(int x, int y) {
        this.x        = x;
        this.y        = y;
        this.deployee = true;
    }


    /**
     * Trouve le bâtiment non détruit le plus proche parmi une liste.
     *
     * @param liste  Liste de bâtiments à parcourir
     * @return       Le bâtiment le plus proche, ou null si tous sont détruits
     */
    public Batiment trouverPlusProche(List<? extends Batiment> liste) {
        Batiment plusProche = null;
        double minDistance  = Double.MAX_VALUE;

        for (Batiment b : liste) {
            if (b == null || b.estDetruit()) continue;
            double dist = distance(this.x, this.y, b.getX(), b.getY());
            if (dist < minDistance) {
                minDistance = dist;
                plusProche  = b;
            }
        }
        return plusProche;
    }

    /**
     * Choisit automatiquement la meilleure cible bâtiment.
     *
     * Priorité :
     * 1. La défense la plus proche
     * 2. L'hôtel de ville si aucune défense n'est disponible
     * 3. Les autres bâtiments en dernier recours
     *
     * @param defenses         Liste des défenses du village
     * @param chateauDeClan    Bâtiment principal
     * @param autresBatiments  Autres bâtiments normaux
     * @return                 Le bâtiment à attaquer, ou null si tout est détruit
     */
    public Batiment choisirCible(List<? extends Batiment> defenses,
                                 Batiment chateau,
                                 List<? extends Batiment> autresBatiments) {
        // 1. Priorité aux défenses
        Batiment cible = trouverPlusProche(defenses);

        // 2. Hôtel de ville si aucune défense
        if (cible == null && chateau != null && !chateau.estDetruit()) {
            cible = chateau;
        }

        // 3. Autres bâtiments en dernier recours
        if (cible == null) {
            cible = trouverPlusProche(autresBatiments);
        }

        return cible;
    }


    /**
     * Fait agir la troupe pendant un tick.
     *
     * Priorité :
     * 1. Si une troupe ennemie est ciblée et vivante → se déplacer et l'attaquer
     * 2. Sinon → attaquer le bâtiment cible
     *
     * Peut être surchargée dans les sous-classes (ex : Pekka).
     */
    public void agirManuellement() {

        // 1. Priorité : combat contre une troupe ennemie
        if (cibleTroupe != null && !cibleTroupe.estMorte()) {
            if (!isArrived(cibleTroupe.getX(), cibleTroupe.getY())) {
                moveTo(cibleTroupe.getX(), cibleTroupe.getY());
            } else {
                cibleTroupe.prendreDegats(damage);
            }
            return;
        }

        // 2. Combat contre un bâtiment
        if (cible == null || cible.estDetruit()) return;

        if (!isArrived(cible.getX(), cible.getY())) {
            moveTo(cible.getX(), cible.getY());
        } else {
            cible.prendreDegats(damage);
        }
    }


    /**
     * Applique des dégâts à la troupe.
     * Déclenche l'animation de mort si les PV tombent à 0.
     *
     * @param degats  Points de dégâts reçus
     */
    public void prendreDegats(int degats) {
        health -= degats;
        if (health < 0) health = 0;
        if (health == 0) mortVisuelle = true; // déclenche l'animation
    }

    /**
     * Avance l'animation de mort d'un tick.
     * Retourne true quand l'animation est terminée → la troupe est supprimée.
     *
     * Appelé via removeIf(Troupe::avancerMort) dans Partie.update().
     *
     * @return true si la troupe doit être supprimée
     */
    public boolean avancerMort() {
        if (!mortVisuelle) return false;
        ticksMort++;
        return ticksMort >= DUREE_MORT;
    }

    /**
     * Retourne true si la troupe n'a plus de PV.
     * Utilisé par les défenses pour ignorer les cibles déjà mortes.
     */
    public boolean estMorte() {
        return health <= 0;
    }
}