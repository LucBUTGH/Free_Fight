package model;

import java.util.List;

/**
 * Représente une défense du village ennemi (Canon, Tour Archer, Mortier...).
 *
 * Defense hérite de Batiment : elle a donc un nom, des PV et une position.
 * En plus, elle possède une portée d'attaque et peut tirer sur les troupes
 * ennemies qui entrent dans son rayon.
 *
 * Le système de tir est basé sur une cadence : la défense ne tire pas
 * à chaque tick, mais une fois tous les N ticks (cadenceTir).
 * Cela simule un temps de rechargement entre chaque tir.
 */
public class Defense extends Batiment {


    private int portee;        // Rayon d'attaque en pixels
    private int degats;        // Dégâts infligés à chaque tir
    private int cadenceTir;    // Nombre de ticks entre chaque tir (plus c'est grand = plus lent)
    private int tickDepuisTir; // Compteur interne : ticks écoulés depuis le dernier tir


    /**
     * Crée une défense avec toutes ses caractéristiques.
     *
     * Exemple : new Defense("Canon", 200, 150, 30, 15, 200, 200)
     * → Canon avec 200 PV, portée 150px, 30 dégâts, tire toutes les 15 ticks, en (200, 200)
     *
     * @param nom         Nom affiché à l'écran
     * @param pv          Points de vie
     * @param portee      Rayon de détection et d'attaque (en pixels)
     * @param degats      Dégâts infligés par tir
     * @param cadenceTir  Nombre de ticks entre deux tirs (temps de rechargement)
     * @param x           Position X sur la carte
     * @param y           Position Y sur la carte
     */
    public Defense(String nom, int pv, int portee, int degats, int cadenceTir, int x, int y) {
        super(nom, pv, x, y);
        this.portee        = portee;
        this.degats        = degats;
        this.cadenceTir    = cadenceTir;
        this.tickDepuisTir = 0; // au départ, la défense n'a pas encore tiré
    }


    /** Retourne le rayon de portée de la défense. */
    public int getPortee() { return portee; }


    /**
     * Vérifie si un point (cx, cy) est dans le rayon de portée.
     *
     * Formule : d = sqrt((cx-x)² + (cy-y)²)
     * Si d <= portee → le point est à portée.
     *
     * @param cx  Coordonnée X du point à tester
     * @param cy  Coordonnée Y du point à tester
     * @return    true si le point est dans le rayon d'attaque
     */
    public boolean estAPortee(int cx, int cy) {
        double distance = Math.sqrt(Math.pow(cx - x, 2) + Math.pow(cy - y, 2));
        return distance <= portee;
    }


    /**
     * Fait agir la défense pendant un tick.
     *
     * A chaque appel on incrémente le compteur.
     * Quand il atteint la cadence définie, la défense cherche une cible
     * à portée et tire dessus. Les défenses ne tirent que sur le camp JOUEUR.
     *
     * @param troupes  Liste de toutes les troupes sur la carte
     */
    public void agir(List<Troupe> troupes) {
        if (estDetruit()) return; // une défense détruite ne tire plus

        tickDepuisTir++;

        // Pas encore le moment de tirer
        if (tickDepuisTir < cadenceTir) return;

        // Cherche une troupe du joueur à portée
        Troupe cible = trouverCibleDansPortee(troupes);
        if (cible == null) return; // aucune cible disponible

        // Tir et remise à zéro du compteur
        cible.prendreDegats(degats);
        tickDepuisTir = 0;
    }

    /**
     * Cherche la première troupe du camp JOUEUR encore en vie dans la portée.
     *
     * @param troupes  Liste de toutes les troupes déployées
     * @return         La première troupe à portée, ou null si aucune
     */
    private Troupe trouverCibleDansPortee(List<Troupe> troupes) {
        for (Troupe t : troupes) {
            if (t.estMorte()) continue;                 // ignore les troupes mortes
            if (t.getCamp() != Camp.JOUEUR) continue;   // les défenses ne tirent que sur le joueur
            if (estAPortee(t.getX(), t.getY())) return t;
        }
        return null;
    }


    /**
     * Retourne true si la défense vient de tirer ce tick.
     * Utilisé dans Affichage pour faire clignoter la défense en rouge.
     * tickDepuisTir vaut 0 uniquement juste après un tir.
     */
    public boolean vientDeTirer() {
        return tickDepuisTir == 0;
    }

    @Override
    public String toString() {
        return super.toString() + " | Portée: " + portee + " | Dégâts: " + degats;
    }
}























