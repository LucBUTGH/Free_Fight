package Main;

import controller.GameController;
import model.Partie;
import view.Affichage;
import view.StartPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Point d'entrée de l'application.
 *
 * Utilise un CardLayout pour afficher StartPanel puis Affichage
 * dans la même fenêtre, sans jamais la recréer ni la cacher.
 *
 * Séquence de lancement :
 * 1. new Partie()          → crée le modèle
 * 2. new GameController()  → crée le contrôleur avec les timers
 * 3. new Affichage()       → crée la vue
 * 4. setAffichage()        → lie vue ↔ contrôleur
 * 5. cards.show()          → bascule l'écran
 * 6. demarrer()            → démarre les timers
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
        fenetre.setResizable(false);

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
        // Lancement sur l'Event Dispatch Thread — bonne pratique Swing
        SwingUtilities.invokeLater(Test::new);
    }
}