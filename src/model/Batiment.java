package model;

/**
 * Classe de base représentant un bâtiment du village ennemi.
 *
 * Tous les éléments destructibles du jeu (défenses, hôtel de ville,
 * bâtiments normaux) héritent de cette classe. Elle centralise les
 * attributs communs : nom, points de vie, position, et le suivi du score.
 *
 * On utilise le principe d'héritage pour éviter la duplication de code :
 * Defense et les autres types de bâtiments réutilisent tout ce qui est
 * défini ici sans avoir à le réécrire.
 */
public class Batiment {


    protected String nom;  // Nom affiché à l'écran (ex : "Canon", "Hôtel de Ville")
    protected int pv;      // Points de vie actuels — diminuent quand le bâtiment est attaqué
    protected int x;       // Position horizontale sur la carte (en pixels)
    protected int y;       // Position verticale sur la carte (en pixels)

    // pvMax est final car il ne change jamais après la création du bâtiment.
    // On en a besoin pour calculer le pourcentage de vie restant (barre de vie).
    private final int pvMax;

    // comptee sert à éviter de compter plusieurs fois le même bâtiment détruit
    // dans le calcul du score. Une fois marqué, on ne le recompte plus.
    private boolean comptee = false;


    /**
     * Crée un bâtiment avec son nom, ses PV de départ et sa position.
     *
     * @param nom  Nom du bâtiment affiché dans l'interface
     * @param pv   Points de vie initiaux (aussi utilisés comme maximum)
     * @param x    Position X sur la carte
     * @param y    Position Y sur la carte
     */
    public Batiment(String nom, int pv, int x, int y) {
        this.nom   = nom;
        this.pv    = pv;
        this.pvMax = pv; // on mémorise le max une fois pour toutes
        this.x     = x;
        this.y     = y;
    }


    public String getNom() { return nom;   }
    public int getPv()     { return pv;    }
    public int getPvMax()  { return pvMax; }
    public int getX()      { return x;     }
    public int getY()      { return y;     }


    /**
     * Applique des dégâts au bâtiment.
     * Les PV ne peuvent pas descendre en dessous de 0.
     *
     * @param degats  Nombre de points de dégâts reçus
     */
    public void prendreDegats(int degats) {
        pv -= degats;
        if (pv < 0) pv = 0;
    }

    /**
     * Indique si le bâtiment est détruit (PV à 0).
     *
     * @return true si le bâtiment n'a plus de points de vie
     */
    public boolean estDetruit() {
        return pv <= 0;
    }


    /**
     * Indique si ce bâtiment a déjà été comptabilisé dans le score.
     * Evite de scorer plusieurs fois le même bâtiment entre deux ticks.
     */
    public boolean aEteComptee() { return comptee; }

    /**
     * Marque ce bâtiment comme déjà comptabilisé dans le score.
     * Appelé une seule fois juste après avoir ajouté les points.
     */
    public void marquerComptee() { comptee = true; }


    @Override
    public String toString() {
        return nom + " [PV: " + pv + "] | Pos: (" + x + ", " + y + ")";
    }
}