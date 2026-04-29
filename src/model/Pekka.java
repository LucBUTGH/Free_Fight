package model;

public class Pekka extends Troupe {

    // Attributs de destination (propres au Pekka)
    private Integer targetX = null;
    private Integer targetY = null;

    // Stats de base (niveau 1) : 200 PV, 60 dégâts, vitesse 1.
    // Chaque niveau ajoute : +50 PV, +15 dégâts, et +1 vitesse tous les 2 niveaux.
    public Pekka(int x, int y, int niveau) {
        super(x, y,
                200 + 50 * (niveau - 1),
                60 + 15 * (niveau - 1),
                1 + (niveau - 1) / 2,
                niveau);
    }

    public Pekka(int x, int y) {
        this(x, y, 1);
    }

    // Définir une destination
    public void setDestination(int x, int y) {
        this.targetX = x;
        this.targetY = y;

        // Enlever la cible automatique
        this.setCible(null);
    }

    // Override du comportement
    @Override
    public void agirManuellement() {

        // Déplacement manuel UNIQUEMENT pour le Pekka
        if (targetX != null && targetY != null) {
            if (!isArrived(targetX, targetY)) {
                moveTo(targetX, targetY);
            } else {
                // Arrivé
                targetX = null;
                targetY = null;
            }
            return;
        }

        // Sinon comportement normal
        super.agirManuellement();
    }
}
