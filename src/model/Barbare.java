package model;

/**
 * Troupe de base : rapide mais peu résistante.
 * Pas de comportement spécial — hérite tout de Troupe.
 *
 * Statistiques : 100 PV, 20 dégâts, vitesse 2.
 */
public class Barbare extends Troupe {

    // Stats de base d'un Barbare (niveau 1) : 100 PV, 20 dégâts, vitesse 2.
    // Chaque niveau d'amélioration ajoute :
    //   +25 PV, +5 dégâts, et +1 vitesse tous les 2 niveaux.
    public Barbare(int x, int y, int niveau) {
        super(x, y,
                100 + 25 * (niveau - 1),
                20 + 5 * (niveau - 1),
                2 + (niveau - 1) / 2,
                niveau);
    }

    // Constructeur par défaut (niveau 1) pour conserver la compatibilité
    public Barbare(int x, int y) {
        this(x, y, 1);
    }
}
