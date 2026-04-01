package Main;

import controller.GameController;
import model.Partie;
import view.Affichage;
import view.StartPanel;

import javax.swing.*;
import java.awt.*;

/**
 *
 * Utilise un CardLayout pour afficher StartPanel puis Affichage
 * dans la même fenêtre, sans jamais la recréer ni la cacher.
 *
 * Séquence de lancement nous avons:
 * 1. new Partie()         
 * 2. new GameController()  
 * 3. new Affichage()       
 * 4. setAffichage()        
 * 5. cards.show()          
 * 6. demarrer()           
 */
public class Test {

    // Identifiants des deux écrans dans le CardLayout
    private static final String CARTE_START = "start";
    private static final String CARTE_JEU   = "jeu";

    private final JFrame     fenetre;
    private final CardLayout cards;
    private final JPanel     root;

    public Test() {
        fenetre = new JFrame("FreeFight");
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenetre.setResizable(true);

        // CardLayout : permet de swapper les panels sans recréer la fenêtre
        cards = new CardLayout();
        root  = new JPanel(cards);

        // Premier écran : menu de démarrage
        root.add(new StartPanel(this::lancerJeu), CARTE_START);

        fenetre.add(root);
        fenetre.pack();
        fenetre.setLocationRelativeTo(null);
        fenetre.setVisible(true);
    }

    /**
     * Lance la partie en construisant et reliant les 3 couches MVC.
     * Appelé quand le joueur clique sur "Start Game".
     */
    private void lancerJeu() {

        // 1. Modèle : tout l'état du jeu
        Partie partie = new Partie();

        // 2. Contrôleur : timers + interprétation des clics
        GameController controller = new GameController(partie);

        // 3. Vue : dessin uniquement
        Affichage affichage = new Affichage(partie, controller);

        // 4. Lier la vue au contrôleur (installe le MouseListener)
        controller.setAffichage(affichage);

        // 5. Basculer sur l'écran de jeu sans recréer la fenêtre
        root.add(affichage, CARTE_JEU);
        cards.show(root, CARTE_JEU);
        fenetre.pack();

        // 6. Démarrer les timers
        controller.demarrer();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Test::new);
    }
}