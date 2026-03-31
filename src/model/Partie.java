package model;

import java.util.ArrayList;
import java.util.List;

// On a créé la classe Partie afin de :
// centraliser l’état du jeu
// enlever des données de Affichage
// préparer une architecture plus propre
public class Partie {

    // Liste des troupes du joueur
    private List<Troupe> troupes;

    // Liste des défenses du village
    private List<Defense> defenses;

    // Bâtiment principal du village
    private Batiment hotelDeVille;

    // Liste des autres bâtiments (pour l’instant vide, mais utile pour la suite)
    private List<Batiment> autresBatiments;

    // Temps restant de la partie en secondes
    private int secondesRestantes;

    public Partie() {
        troupes = new ArrayList<>();
        defenses = new ArrayList<>();
        autresBatiments = new ArrayList<>();

        secondesRestantes = 120;

        hotelDeVille = new Batiment("Hôtel de Ville", 1500, 375, 290);

        // Défenses de test du village
        defenses.add(new Defense("Canon", 200, 150, 200, 200));
        defenses.add(new Defense("Tour Archer", 100, 220, 500, 280));
        defenses.add(new Defense("Mortier", 300, 180, 360, 430));
    }

    // Ajoute une troupe à la partie
    public void ajouterTroupe(Troupe troupe) {
        troupes.add(troupe);
    }

 // Met à jour l’état du jeu
 // Chaque troupe agit seulement si le joueur lui a donné une cible
 public void update() {
     for (Troupe t : troupes) {
         t.agirManuellement();
     }
 }

    // Retourne la liste des défenses qui sont à portée d'un point donné
    public List<Defense> getDefensesEnPortee(int x, int y) {
        List<Defense> resultat = new ArrayList<>();

        for (Defense d : defenses) {
            if (d.estAPortee(x, y)) {
                resultat.add(d);
            }
        }

        return resultat;
    }

    // Retourne la liste des troupes
    public List<Troupe> getTroupes() {
        return troupes;
    }

    // Retourne la liste des défenses
    public List<Defense> getDefenses() {
        return defenses;
    }

    // Retourne l’hôtel de ville
    public Batiment getHotelDeVille() {
        return hotelDeVille;
    }

    // Retourne les autres bâtiments
    public List<Batiment> getAutresBatiments() {
        return autresBatiments;
    }

    // Retourne le temps restant
    public int getSecondesRestantes() {
        return secondesRestantes;
    }

    // Réduit le temps restant de 1 seconde
    public void decrementerTemps() {
        if (secondesRestantes > 0) {
            secondesRestantes--;
        }
    }

    // Indique si le temps est écoulé
    public boolean tempsEcoule() {
        return secondesRestantes <= 0;
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