package model;

import java.util.List;

import static java.awt.geom.Point2D.distance;

public abstract class Troupe { // car c'est une base commune à toutes les troupes
    protected int x;    // position x de la troupe
    protected int y;    // position y de la troupe
    protected int health;   // points de vie de la troupe
    protected int damage;   // dégâts que la troupe inflige
    protected int speed;    // vitesse de déplacement de la troupe
    protected Batiment cible; // batiment cibler

    public Troupe(int x, int y, int health, int damage, int speed) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.damage = damage;
        this.speed = speed;
    }

    // Méthode pour déplacer une troupe vers une position cible (targetX, targetY).
    // Comparer la position actuelle avec la destination et déplacer la troupe
    // progressivement selon sa vitesse sans dépasser la position cible.
    public void moveTo(int targetX, int targetY) {
        if (x < targetX) {
            x += speed;
            if (x > targetX) x = targetX;
        } else if (x > targetX) {
            x -= speed;
            if (x < targetX) x = targetX;
        }

        if (y < targetY) {
            y += speed;
            if (y > targetY) y = targetY;
        } else if (y > targetY) {
            y -= speed;
            if (y < targetY) y = targetY;
        }
    }

    // Vérifier si la troupe est arrivée à la position cible.
    public boolean isArrived(int targetX, int targetY) {

        return x == targetX && y == targetY;
    }

    // Getters
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getHealth() {
        return health;
    }
    
    
    //Méthode pour trouver le Batiment le plus proche des troupes 

    public Batiment trouverPlusProche(List<? extends Batiment> liste) {
        Batiment plusProche = null;
        double minDistance = Double.MAX_VALUE;

        for (Batiment b : liste) {
            if (b == null || b.estDetruit()) continue;

            double dist = distance(this.x, this.y, b.getX(), b.getY());

            if (dist < minDistance) {
                minDistance = dist;
                plusProche = b;
            }
        }

        return plusProche;
    }
    
    
    // methode qui choisi le batiment cible

    public Batiment choisirCible(List<? extends Batiment> defenses,
                                 Batiment chateauDeClan,
                                 List<? extends Batiment> autresBatiments) {
        Batiment cible = trouverPlusProche(defenses);

        if (cible == null && chateauDeClan != null && !chateauDeClan.estDetruit()) {
            cible = chateauDeClan;
        }

        if (cible == null) {
            cible = trouverPlusProche(autresBatiments);
        }

        return cible;
    }
    
 // Définit la cible de la troupe
    public void setCible(Batiment cible) {
        this.cible = cible;
    }

    // Retourne la cible actuelle
    public Batiment getCible() {
        return cible;
    }
 // La troupe agit seulement si une cible a été choisie
    public void agirManuellement() {
        if (cible == null || cible.estDetruit()) {
            return;
        }

        if (!isArrived(cible.getX(), cible.getY())) {
            moveTo(cible.getX(), cible.getY());
        } else {
            cible.prendreDegats(damage);
        }
    }
}