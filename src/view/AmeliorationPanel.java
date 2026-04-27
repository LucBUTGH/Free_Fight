package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import model.Ameliorations;
import model.Barbare;
import model.Pekka;
import model.Sorcier;

/**
 * Écran d'amélioration des troupes entre le menu et la bataille.
 *
 * FONCTIONNALITÉS :
 * - Affiche les 3 types de troupes avec leurs stats et niveau actuels.
 * - Permet au joueur d'acheter des améliorations avec l'or disponible.
 * - Met à jour l'affichage en temps réel des stats (PV, dégâts, vitesse).
 * - Désactive les boutons si : troupe déjà max, or insuffisant, ou pas de troupes.
 *
 * LAYOUT :
 * - HAUT    : Titre + Or disponible
 * - CENTRE  : 3 lignes (une par type de troupe) avec nom, stats, bouton d'amélioration
 * - BAS     : Bouton "Lancer la bataille" pour commencer le combat
 *
 * UI/UX :
 * - Utilise des couleurs thématisées : fond sombre, texte doré pour l'or, vert pour boutons actifs.
 * - Les boutons inactifs sont grisés avec une explication du motif (niveau max / or insuffisant).
 *
 * @author Développeur principal
 * @version 1.0
 */
public class AmeliorationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // ═══ Constantes de thème UI ═══
    private static final Color COLOR_BACKGROUND = new Color(30, 30, 45);
    private static final Color COLOR_PANEL_BG = new Color(50, 50, 70);
    private static final Color COLOR_TEXT_PRIMARY = Color.WHITE;
    private static final Color COLOR_TEXT_SECONDARY = new Color(220, 220, 220);
    private static final Color COLOR_OR = new Color(255, 215, 0);
    private static final Color COLOR_BUTTON_ACTIVE = new Color(80, 140, 80);
    private static final Color COLOR_BUTTON_INACTIVE = new Color(90, 90, 90);
    private static final Color COLOR_BUTTON_LAUNCH = new Color(180, 30, 30);

    // ═══ Constantes de dimensions UI ═══
    private static final int PANEL_WIDTH = 720;
    private static final int PANEL_HEIGHT = 520;
    private static final int TITLE_FONT_SIZE = 28;
    private static final int OR_LABEL_FONT_SIZE = 20;
    private static final int TROOP_NAME_FONT_SIZE = 18;
    private static final int STATS_FONT_SIZE = 14;
    private static final int BUTTON_FONT_SIZE = 14;
    private static final int LAUNCH_BUTTON_FONT_SIZE = 20;
    private static final int BUTTON_WIDTH = 180;
    private static final int BUTTON_HEIGHT = 36;
    private static final int TROOP_NAME_WIDTH = 90;

    // ═══ Formules de calcul des stats (doivent correspondre à celles des troupes) ═══
    // Source : classe Barbare, Sorcier, Pekka
    private static final int DMG_BASE_BARBARE = 20;
    private static final int DMG_PROGRESSION_BARBARE = 5;
    private static final int DMG_BASE_SORCIER = 40;
    private static final int DMG_PROGRESSION_SORCIER = 10;
    private static final int DMG_BASE_PEKKA = 60;
    private static final int DMG_PROGRESSION_PEKKA = 15;
    
    private static final int SPEED_BASE_BARBARE = 2;
    private static final int SPEED_BASE_SORCIER = 3;
    private static final int SPEED_BASE_PEKKA = 1;
    private static final int SPEED_PROGRESSION = 2;  // +1 vitesse tous les 2 niveaux

    // ═══ État du jeu ═══
    private final Ameliorations ameliorations;

    // ─── Composants d'affichage des troupes ───
    /** Affiche l'or disponible en haut du panneau. */
    private JLabel orLabel;
    
    /** Affiche les stats du Barbare (niveau, PV, dégâts, vitesse). */
    private JLabel barbareInfo;
    /** Affiche les stats du Sorcier. */
    private JLabel sorcierInfo;
    /** Affiche les stats du Pekka. */
    private JLabel pekkaInfo;

    // ─── Boutons d'amélioration ───
    /** Bouton pour améliorer le Barbare au niveau suivant. */
    private JButton btnBarbare;
    /** Bouton pour améliorer le Sorcier au niveau suivant. */
    private JButton btnSorcier;
    /** Bouton pour améliorer le Pekka au niveau suivant. */
    private JButton btnPekka;

    /**
     * Construit le panneau d'amélioration des troupes.
     *
     * LAYOUT (BorderLayout) :
     *   - NORTH : En-tête avec titre et or disponible
     *   - CENTER : GridLayout 3×1 avec les 3 types de troupes
     *   - SOUTH : Bouton "Lancer la bataille"
     *
     * @param onLancer Callback exécuté quand le joueur clique sur "Lancer la bataille".
     *                 Reçoit en paramètre l'objet Ameliorations configuré.
     * @param orInitial Or initial du joueur (remplace OR_DEPART si différent)
     */
    public AmeliorationPanel(Consumer<Ameliorations> onLancer, int orInitial) {
        this.ameliorations = new Ameliorations(orInitial);

        // ═══ Configuration globale du panneau ═══
        setLayout(new BorderLayout());
        setBackground(COLOR_BACKGROUND);
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));

        // ═══ SECTION HAUT : En-tête (titre + or) ═══
        JPanel header = creerEnTete();
        add(header, BorderLayout.NORTH);

        // ═══ SECTION CENTRE : Les 3 types de troupes ═══
        JPanel centre = creerListeTroupes();
        add(centre, BorderLayout.CENTER);

        // ═══ SECTION BAS : Bouton Lancer ═══
        JPanel bas = creerPiedPage(onLancer);
        add(bas, BorderLayout.SOUTH);

        // Initialise l'affichage des stats et états des boutons
        rafraichir();
    }

    /**
     * Crée l'en-tête du panneau (titre + or disponible).
     * @return Panneau JPanel configuré
     */
    private JPanel creerEnTete() {
        JPanel header = new JPanel();
        header.setBackground(COLOR_BACKGROUND);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        // Titre
        JLabel titre = new JLabel("Améliorations des troupes", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, TITLE_FONT_SIZE));
        titre.setForeground(COLOR_TEXT_PRIMARY);
        titre.setAlignmentX(CENTER_ALIGNMENT);

        // Or disponible
        orLabel = new JLabel("", SwingConstants.CENTER);
        orLabel.setFont(new Font("Arial", Font.BOLD, OR_LABEL_FONT_SIZE));
        orLabel.setForeground(COLOR_OR);
        orLabel.setAlignmentX(CENTER_ALIGNMENT);

        header.add(titre);
        header.add(Box.createVerticalStrut(10));
        header.add(orLabel);

        return header;
    }

    /**
     * Crée la section centrale avec les 3 types de troupes.
     * @return Panneau JPanel en GridLayout 3×1
     */
    private JPanel creerListeTroupes() {
        JPanel centre = new JPanel();
        centre.setBackground(COLOR_BACKGROUND);
        centre.setLayout(new GridLayout(3, 1, 10, 10));
        centre.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        // Initialise les labels d'infos et boutons
        barbareInfo = new JLabel();
        sorcierInfo = new JLabel();
        pekkaInfo = new JLabel();

        btnBarbare = new JButton();
        btnSorcier = new JButton();
        btnPekka = new JButton();

        // Listeners pour acheter les améliorations et rafraîchir
        btnBarbare.addActionListener(e -> {
            ameliorations.ameliorerBarbare();
            rafraichir();
        });
        btnSorcier.addActionListener(e -> {
            ameliorations.ameliorerSorcier();
            rafraichir();
        });
        btnPekka.addActionListener(e -> {
            ameliorations.ameliorerPekka();
            rafraichir();
        });

        // Crée les 3 lignes (une par type de troupe)
        centre.add(creerLigneTroupe("Barbare", barbareInfo, btnBarbare));
        centre.add(creerLigneTroupe("Sorcier", sorcierInfo, btnSorcier));
        centre.add(creerLigneTroupe("Pekka",   pekkaInfo,   btnPekka));

        return centre;
    }

    /**
     * Crée le pied de page avec le bouton "Lancer la bataille".
     * @param onLancer Callback à exécuter au clic
     * @return Panneau JPanel avec le bouton
     */
    private JPanel creerPiedPage(Consumer<Ameliorations> onLancer) {
        JPanel bas = new JPanel();
        bas.setBackground(COLOR_BACKGROUND);
        bas.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        JButton btnLancer = new JButton("Lancer la bataille");
        btnLancer.setFont(new Font("Arial", Font.BOLD, LAUNCH_BUTTON_FONT_SIZE));
        btnLancer.setBackground(COLOR_BUTTON_LAUNCH);
        btnLancer.setForeground(COLOR_TEXT_PRIMARY);
        btnLancer.setFocusPainted(false);
        btnLancer.addActionListener(e -> onLancer.accept(ameliorations));

        bas.add(btnLancer);
        return bas;
    }

    /**
     * Crée une ligne d'affichage pour un type de troupe.
     * LAYOUT : [Nom] [Stats HTML] [Bouton]
     *
     * @param nom Nom du type de troupe ("Barbare", "Sorcier", "Pekka")
     * @param info JLabel pour afficher les stats (sera mis à jour par rafraichir())
     * @param bouton JButton pour acheter l'amélioration
     * @return Panneau BorderLayout configured
     */
    private JPanel creerLigneTroupe(String nom, JLabel info, JButton bouton) {
        JPanel ligne = new JPanel(new BorderLayout(10, 0));
        ligne.setBackground(COLOR_PANEL_BG);
        ligne.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Colonne gauche : Nom de la troupe
        JLabel nomLabel = new JLabel(nom);
        nomLabel.setFont(new Font("Arial", Font.BOLD, TROOP_NAME_FONT_SIZE));
        nomLabel.setForeground(COLOR_TEXT_PRIMARY);
        nomLabel.setPreferredSize(new Dimension(TROOP_NAME_WIDTH, 30));

        // Colonne centre : Stats (mise à jour dynamiquement par rafraichir())
        info.setFont(new Font("SansSerif", Font.PLAIN, STATS_FONT_SIZE));
        info.setForeground(COLOR_TEXT_SECONDARY);

        // Colonne droite : Bouton d'amélioration (état mis à jour par majBouton())
        bouton.setFont(new Font("Arial", Font.BOLD, BUTTON_FONT_SIZE));
        bouton.setBackground(COLOR_BUTTON_ACTIVE);
        bouton.setForeground(COLOR_TEXT_PRIMARY);
        bouton.setFocusPainted(false);
        bouton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));

        ligne.add(nomLabel, BorderLayout.WEST);
        ligne.add(info, BorderLayout.CENTER);
        ligne.add(bouton, BorderLayout.EAST);

        return ligne;
    }

    /**
     * Rafraîchit l'affichage complet du panneau.
     *
     * ÉTAPES :
     *   1. Met à jour l'affichage de l'or disponible (haut du panneau)
     *   2. Crée des instances de troupes pour lire leurs stats au niveau courant
     *   3. Met à jour les labels d'infos de chaque troupe (niveau, PV, dégâts, vitesse)
     *   4. Met à jour l'état de chaque bouton (activé/désactivé + texte)
     */
    private void rafraichir() {
        // Affiche l'or disponible
        orLabel.setText("Or disponible : " + ameliorations.getOr());

        // Crée des instances de troupes pour accéder à leurs PV au niveau courant
        // (Les formules de stats sont appliquées dans le constructeur de chaque troupe)
        Barbare b = new Barbare(0, 0, ameliorations.getNiveauBarbare());
        Sorcier s = new Sorcier(0, 0, ameliorations.getNiveauSorcier());
        Pekka   p = new Pekka  (0, 0, ameliorations.getNiveauPekka());

        // Met à jour les labels avec les stats formatées en HTML
        barbareInfo.setText(formatStats(b.getHealth(), degatsBarbare(b.getNiveau()), vitesseBarbare(b.getNiveau()), b.getNiveau()));
        sorcierInfo.setText(formatStats(s.getHealth(), degatsSorcier(s.getNiveau()), vitesseSorcier(s.getNiveau()), s.getNiveau()));
        pekkaInfo  .setText(formatStats(p.getHealth(), degatsPekka(p.getNiveau()),   vitessePekka(p.getNiveau()),   p.getNiveau()));

        // Met à jour l'état des boutons (activé/désactivé + texte informatif)
        majBouton(btnBarbare, ameliorations.getNiveauBarbare(),
                Ameliorations.coutBarbare(ameliorations.getNiveauBarbare() + 1));
        majBouton(btnSorcier, ameliorations.getNiveauSorcier(),
                Ameliorations.coutSorcier(ameliorations.getNiveauSorcier() + 1));
        majBouton(btnPekka, ameliorations.getNiveauPekka(),
                Ameliorations.coutPekka(ameliorations.getNiveauPekka() + 1));
    }

    /**
     * Formate les stats d'une troupe en HTML pour affichage dans un JLabel.
     *
     * CONTENU AFFICHÉ :
     *   Niv. X/5   PV: 100   Dégâts: 50   Vitesse: 3
     *
     * Utilise du HTML pour pouvoir formater différemment :
     *   - Niveau courant/max en texte normal
     *   - Valeurs numériques en gras pour plus de visibilité
     *   - Espaces multiples (&nbsp;) pour l'alignement
     *
     * @param pv Points de vie au niveau courant
     * @param dmg Dégâts au niveau courant
     * @param spd Vitesse au niveau courant
     * @param niveau Niveau courant de la troupe
     * @return String HTML formaté pour JLabel
     */
    private String formatStats(int pv, int dmg, int spd, int niveau) {
        return "<html>Niv. " + niveau + "/" + Ameliorations.NIVEAU_MAX
                + " &nbsp;&nbsp; PV: <b>" + pv + "</b>"
                + " &nbsp;&nbsp; Dégâts: <b>" + dmg + "</b>"
                + " &nbsp;&nbsp; Vitesse: <b>" + spd + "</b></html>";
    }

    /**
     * Met à jour l'état et l'affichage d'un bouton d'amélioration.
     *
     * ÉTATS POSSIBLES :
     *   1. NIVEAU MAX : Le bouton affiche "Niveau max" et est désactivé (grisé).
     *   2. OR INSUFFISANT : Le bouton affiche le coût et est désactivé (grisé).
     *   3. ACTIF : Le bouton affiche "Améliorer (XXX or)" et est clickable (vert).
     *
     * @param btn Le bouton à mettre à jour
     * @param niveauActuel Niveau courant de la troupe
     * @param cout Coût de la prochaine amélioration
     */
    private void majBouton(JButton btn, int niveauActuel, int cout) {
        if (niveauActuel >= Ameliorations.NIVEAU_MAX) {
            // Troupe déjà au maximum
            btn.setText("Niveau max");
            btn.setEnabled(false);
            btn.setBackground(COLOR_BUTTON_INACTIVE);
        } else if (ameliorations.getOr() < cout) {
            // Pas assez d'or pour l'amélioration suivante
            btn.setText("Améliorer (" + cout + " or)");
            btn.setEnabled(false);
            btn.setBackground(COLOR_BUTTON_INACTIVE);
        } else {
            // Amélioration disponible
            btn.setText("Améliorer (" + cout + " or)");
            btn.setEnabled(true);
            btn.setBackground(COLOR_BUTTON_ACTIVE);
        }
    }

    // ═══ Formules de calcul des stats (doivent rester synchronisées avec les classes de troupes) ═══

    /**
     * Calcule les dégâts d'un Barbare au niveau donné.
     *
     * FORMULE : 20 + 5 × (niveau - 1)
     *   - Niveau 1 : 20 dmg
     *   - Niveau 2 : 25 dmg
     *   - Niveau 3 : 30 dmg
     *   - Niveau 4 : 35 dmg
     *   - Niveau 5 : 40 dmg
     *
     * @param n Niveau de la troupe (1 à NIVEAU_MAX)
     * @return Dégâts infligés par ce Barbare
     */
    private int degatsBarbare(int n) { 
        return DMG_BASE_BARBARE + DMG_PROGRESSION_BARBARE * (n - 1); 
    }

    /**
     * Calcule les dégâts d'un Sorcier au niveau donné.
     *
     * FORMULE : 40 + 10 × (niveau - 1)
     *   - Niveau 1 : 40 dmg
     *   - Niveau 2 : 50 dmg
     *   - Niveau 3 : 60 dmg
     *   - Niveau 4 : 70 dmg
     *   - Niveau 5 : 80 dmg
     *
     * @param n Niveau de la troupe (1 à NIVEAU_MAX)
     * @return Dégâts infligés par ce Sorcier
     */
    private int degatsSorcier(int n) { 
        return DMG_BASE_SORCIER + DMG_PROGRESSION_SORCIER * (n - 1); 
    }

    /**
     * Calcule les dégâts d'un Pekka au niveau donné.
     *
     * FORMULE : 60 + 15 × (niveau - 1)
     *   - Niveau 1 : 60 dmg
     *   - Niveau 2 : 75 dmg
     *   - Niveau 3 : 90 dmg
     *   - Niveau 4 : 105 dmg
     *   - Niveau 5 : 120 dmg
     *
     * @param n Niveau de la troupe (1 à NIVEAU_MAX)
     * @return Dégâts infligés par ce Pekka
     */
    private int degatsPekka(int n) { 
        return DMG_BASE_PEKKA + DMG_PROGRESSION_PEKKA * (n - 1); 
    }

    /**
     * Calcule la vitesse d'un Barbare au niveau donné.
     *
     * FORMULE : 2 + (niveau - 1) / 2
     *   - Niveau 1 : 2 vitesse
     *   - Niveau 2 : 2 vitesse
     *   - Niveau 3 : 3 vitesse
     *   - Niveau 4 : 3 vitesse
     *   - Niveau 5 : 4 vitesse
     *
     * La vitesse augmente tous les 2 niveaux (progression lente).
     *
     * @param n Niveau de la troupe (1 à NIVEAU_MAX)
     * @return Vitesse de déplacement
     */
    private int vitesseBarbare(int n) { 
        return SPEED_BASE_BARBARE + (n - 1) / SPEED_PROGRESSION; 
    }
    /**
     * Calcule la vitesse d'un Sorcier au niveau donné.
     *
     * FORMULE : 3 + (niveau - 1) / 2
     *   - Niveau 1 : 3 vitesse
     *   - Niveau 2 : 3 vitesse
     *   - Niveau 3 : 4 vitesse
     *   - Niveau 4 : 4 vitesse
     *   - Niveau 5 : 5 vitesse
     *
     * Le Sorcier est plus rapide que le Barbare (base 3 vs 2).
     * La vitesse augmente tous les 2 niveaux.
     *
     * @param n Niveau de la troupe (1 à NIVEAU_MAX)
     * @return Vitesse de déplacement
     */
    private int vitesseSorcier(int n) { 
        return SPEED_BASE_SORCIER + (n - 1) / SPEED_PROGRESSION; 
    }

    /**
     * Calcule la vitesse d'un Pekka au niveau donné.
     *
     * FORMULE : 1 + (niveau - 1) / 2
     *   - Niveau 1 : 1 vitesse
     *   - Niveau 2 : 1 vitesse
     *   - Niveau 3 : 2 vitesse
     *   - Niveau 4 : 2 vitesse
     *   - Niveau 5 : 3 vitesse
     *
     * Le Pekka est plus lent que les autres (base 1). Cela compense sa puissance élevée.
     * La vitesse augmente tous les 2 niveaux.
     *
     * @param n Niveau de la troupe (1 à NIVEAU_MAX)
     * @return Vitesse de déplacement
     */
    private int vitessePekka(int n) { 
        return SPEED_BASE_PEKKA + (n - 1) / SPEED_PROGRESSION; 
    }
}
