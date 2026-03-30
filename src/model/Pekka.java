package model;

public class Pekka extends Troupe {

    // Attributs de destination (propres au Pekka)
    private Integer targetX = null;
    private Integer targetY = null;
	
	// Constructeur de Pekka
    public Pekka(int x, int y) {
        super(x, y, 200, 60, 1);
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
