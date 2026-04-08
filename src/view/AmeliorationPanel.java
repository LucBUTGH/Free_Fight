package view;

import java.awt.*;
import javax.swing.*;

import java.util.function.Consumer;
import model.Ameliorations;
import model.Barbare;
import model.Pekka;
import model.Sorcier;

// Panneau qui s'affiche entre le menu de démarrage et la partie.
// Le joueur y dépense l'or reçu en début de partie pour améliorer ses troupes.
// Chaque amélioration augmente les PV, les dégâts et la vitesse de la troupe.
public class AmeliorationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Ameliorations ameliorations;

    private final JLabel orLabel;

    // Une ligne d'interface par type de troupe
    private final JLabel barbareInfo;
    private final JLabel sorcierInfo;
    private final JLabel pekkaInfo;

    private final JButton btnBarbare;
    private final JButton btnSorcier;
    private final JButton btnPekka;

    // Construit l'écran d'amélioration. Il est composé de trois zones :
    // - en haut : titre + or disponible
    // - au centre : une ligne par troupe avec ses stats et un bouton d'amélioration
    // - en bas : un bouton pour lancer la bataille une fois les choix faits
    public AmeliorationPanel(Consumer<Ameliorations> onLancer) {
        this.ameliorations = new Ameliorations();

        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 45));
        setPreferredSize(new Dimension(720, 520));

        // --- En-tête ---
        JPanel header = new JPanel();
        header.setBackground(new Color(30, 30, 45));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titre = new JLabel("Améliorations des troupes", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 28));
        titre.setForeground(Color.WHITE);
        titre.setAlignmentX(CENTER_ALIGNMENT);

        orLabel = new JLabel("", SwingConstants.CENTER);
        orLabel.setFont(new Font("Arial", Font.BOLD, 20));
        orLabel.setForeground(new Color(255, 215, 0));
        orLabel.setAlignmentX(CENTER_ALIGNMENT);

        header.add(titre);
        header.add(Box.createVerticalStrut(10));
        header.add(orLabel);

        add(header, BorderLayout.NORTH);

        // --- Liste des troupes ---
        JPanel centre = new JPanel();
        centre.setBackground(new Color(30, 30, 45));
        centre.setLayout(new GridLayout(3, 1, 10, 10));
        centre.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        barbareInfo = new JLabel();
        sorcierInfo = new JLabel();
        pekkaInfo = new JLabel();

        btnBarbare = new JButton();
        btnSorcier = new JButton();
        btnPekka = new JButton();

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

        centre.add(creerLigneTroupe("Barbare", barbareInfo, btnBarbare));
        centre.add(creerLigneTroupe("Sorcier", sorcierInfo, btnSorcier));
        centre.add(creerLigneTroupe("Pekka",   pekkaInfo,   btnPekka));

        add(centre, BorderLayout.CENTER);

        // --- Pied de page : bouton Lancer ---
        JPanel bas = new JPanel();
        bas.setBackground(new Color(30, 30, 45));
        bas.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        JButton btnLancer = new JButton("Lancer la bataille");
        btnLancer.setFont(new Font("Arial", Font.BOLD, 20));
        btnLancer.setBackground(new Color(180, 30, 30));
        btnLancer.setForeground(Color.WHITE);
        btnLancer.setFocusPainted(false);
        btnLancer.addActionListener(e -> onLancer.accept(ameliorations));

        bas.add(btnLancer);
        add(bas, BorderLayout.SOUTH);

        rafraichir();
    }

    // Crée une ligne d'affichage : nom, infos stats, bouton d'amélioration
    private JPanel creerLigneTroupe(String nom, JLabel info, JButton bouton) {
        JPanel ligne = new JPanel(new BorderLayout(10, 0));
        ligne.setBackground(new Color(50, 50, 70));
        ligne.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel nomLabel = new JLabel(nom);
        nomLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nomLabel.setForeground(Color.WHITE);
        nomLabel.setPreferredSize(new Dimension(90, 30));

        info.setFont(new Font("SansSerif", Font.PLAIN, 14));
        info.setForeground(new Color(220, 220, 220));

        bouton.setFont(new Font("Arial", Font.BOLD, 14));
        bouton.setBackground(new Color(80, 140, 80));
        bouton.setForeground(Color.WHITE);
        bouton.setFocusPainted(false);
        bouton.setPreferredSize(new Dimension(180, 36));

        ligne.add(nomLabel, BorderLayout.WEST);
        ligne.add(info, BorderLayout.CENTER);
        ligne.add(bouton, BorderLayout.EAST);

        return ligne;
    }

    // Met à jour tous les textes et l'état des boutons en fonction
    // de l'or restant et des niveaux actuels.
    private void rafraichir() {
        orLabel.setText("Or disponible : " + ameliorations.getOr());

        // Troupes de prévisualisation pour lire les stats au niveau courant
        Barbare b = new Barbare(0, 0, ameliorations.getNiveauBarbare());
        Sorcier s = new Sorcier(0, 0, ameliorations.getNiveauSorcier());
        Pekka   p = new Pekka  (0, 0, ameliorations.getNiveauPekka());

        barbareInfo.setText(formatStats(b.getHealth(), degatsBarbare(b.getNiveau()), vitesseBarbare(b.getNiveau()), b.getNiveau()));
        sorcierInfo.setText(formatStats(s.getHealth(), degatsSorcier(s.getNiveau()), vitesseSorcier(s.getNiveau()), s.getNiveau()));
        pekkaInfo  .setText(formatStats(p.getHealth(), degatsPekka(p.getNiveau()),   vitessePekka(p.getNiveau()),   p.getNiveau()));

        majBouton(btnBarbare, ameliorations.getNiveauBarbare(),
                Ameliorations.coutBarbare(ameliorations.getNiveauBarbare() + 1));
        majBouton(btnSorcier, ameliorations.getNiveauSorcier(),
                Ameliorations.coutSorcier(ameliorations.getNiveauSorcier() + 1));
        majBouton(btnPekka, ameliorations.getNiveauPekka(),
                Ameliorations.coutPekka(ameliorations.getNiveauPekka() + 1));
    }

    // Construit le texte HTML qui affiche les stats d'une troupe
    // (niveau courant, PV, dégâts, vitesse) dans son JLabel.
    private String formatStats(int pv, int dmg, int spd, int niveau) {
        return "<html>Niv. " + niveau + "/" + Ameliorations.NIVEAU_MAX
                + " &nbsp;&nbsp; PV: <b>" + pv + "</b>"
                + " &nbsp;&nbsp; Dégâts: <b>" + dmg + "</b>"
                + " &nbsp;&nbsp; Vitesse: <b>" + spd + "</b></html>";
    }

    // Met à jour le texte et l'activation d'un bouton d'amélioration :
    // - désactivé et grisé si la troupe est déjà au niveau max
    // - désactivé et grisé si le joueur n'a pas assez d'or
    // - actif (vert) sinon, avec le coût de la prochaine amélioration
    private void majBouton(JButton btn, int niveauActuel, int cout) {
        if (niveauActuel >= Ameliorations.NIVEAU_MAX) {
            btn.setText("Niveau max");
            btn.setEnabled(false);
            btn.setBackground(new Color(90, 90, 90));
        } else if (ameliorations.getOr() < cout) {
            btn.setText("Améliorer (" + cout + " or)");
            btn.setEnabled(false);
            btn.setBackground(new Color(90, 90, 90));
        } else {
            btn.setText("Améliorer (" + cout + " or)");
            btn.setEnabled(true);
            btn.setBackground(new Color(80, 140, 80));
        }
    }

    // Petits helpers pour récupérer les stats d'une troupe sans les lire depuis
    // la classe (qui ne les expose pas toutes). On recrée la formule de chaque troupe.
    private int degatsBarbare(int n) { return 20 + 5 * (n - 1); }
    private int degatsSorcier(int n) { return 40 + 10 * (n - 1); }
    private int degatsPekka(int n)   { return 60 + 15 * (n - 1); }

    private int vitesseBarbare(int n) { return 2 + (n - 1) / 2; }
    private int vitesseSorcier(int n) { return 3 + (n - 1) / 2; }
    private int vitessePekka(int n)   { return 1 + (n - 1) / 2; }
}
