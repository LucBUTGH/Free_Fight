package model;

import java.util.ArrayList;
import java.util.List;

/* Classe du chateau de clan : défense particulière qui sort des 
troupes ennemies lorsque nos troupes sont à portée du chateau */

public class Chateau extends Defense {
	// indiquer si le chateau a déjà généré des troupes
    private boolean aSpawn = false;
    
    /* Constructeur du chateau 
    * @param nom Nom du bâtiment
    * @param pv Points de vie (non utilisé ici car fixé à 60 pour équitablité du combat)
    * @param portee Rayon de détection des troupes ennemies
    * @param x Position en X sur la carte
    * @param y Position en Y sur la carte
    */
    public Chateau(String nom, int pv, int portee, int x, int y) {
        super(nom, 60, portee, x, y);
    }

    /*
     * Génère les troupes de défense du château.
     * 
     * Les troupes apparaissent autour du château avec un léger décalage
     * pour éviter qu'elles ne soient superposées.
     * 
     * retourne Liste des nouvelles troupes générées
     */
    public List<Troupe> spawnDefense() {
    	// Liste qui contiendra les troupes ennemies 
        List<Troupe> nouvelles = new ArrayList<>();

        // Création des troupes défensives autour du château
        
        // Génération des troupes autour du chateau
        Barbare b1 = new Barbare(getX() + 30, getY());
        Pekka   b2 = new Pekka(getX() - 30,   getY());
        Sorcier b3 = new Sorcier(getX(),       getY() + 30);

        // Camp ENNEMI : ces troupes attaqueront les troupes du joueur
        b1.setCamp(Camp.ENNEMI);
        b2.setCamp(Camp.ENNEMI);
        b3.setCamp(Camp.ENNEMI);

        // Les troupes ennemies sont directement déployées sur la carte
        b1.deployer(b1.getX(), b1.getY());
        b2.deployer(b2.getX(), b2.getY());
        b3.deployer(b3.getX(), b3.getY());

        nouvelles.add(b1);
        nouvelles.add(b2);
        nouvelles.add(b3);
        
        // Retourne la liste des troupes générées 
        return nouvelles;
    }

    /** Retourne vrai si les troupes défensives ont déjà été spawned. */
    public boolean hasSpawn() { return aSpawn; }

    /** Marque le spawn comme effectué pour ne pas le déclencher deux fois. */
    public void setSpawn(boolean value) { this.aSpawn = value; }
}