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

    // ── Attributs ────────────────────────────────────────────────────────────

    private int portee;        // Rayon d'attaque en pixels
    private int degats;        // Dégâts infligés à chaque tir
    private int cadenceTir;    // Nombre de ticks entre chaque tir (plus c'est grand, plus c'est lent)
    private int tickDepuisTir; // Compteur interne : nombre de ticks écoulés depuis le dernier tir

    // ── Constructeur ─────────────────────────────────────────────────────────

    /**
     * Crée une défense avec toutes ses caractéristiques.
     * 
     * Exemple d'utilisation :
     * new Defense("Canon", 200, 150, 30, 15, 200, 200)
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
        super(nom, pv, x, y); // appel du constructeur parent (Batiment)
        this.portee        = portee;
        this.degats        = degats;
        this.cadenceTir    = cadenceTir;
        this.tickDepuisTir = 0; // au départ, la défense n'a pas encore tiré
    }


    /** Retourne le rayon de portée de la défense. */
    public int getPortee() { return portee; }


    /**
     * Vérifie si un point (cx, cy) est dans le rayon de portée de cette défense.
     * 
     * On utilise la formule de distance euclidienne entre deux points :
     * d = sqrt((cx - x)² + (cy - y)²)
     * Si cette distance est inférieure ou égale à la portée, le point est à portée.
     * 
     * Utilisé à la fois pour le test visuel (clic sur la carte)
     * et pour détecter les troupes ennemies à attaquer.
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
     * Principe : à chaque appel (chaque tick de la boucle de jeu),
     * on incrémente le compteur. Quand il atteint la cadence définie,
     * la défense cherche une cible à portée et tire dessus.
     * 
     * Si la défense est détruite, elle ne fait plus rien.
     * Si aucune troupe n'est à portée, elle attend sans tirer.
     * 
     * @param troupes  Liste de toutes les troupes déployées sur la carte
     */
    public void agir(List<Troupe> troupes) {
        // Une défense détruite ne peut plus tirer
        if (estDetruit()) return;

        tickDepuisTir++;

        // On n'a pas encore atteint la cadence de tir → on attend
        if (tickDepuisTir < cadenceTir) return;

        // On cherche une troupe ennemie à portée
        Troupe cible = trouverCibleDansPortee(troupes);

        // Aucune cible à portée → on attend sans remettre le compteur à zéro
        if (cible == null) return;

        // Tir ! On inflige les dégâts et on remet le compteur à zéro
        cible.prendreDegats(degats);
        tickDepuisTir = 0;
    }

    /**
     * Cherche la première troupe encore en vie dans le rayon de portée.
     * 
     * On parcourt la liste des troupes et on retourne la première
     * qui est vivante et dans la portée. On pourrait améliorer cela
     * en prenant la troupe la plus proche ou celle avec le moins de PV,
     * mais pour l'instant on prend simplement la première trouvée.
     * 
     * @param troupes  Liste de toutes les troupes déployées
     * @return         La première troupe à portée, ou null si aucune
     */
    private Troupe trouverCibleDansPortee(List<Troupe> troupes) {
        for (Troupe t : troupes) {
            if (t.estMorte()) continue; // on ignore les troupes déjà mortes
            if (estAPortee(t.getX(), t.getY())) return t;
        }
        return null; // aucune troupe à portée
    }


    /**
     * Indique si la défense vient de tirer lors du tick courant.
     * 
     * Utilisé dans Affichage pour faire clignoter la défense en rouge
     * juste après un tir. tickDepuisTir vaut 0 uniquement dans le tick
     * où le tir vient d'avoir lieu (remis à zéro dans agir()).
     * 
     * @return true si la défense a tiré ce tick
     */
    public boolean vientDeTirer() {
        return tickDepuisTir == 0;
    }


    @Override
    public String toString() {
        return super.toString() + " | Portée: " + portee + " | Dégâts: " + degats;
    }
}