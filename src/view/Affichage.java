package view;

import controller.GameController;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import model.Barbare;
import model.Batiment;
import model.Chateau;
import model.Defense;
import model.Partie;
import model.Pekka;
import model.Sorcier;
import model.Troupe;

/**
 * Vue principale du jeu.
 *
 * Responsabilités (uniquement) :
 *  - Dessiner l'état du jeu (troupes, bâtiments, grille, chrono, barre du bas)
 *  - Exposer getMapPanel() pour que le contrôleur y branche son listener souris
 *  - Recevoir de l'extérieur les données d'état visuel via setClickInfo()
 *
 * Ce qu'elle NE fait PAS :
 *  - Timers (délégués au contrôleur)
 *  - Interprétation des clics (délégués au contrôleur)
 *  - Création / modification du modèle
 */
public class Affichage extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Partie         partie;
    private final GameController controller;
    private final MapPanel       mapPanel;

    // ── État visuel (fourni par le contrôleur via setClickInfo) 
    // Point de clic utilisé pour tester la portée des défenses
    private Point clickPoint = null;
    // Liste des défenses qui sont en portée du point cliqué
    private List<Defense> defensesEnPortee = new ArrayList<>();

    // ── Images des troupes 
    private final Image barbareImg;
    private final Image sorcierImg;
    private final Image pekkaImg;

    // Message affiché quand le stock d'un type de troupe est épuisé
    private String messageStock = null;
    private int ticksMessage    = 0;          // compteur pour faire disparaître le message
    private static final int DUREE_MESSAGE = 60; // ticks d'affichage (~2.4 secondes)

    /**
     * Crée la vue principale du jeu.
     *
     * @param partie      L'état du jeu à afficher (lecture seule)
     * @param controller  Le contrôleur (pour lire la sélection courante)
     */
    public Affichage(Partie partie, GameController controller) {
        this.partie     = partie;
        this.controller = controller;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 720));

        barbareImg = chargerImage("res/Barbare.png");
        sorcierImg = chargerImage("res/Sorcier.png");
        pekkaImg   = chargerImage("res/Pekka.png");

        mapPanel = new MapPanel();
        add(mapPanel, BorderLayout.CENTER);
    }

    // Charge une image depuis le classpath (fonctionne quel que soit le répertoire de lancement)
    private Image chargerImage(String chemin) {
        java.net.URL url = getClass().getClassLoader().getResource(chemin);
        if (url != null) return new ImageIcon(url).getImage();
        return null;
    }


    /**
     * Exposé pour que le contrôleur branche son MouseListener.
     * @return Le panneau de la carte
     */
    public MapPanel getMapPanel() {
        return mapPanel;
    }

    /**
     * Mis à jour par le contrôleur après un clic libre (test de portée).
     * La vue stocke ces infos uniquement pour les dessiner.
     *
     * @param x        Coordonnée X du clic
     * @param y        Coordonnée Y du clic
     * @param enPortee Liste des défenses à portée du clic
     */
    public void setClickInfo(int x, int y, List<Defense> enPortee) {
        this.clickPoint       = new Point(x, y);
        this.defensesEnPortee = enPortee;
    }
    
    /**
     * Affiche un message de stock vide à l'écran pendant quelques secondes.
     * Appelé par le contrôleur quand le stock d'un type est épuisé.
     *
     * @param message  Le message à afficher
     */
    public void setMessageStock(String message) {
        this.messageStock  = message;
        this.ticksMessage  = 0;
    }

    public class MapPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        private static final int CELL     = 50; // taille d'une cellule de la grille
        private static final int DEF_SIZE = 30; // taille d'une défense en pixels

            // ── Constantes pour l'affichage des étoiles (style Clash of Clans) ──
            private static final int STAR_SIZE = 28;         // Diamètre d'une étoile en pixels
            private static final int STAR_SPACING = 8;       // Espace entre deux étoiles
            private static final int STAR_Y_POSITION = 46;   // Position Y depuis le haut
            private static final double STAR_INNER_RATIO = 0.4;  // Ratio rayon intérieur / rayon extérieur
            private static final int STAR_COUNT = 3;         // Nombre d'étoiles Clash of Clans
            private static final int STAR_POINTS = 10;       // Nombre de points pour dessiner une étoile
            
            // Couleurs des étoiles
            private static final Color STAR_FILLED_COLOR = new Color(255, 215, 0);     // Or pour étoiles gagnées
            private static final Color STAR_FILLED_BORDER = new Color(200, 160, 0);   // Bordure or foncé
            private static final Color STAR_EMPTY_FILL = new Color(80, 80, 80);       // Gris pour étoiles non gagnées
            private static final Color STAR_EMPTY_BORDER = new Color(140, 140, 140);  // Gris clair pour bordure
            private static final Color STARS_BACKGROUND = new Color(0, 0, 0, 160);    // Fond semi-transparent
            private static final Color DESTRUCTION_TEXT_COLOR = Color.WHITE;          // Texte du pourcentage
        
        /**
         * Constructeur du panneau de la carte.
         * Définit la couleur de fond verte (herbe).
         */
        public MapPanel() {
            setBackground(new Color(34, 139, 34)); // vert herbe (Clash of Clans style)
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Ordre de dessin : du fond vers le premier plan
            dessinerGrille(g2);
            dessinerBatimentsNormaux(g2);
            dessinerHotelDeVille(g2);
            dessinerChateau(g2);
            dessinerDefenses(g2);
            dessinerResultatPortee(g2);
            dessinerChrono(g2);
            dessinerEtoiles(g2);

            // Dessine la barre en bas de l'écran
            g.setColor(new Color(50, 50, 50));
            g.fillRect(0, getHeight() - 100, getWidth(), 100);

            // Troupes et avatars par-dessus tout
            dessinerTroupesSurCarte(g);
            dessinerAvatarsBarre(g);
            dessinerResultat(g2);

            // Écran de fin par-dessus tout
            if (partie.estTerminee()) {
                dessinerEcranFin(g2);
            }
        }

        // Méthode qui retourne la troupe située à la position du clic de la souris.
        // Parcourir la liste des troupes et vérifier si les coordonnées de la souris
        // Si une troupe est trouvée, la retourner, sinon retourner null.
//        private Troupe getTroupeAtPosition(int mouseX, int mouseY) {
//            for (Troupe t : partie.getTroupes()) {
//                int tx = t.getX();
//                int ty = t.getY();
//
//                if (mouseX >= tx && mouseX <= tx + 40 &&
//                        mouseY >= ty && mouseY <= ty + 40) {
//                    return t;
//                }
//            }
//            return null;
//        }

        // Dessine une grille sur le terrain
        private void dessinerGrille(Graphics2D g2) {
            g2.setColor(new Color(0, 80, 0, 90));
            for (int x = 0; x < getWidth(); x += CELL)  g2.drawLine(x, 0, x, getHeight());
            for (int y = 0; y < getHeight(); y += CELL) g2.drawLine(0, y, getWidth(), y);
        }


        /** Dessine l'hôtel de ville s'il n'est pas détruit. */
        private void dessinerHotelDeVille(Graphics2D g2) {
            if (partie.getHotelDeVille().estDetruit()) return;

            final int SIZE = 50;
            int x  = partie.getHotelDeVille().getX();
            int y  = partie.getHotelDeVille().getY();
            int bx = x - SIZE / 2;
            int by = y - SIZE / 2;

            // Corps du bâtiment
            g2.setColor(new Color(218, 165, 32));
            g2.fillRect(bx, by, SIZE, SIZE);
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(3));
            g2.drawRect(bx, by, SIZE, SIZE);
            g2.setStroke(new BasicStroke(1));

            // Toit
            int[] xs = {bx, bx + SIZE / 2, bx + SIZE};
            int[] ys = {by, by - 18, by};
            g2.setColor(new Color(160, 82, 45));
            g2.fillPolygon(xs, ys, 3);
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(2));
            g2.drawPolygon(xs, ys, 3);
            g2.setStroke(new BasicStroke(1));

            // Étoile centrale
            g2.setColor(new Color(255, 255, 200));
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x - 10, y, x + 10, y);
            g2.drawLine(x, y - 10, x, y + 10);
            g2.setStroke(new BasicStroke(1));

            // Barre de vie
            dessinerBarreVie(g2, bx, by - 8, SIZE,
                partie.getHotelDeVille().getPv(),
                partie.getHotelDeVille().getPvMax());

            // Étiquette nom
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            String label = partie.getHotelDeVille().getNom();
            g2.setColor(Color.BLACK);
            g2.drawString(label, x - fm.stringWidth(label) / 2 + 1, y + SIZE / 2 + 17);
            g2.setColor(new Color(255, 240, 150));
            g2.drawString(label, x - fm.stringWidth(label) / 2, y + SIZE / 2 + 16);

            // Info PV
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            fm = g2.getFontMetrics();
            String info = "PV:" + partie.getHotelDeVille().getPv();
            g2.setColor(new Color(220, 220, 220));
            g2.drawString(info, x - fm.stringWidth(info) / 2, y + SIZE / 2 + 27);
        }


        /**
         * Dessine le Château de Clan s'il n'est pas détruit.
         * Affiché en violet pour le distinguer des autres défenses.
         */
        private void dessinerChateau(Graphics2D g2) {
            Chateau c = partie.getChateau();
            if (c.estDetruit()) return;

            // Zone de portée
            int rx = c.getX() - c.getPortee();
            int ry = c.getY() - c.getPortee();
            int rd = c.getPortee() * 2;
            g2.setColor(new Color(150, 0, 200, 40));
            g2.fillOval(rx, ry, rd, rd);
            g2.setColor(new Color(150, 0, 200, 140));
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6, 4}, 0));
            g2.drawOval(rx, ry, rd, rd);
            g2.setStroke(new BasicStroke(1));

            // Bâtiment
            final int SIZE = 40;
            int bx = c.getX() - SIZE / 2;
            int by = c.getY() - SIZE / 2;
            g2.setColor(c.vientDeTirer() ? new Color(200, 50, 255) : new Color(120, 0, 160));
            g2.fillRect(bx, by, SIZE, SIZE);
            g2.setColor(new Color(200, 150, 255));
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(bx, by, SIZE, SIZE);
            g2.setStroke(new BasicStroke(1));

            // Barre de vie
            dessinerBarreVie(g2, bx, by - 8, SIZE, c.getPv(), c.getPvMax());

            // Nom
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            String label = c.getNom();
            g2.setColor(Color.BLACK);
            g2.drawString(label, c.getX() - fm.stringWidth(label) / 2 + 1, c.getY() + SIZE / 2 + 14);
            g2.setColor(new Color(220, 180, 255));
            g2.drawString(label, c.getX() - fm.stringWidth(label) / 2, c.getY() + SIZE / 2 + 13);

            // Info PV
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            fm = g2.getFontMetrics();
            String info = "PV:" + c.getPv() + (c.hasSpawn() ? " (spawn)" : "");
            g2.setColor(new Color(220, 220, 220));
            g2.drawString(info, c.getX() - fm.stringWidth(info) / 2, c.getY() + SIZE / 2 + 24);
        }


        /** Dessine toutes les défenses encore vivantes avec leur zone de portée. */
        private void dessinerDefenses(Graphics2D g2) {
            for (Defense d : partie.getDefenses()) {
                if (d.estDetruit()) continue;

                boolean touchee    = defensesEnPortee.contains(d);
                Color fillPortee   = touchee ? new Color(255,  50,  50,  55) : new Color(255, 200,   0,  40);
                Color strokePortee = touchee ? new Color(255,  50,  50, 160) : new Color(255, 200,   0, 140);
                int rx = d.getX() - d.getPortee();
                int ry = d.getY() - d.getPortee();
                int rd = d.getPortee() * 2;

                // Zone de portée
                g2.setColor(fillPortee);
                g2.fillOval(rx, ry, rd, rd);
                g2.setColor(strokePortee);
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6, 4}, 0));
                g2.drawOval(rx, ry, rd, rd);
                g2.setStroke(new BasicStroke(1));

                // Bâtiment — rouge vif si vient de tirer
                int bx = d.getX() - DEF_SIZE / 2;
                int by = d.getY() - DEF_SIZE / 2;
                g2.setColor(d.vientDeTirer() ? new Color(255, 50, 50) :
                            touchee          ? new Color(220, 50, 50) :
                                               new Color(160, 60, 60));
                g2.fillRect(bx, by, DEF_SIZE, DEF_SIZE);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(bx, by, DEF_SIZE, DEF_SIZE);
                g2.setStroke(new BasicStroke(1));

                // Barre de vie
                dessinerBarreVie(g2, bx, by - 8, DEF_SIZE, d.getPv(), d.getPvMax());

                // Nom
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String label = d.getNom();
                g2.setColor(Color.BLACK);
                g2.drawString(label, d.getX() - fm.stringWidth(label) / 2 + 1, d.getY() + DEF_SIZE / 2 + 15);
                g2.setColor(Color.WHITE);
                g2.drawString(label, d.getX() - fm.stringWidth(label) / 2,     d.getY() + DEF_SIZE / 2 + 14);

                // Info PV / portée
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                fm = g2.getFontMetrics();
                String info = "PV:" + d.getPv() + "  P:" + d.getPortee();
                g2.setColor(new Color(220, 220, 220));
                g2.drawString(info, d.getX() - fm.stringWidth(info) / 2, d.getY() + DEF_SIZE / 2 + 25);
            }
        }


        /** Dessine les bâtiments normaux du village (ni défenses, ni hôtel de ville). */
        private void dessinerBatimentsNormaux(Graphics2D g2) {
            final int SIZE = 35;

            for (Batiment b : partie.getAutresBatiments()) {
                if (b.estDetruit()) continue;

                int bx = b.getX() - SIZE / 2;
                int by = b.getY() - SIZE / 2;

                // Corps du bâtiment — couleur bleue neutre
                g2.setColor(new Color(70, 130, 180));
                g2.fillRect(bx, by, SIZE, SIZE);
                g2.setColor(new Color(173, 216, 230));
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(bx, by, SIZE, SIZE);
                g2.setStroke(new BasicStroke(1));

                // Barre de vie
                dessinerBarreVie(g2, bx, by - 8, SIZE, b.getPv(), b.getPvMax());

                // Nom
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                String label = b.getNom();
                g2.setColor(Color.BLACK);
                g2.drawString(label, b.getX() - fm.stringWidth(label) / 2 + 1, b.getY() + SIZE / 2 + 14);
                g2.setColor(Color.WHITE);
                g2.drawString(label, b.getX() - fm.stringWidth(label) / 2,     b.getY() + SIZE / 2 + 13);

                // Info PV
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                fm = g2.getFontMetrics();
                String info = "PV:" + b.getPv();
                g2.setColor(new Color(220, 220, 220));
                g2.drawString(info, b.getX() - fm.stringWidth(info) / 2, b.getY() + SIZE / 2 + 24);
            }
        }


        /**
         * Dessine toutes les troupes déployées sur la carte.
         * Affiche une croix rouge si la troupe est en train de mourir.
         * Affiche la barre de vie au-dessus de chaque troupe vivante.
         */
        private void dessinerTroupesSurCarte(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;

            for (Troupe t : partie.getTroupes()) {
                if (!t.isDeployee()) continue; // ne dessine que les troupes déployées

                // Animation de mort → croix rouge à la place de l'image
                if (t.isMortVisuelle()) {
                    dessinerCroixMort(g2, t.getX(), t.getY(), 40);
                    continue;
                }

                // Image de la troupe
                Image img = imageFor(t);
                if (img != null) g.drawImage(img, t.getX(), t.getY(), 40, 40, Affichage.this);

                // Contour cyan si ce Pekka est sélectionné pour déplacement
                if (t instanceof model.Pekka && t == controller.getPekkaActive()) {
                    g2.setColor(Color.CYAN);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRect(t.getX(), t.getY(), 40, 40);
                    g2.setStroke(new BasicStroke(1));
                }

                // Barre de vie au-dessus
                dessinerBarreVie(g2, t.getX(), t.getY() - 8, 40, t.getHealth(), t.getHealthMax());
            }
        }


        /**
         * Dessine les 3 types de troupes dans la barre du bas.
         * Affiche le stock disponible et un contour jaune sur le type sélectionné.
         * Ordre : Pekka, Sorcier, Barbare.
         */
        private void dessinerAvatarsBarre(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            String typeSelectionne = controller.getTypeSelectionne();

            // Or de combat — affiché en haut à gauche de la barre
            int orCombat = partie.getOrCombat();
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.setColor(new Color(255, 215, 0));
            g2.drawString("Or: " + orCombat, 10, getHeight() - 105);

            // Ordre des avatars : Pekka, Sorcier, Barbare
            Image[]  imgs   = {pekkaImg,  sorcierImg, barbareImg};
            String[] types  = {"Pekka",   "Sorcier",  "Barbare"};
            int[]    stocks = {
                partie.getStockPekka(),
                partie.getStockSorcier(),
                partie.getStockBarbare()
            };
            int[] prix = {
                Partie.getPrixTroupe("Pekka"),
                Partie.getPrixTroupe("Sorcier"),
                Partie.getPrixTroupe("Barbare")
            };

            int avatarSize = 50, spacing = 80, startX = 20;
            int y = getHeight() - 80;

            for (int i = 0; i < 3; i++) {
                int x = startX + i * spacing;

                // Image de l'avatar
                if (imgs[i] != null) g.drawImage(imgs[i], x, y, avatarSize, avatarSize, Affichage.this);

                // Contour jaune si ce type est sélectionné
                if (types[i].equals(typeSelectionne)) {
                    g.setColor(Color.YELLOW);
                    g2.setStroke(new BasicStroke(3));
                    g.drawRect(x, y, avatarSize, avatarSize);
                    g2.setStroke(new BasicStroke(1));
                }

                // Stock disponible — rouge si épuisé
                g.setColor(stocks[i] > 0 ? Color.WHITE : new Color(180, 60, 60));
                g.setFont(new Font("SansSerif", Font.BOLD, 12));
                g.drawString("x" + stocks[i], x + avatarSize - 15, y + avatarSize - 5);

                // Nom du type
                g.setColor(Color.LIGHT_GRAY);
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g.drawString(types[i], x, y + avatarSize + 13);

                // Bouton "+" pour acheter une troupe avec l'or de combat
                int bx = x + avatarSize + 3;
                int by = y;
                boolean peutAcheter = orCombat >= prix[i];
                g2.setColor(peutAcheter ? new Color(50, 180, 50) : new Color(80, 80, 80));
                g2.fillRoundRect(bx, by, 22, 22, 6, 6);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2.drawString("+", bx + 5, by + 16);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2.setColor(peutAcheter ? new Color(200, 255, 200) : new Color(140, 140, 140));
                g2.drawString(prix[i] + "g", bx + 1, by + 32);
            }
        }


        /**
         * Dessine le chronomètre en haut au centre et le score en haut à droite.
         *
         * Conversion : 125 secondes → "2:05"
         * Couleur : blanc normal, orange sous 30s, rouge quand terminé.
         */
        private void dessinerChrono(Graphics2D g2) {
            int sec      = partie.getSecondesRestantes();
            String temps = String.format("%d:%02d", sec / 60, sec % 60);

            // État du chrono
            boolean urgent = sec <= 30; // mode urgence sous 30s
            boolean fini   = sec == 0;  // chrono terminé

            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(temps) + 28;
            int x = getWidth() / 2 - w / 2;

            // Fond du chrono selon l'état
            g2.setColor(fini   ? new Color(180,   0,   0, 210) :
                        urgent ? new Color(180,  80,   0, 200) :
                                 new Color(  0,   0,   0, 180));
            g2.fillRoundRect(x, 8, w, 32, 10, 10);

            // Texte du chrono
            g2.setColor(fini   ? new Color(255, 100, 100) :
                        urgent ? new Color(255, 200,  80) :
                                 Color.WHITE);
            g2.drawString(temps, x + 14, 30);

            // Message "Temps écoulé !" si fini
            if (fini) {
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                String msg = "Temps écoulé !";
                g2.setColor(new Color(255, 80, 80));
                g2.drawString(msg,
                        getWidth() / 2 - g2.getFontMetrics().stringWidth(msg) / 2,
                        55);
            }
        }

        /**
         * Affiche le système de récompense : 3 étoiles (Clash of Clans) avec le pourcentage de destruction.
         * Les étoiles gagnées sont affichées en doré, les étoiles non gagnées en gris.
         * 
         * Algorithme :
         * - Chaque étoile est un polygone à 10 points alternant entre rayon externe et rayon interne
         * - Les angles sont calculés en degrés (0° en haut)
         * - L'affichage est centré horizontalement dans la fenêtre
         * 
         * @param g2 Contexte graphique 2D pour le rendu
         * 
         * @see Partie#getEtoiles() pour accéder au nombre d'étoiles gagnées
         * @see Partie#getPourcentageDestruction() pour le pourcentage de destruction
         */
        private void dessinerEtoiles(Graphics2D g2) {
            // Récupère les données de la partie
            int etoilesGagnees = partie.getEtoiles();
            int pourcentageDestruction = partie.getPourcentageDestruction();

            // Calculs de positionnement
            int largeurTotale = STAR_COUNT * STAR_SIZE + (STAR_COUNT - 1) * STAR_SPACING;
            int posX = getWidth() / 2 - largeurTotale / 2;
            int posY = STAR_Y_POSITION;

            // Étape 1 : Dessiner le fond semi-transparent derrière les étoiles
            dessinerFondEtoiles(g2, posX, posY, largeurTotale, pourcentageDestruction);

            // Étape 2 : Dessiner les 3 étoiles individuelles
            for (int i = 0; i < STAR_COUNT; i++) {
                boolean etoileGagnee = i < etoilesGagnees;
                int[] xPoints = new int[STAR_POINTS];
                int[] yPoints = new int[STAR_POINTS];

                // Génère les coordonnées des 10 points de l'étoile
                genererPointsEtoile(xPoints, yPoints, posX, posY, i);

                // Remplit l'étoile avec la couleur appropriée
                if (etoileGagnee) {
                    dessinerEtoileRemplie(g2, xPoints, yPoints);
                } else {
                    dessinerEtoileVide(g2, xPoints, yPoints);
                }
            }
        }

        /**
         * Dessine le fond semi-transparent et le pourcentage de destruction.
         * 
         * @param g2 Contexte graphique
         * @param etoilesX Position X de départ des étoiles
         * @param etoilesY Position Y des étoiles
         * @param largeurEtoiles Largeur totale occupée par les 3 étoiles
         * @param pourcentage Pourcentage de destruction à afficher
         */
        private void dessinerFondEtoiles(Graphics2D g2, int etoilesX, int etoilesY, 
                                        int largeurEtoiles, int pourcentage) {
            String textePercent = pourcentage + "%";
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            FontMetrics fontMetrics = g2.getFontMetrics();
            int largeurTexte = fontMetrics.stringWidth(textePercent);

            // Largeur du fond : étoiles + espace + pourcentage
            int largeurFond = largeurEtoiles + 16 + largeurTexte + 10;
            int xFond = getWidth() / 2 - largeurFond / 2;
            int yFond = etoilesY - 4;

            // Fond arrondi
            g2.setColor(STARS_BACKGROUND);
            g2.fillRoundRect(xFond, yFond, largeurFond, STAR_SIZE + 8, 10, 10);

            // Texte du pourcentage (à droite des étoiles)
            int xTexte = etoilesX + largeurEtoiles + 10;
            g2.setColor(DESTRUCTION_TEXT_COLOR);
            g2.drawString(textePercent, xTexte, etoilesY + STAR_SIZE / 2 + 5);
        }

        /**
         * Génère les 10 points d'une étoile en utilisant la trigonométrie.
         * 
         * Géométrie :
         * - Chaque étoile a 5 pointes avec rayons alternants (externe, interne)
         * - Les angles commencent à 90° (haut) et tournent de 36° (360°/10)
         * - Les points pairs (0,2,4...) utilisent le rayon externe
         * - Les points impairs (1,3,5...) utilisent le rayon interne
         * 
         * @param xPoints Tableau à remplir avec les coordonnées X
         * @param yPoints Tableau à remplir avec les coordonnées Y
         * @param posX Position X de départ (centre du groupe d'étoiles)
         * @param posY Position Y des étoiles
         * @param indexEtoile Index de l'étoile (0, 1, ou 2)
         */
        private void genererPointsEtoile(int[] xPoints, int[] yPoints, int posX, int posY, int indexEtoile) {
            // Position du centre de cette étoile spécifique
            int centreX = posX + indexEtoile * (STAR_SIZE + STAR_SPACING) + STAR_SIZE / 2;
            int centreY = posY + STAR_SIZE / 2;

            // Rayons : externe pour les pointes, interne pour les creux
            double rayonExterne = STAR_SIZE / 2.0;
            double rayonInterne = rayonExterne * STAR_INNER_RATIO;

            // Calcule les 10 points (5 pointes = 10 points alternants)
            for (int j = 0; j < STAR_POINTS; j++) {
                // Angle en radians : commence à 90° (π/2) et ajoute j × 36° (π/5)
                double angle = Math.PI / 2 + j * Math.PI / 5;
                
                // Alterne entre rayon externe (points pairs) et interne (impairs)
                double rayon = (j % 2 == 0) ? rayonExterne : rayonInterne;
                
                // Convertit coordonnées polaires en coordonnées cartésiennes
                xPoints[j] = centreX + (int) (Math.cos(angle) * rayon);
                yPoints[j] = centreY - (int) (Math.sin(angle) * rayon);
            }
        }

        /**
         * Dessine une étoile remplie (dorée) pour une étoile gagnée.
         * 
         * @param g2 Contexte graphique
         * @param xPoints Coordonnées X des 10 points
         * @param yPoints Coordonnées Y des 10 points
         */
        private void dessinerEtoileRemplie(Graphics2D g2, int[] xPoints, int[] yPoints) {
            // Remplissage doré
            g2.setColor(STAR_FILLED_COLOR);
            g2.fillPolygon(xPoints, yPoints, STAR_POINTS);
            
            // Bordure or foncé pour relief
            g2.setColor(STAR_FILLED_BORDER);
            g2.setStroke(new BasicStroke(2));
            g2.drawPolygon(xPoints, yPoints, STAR_POINTS);
            g2.setStroke(new BasicStroke(1));
        }

        /**
         * Dessine une étoile vide (grise) pour une étoile non gagnée.
         * 
         * @param g2 Contexte graphique
         * @param xPoints Coordonnées X des 10 points
         * @param yPoints Coordonnées Y des 10 points
         */
        private void dessinerEtoileVide(Graphics2D g2, int[] xPoints, int[] yPoints) {
            // Remplissage gris
            g2.setColor(STAR_EMPTY_FILL);
            g2.fillPolygon(xPoints, yPoints, STAR_POINTS);
            
            // Bordure gris clair
            g2.setColor(STAR_EMPTY_BORDER);
            g2.setStroke(new BasicStroke(2));
            g2.drawPolygon(xPoints, yPoints, STAR_POINTS);
            g2.setStroke(new BasicStroke(1));
        }

        // Dessine l'écran de fin (victoire ou défaite)
        private void dessinerEcranFin(Graphics2D g2) {
            int w = getWidth();
            int h = getHeight();

            // Fond semi-transparent
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, w, h);

            // Panneau central
            int panelW = 400;
            int panelH = 220;
            int px = w / 2 - panelW / 2;
            int py = h / 2 - panelH / 2;

            boolean victoire = partie.estGagnee();
            Color bordure = victoire ? new Color(255, 215, 0) : new Color(200, 50, 50);

            g2.setColor(new Color(30, 30, 30, 230));
            g2.fillRoundRect(px, py, panelW, panelH, 20, 20);
            g2.setColor(bordure);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(px, py, panelW, panelH, 20, 20);
            g2.setStroke(new BasicStroke(1));

            // Titre VICTOIRE / DÉFAITE
            String titre = victoire ? "VICTOIRE" : "DÉFAITE";
            g2.setFont(new Font("SansSerif", Font.BOLD, 36));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(bordure);
            g2.drawString(titre, w / 2 - fm.stringWidth(titre) / 2, py + 55);

            // Étoiles au centre du panneau
            int etoiles = partie.getEtoiles();
            int starSize = 40;
            int spacing = 12;
            int totalStarW = 3 * starSize + 2 * spacing;
            int starStartX = w / 2 - totalStarW / 2;
            int starY = py + 80;

            for (int i = 0; i < 3; i++) {
                int cx = starStartX + i * (starSize + spacing) + starSize / 2;
                int cy = starY + starSize / 2;

                int[] xPoints = new int[10];
                int[] yPoints = new int[10];
                double outerR = starSize / 2.0;
                double innerR = outerR * 0.4;

                for (int j = 0; j < 10; j++) {
                    double angle = Math.PI / 2 + j * Math.PI / 5;
                    double r = (j % 2 == 0) ? outerR : innerR;
                    xPoints[j] = cx + (int) (Math.cos(angle) * r);
                    yPoints[j] = cy - (int) (Math.sin(angle) * r);
                }

                if (i < etoiles) {
                    g2.setColor(new Color(255, 215, 0));
                    g2.fillPolygon(xPoints, yPoints, 10);
                    g2.setColor(new Color(200, 160, 0));
                } else {
                    g2.setColor(new Color(80, 80, 80));
                    g2.fillPolygon(xPoints, yPoints, 10);
                    g2.setColor(new Color(140, 140, 140));
                }
                g2.setStroke(new BasicStroke(2));
                g2.drawPolygon(xPoints, yPoints, 10);
                g2.setStroke(new BasicStroke(1));
            }

            // Pourcentage de destruction
            String pct = partie.getPourcentageDestruction() + "% détruit";
            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            fm = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            g2.drawString(pct, w / 2 - fm.stringWidth(pct) / 2, py + panelH - 30);
        }

        public void dessinerResultat(Graphics2D g2) {
            // Score en haut à droite
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            String scoreStr = "Score : " + partie.getScore();
            FontMetrics fmScore = g2.getFontMetrics();
            int sw = fmScore.stringWidth(scoreStr);
            int sx = getWidth() - sw - 16;
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(sx - 8, 8, sw + 16, 32, 10, 10);
            g2.setColor(new Color(255, 215, 0));
            g2.drawString(scoreStr, sx, 30);
            
             // Message de stock épuisé — affiché pendant DUREE_MESSAGE ticks puis effacé
            if (messageStock != null) {
                ticksMessage++;

                float ratio   = 1f - (float) ticksMessage / DUREE_MESSAGE;
                int   alpha   = (int) (255 * ratio);

                g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                FontMetrics fmMsg = g2.getFontMetrics();
                int mw = fmMsg.stringWidth(messageStock) + 24;
                int mx = getWidth() / 2 - mw / 2;

                // Fond rouge semi-transparent
                g2.setColor(new Color(180, 0, 0, Math.min(alpha, 200)));
                g2.fillRoundRect(mx, getHeight() - 130, mw, 28, 10, 10);

                // Texte blanc
                g2.setColor(new Color(255, 255, 255, alpha));
                g2.drawString(messageStock, mx + 12, getHeight() - 111);

                // Efface le message quand la durée est écoulée
                if (ticksMessage >= DUREE_MESSAGE) {
                    messageStock = null;
                }
            }
            
        }


        /**
         * Dessine le point cliqué et le message indiquant les défenses en portée.
         * Visible uniquement après un clic libre sur la carte.
         */
        private void dessinerResultatPortee(Graphics2D g2) {
            if (clickPoint == null) return;

            // Point cliqué
            g2.setColor(Color.CYAN);
            g2.fillOval(clickPoint.x - 6, clickPoint.y - 6, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(clickPoint.x - 6, clickPoint.y - 6, 12, 12);
            g2.setStroke(new BasicStroke(1));

            // Message de portée
            String msg;
            if (defensesEnPortee.isEmpty()) {
                msg = "Hors de portée de toutes les défenses";
            } else {
                StringBuilder sb = new StringBuilder("À portée de : ");
                for (int i = 0; i < defensesEnPortee.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(defensesEnPortee.get(i).getNom());
                }
                msg = sb.toString();
            }

            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(msg) + 24;
            g2.setColor(new Color(0, 0, 0, 190));
            g2.fillRoundRect(8, 48, w, 28, 10, 10);
            g2.setColor(defensesEnPortee.isEmpty() ? new Color(100, 230, 100) : new Color(255, 100, 100));
            g2.drawString(msg, 20, 67);
        }


        /**
         * Dessine une barre de vie horizontale.
         *
         * Couleur selon les PV restants :
         * - Vert   : > 50%
         * - Orange : entre 25% et 50%
         * - Rouge  : < 25%
         *
         * @param g2      Contexte graphique
         * @param x       Position X de la barre
         * @param y       Position Y de la barre
         * @param largeur Largeur totale de la barre
         * @param pv      PV actuels
         * @param pvMax   PV maximum
         */
        private void dessinerBarreVie(Graphics2D g2, int x, int y, int largeur, int pv, int pvMax) {
            int hauteur = 5;
            float ratio = (float) pv / pvMax;
            int rempli  = (int) (largeur * ratio);

            // Fond gris
            g2.setColor(new Color(60, 60, 60));
            g2.fillRect(x, y, largeur, hauteur);

            // Barre colorée
            Color couleur = ratio > 0.5f  ? new Color(50, 200, 50)  :
                            ratio > 0.25f ? new Color(255, 165, 0)  :
                                            new Color(220, 50, 50);
            g2.setColor(couleur);
            g2.fillRect(x, y, rempli, hauteur);

            // Contour
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, largeur, hauteur);
        }


        /**
         * Dessine une croix rouge à la position d'une troupe morte.
         * Affichée pendant DUREE_MORT ticks avant suppression.
         *
         * @param g2     Contexte graphique
         * @param x      Position X de la troupe
         * @param y      Position Y de la troupe
         * @param taille Taille de la croix en pixels
         */
        private void dessinerCroixMort(Graphics2D g2, int x, int y, int taille) {
            g2.setColor(new Color(220, 50, 50, 180));
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 5,          y + 5,          x + taille - 5, y + taille - 5);
            g2.drawLine(x + taille - 5, y + 5,          x + 5,          y + taille - 5);
            g2.setStroke(new BasicStroke(1));
        }


        /** Retourne l'image correspondant au type de troupe. */
        private Image imageFor(Troupe t) {
            if (t instanceof Barbare) return barbareImg;
            if (t instanceof Sorcier) return sorcierImg;
            if (t instanceof Pekka)   return pekkaImg;
            return null;
        }
    }
}
