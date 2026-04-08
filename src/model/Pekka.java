package model;

public class Pekka extends Troupe {

    // Attributs de destination (propres au Pekka)
    private int targetX = 0;
    private int targetY = 0;
	
	// Constructeur de Pekka
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
    	// Si une destination est définie 
    	if (targetX != 0 && targetY != 0) {
    		// Si on est tjr pas arrivés à une cible, on bouge
    		if(!isArrived(targetX, targetY)){
    			moveTo(targetX, targetY);
    			return; 
    		}else {
    			// Arrivée on reset la destination
    			targetX = 0;
    			targetY = 0;
    		}
    	}

        // Ensuite comportement normal (Attaquer les cibles)
        super.agirManuellement();
    }
}
