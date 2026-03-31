package model;

import java.util.ArrayList;
import java.util.List;

/**
 * La Classe représente l'état complet d'une partie en cours.
 * 
 * Elle contient toutes les données du jeu :
 * - Les troupes déployées par le joueur
 * - Les défenses et bâtiments du village ennemi
 * - Le chronomètre
 * - Le score
 * - Les stocks de troupes disponibles
 * 
 * Elle expose aussi la méthode update() qui fait avancer le jeu d'un tick,
 * appelée toutes les 40ms par le timer dans GameController.
 */
public class Partie {


    // Troupes actuellement sur la carte (déployées ou en attente)
    private List<Troupe> troupes;

    // Défenses du village ennemi (Canon, Tour Archer, Mortier...)
    private List<Defense> defenses;

    // Bâtiment principal du village — objectif prioritaire des troupes
    private Batiment hotelDeVille;

    // Autres bâtiments normaux (Cabane en Or, Extracteur, Laboratoire...)
    // Ciblés en dernier recours quand il n'y a plus de défenses
    private List<Batiment> autresBatiments;


    // Temps restant en secondes. Décrémenté chaque seconde par timerChrono.
    // La partie s'arrête quand il atteint 0.
    private int secondesRestantes;


    // Nombre de troupes de chaque type encore disponibles pour le déploiement.
    // Ces valeurs diminuent à chaque déploiement et ne se rechargent pas.
    private int stockBarbare;
    private int stockSorcier;
    private int stockPekka;


    // Score accumulé au fil de la partie.
    // Augmente quand un bâtiment ennemi est détruit.
    private int score;
    
 // Le chateau de clan
    private Chateau chateau;


    /**
     * Initialise une nouvelle partie avec tous les éléments du village ennemi
     * et les stocks de troupes du joueur.
     * 
     * C'est ici qu'on configure le "niveau" : positions des bâtiments,
     * PV des défenses, cadence de tir, stocks de troupes disponibles.
     */
    public Partie() {
        troupes         = new ArrayList<>();
        defenses        = new ArrayList<>();
        autresBatiments = new ArrayList<>();

        secondesRestantes = 120; // 2 minutes de jeu
        score             = 0;

        // Bâtiment principal — le détruire rapporte le plus de points
        hotelDeVille = new Batiment("Hôtel de Ville", 1500, 375, 290);
        
        chateau = new Chateau("Château", 500, 150, 600, 300, 30, 25);


        // Défenses du village
        // Signature : Defense(nom, pv, portee, degats, cadenceTir, x, y)
        // cadenceTir : plus c'est grand, plus la défense tire lentement
        defenses.add(new Defense("Canon",       200, 150, 15, 30, 200, 200));
        defenses.add(new Defense("Tour Archer", 100, 220,  8, 20, 500, 280));
        defenses.add(new Defense("Mortier",     300, 180, 25, 40, 360, 430));

        // Bâtiments normaux — pas de portée, juste des PV et une position
        autresBatiments.add(new Batiment("Cabane en Or", 500, 600, 150));
        autresBatiments.add(new Batiment("Extracteur",   300, 700, 300));
        autresBatiments.add(new Batiment("Laboratoire",  800, 250, 450));

        // Stocks de troupes disponibles pour le joueur
        stockBarbare = 6;
        stockSorcier = 3;
        stockPekka   = 3;
    }


    /**
     * Met à jour l'état du jeu pour un tick (appelé toutes les 40ms).
     * 
     * Ordre d'exécution important :
     * 1. Les troupes bougent et attaquent
     * 2. Les défenses ripostent
     * 3. On comptabilise les destructions dans le score
     * 4. On supprime les troupes mortes
     * 
     * Note : on ne supprime pas les troupes mortes avant la fin du tick,
     * sinon on risque une ConcurrentModificationException en modifiant
     * la liste pendant qu'on la parcourt.
     */
    public void update() {

        // 1. Chaque troupe déployée agit (déplacement + attaque)
        for (Troupe t : troupes) {
            if (!t.isDeployee()) continue; // les troupes non déployées sont ignorées

            // Réassigner une cible si la cible actuelle est nulle ou détruite
            if (t.getCible() == null || t.getCible().estDetruit()) {
                Batiment nouvelleCible = t.choisirCible(defenses, hotelDeVille, autresBatiments);
                t.setCible(nouvelleCible);
            }

            t.agirManuellement();
        }

        
        Chateau chateau = getChateau();

   	 if (!chateau.hasSpawn()) {
   	     for (Troupe t : troupes) {
   	         if (chateau.estAPortee(t.getX(), t.getY())) {

   	             List<Troupe> nouvelles = chateau.spawnDefense();
   	             troupes.addAll(nouvelles);

   	             chateau.setSpawn(true);
   	             break;
   	         }
   	     }
   	 }
   	 
        // 2. Chaque défense tire sur les troupes à portée
        for (Defense d : defenses) {
            d.agir(troupes);
        }

        // 3. Score : on détecte les bâtiments détruits ce tick
        // On utilise aEteComptee() pour ne scorer qu'une seule fois par bâtiment
        for (Defense d : defenses) {
            if (d.estDetruit() && !d.aEteComptee()) {
                score += 100;
                d.marquerComptee();
            }
        }
        for (Batiment b : autresBatiments) {
            if (b.estDetruit() && !b.aEteComptee()) {
                score += 50;
                b.marquerComptee();
            }
        }
        if (hotelDeVille.estDetruit() && !hotelDeVille.aEteComptee()) {
            score += 500;
            hotelDeVille.marquerComptee();
        }

        // 4. Avance l'animation de mort et supprime les troupes mortes
        // removeIf supprime les éléments pour lesquels avancerMort() retourne true
        troupes.removeIf(Troupe::avancerMort);
    }


    /**
     * Déploie un certain nombre de troupes d'un type donné à une position.
     * 
     * Processus pour chaque troupe :
     * 1. Vérifier que le stock n'est pas vide
     * 2. Créer l'instance de la troupe
     * 3. La marquer comme déployée (elle apparaît sur la carte)
     * 4. Lui assigner automatiquement une cible
     * 5. L'ajouter à la liste des troupes actives
     * 
     * Les troupes sont légèrement espacées (i * 15 pixels) pour ne pas
     * toutes être empilées au même endroit.
     * 
     * @param type      "Barbare", "Sorcier" ou "Pekka"
     * @param quantite  Nombre de troupes à déployer
     * @param x         Position X du clic de déploiement
     * @param y         Position Y du clic de déploiement
     * @return          Nombre de troupes réellement déployées (≤ quantite)
     */
    public int deployerTroupes(String type, int quantite, int x, int y) {
        int deployed = 0;

        for (int i = 0; i < quantite; i++) {
            Troupe t = null;

            // Création selon le type et décrémentation du stock
            switch (type) {
                case "Barbare":
                    if (stockBarbare <= 0) break; // stock épuisé
                    t = new Barbare(x + i * 15, y);
                    stockBarbare--;
                    break;
                case "Sorcier":
                    if (stockSorcier <= 0) break;
                    t = new Sorcier(x + i * 15, y);
                    stockSorcier--;
                    break;
                case "Pekka":
                    if (stockPekka <= 0) break;
                    t = new Pekka(x + i * 15, y);
                    stockPekka--;
                    break;
            }

            // Si t est null, le stock était vide → on arrête la boucle
            if (t == null) break;

            // Déploiement : la troupe apparaît sur la carte
            t.deployer(x + i * 15, y);

            // Assignation automatique de la cible la plus proche
            Batiment cible = t.choisirCible(defenses, hotelDeVille, autresBatiments);
            t.setCible(cible);

            troupes.add(t);
            deployed++;
        }

        return deployed;
    }


    /**
     * Réduit le temps restant de 1 seconde.
     * Appelé par timerChrono dans GameController toutes les secondes.
     * On s'assure de ne pas descendre sous 0.
     */
    public void decrementerTemps() {
        if (secondesRestantes > 0) secondesRestantes--;
    }

    /**
     * Indique si le temps de la partie est écoulé.
     * @return true si secondesRestantes == 0
     */
    public boolean tempsEcoule() {
        return secondesRestantes <= 0;
    }


    /** Retourne la liste de toutes les troupes (déployées ou non). */
    public List<Troupe> getTroupes()          { return troupes;           }

    /** Retourne la liste des défenses du village. */
    public List<Defense> getDefenses()        { return defenses;          }

    /** Retourne l'hôtel de ville. */
    public Batiment getHotelDeVille()         { return hotelDeVille;      }

    /** Retourne les bâtiments normaux (non-défenses). */
    public List<Batiment> getAutresBatiments(){ return autresBatiments;   }

    /** Retourne le temps restant en secondes. */
    public int getSecondesRestantes()         { return secondesRestantes; }

    /** Retourne le score actuel. */
    public int getScore()                     { return score;             }

    /** Ajoute des points au score (utilisable pour des bonus futurs). */
    public void ajouterScore(int points)      { score += points;          }

    /** Retourne le stock de Barbares restants. */
    public int getStockBarbare()              { return stockBarbare;      }

    /** Retourne le stock de Sorciers restants. */
    public int getStockSorcier()              { return stockSorcier;      }

    /** Retourne le stock de Pekkas restants. */
    public int getStockPekka()                { return stockPekka;        }

    /**
     * Retourne les défenses dont le rayon couvre le point (x, y).
     * Utilisé pour le test visuel de portée (clic sur la carte).
     * 
     * @param x  Coordonnée X du point testé
     * @param y  Coordonnée Y du point testé
     * @return   Liste des défenses à portée de ce point
     */
    public List<Defense> getDefensesEnPortee(int x, int y) {
        List<Defense> resultat = new ArrayList<>();
        for (Defense d : defenses) {
            if (d.estAPortee(x, y)) resultat.add(d);
        }
        return resultat;
    }

    /**
     * Ajoute une troupe directement à la liste (utilisé pour les tests).
     * En conditions normales, passer par deployerTroupes() à la place.
     */
    public void ajouterTroupe(Troupe troupe) {
        troupes.add(troupe);
    }
    
    public Chateau getChateau() {
        return chateau;
    }
}