package Main;

import controller.GameController;
import model.Ameliorations;
import model.Partie;
import model.Sauvegarde;
import view.Affichage;
import view.AmeliorationPanel;
import view.FinPanel;
import view.StartPanel;

import javax.swing.*;
import java.awt.*;

public class Test {

    private static final String CARTE_START  = "start";
    private static final String CARTE_AMELIO = "amelio";
    private static final String CARTE_JEU    = "jeu";
    private static final String CARTE_FIN    = "fin";

    private final JFrame     fenetre;
    private final CardLayout cards;
    private final JPanel     root;

    private final Sauvegarde sauvegarde = new Sauvegarde();

    private GameController controleurActuel;
    private int niveauActuel = 1;

    public Test() {
        sauvegarde.charger();

        fenetre = new JFrame("FreeFight");
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenetre.setResizable(true);

        cards = new CardLayout();
        root  = new JPanel(cards);

        afficherMenu();

        fenetre.add(root);
        fenetre.pack();
        fenetre.setLocationRelativeTo(null);
        fenetre.setVisible(true);
    }

    // Construit et affiche le menu de sélection des niveaux
    private void afficherMenu() {
        StartPanel menu = new StartPanel(sauvegarde.getOrTotal(), sauvegarde.getNiveauDebloque(), this::choisirNiveau);
        root.add(menu, CARTE_START);
        cards.show(root, CARTE_START);
        fenetre.pack();
    }

    // Le joueur clique sur un niveau dans le menu
    private void choisirNiveau(int niveau) {
        niveauActuel = niveau;
        AmeliorationPanel amelioPanel = new AmeliorationPanel(this::demarrerCombat, sauvegarde.getOrTotal());
        root.add(amelioPanel, CARTE_AMELIO);
        cards.show(root, CARTE_AMELIO);
        fenetre.pack();
    }

    private void demarrerCombat(Ameliorations ameliorations) {
        Partie partie = new Partie(ameliorations, niveauActuel);

        controleurActuel = new GameController(partie);
        Affichage affichage = new Affichage(partie, controleurActuel);
        controleurActuel.setAffichage(affichage);
        controleurActuel.setFinPartieCallback(this::afficherEcranFin);

        root.add(affichage, CARTE_JEU);
        cards.show(root, CARTE_JEU);
        fenetre.pack();

        controleurActuel.demarrer();
    }

    private void afficherEcranFin() {
        if (controleurActuel == null) return;

        int score    = controleurActuel.getScore();
        int etoiles  = controleurActuel.getEtoiles();
        int temps    = controleurActuel.getTempsRestant();
        int orGagne  = controleurActuel.getOrGagne();

        // Mise à jour de la progression
        sauvegarde.ajouterOr(orGagne);
        boolean nouveauNiveau = etoiles > 0 && sauvegarde.debloquerNiveauSuivant(niveauActuel);
        sauvegarde.sauvegarder();

        boolean peutAvancer = nouveauNiveau || (etoiles > 0 && niveauActuel < sauvegarde.getNiveauDebloque());

        FinPanel finPanel = new FinPanel(
            score, etoiles, temps, orGagne,
            niveauActuel, peutAvancer,
            this::rejouer, this::niveauSuivant, this::retourMenu
        );

        root.add(finPanel, CARTE_FIN);
        cards.show(root, CARTE_FIN);
        fenetre.pack();
    }

    // Rejouer le même niveau
    private void rejouer() {
        AmeliorationPanel amelioPanel = new AmeliorationPanel(this::demarrerCombat, sauvegarde.getOrTotal());
        root.add(amelioPanel, CARTE_AMELIO);
        cards.show(root, CARTE_AMELIO);
        fenetre.pack();
    }

    // Passer au niveau suivant
    private void niveauSuivant() {
        niveauActuel = Math.min(niveauActuel + 1, Sauvegarde.NOMBRE_NIVEAUX);
        AmeliorationPanel amelioPanel = new AmeliorationPanel(this::demarrerCombat, sauvegarde.getOrTotal());
        root.add(amelioPanel, CARTE_AMELIO);
        cards.show(root, CARTE_AMELIO);
        fenetre.pack();
    }

    // Retour au menu principal
    private void retourMenu() {
        afficherMenu();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Test::new);
    }
}
