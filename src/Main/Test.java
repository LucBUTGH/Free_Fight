package Main;

import javax.swing.JFrame;

import controller.GameController;
import model.Partie;
import view.Affichage;
import view.StartPanel;

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
        
        Partie partie = new Partie();
     

        // Création du contrôleur
        GameController controller = new GameController(partie);

        // Création de l'affichage à partir de la partie du contrôleur
        affichage = new Affichage(partie, controller);

        // On lie l'affichage au contrôleur
        controller.setAffichage(affichage);

        // On démarre le jeu
        controller.demarrer();

        // Remplacer le contenu de la fenêtre
        fenetre.setVisible(false);
        fenetre.remove(startPanel);

        // On ajoute la vue principale
        fenetre.add(affichage);

        fenetre.pack();
        fenetre.setLocationRelativeTo(null);
        fenetre.setVisible(true);
    }
}