//package test;
//
//import controller.GameController;
//import model.*;
//import view.Affichage;
//import view.AmeliorationPanel;
//import view.StartPanel;
//
//import javax.swing.JFrame;
//
//public class Test {
//
//    /* la fenêtre d'affichage : menu au début puis fenêtre de jeu */
//    private JFrame fenetre;
//    private StartPanel startPanel;             // le panneau de départ
//    private AmeliorationPanel ameliorationPanel; // le panneau d'amélioration
//    private Affichage affichage;               // la vue principale
//
//    /* On va créer juste une instance */
//    public Test() {
//        fenetre = new JFrame("FreeFight");
//        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//        // Appel de la méthode maintenant existante
//        startPanel = new StartPanel(this::lancerJeu);
//        fenetre.add(startPanel);
//
//        fenetre.pack();
//        fenetre.setResizable(false);
//        fenetre.setVisible(true);
//    }
//
//    public static void main(String[] args) {
//        new Test();
//    }
//
//    // Étape 1 : depuis le menu de démarrage, on ouvre le panneau d'amélioration
//    // pour que le joueur dépense son or avant le combat.
//    public void lancerJeu() {
//        fenetre.setTitle("FreeFight – Améliorations");
//
//        fenetre.setVisible(false);
//        fenetre.remove(startPanel);
//
//        ameliorationPanel = new AmeliorationPanel(this::demarrerCombat);
//        fenetre.add(ameliorationPanel);
//
//        fenetre.pack();
//        fenetre.setLocationRelativeTo(null);
//        fenetre.setVisible(true);
//    }
//
//    // Étape 2 : une fois les améliorations choisies, on lance la partie
//    // en créant les troupes avec les niveaux sélectionnés par le joueur.
//    public void demarrerCombat(Ameliorations ameliorations) {
//        fenetre.setTitle("FreeFight – Test Portée Défenses");
//
//        // Création de la partie
//        Partie partie = new Partie();
//
//        // Création des troupes dans la partie (avec leur niveau d'amélioration)
//        partie.ajouterTroupe(new Barbare(50,  500, ameliorations.getNiveauBarbare()));
//        partie.ajouterTroupe(new Sorcier(120, 500, ameliorations.getNiveauSorcier()));
//        partie.ajouterTroupe(new Pekka  (190, 500, ameliorations.getNiveauPekka()));
//
//        // Remplacer le contenu de la fenêtre
//        fenetre.setVisible(false);
//        if (ameliorationPanel != null) {
//            fenetre.remove(ameliorationPanel);
//        }
//
//        GameController controller = new GameController(partie);
//        affichage = new Affichage(partie, controller);
//        controller.setAffichage(affichage);
//
//        fenetre.add(affichage);
//
//        fenetre.pack();
//        fenetre.setLocationRelativeTo(null);
//        fenetre.setVisible(true);
//
//        controller.demarrer();
//    }
//}
package test;


