package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import model.*;
import controller.GameController;

public class Affichage extends JPanel {

    // Référence vers l’état du jeu
    private Partie partie;

    // Point de clic utilisé pour tester la portée des défenses
    private Point clickPoint = null;

    // Liste des défenses qui sont en portée du point cliqué
    private final List<Defense> defensesEnPortee = new ArrayList<>();

    // Contrôleur du jeu
    private GameController controller;

    // Panneau principal de la carte
    private MapPanel mapPanel;

    // Images des troupes
    private Image barbareImg;
    private Image sorcierImg;
    private Image pekkaImg;

    private static final long serialVersionUID = 1L;

    public Affichage(Partie partie, GameController controller) {
    	
        setLayout(new BorderLayout());

        this.setPreferredSize(new Dimension(1280, 720));

        // On initialise d’abord la partie
        this.partie = partie;
        this.controller = controller;

        // Chargement des images
        barbareImg = new ImageIcon("res/barbare.png").getImage();
        sorcierImg = new ImageIcon("res/sorcier.png").getImage();
        pekkaImg = new ImageIcon("res/pekka.png").getImage();

        // Création du panneau principal de la carte
         mapPanel = new MapPanel();
        // mapPanel.setLayout(null);


        add(mapPanel, BorderLayout.CENTER);

}

    // ---------------------------------------------------------------
    private class MapPanel extends JPanel {

        private static final int CELL = 50;
        private static final int DEF_SIZE = 30;

        private static final long serialVersionUID = 1L;

        public MapPanel() {
            
                setBackground(new Color(34, 139, 34));
            
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            dessinerGrille(g2);
            dessinerHotelDeVille(g2);
            dessinerBatimentsNormaux(g2);   
            dessinerDefenses(g2);
            dessinerResultat(g2);
            dessinerChrono(g2);

            // Dessine la barre en bas de l’écran
            g.setColor(new Color(50, 50, 50));
            g.fillRect(0, getHeight() - 100, getWidth(), 100);

            dessinerTroupesSurCarte(g);
            dessinerAvatarsBarre(g);
        }


        // Méthode qui dessine toutes les troupes sur la carte.
        // Parcourir la liste des troupes et afficher l'image correspondant au type
        // de troupe (Barbare, Sorcier, Pekka) à sa position.
        private void dessinerTroupesSurCarte(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;

            for (Troupe t : partie.getTroupes()) {

                // Animation de mort : on dessine une croix rouge à la place
                if (t.isMortVisuelle()) {
                    dessinerCroixMort(g2, t.getX(), t.getY(), 40);
                    continue;
                }

                // Image de la troupe
                Image img = imageFor(t);
                if (img != null) g.drawImage(img, t.getX(), t.getY(), 40, 40, Affichage.this);
                
                // Barre de vie au-dessus de la troupe
                dessinerBarreVie(g2, t.getX(), t.getY() - 8, 40, t.getHealth(), t.getHealthMax());
            }
        }
        
        /** Retourne l'image correspondant au type de troupe. */
        private Image imageFor(Troupe t) {
            if (t instanceof Barbare) return barbareImg;
            if (t instanceof Sorcier) return sorcierImg;
            if (t instanceof Pekka)   return pekkaImg;
            return null;
        }

		// Dessine une grille sur le terrain
        private void dessinerGrille(Graphics2D g2) {
            g2.setColor(new Color(0, 80, 0, 90));

            for (int x = 0; x < getWidth(); x += CELL) {
                g2.drawLine(x, 0, x, getHeight());
            }

            for (int y = 0; y < getHeight(); y += CELL) {
                g2.drawLine(0, y, getWidth(), y);
            }
        }

        // Dessine toutes les défenses encore vivantes
        private void dessinerDefenses(Graphics2D g2) {
            for (Defense d : partie.getDefenses()) {
                if (d.estDetruit()) continue;

                boolean touchee = defensesEnPortee.contains(d);

                // Zone de portée
                Color fillPortee = touchee ? new Color(255, 50, 50, 55) : new Color(255, 200, 0, 40);
                Color strokePortee = touchee ? new Color(255, 50, 50, 160) : new Color(255, 200, 0, 140);
                int rx = d.getX() - d.getPortee();
                int ry = d.getY() - d.getPortee();
                int rd = d.getPortee() * 2;

                g2.setColor(fillPortee);
                g2.fillOval(rx, ry, rd, rd);
                g2.setColor(strokePortee);
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        0, new float[]{6, 4}, 0));
                g2.drawOval(rx, ry, rd, rd);
                g2.setStroke(new BasicStroke(1));

                // Bâtiment
                int bx = d.getX() - DEF_SIZE / 2;
                int by = d.getY() - DEF_SIZE / 2;
                // Bâtiment — rouge vif si vient de tirer, rouge normal sinon
                g2.setColor(d.vientDeTirer()  ? new Color(255,  50,  50) :
                 touchee ? new Color(220,  50,  50) :
                           new Color(160,  60,  60));
                                                 
                g2.fillRect(bx, by, DEF_SIZE, DEF_SIZE);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(bx, by, DEF_SIZE, DEF_SIZE);
                g2.setStroke(new BasicStroke(1));

                // Nom
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String label = d.getNom();
                g2.setColor(Color.BLACK);
                g2.drawString(label, d.getX() - fm.stringWidth(label) / 2 + 1, d.getY() + DEF_SIZE / 2 + 15);
                g2.setColor(Color.WHITE);
                g2.drawString(label, d.getX() - fm.stringWidth(label) / 2, d.getY() + DEF_SIZE / 2 + 14);

                // Info PV / portée
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                fm = g2.getFontMetrics();
                String info = "PV:" + d.getPv() + "  P:" + d.getPortee();
                g2.setColor(new Color(220, 220, 220));
                g2.drawString(info, d.getX() - fm.stringWidth(info) / 2, d.getY() + DEF_SIZE / 2 + 25);
            }
        }
        
     // Dessine l’hôtel de ville s’il n’est pas détruit
        private void dessinerChateau(Graphics2D g2) {
            if (partie.getChateau().estDetruit()) return;

            final int SIZE = 50;
            int x = partie.getChateau().getX();
            int y = partie.getChateau().getY();
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

            // Étiquette nom
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            String label = partie.getChateau().getNom();
            g2.setColor(Color.BLACK);
            g2.drawString(label, x - fm.stringWidth(label) / 2 + 1, y + SIZE / 2 + 17);
            g2.setColor(new Color(255, 240, 150));
            g2.drawString(label, x - fm.stringWidth(label) / 2, y + SIZE / 2 + 16);

            // Info PV
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            fm = g2.getFontMetrics();
            String info = "PV:" + partie.getChateau().getPv();
            g2.setColor(new Color(220, 220, 220));
            g2.drawString(info, x - fm.stringWidth(info) / 2, y + SIZE / 2 + 27);
        }
        
        /**
         * Dessine une barre de vie horizontale.
         * Verte si > 50%, orange si > 25%, rouge sinon.
         */
        private void dessinerBarreVie(Graphics2D g2, int x, int y, int largeur, int pv, int pvMax) {
            int hauteur   = 5;
            float ratio   = (float) pv / pvMax;
            int rempli    = (int) (largeur * ratio);

            // Fond gris
            g2.setColor(new Color(60, 60, 60));
            g2.fillRect(x, y, largeur, hauteur);

            // Barre colorée selon les PV restants
            Color couleur = ratio > 0.5f ? new Color(50, 200, 50) :
                            ratio > 0.25f ? new Color(255, 165, 0) :
                                            new Color(220, 50, 50);
            g2.setColor(couleur);
            g2.fillRect(x, y, rempli, hauteur);

            // Contour
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, largeur, hauteur);
        }

        /**
         * Dessine une croix rouge à la position donnée (animation de mort).
         */
        private void dessinerCroixMort(Graphics2D g2, int x, int y, int taille) {
            g2.setColor(new Color(220, 50, 50, 180));
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 5,        y + 5,        x + taille - 5, y + taille - 5);
            g2.drawLine(x + taille - 5, y + 5,      x + 5,          y + taille - 5);
            g2.setStroke(new BasicStroke(1));
        }
        

        // Dessine l’hôtel de ville s’il n’est pas détruit
        private void dessinerHotelDeVille(Graphics2D g2) {
            if (partie.getHotelDeVille().estDetruit()) return;

            final int SIZE = 50;
            int x = partie.getHotelDeVille().getX();
            int y = partie.getHotelDeVille().getY();
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
        
        //	 Dessine les bâtiments normaux du village (ni défenses, ni hôtel de ville). */
        private void dessinerBatimentsNormaux(Graphics2D g2) {
            final int SIZE = 35;

            for (Batiment b : partie.getAutresBatiments()) {
                if (b.estDetruit()) continue;

                int bx = b.getX() - SIZE / 2;
                int by = b.getY() - SIZE / 2;

                // Corps du bâtiment — couleur neutre bleue
                g2.setColor(new Color(70, 130, 180));
                g2.fillRect(bx, by, SIZE, SIZE);
                g2.setColor(new Color(173, 216, 230));
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(bx, by, SIZE, SIZE);
                g2.setStroke(new BasicStroke(1));

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

        public void dessinerChrono(Graphics2D g2) {

            // Conversion des secondes restantes en minutes + secondes
            // Exemple : 125 secondes → 2 minutes 05 secondes
            int minutes = partie.getSecondesRestantes() / 60;
            int secondes = partie.getSecondesRestantes() % 60;

            // Formatage de l'affichage (ex : 2:05)
            String temps = String.format("%d:%02d", minutes, secondes);

            // Etat du chrono
            boolean urgent = partie.getSecondesRestantes() <= 30; // passe en mode urgence sous 30s
            boolean fini = partie.getSecondesRestantes() == 0;    // chrono terminé

            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(temps) + 28;
            int x = getWidth() / 2 - w / 2;

            // Choix de la couleur du fond selon l'état du chrono
            Color fond = fini ? new Color(180, 0, 0, 210) :
                    urgent ? new Color(180, 80, 0, 200) :
                            new Color(0, 0, 0, 180);

            g2.setColor(fond);
            g2.fillRoundRect(x, 8, w, 32, 10, 10);

            g2.setColor(fini ? new Color(255, 100, 100) :
                    urgent ? new Color(255, 200, 80) :
                            Color.WHITE);
            g2.drawString(temps, x + 14, 30);

            // Si le chrono est terminé on affiche un message
            if (fini) {
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                String msg = "Temps écoulé !";

                g2.setColor(new Color(255, 80, 80));
                g2.drawString(msg,
                        getWidth() / 2 - g2.getFontMetrics().stringWidth(msg) / 2,
                        55);
            }
            
            // Score en haut à droite
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            String scoreStr = "Score : " + partie.getScore();
            FontMetrics fmScore = g2.getFontMetrics();
            int sx = getWidth() - fmScore.stringWidth(scoreStr) - 16;
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(sx - 8, 8, fmScore.stringWidth(scoreStr) + 16, 32, 10, 10);
            g2.setColor(new Color(255, 215, 0));
            g2.drawString(scoreStr, sx, 30);
        }

        public void dessinerResultat(Graphics2D g2) {
            if (clickPoint == null) return;

            // Dessine le point cliqué
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
                StringBuilder sb = new StringBuilder("A portée de : ");
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
    }

 // Dessine les troupes du joueur dans la barre du bas
 // Ces troupes sont cliquables pour être sélectionnées
 private void drawAvatars(Graphics g) {

     int avatarSize = 50;
     int spacing = 80;
     int startX = 20;
     int y = getHeight() - 80;

     int index = 0;

     for (Troupe t : partie.getTroupes()) {

         int x = startX + index * spacing;

         // Dessine l'image selon le type
         if (t instanceof Barbare) {
             g.drawImage(barbareImg, x, y, avatarSize, avatarSize, this);
         } else if (t instanceof Sorcier) {
             g.drawImage(sorcierImg, x, y, avatarSize, avatarSize, this);
         } else if (t instanceof Pekka) {
             g.drawImage(pekkaImg, x, y, avatarSize, avatarSize, this);
         }       

         index++;
         g.setColor(Color.WHITE);
         g.drawString("PV:" + t.getHealth(), x, y + 75);
     }
 }  	
 	
 	
 	private void dessinerAvatarsBarre(Graphics g) {
 	    Graphics2D g2 = (Graphics2D) g;
 	    String typeSelectionne = controller.getTypeSelectionne();

 	    // Ordre des avatars : Pekka, Sorcier, Barbare
 	    Image[]  imgs   = {pekkaImg,  sorcierImg, barbareImg};
 	    String[] types  = {"Pekka",   "Sorcier",  "Barbare"};
 	    int[]    stocks = {
 	        partie.getStockPekka(),
 	        partie.getStockSorcier(),
 	        partie.getStockBarbare()
 	    };

 	    int avatarSize = 50, spacing = 80, startX = 20;
 	    int y = getHeight() - 80;

 	    for (int i = 0; i < 3; i++) {
 	        int x = startX + i * spacing;

 	        // Image
 	        if (imgs[i] != null) g.drawImage(imgs[i], x, y, avatarSize, avatarSize, Affichage.this);

 	        // Contour jaune si ce type est sélectionné
 	        if (types[i].equals(typeSelectionne)) {
 	            g.setColor(Color.YELLOW);
 	            ((Graphics2D) g).setStroke(new BasicStroke(3));
 	            g.drawRect(x, y, avatarSize, avatarSize);
 	            ((Graphics2D) g).setStroke(new BasicStroke(1));
 	        }

 	        // Stock disponible
 	        g.setColor(stocks[i] > 0 ? Color.WHITE : new Color(180, 60, 60));
 	        g.drawString("x" + stocks[i], x + avatarSize - 15, y + avatarSize - 5);

 	        // Nom du type
 	        g.setColor(Color.LIGHT_GRAY);
 	        g.drawString(types[i], x, y + avatarSize + 15);
 	    }
 	}
 
 
 
	 // Retourne le panneau principal de la carte
	 public JPanel getMapPanel() {
	     return mapPanel;
	 }
	
	 // Met à jour les informations du clic pour l'affichage
	 public void setClickInfo(int x, int y, List<Defense> defenses) {
	     this.clickPoint = new Point(x, y);
	     this.defensesEnPortee.clear();
	     this.defensesEnPortee.addAll(defenses);
	 }
	 
	 
	 
}
