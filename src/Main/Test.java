package Main;

import controller.GameController;
import model.Ameliorations;
import model.Partie;
import view.Affichage;
import view.AmeliorationPanel;
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

    // Identifiants des écrans dans le CardLayout
    private static final String CARTE_START  = "start";
    private static final String CARTE_AMELIO = "amelio";
    private static final String CARTE_JEU    = "jeu";

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
     * Affiche le panneau d'amélioration des troupes.
     * Appelé quand le joueur clique sur "Start Game".
     */
    private void lancerJeu() {
        AmeliorationPanel amelioPanel = new AmeliorationPanel(this::demarrerCombat);
        root.add(amelioPanel, CARTE_AMELIO);
        cards.show(root, CARTE_AMELIO);
        fenetre.pack();
    }

    /**
     * Lance la partie avec les niveaux d'amélioration choisis.
     * Appelé quand le joueur clique sur "Lancer la bataille".
     */
    private void demarrerCombat(Ameliorations ameliorations) {

        // 1. Modèle
        Partie partie = new Partie(ameliorations);

        // 2. Contrôleur
        GameController controller = new GameController(partie);

        // 3. Vue
        Affichage affichage = new Affichage(partie, controller);

        // 4. Lier vue et contrôleur
        controller.setAffichage(affichage);

        // 5. Basculer sur l'écran de jeu
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