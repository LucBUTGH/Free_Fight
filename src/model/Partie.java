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
    
    // Le chateau de clan
    private Chateau chateau;

    // Temps restant de la partie en secondes
    private int secondesRestantes;
    
    public Chateau getChateau() {
        return chateau;
    }

    public Partie() {
        troupes = new ArrayList<>();
        defenses = new ArrayList<>();
        autresBatiments = new ArrayList<>();

        secondesRestantes = 120;

        hotelDeVille = new Batiment("Hôtel de Ville", 1500, 375, 290);
        
        chateau = new Chateau("Château", 500, 150, 600, 300);
        // Défenses de test du village
        defenses.add(new Defense("Canon", 200, 150, 200, 200));
        defenses.add(new Defense("Tour Archer", 100, 220, 500, 280));
        defenses.add(new Defense("Mortier", 300, 180, 360, 430));
    }

    // Ajoute une troupe à la partie
    public void ajouterTroupe(Troupe troupe) {
        troupe.setCamp(Camp.JOUEUR);  // définis le camp de la troupe
        troupes.add(troupe);           // ajoute la troupe à la liste
    }

 // Met à jour l’état du jeu
 // Chaque troupe agit seulement si le joueur lui a donné une cible
 public void update() {
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
	 
	 for (Troupe t1 : troupes) {
		    // si la troupe a déjà une cible vivante, on continue
		    if (t1.getCibleTroupe() != null && !t1.getCibleTroupe().estMorte()) continue;

		    for (Troupe t2 : troupes) {
		        if (t1 == t2) continue;            // pas se cibler soi-même
		        if (t1.getCamp() == t2.getCamp()) continue; // ne cible que les ennemis

		        t1.setCibleTroupe(t2);  // trouve la première cible ennemie
		        break;                  // stop après avoir trouvé un ennemi
		    }
	}
    for (Troupe t : troupes) {
         t.agirManuellement();
     }
     // Suppression des troupes mortes
     troupes.removeIf(Troupe::estMorte);
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
}