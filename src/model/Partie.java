package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe centrale du modèle : état complet d'une partie en cours.
 *
 * Contient :
 * - Les troupes déployées par le joueur
 * - Les défenses et bâtiments du village ennemi
 * - Le Château de Clan
 * - Le chronomètre
 * - Le score
 * - Les stocks de troupes disponibles
 *
 * La méthode update() fait avancer le jeu d'un tick (appelée toutes les 40ms).
 * Aucune logique d'affichage ici — Partie ne connaît pas Swing.
 */
public class Partie {


    // Troupes actuellement sur la carte (déployées ou en attente)
    private List<Troupe> troupes;

    // Défenses du village ennemi
    private List<Defense> defenses;

    // Bâtiment principal — objectif prioritaire des troupes du joueur
    private Batiment hotelDeVille;

    // Bâtiments normaux — ciblés en dernier recours
    private List<Batiment> autresBatiments;

    // Château de Clan — défense spéciale qui spawne des troupes défensives
    private Chateau chateau;


    // Décrémenté chaque seconde — la partie s'arrête quand il atteint 0
    private int secondesRestantes;


    // Nombre de troupes disponibles par type — diminuent à chaque déploiement
    private int stockBarbare;
    private int stockSorcier;
    private int stockPekka;


    // Augmente quand un bâtiment ennemi est détruit
    private int score;


    /**
     * Initialise une nouvelle partie avec le village ennemi complet
     * et les stocks de troupes du joueur.
     */
    public Partie() {
        troupes         = new ArrayList<>();
        defenses        = new ArrayList<>();
        autresBatiments = new ArrayList<>();
        

        secondesRestantes = 120; // 2 minutes de jeu
        score             = 0;

        // Bâtiment principal — 500 points si détruit
        hotelDeVille = new Batiment("Hôtel de Ville", 1500, 375, 290);

        // Château de Clan — spawne des défenseurs quand attaqué
        // Chateau(nom, pv, portee, degats, cadenceTir, x, y)
        chateau = new Chateau("Château de Clan", 500, 150, 20, 30, 600, 300);

        // Défenses du village
        // Defense(nom, pv, portee, degats, cadenceTir, x, y)
        defenses.add(new Defense("Canon",       200, 150, 15, 30, 200, 200));
        defenses.add(new Defense("Tour Archer", 100, 220,  8, 20, 500, 280));
        defenses.add(new Defense("Mortier",     300, 180, 25, 40, 360, 430));

        // Bâtiments normaux
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
     * Ordre d'exécution :
     * 1. Spawn du Château de Clan si une troupe entre dans sa portée
     * 2. Ciblage troupe vs troupe
     * 3. Déplacement et attaque des troupes
     * 4. Tir des défenses
     * 5. Calcul du score
     * 6. Suppression des troupes mortes
     */
    public void update() {

        // ── 1. Spawn du Château de Clan 
        // Se déclenche une seule fois quand une troupe du joueur entre dans la portée
        if (!chateau.hasSpawn()) {
            for (Troupe t : troupes) {
                if (t.getCamp() == Camp.JOUEUR && chateau.estAPortee(t.getX(), t.getY())) {
                    List<Troupe> nouvelles = chateau.spawnDefense();
                    troupes.addAll(nouvelles);
                    chateau.setSpawn(true);
                    break;
                }
            }
        }

        // ── 2. Ciblage troupe vs troupe
        // Chaque troupe cherche une cible ennemie si elle n'en a pas déjà une vivante
        for (Troupe t1 : troupes) {
            if (t1.getCibleTroupe() != null && !t1.getCibleTroupe().estMorte()) continue;

            for (Troupe t2 : troupes) {
                if (t1 == t2) continue;                     // pas se cibler soi-même
                if (t1.getCamp() == t2.getCamp()) continue; // pas cibler un allié
                t1.setCibleTroupe(t2);
                break;
            }
        }

        // ── 3. Déplacement et attaque des troupes 
        for (Troupe t : troupes) {
            if (!t.isDeployee()) continue;

            // Réassigner une cible bâtiment uniquement pour les troupes du JOUEUR
            // Les troupes ENNEMI n'attaquent que les troupes adverses
            if (t.getCamp() == Camp.JOUEUR) {
                if (t.getCible() == null || t.getCible().estDetruit()) {
                    Batiment nouvelleCible = t.choisirCible(defenses, hotelDeVille, autresBatiments);
                    t.setCible(nouvelleCible);
                }
            }

            t.agirManuellement();
        }

        // ── 4. Tir des défenses 
        for (Defense d : defenses) {
            d.agir(troupes);
        }
        // Le château tire aussi comme une défense normale
        //chateau.agir(troupes);

        // aEteComptee() évite de scorer plusieurs fois le même bâtiment
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
        if (chateau.estDetruit() && !chateau.aEteComptee()) {
            score += 200;
            chateau.marquerComptee();
        }

        // ── 6. Suppression des troupes mortes ─────────────────────────────────
        // avancerMort() retourne true quand l'animation de mort est terminée
        troupes.removeIf(Troupe::avancerMort);
    }


    /**
     * Déploie n troupes d'un type donné à la position (x, y).
     *
     * Pour chaque troupe :
     * 1. Vérifie le stock
     * 2. Crée la troupe
     * 3. La marque comme déployée
     * 4. Lui assigne une cible automatique
     * 5. L'ajoute à la liste
     *
     * Les troupes sont espacées de 15px pour ne pas être empilées.
     *
     * @param type      "Barbare", "Sorcier" ou "Pekka"
     * @param quantite  Nombre de troupes à déployer
     * @param x         Position X du clic
     * @param y         Position Y du clic
     * @return          Nombre réellement déployé (limité par le stock)
     */
    public int deployerTroupes(String type, int quantite, int x, int y) {
        int deployed = 0;

        for (int i = 0; i < quantite; i++) {
            Troupe t = null;

            switch (type) {
                case "Barbare":
                    if (stockBarbare <= 0) break;
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

            if (t == null) break; // stock épuisé

            // Camp JOUEUR par défaut (défini dans le constructeur de Troupe)
            t.deployer(x + i * 15, y);

            // Cible automatique la plus proche
            Batiment cible = t.choisirCible(defenses, hotelDeVille, autresBatiments);
            t.setCible(cible);

            troupes.add(t);
            deployed++;
        }

        return deployed;
    }


    /** Réduit le temps restant de 1 seconde. Ne descend pas sous 0. */
    public void decrementerTemps() {
        if (secondesRestantes > 0) secondesRestantes--;
    }

    /** Retourne true si le temps est écoulé. */
    public boolean tempsEcoule() {
        return secondesRestantes <= 0;
    }


    public List<Troupe>    getTroupes()           { return troupes;           }
    public List<Defense>   getDefenses()          { return defenses;          }
    public Batiment        getHotelDeVille()      { return hotelDeVille;      }
    public List<Batiment>  getAutresBatiments()   { return autresBatiments;   }
    public Chateau         getChateau()           { return chateau;           }
    public int             getSecondesRestantes() { return secondesRestantes; }
    public int             getScore()             { return score;             }
    public int             getStockBarbare()      { return stockBarbare;      }
    public int             getStockSorcier()      { return stockSorcier;      }
    public int             getStockPekka()        { return stockPekka;        }

    public void ajouterScore(int points) { score += points; }

    /**
     * Retourne les défenses dont la portée couvre le point (x, y).
     * Utilisé pour le test visuel de portée (clic sur la carte).
     */
    public List<Defense> getDefensesEnPortee(int x, int y) {
        List<Defense> resultat = new ArrayList<>();
        for (Defense d : defenses) {
            if (d.estAPortee(x, y)) resultat.add(d);
        }
        return resultat;
    }

    /**
     * Ajoute une troupe directement à la liste.
     * En conditions normales, passer par deployerTroupes() à la place.
     */
    public void ajouterTroupe(Troupe troupe) {
        troupes.add(troupe);
    }

    // Retourne le pourcentage de destruction (0 à 100)
    public int getPourcentageDestruction() {
        int total = 1 + defenses.size() + autresBatiments.size(); // +1 pour l'hôtel de ville
        int detruits = 0;

        if (hotelDeVille.estDetruit()) detruits++;
        for (Defense d : defenses) {
            if (d.estDetruit()) detruits++;
        }
        for (Batiment b : autresBatiments) {
            if (b.estDetruit()) detruits++;
        }

        return (detruits * 100) / total;
    }

    // Retourne le nombre d'étoiles obtenues (0 à 3)
    // 1 étoile : hôtel de ville détruit
    // 1 étoile : 50% des bâtiments détruits
    // 1 étoile : 100% des bâtiments détruits
    public int getEtoiles() {
        int etoiles = 0;
        int pourcentage = getPourcentageDestruction();

        if (hotelDeVille.estDetruit()) etoiles++;
        if (pourcentage >= 50) etoiles++;
        if (pourcentage >= 100) etoiles++;

        return etoiles;
    }

    // Indique si la partie est terminée
    public boolean estTerminee() {
        return tempsEcoule() || getPourcentageDestruction() >= 100;
    }

    // Indique si le joueur a gagné (au moins 1 étoile)
    public boolean estGagnee() {
        return estTerminee() && getEtoiles() > 0;
    }

    // Indique si le joueur a perdu (0 étoile et temps écoulé)
    public boolean estPerdue() {
        return estTerminee() && getEtoiles() == 0;
    }
}