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
        
        // Génération des troupes autour du chateau
        Barbare b1 = new Barbare(getX() + 30, getY());
        Pekka b2 = new Pekka(getX() - 30, getY());
        Sorcier b3 = new Sorcier(getX() + 30, getY());
        
        // Définition du camp
        b1.setCamp(Camp.ENNEMI);
        b2.setCamp(Camp.ENNEMI);
        b3.setCamp(Camp.ENNEMI);
        
        // Ajout à la liste
        nouvelles.add(b1);
        nouvelles.add(b2);
        nouvelles.add(b3);
        
        // Retourne la liste des troupes générées 
        return nouvelles;
    }
    
    // indique si le chateau a déjà généré des troupes et retourne oui ou non 
    public boolean hasSpawn() {
        return aSpawn;
    }
    
    /* Permet de modifier l'état du spawn 
     * @param value true → le château a spawn
     *              false → il peut encore spawn
     */
    public void setSpawn(boolean value) {
        this.aSpawn = value;
    }
    
}