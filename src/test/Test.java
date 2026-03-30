package test;

import model.*;
import view.Affichage;
import view.StartPanel;

import javax.swing.JFrame;

public class Test {

    /* la fenêtre d'affichage : menu au début puis fenêtre de jeu */
    private JFrame fenetre;
    private StartPanel startPanel; // le panneau de départ
    private Affichage affichage;   // la vue principale

    /* On va créer juste une instance */
    public Test() {
        fenetre = new JFrame("FreeFight");
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Appel de la méthode maintenant existante
        startPanel = new StartPanel(this);
        fenetre.add(startPanel);

        fenetre.pack();
        fenetre.setResizable(false);
        fenetre.setVisible(true);
    }

    public static void main(String[] args) {
        new Test();
    }

    public void lancerJeu() {

        fenetre.setTitle("FreeFight – Test Portée Défenses");

        // Création de la partie
        Partie partie = new Partie();

        // Création des troupes dans la partie
        partie.ajouterTroupe(new Barbare(50, 500));
        partie.ajouterTroupe(new Sorcier(120, 500));
        partie.ajouterTroupe(new Pekka(190, 500));

        // Remplacer le contenu de la fenêtre
        fenetre.setVisible(false);
        fenetre.remove(startPanel);

        // On passe la partie à l'affichage
        affichage = new Affichage(partie);

        // On passe la partie à l'affichage pour que la vue puisse
        // lire l'état du jeu (troupes, défenses, hôtel de ville, chrono)
        // sans stocker elle-même ces données.
        fenetre.add(affichage);
        
        fenetre.pack();
        fenetre.setLocationRelativeTo(null);
        fenetre.setVisible(true);
    }
}