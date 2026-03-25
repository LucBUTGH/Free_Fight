package test;

import model.*;
import view.Affichage;
import view.StartPanel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Test {

	/* la fenêtre d'affichage : menu au début puis fenêtre de jeu */
	private JFrame fenetre;
	private StartPanel startPanel; // le panneau de départ
	private Affichage affichage; // la vue principale
	
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
    	
        // Création troupes
        List<Troupe> troupes = new ArrayList<>();
        troupes.add(new Barbare(50, 500));
        troupes.add(new Sorcier(120, 500));
        troupes.add(new Pekka(190, 500));

        // Remplacer le contenu de la fenêtre
        fenetre.setVisible(false);
        fenetre.remove(startPanel);
        affichage = new Affichage(troupes);
        fenetre.add(affichage); // Pourquoi on passe du modèle dans la vue ?
        fenetre.pack();
        fenetre.setVisible(true);
    }
}