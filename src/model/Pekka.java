package model;

public class Pekka extends Troupe {

    // Attributs de destination (propres au Pekka)
    private int targetX = 0;
    private int targetY = 0;
	
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
