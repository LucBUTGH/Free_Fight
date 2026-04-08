package model;

/**
 * Troupe de base : rapide mais peu résistante.
 * Pas de comportement spécial — hérite tout de Troupe.
 *
 * Statistiques : 100 PV, 20 dégâts, vitesse 2.
 */
public class Barbare extends Troupe {

    public Barbare(int x, int y) {
        super(x, y, 100, 20, 2);
    }
}