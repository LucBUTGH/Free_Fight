package model;

/**
 * Troupe lourde contrôlable manuellement par le joueur.
 *
 * Le Pekka peut recevoir une destination de déplacement libre via clic,
 * avant de reprendre son comportement automatique.
 *
 * Statistiques : 200 PV, 60 dégâts, vitesse 1.
 */
public class Pekka extends Troupe {

    // Destination de déplacement manuel définie par le joueur.
    // Vaut 0,0 quand aucune destination n'est active.
    private int targetX = 0;
    private int targetY = 0;

    public Pekka(int x, int y) {
        super(x, y, 200, 60, 1);
    }

    /**
     * Définit une destination de déplacement libre.
     * Annule la cible bâtiment le temps d'atteindre la destination.
     *
     * @param x  Coordonnée X de destination
     * @param y  Coordonnée Y de destination
     */
    public void setDestination(int x, int y) {
        this.targetX = x;
        this.targetY = y;
        this.setCible(null); // annule la cible bâtiment pendant le déplacement
    }

    /**
     * Comportement du Pekka par tick.
     *
     * Si une destination manuelle est définie → se déplacer vers elle
     * Sinon → comportement standard (attaque bâtiment ou troupe ennemie)
     */
    @Override
    public void agirManuellement() {
        if (targetX != 0 && targetY != 0) {
            if (!isArrived(targetX, targetY)) {
                moveTo(targetX, targetY);
                return;
            } else {
                // Arrivé à destination → on efface la destination
                targetX = 0;
                targetY = 0;
            }
        }
        // Comportement normal hérité
        super.agirManuellement();
    }
}