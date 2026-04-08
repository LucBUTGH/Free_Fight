package model;



public class Sorcier extends Troupe {

    // Stats de base d'un Sorcier (niveau 1) : 70 PV, 40 dégâts, vitesse 3.
    // Chaque niveau d'amélioration ajoute :
    //   +20 PV, +10 dégâts, et +1 vitesse tous les 2 niveaux.
    public Sorcier(int x, int y, int niveau) {
        super(x, y,
                70 + 20 * (niveau - 1),
                40 + 10 * (niveau - 1),
                3 + (niveau - 1) / 2,
                niveau);
    }

    public Sorcier(int x, int y) {
        this(x, y, 1);
    }
}
