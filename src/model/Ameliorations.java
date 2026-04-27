package model;

/**
 * Gère le système d'améliorations des troupes disponibles entre les niveaux.
 *
 * MÉCANIQUE :
 * - Le joueur reçoit un montant d'or fixe au début (OR_DEPART).
 * - Chaque troupe (Barbare, Sorcier, Pekka) peut être améliorée de niveau 1 à NIVEAU_MAX.
 * - Un niveau plus élevé augmente les PV, les dégâts et la vitesse.
 * - Les coûts augmentent avec chaque niveau pour inciter à faire des choix stratégiques.
 *
 * ÉQUILIBRE :
 * - OR_DEPART = 1500 : insuffisant pour tout maxer → force le joueur à spécialiser ou équilibrer.
 * - Les troupes faibles (Barbare, Sorcier) coûtent moins cher que les troupes fortes (Pekka).
 * - Cela permet aux joueurs de choisir une stratégie cohérente.
 *
 * @author Développeur principal
 * @version 1.0
 */
public class Ameliorations {

    // ═══ Constantes d'équilibre du jeu ═══
    /** Quantité d'or donnée au joueur au début de la partie. */
    public static final int OR_DEPART = 1500;

    /** Niveau maximum atteignable pour chaque type de troupe. */
    public static final int NIVEAU_MAX = 5;

    // ═══ Constantes de formules de coût ═══
    /** Coût de base par niveau pour Barbare (progression linéaire). */
    private static final int COUT_BASE_BARBARE = 100;

    /** Offset pour Sorcier (coûte 50 or de plus que Barbare à chaque niveau). */
    private static final int COUT_OFFSET_SORCIER = 50;

    /** Progression par niveau pour Sorcier. */
    private static final int COUT_PROGRESSION_SORCIER = 100;

    /** Offset pour Pekka (coûte 200 or de plus que Barbare à chaque niveau). */
    private static final int COUT_OFFSET_PEKKA = 200;

    /** Progression par niveau pour Pekka. */
    private static final int COUT_PROGRESSION_PEKKA = 100;

    // ═══ État du joueur ═══
    /** Or restant disponible pour les améliorations. */
    private int or;

    /** Niveau courant du Barbare (1 à NIVEAU_MAX). */
    private int niveauBarbare;

    /** Niveau courant du Sorcier (1 à NIVEAU_MAX). */
    private int niveauSorcier;

    /** Niveau courant du Pekka (1 à NIVEAU_MAX). */
    private int niveauPekka;

    /**
     * Crée une instance d'améliorations avec l'or de départ par défaut.
     * Tous les niveaux commencent à 1.
     */
    public Ameliorations() {
        this(OR_DEPART);
    }

    /**
     * Crée une instance d'améliorations avec un montant d'or personnalisé.
     * Utile pour les tests ou les sauvegardes.
     *
     * @param orInitial Quantité d'or de départ (ore disponible pour acheter des améliorations)
     */
    public Ameliorations(int orInitial) {
        this.or = orInitial;
        this.niveauBarbare = 1;
        this.niveauSorcier = 1;
        this.niveauPekka = 1;
    }

    // ═══ Système de coûts d'améliorations ═══

    /**
     * Calcule le coût pour améliorer Barbare au niveau spécifié.
     *
     * FORMULE : 100 × (nextLevel - 1)
     *   - Niveau 2 : 100 or
     *   - Niveau 3 : 200 or
     *   - Niveau 4 : 300 or
     *   - Niveau 5 : 400 or
     *   - Total max : 1000 or (pour passer de 1 à 5)
     *
     * @param nextLevel Niveau cible (doit être entre 2 et NIVEAU_MAX)
     * @return Coût en or pour cette amélioration
     */
    public static int coutBarbare(int nextLevel) {
        return COUT_BASE_BARBARE * (nextLevel - 1);
    }

    /**
     * Calcule le coût pour améliorer Sorcier au niveau spécifié.
     *
     * FORMULE : 50 + 100 × (nextLevel - 1)
     *   - Niveau 2 : 150 or
     *   - Niveau 3 : 250 or
     *   - Niveau 4 : 350 or
     *   - Niveau 5 : 450 or
     *   - Total max : 1200 or (pour passer de 1 à 5)
     *
     * Le Sorcier coûte 50 or de plus par niveau que le Barbare (troupe plus équilibrée).
     *
     * @param nextLevel Niveau cible (doit être entre 2 et NIVEAU_MAX)
     * @return Coût en or pour cette amélioration
     */
    public static int coutSorcier(int nextLevel) {
        return COUT_OFFSET_SORCIER + COUT_PROGRESSION_SORCIER * (nextLevel - 1);
    }

    /**
     * Calcule le coût pour améliorer Pekka au niveau spécifié.
     *
     * FORMULE : 200 + 100 × (nextLevel - 1)
     *   - Niveau 2 : 300 or
     *   - Niveau 3 : 400 or
     *   - Niveau 4 : 500 or
     *   - Niveau 5 : 600 or
     *   - Total max : 1800 or (pour passer de 1 à 5)
     *
     * Le Pekka coûte 200 or de plus par niveau que le Barbare (troupe légendaire et chère).
     * Cela force le joueur à faire des choix : spécialiser sur le Pekka OU équilibrer les trois.
     *
     * @param nextLevel Niveau cible (doit être entre 2 et NIVEAU_MAX)
     * @return Coût en or pour cette amélioration
     */
    public static int coutPekka(int nextLevel) {
        return COUT_OFFSET_PEKKA + COUT_PROGRESSION_PEKKA * (nextLevel - 1);
    }

    // ═══ Système d'achat d'améliorations ═══

    /**
     * Tente d'améliorer Barbare du niveau suivant.
     *
     * RÈGLES :
     * - Impossible si le Barbare est déjà au niveau max.
     * - Impossible si l'or disponible est inférieur au coût.
     * - Sinon, déduit le coût et augmente le niveau.
     *
     * @return true si l'amélioration a réussi, false sinon
     */
    public boolean ameliorerBarbare() {
        if (niveauBarbare >= NIVEAU_MAX) return false;
        int cout = coutBarbare(niveauBarbare + 1);
        if (or < cout) return false;
        or -= cout;
        niveauBarbare++;
        return true;
    }

    /**
     * Tente d'améliorer Sorcier du niveau suivant.
     *
     * RÈGLES :
     * - Impossible si le Sorcier est déjà au niveau max.
     * - Impossible si l'or disponible est inférieur au coût.
     * - Sinon, déduit le coût et augmente le niveau.
     *
     * @return true si l'amélioration a réussi, false sinon
     */
    public boolean ameliorerSorcier() {
        if (niveauSorcier >= NIVEAU_MAX) return false;
        int cout = coutSorcier(niveauSorcier + 1);
        if (or < cout) return false;
        or -= cout;
        niveauSorcier++;
        return true;
    }

    /**
     * Tente d'améliorer Pekka du niveau suivant.
     *
     * RÈGLES :
     * - Impossible si le Pekka est déjà au niveau max.
     * - Impossible si l'or disponible est inférieur au coût.
     * - Sinon, déduit le coût et augmente le niveau.
     *
     * @return true si l'amélioration a réussi, false sinon
     */
    public boolean ameliorerPekka() {
        if (niveauPekka >= NIVEAU_MAX) return false;
        int cout = coutPekka(niveauPekka + 1);
        if (or < cout) return false;
        or -= cout;
        niveauPekka++;
        return true;
    }

    // ═══ Accesseurs en lecture ═══

    /**
     * @return Or restant disponible pour acheter des améliorations
     */
    public int getOr() { return or; }

    /**
     * @return Niveau courant du Barbare (1 à NIVEAU_MAX)
     */
    public int getNiveauBarbare() { return niveauBarbare; }

    /**
     * @return Niveau courant du Sorcier (1 à NIVEAU_MAX)
     */
    public int getNiveauSorcier() { return niveauSorcier; }

    /**
     * @return Niveau courant du Pekka (1 à NIVEAU_MAX)
     */
    public int getNiveauPekka() { return niveauPekka; }
}
