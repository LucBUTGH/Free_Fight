package model;

import java.util.List;
import static java.awt.geom.Point2D.distance;

/**
 * Classe abstraite représentant une troupe d'attaque contrôlée par le joueur.
 * 
 * Une troupe est une unité mobile qui se déplace sur la carte, attaque les
 * bâtiments ennemis et peut mourir si elle reçoit trop de dégâts.
 * 
 * Cette classe est abstraite car on ne crée jamais une "Troupe" générique :
 * on crée toujours un Barbare, un Sorcier ou un Pekka. Chaque sous-classe
 * peut surcharger le comportement (ex : Pekka avec déplacement manuel).
 * 
 * Le cycle de vie d'une troupe :
 * 1. Créée dans le stock (deployee = false)
 * 2. Déployée par le joueur sur la carte (deployee = true)
 * 3. Se déplace vers sa cible automatiquement
 * 4. Attaque quand elle arrive sur la cible
 * 5. Meurt si ses PV tombent à 0 (mortVisuelle = true)
 * 6. Animation de mort pendant DUREE_MORT ticks, puis supprimée
 */
public abstract class Troupe {


    protected int x;          // Position X actuelle sur la carte
    protected int y;          // Position Y actuelle sur la carte
    protected int health;     // Points de vie actuels
    protected int damage;     // Dégâts infligés à chaque attaque
    protected int speed;      // Vitesse de déplacement en pixels par tick
    protected Batiment cible; // Bâtiment actuellement ciblé par la troupe


    // Mémorisé à la création pour calculer le ratio de la barre de vie.
    // final car les PV max ne changent jamais pendant la partie.
    private final int healthMax;


    // Une troupe existe dans le stock avant d'être déployée.
    // Tant que deployee est false, elle n'est pas dessinée sur la carte
    // et n'est pas mise à jour dans la boucle de jeu.
    private boolean deployee = false;


    // Quand une troupe meurt, on ne la supprime pas immédiatement.
    // On affiche une croix rouge pendant DUREE_MORT ticks, puis on la retire.
    private boolean mortVisuelle = false; // true dès que les PV tombent à 0
    private int ticksMort = 0;            // compteur de ticks depuis la mort
    private static final int DUREE_MORT = 8; // durée de l'animation en ticks


    /**
     * Initialise une troupe avec ses caractéristiques de base.
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
        this.healthMax = health; // on mémorise les PV de départ comme maximum
        this.damage    = damage;
        this.speed     = speed;
    }


    /**
     * Déplace la troupe d'un pas vers la position cible (targetX, targetY).
     * 
     * On avance de 'speed' pixels par tick sur chaque axe séparément.
     * Les conditions min/max évitent de dépasser la destination
     * (sans ça, la troupe oscillerait autour de la cible).
     * 
     * Exemple : si x=10, targetX=15, speed=3 → x devient 13
     *           si x=13, targetX=15, speed=3 → x devient 15 (pas 16)
     * 
     * @param targetX  Coordonnée X de destination
     * @param targetY  Coordonnée Y de destination
     */
    public void moveTo(int targetX, int targetY) {
        // Déplacement horizontal
        if (x < targetX) {
            x += speed;
            if (x > targetX) x = targetX; // on ne dépasse pas
        } else if (x > targetX) {
            x -= speed;
            if (x < targetX) x = targetX;
        }

        // Déplacement vertical
        if (y < targetY) {
            y += speed;
            if (y > targetY) y = targetY;
        } else if (y > targetY) {
            y -= speed;
            if (y < targetY) y = targetY;
        }
    }

    /**
     * Vérifie si la troupe est exactement arrivée à la position cible.
     * 
     * On compare x == targetX ET y == targetY.
     * Grâce au clamp dans moveTo(), la troupe ne peut pas dépasser,
     * donc cette égalité exacte est toujours atteignable.
     * 
     * @return true si la troupe est à la position exacte de la cible
     */
    public boolean isArrived(int targetX, int targetY) {
        return x == targetX && y == targetY;
    }


    public int getX()          { return x;          }
    public int getY()          { return y;           }
    public int getHealth()     { return health;      }
    public int getHealthMax()  { return healthMax;   }
    public boolean isDeployee()      { return deployee;      }
    public boolean isMortVisuelle()  { return mortVisuelle;  }
    public Batiment getCible()       { return cible;         }


    /**
     * Déploie la troupe à une position donnée sur la carte.
     * 
     * Appelé par Partie.deployerTroupes() quand le joueur clique
     * sur la carte après avoir sélectionné un type de troupe.
     * Une fois deployee = true, la troupe apparaît à l'écran
     * et commence à être mise à jour dans la boucle de jeu.
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
     * On utilise la distance euclidienne (Point2D.distance) pour comparer
     * les distances. On parcourt tous les bâtiments et on garde le plus proche.
     * 
     * @param liste  Liste de bâtiments à parcourir
     * @return       Le bâtiment le plus proche, ou null si tous sont détruits
     */
    public Batiment trouverPlusProche(List<? extends Batiment> liste) {
        Batiment plusProche = null;
        double minDistance = Double.MAX_VALUE; // valeur initiale très grande

        for (Batiment b : liste) {
            if (b == null || b.estDetruit()) continue; // on ignore les bâtiments détruits

            double dist = distance(this.x, this.y, b.getX(), b.getY());

            if (dist < minDistance) {
                minDistance = dist;
                plusProche  = b;
            }
        }

        return plusProche;
    }

    /**
     * Choisit automatiquement la meilleure cible pour la troupe.
     * 
     * Priorité (comme dans Clash of Clans) :
     * 1. La défense la plus proche (Canon, Tour Archer, Mortier...)
     * 2. L'hôtel de ville si aucune défense n'est disponible
     * 3. Les autres bâtiments normaux en dernier recours
     * 
     * @param defenses         Liste des défenses du village
     * @param chateauDeClan    Bâtiment principal (hôtel de ville)
     * @param autresBatiments  Autres bâtiments normaux
     * @return                 Le bâtiment à attaquer, ou null si tout est détruit
     */
    public Batiment choisirCible(List<? extends Batiment> defenses,
                                 Batiment chateauDeClan,
                                 List<? extends Batiment> autresBatiments) {
        // 1. Priorité aux défenses
        Batiment cible = trouverPlusProche(defenses);

        // 2. Si aucune défense disponible, cibler l'hôtel de ville
        if (cible == null && chateauDeClan != null && !chateauDeClan.estDetruit()) {
            cible = chateauDeClan;
        }

        // 3. En dernier recours, cibler les autres bâtiments
        if (cible == null) {
            cible = trouverPlusProche(autresBatiments);
        }

        return cible;
    }


    /** Permet d'assigner manuellement une cible à la troupe. */
    public void setCible(Batiment cible) {
        this.cible = cible;
    }


    /**
     * Fait agir la troupe pendant un tick de la boucle de jeu.
     * 
     * Si la troupe a une cible valide :
     * - Elle se déplace vers elle si elle n'est pas encore arrivée
     * - Elle l'attaque si elle est sur place
     * 
     * Si la cible est nulle ou détruite, on ne fait rien
     * (Partie.update() se chargera de réassigner une nouvelle cible).
     * 
     * Cette méthode peut être surchargée dans les sous-classes
     * (ex : Pekka qui a un comportement de déplacement manuel en plus).
     */
    public void agirManuellement() {
        // Rien à faire si pas de cible ou cible déjà détruite
        if (cible == null || cible.estDetruit()) return;

        if (!isArrived(cible.getX(), cible.getY())) {
            moveTo(cible.getX(), cible.getY()); // on avance vers la cible
        } else {
            cible.prendreDegats(damage); // on attaque la cible
        }
    }


    /**
     * Applique des dégâts à la troupe.
     * 
     * Si les PV tombent à 0 ou moins, on déclenche l'animation de mort.
     * La troupe n'est pas supprimée immédiatement — elle reste dans la liste
     * jusqu'à ce que avancerMort() indique que l'animation est terminée.
     * 
     * @param degats  Nombre de points de dégâts reçus
     */
    public void prendreDegats(int degats) {
        health -= degats;
        if (health < 0) health = 0; // plancher à 0
        if (health == 0) mortVisuelle = true; // déclenche l'animation
    }

    /**
     * Avance l'animation de mort d'un tick.
     * 
     * Appelé dans Partie.update() via removeIf(Troupe::avancerMort).
     * Retourne true quand l'animation est terminée → la troupe est supprimée.
     * Retourne false si la troupe est encore vivante ou en cours d'animation.
     * 
     * @return true si la troupe doit être supprimée de la liste
     */
    public boolean avancerMort() {
        if (!mortVisuelle) return false; // pas encore morte
        ticksMort++;
        return ticksMort >= DUREE_MORT; // suppression après DUREE_MORT ticks
    }

    /**
     * Indique si la troupe est morte (PV à 0).
     * Utilisé par les défenses pour ignorer les cibles déjà mortes.
     * 
     * @return true si health == 0
     */
    public boolean estMorte() {
        return health <= 0;
    }
}