package model;

import java.util.ArrayList;
import java.util.List;

public class Chateau extends Defense {

    private boolean aSpawn = false;

    public Chateau(String nom, int pv, int portee, int x, int y) {
        super(nom, 50, portee, x, y);
    }

    // Spawn des troupes de défense
    public List<Troupe> spawnDefense() {
        List<Troupe> nouvelles = new ArrayList<>();
        
        // Génération des troupes qui sortent du chateau
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

        return nouvelles;
    }

    public boolean hasSpawn() {
        return aSpawn;
    }

    public void setSpawn(boolean value) {
        this.aSpawn = value;
    }
    
}