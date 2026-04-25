package model;

// Classe qui gère les améliorations de troupes achetées avec l'or
// donné au joueur au début de la partie.
// Chaque type de troupe a son propre niveau (de 1 à NIVEAU_MAX).
// Un niveau plus élevé augmente les PV, les dégâts et la vitesse.
public class Ameliorations {

    // Quantité d'or donnée au joueur au début de la partie.
    // Avec 1500 d'or, le joueur ne peut pas tout maxer : il doit choisir
    // sa stratégie (spécialiser une troupe ou équilibrer les trois).
    public static final int OR_DEPART = 1500;

    // Niveau maximum atteignable par chaque troupe
    public static final int NIVEAU_MAX = 5;

    private int or;
    private int niveauBarbare;
    private int niveauSorcier;
    private int niveauPekka;

    public Ameliorations() {
        this(OR_DEPART);
    }

    /**
     * Crée les améliorations avec un montant d'or personnalisé.
     * @param orInitial Quantité d'or de départ
     */
    public Ameliorations(int orInitial) {
        this.or = orInitial;
        this.niveauBarbare = 1;
        this.niveauSorcier = 1;
        this.niveauPekka = 1;
    }

    // --- Coûts ---
    // Le coût augmente avec le niveau visé pour rendre les derniers paliers
    // plus difficiles à atteindre. Les troupes les plus fortes (Pekka) coûtent
    // plus cher à améliorer que les troupes faibles (Barbare).

    // Coût pour passer du niveau (nextLevel - 1) au niveau nextLevel
    public static int coutBarbare(int nextLevel) {
        // 2->100, 3->200, 4->300, 5->400   (total max = 1000)
        return 100 * (nextLevel - 1);
    }

    public static int coutSorcier(int nextLevel) {
        // 2->150, 3->250, 4->350, 5->450   (total max = 1200)
        return 50 + 100 * (nextLevel - 1);
    }

    public static int coutPekka(int nextLevel) {
        // 2->300, 3->400, 4->500, 5->600   (total max = 1800)
        return 200 + 100 * (nextLevel - 1);
    }

    // --- Achats ---

    public boolean ameliorerBarbare() {
        if (niveauBarbare >= NIVEAU_MAX) return false;
        int cout = coutBarbare(niveauBarbare + 1);
        if (or < cout) return false;
        or -= cout;
        niveauBarbare++;
        return true;
    }

    public boolean ameliorerSorcier() {
        if (niveauSorcier >= NIVEAU_MAX) return false;
        int cout = coutSorcier(niveauSorcier + 1);
        if (or < cout) return false;
        or -= cout;
        niveauSorcier++;
        return true;
    }

    public boolean ameliorerPekka() {
        if (niveauPekka >= NIVEAU_MAX) return false;
        int cout = coutPekka(niveauPekka + 1);
        if (or < cout) return false;
        or -= cout;
        niveauPekka++;
        return true;
    }

    // --- Getters ---

    public int getOr() { return or; }
    public int getNiveauBarbare() { return niveauBarbare; }
    public int getNiveauSorcier() { return niveauSorcier; }
    public int getNiveauPekka() { return niveauPekka; }
}
