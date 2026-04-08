package model;

/**
 * Troupe magique : dégâts élevés mais peu résistante.
 * Pas de comportement spécial — hérite tout de Troupe.
 *
 * Statistiques : 70 PV, 40 dégâts, vitesse 3.
 */
public class Sorcier extends Troupe {

    public Sorcier(int x, int y) {
        super(x, y, 70, 40, 3);
    }
}