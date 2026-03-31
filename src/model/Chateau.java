package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente le Château de Clan du village ennemi.
 *
 * Le Château de Clan est une défense spéciale : en plus de tirer sur les
 * troupes ennemies comme une défense normale, il spawne des troupes
 * défensives (camp ENNEMI) quand une troupe du joueur entre dans sa portée.
 *
 * Ce spawn ne se produit qu'une seule fois par partie.
 *
 * Hérite de Defense : il a donc une portée, des dégâts, une cadence de tir.
 */
public class Chateau extends Defense {

    // Indique si les troupes défensives ont déjà été spawned.
    // Le spawn ne se déclenche qu'une seule fois par partie.
    private boolean aSpawn = false;


    /**
     * Crée un Château de Clan.
     *
     * @param nom         Nom affiché
     * @param pv          Points de vie
     * @param portee      Rayon de détection et de tir
     * @param degats      Dégâts infligés par tir
     * @param cadenceTir  Ticks entre chaque tir
     * @param x           Position X
     * @param y           Position Y
     */
    public Chateau(String nom, int pv, int portee, int degats, int cadenceTir, int x, int y) {
        super(nom, pv, portee, degats, cadenceTir, x, y);
    }


    /**
     * Crée et retourne les troupes défensives à spawner autour du château.
     *
     * Les troupes sont assignées au camp ENNEMI pour qu'elles attaquent
     * les troupes du joueur. Elles sont immédiatement marquées comme déployées.
     *
     * @return Liste des nouvelles troupes défensives
     */
    public List<Troupe> spawnDefense() {
        List<Troupe> nouvelles = new ArrayList<>();

        // Création des troupes défensives autour du château
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

        return nouvelles;
    }

    /** Retourne vrai si les troupes défensives ont déjà été spawned. */
    public boolean hasSpawn() { return aSpawn; }

    /** Marque le spawn comme effectué pour ne pas le déclencher deux fois. */
    public void setSpawn(boolean value) { this.aSpawn = value; }
}