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

/**
 * Classe principale du jeu FreeFight.
 *
 * Gère la navigation entre les différents écrans grâce à un CardLayout :
 *   - "start"  : menu de sélection des niveaux (StartPanel)
 *   - "amelio" : écran d'amélioration des troupes avant le combat (AmeliorationPanel)
 *   - "jeu"    : écran de combat en cours (Affichage)
 *   - "fin"    : écran de résultats après le combat (FinPanel)
 *
 * La sauvegarde (or total + niveau débloqué) est chargée au démarrage
 * et mise à jour après chaque combat.
 */
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

    /**
     * Constructeur principal.
     * Charge la sauvegarde, crée la fenêtre Swing et affiche le menu de départ.
     */
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

    /**
     * Construit le StartPanel avec l'or actuel et le niveau débloqué,
     * puis affiche l'écran de sélection des niveaux.
     */
    private void afficherMenu() {
        StartPanel menu = new StartPanel(sauvegarde.getOrTotal(), sauvegarde.getNiveauDebloque(), this::choisirNiveau);
        root.add(menu, CARTE_START);
        cards.show(root, CARTE_START);
        fenetre.pack();
    }

    /**
     * Appelée quand le joueur clique sur un niveau dans le menu.
     * Mémorise le niveau choisi et affiche l'écran d'amélioration des troupes.
     *
     * @param niveau  Numéro du niveau sélectionné (1 à NOMBRE_NIVEAUX)
     */
    private void choisirNiveau(int niveau) {
        niveauActuel = niveau;
        AmeliorationPanel amelioPanel = new AmeliorationPanel(this::demarrerCombat, sauvegarde.getOrTotal());
        root.add(amelioPanel, CARTE_AMELIO);
        cards.show(root, CARTE_AMELIO);
        fenetre.pack();
    }

    /**
     * Lance un combat après que le joueur a validé ses améliorations.
     *
     * Crée une nouvelle Partie avec les améliorations choisies et le niveau actuel,
     * instancie le GameController et l'Affichage, puis démarre la boucle de jeu.
     * À la fin du combat, le callback afficherEcranFin() sera appelé automatiquement.
     *
     * @param ameliorations  Les améliorations achetées par le joueur avant le combat
     */
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

    /**
     * Affiche l'écran de résultats à la fin d'un combat.
     *
     * Récupère le score, les étoiles, le temps restant et l'or gagné depuis
     * le contrôleur, met à jour la sauvegarde (or + éventuellement niveau débloqué),
     * puis construit et affiche le FinPanel.
     *
     * La "vraie victoire" exige que l'Hôtel de Ville ennemi soit détruit.
     * Le bouton "niveau suivant" n'est activé que si le niveau suivant est déjà débloqué.
     */
    private void afficherEcranFin() {
        if (controleurActuel == null) return;

        int score    = controleurActuel.getScore();
        int etoiles  = controleurActuel.getEtoiles();
        int temps    = controleurActuel.getTempsRestant();
        int orGagne  = controleurActuel.getOrGagne();

        // L'Hôtel de Ville doit être détruit pour valider la victoire et débloquer
        boolean vraiVictoire = controleurActuel.hotelDeVilleDetruit();

        sauvegarde.ajouterOr(orGagne);
        //boolean nouveauNiveau = vraiVictoire && sauvegarde.debloquerNiveauSuivant(niveauActuel);
        sauvegarde.sauvegarder();

        boolean peutAvancer = vraiVictoire && niveauActuel < sauvegarde.getNiveauDebloque();

        FinPanel finPanel = new FinPanel(
            score, etoiles, temps, orGagne,
            niveauActuel, peutAvancer,
            this::rejouer, this::niveauSuivant, this::retourMenu
        );

        root.add(finPanel, CARTE_FIN);
        cards.show(root, CARTE_FIN);
        fenetre.pack();
    }

    /**
     * Rejoue le même niveau en revenant à l'écran d'amélioration.
     * Le niveau actuel (niveauActuel) reste inchangé.
     */
    private void rejouer() {
        AmeliorationPanel amelioPanel = new AmeliorationPanel(this::demarrerCombat, sauvegarde.getOrTotal());
        root.add(amelioPanel, CARTE_AMELIO);
        cards.show(root, CARTE_AMELIO);
        fenetre.pack();
    }

    /**
     * Passe au niveau suivant (plafonné à NOMBRE_NIVEAUX)
     * et affiche l'écran d'amélioration pour ce nouveau niveau.
     */
    private void niveauSuivant() {
        niveauActuel = Math.min(niveauActuel + 1, Sauvegarde.NOMBRE_NIVEAUX);
        AmeliorationPanel amelioPanel = new AmeliorationPanel(this::demarrerCombat, sauvegarde.getOrTotal());
        root.add(amelioPanel, CARTE_AMELIO);
        cards.show(root, CARTE_AMELIO);
        fenetre.pack();
    }

    /**
     * Retourne au menu principal de sélection des niveaux.
     */
    private void retourMenu() {
        afficherMenu();
    }

    /**
     * Point d'entrée de l'application.
     * Lance la fenêtre sur le thread Swing (EDT) via invokeLater.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Test::new);
    }
}
