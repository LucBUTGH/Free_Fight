package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class Affichage extends JPanel {

    // Référence vers l’état du jeu
    private Partie partie;

    // Point de clic utilisé pour tester la portée des défenses
    private Point clickPoint = null;

    // Liste des défenses qui sont en portée du point cliqué
    private final List<Defense> defensesEnPortee = new ArrayList<>();

    // Troupe actuellement sélectionnée à la souris
    private Troupe troupeSelectionnee = null;

    // Timer Swing utilisé pour faire fonctionner le chronomètre.
    // Il exécute une action automatiquement toutes les X millisecondes.
    private Timer chrono;

    // Images des troupes
    private Image barbareImg;
    private Image sorcierImg;
    private Image pekkaImg;

    private static final long serialVersionUID = 1L;

    public Affichage(Partie partie) {

        setLayout(new BorderLayout());

        this.setPreferredSize(new Dimension(1280, 720));

        // On initialise d’abord la partie
        this.partie = partie;

        // Chargement des images
        barbareImg = new ImageIcon("res/barbare.png").getImage();
        sorcierImg = new ImageIcon("res/sorcier.png").getImage();
        pekkaImg = new ImageIcon("res/pekka.png").getImage();

        // Création du panneau principal de la carte
        MapPanel mapPanel = new MapPanel();
        // mapPanel.setLayout(null);

        // Bouton Déployer en haut à gauche
        JButton btnDeployer = new JButton("⚔ Déployer");
        btnDeployer.setFocusPainted(false);
        btnDeployer.setBackground(new Color(180, 30, 30));
        btnDeployer.setForeground(Color.WHITE);
        btnDeployer.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnDeployer.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnDeployer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDeployer.addActionListener(e -> demarrerChrono());
        btnDeployer.setBounds(10, 8, 130, 32);
        mapPanel.add(btnDeployer);


        add(mapPanel, BorderLayout.CENTER);

        // Création du chronomètre
        chrono = new Timer(1000, e -> {
            if (!partie.tempsEcoule()) {
                partie.decrementerTemps();
                repaint();
            } else {
                chrono.stop();
                repaint();
            }
        });

        // Ce timer est pour :
        // faire bouger les troupes
        // faire attaquer
        // mettre à jour l’état du jeu
        // redessiner l’écran
        Timer timer = new Timer(40, e -> {
            partie.update();
            repaint();
        });
        timer.start();
    }

    // ---------------------------------------------------------------
    private class MapPanel extends JPanel {

        private static final int CELL = 50;
        private static final int DEF_SIZE = 30;

        private static final long serialVersionUID = 1L;

        public MapPanel() {
            setBackground(new Color(34, 139, 34));

            addMouseListener(new MouseAdapter() {
            	@Override
            	public void mouseClicked(MouseEvent e) {
            	    int mx = e.getX();
            	    int my = e.getY();

            	    // 1. Clique sur une troupe dans la barre du bas
            	    Troupe tBar = getTroupeFromBar(mx, my);
            	    if (tBar != null) {
            	        troupeSelectionnee = tBar;
            	        repaint();
            	        return;
            	    }

            	    // 2. Clique sur une troupe sur la map
            	    Troupe t = getTroupeAtPosition(mx, my);
            	    if (t != null) {
            	        troupeSelectionnee = t;
            	        repaint();
            	        return;
            	    }
                    
            	    // Clique sur le sol pour déplacer le pekka 
            	    if (troupeSelectionnee instanceof Pekka) {
    					((Pekka) troupeSelectionnee).setDestination(mx, my);
					}
            	    // 3. Clique sur un bâtiment → donner une cible
            	    if (troupeSelectionnee != null) {
            	        Batiment b = getBatimentAtPosition(mx, my);
            	        if (b != null) {
            	            troupeSelectionnee.setCible(b);
            	            repaint();
            	            return;
            	        }
            	    }

            	    // 4. Test portée (optionnel)
            	    clickPoint = e.getPoint();
            	    defensesEnPortee.clear();
            	    defensesEnPortee.addAll(partie.getDefensesEnPortee(mx, my));

            	    repaint();
            	}
            	
            	// Retourne la troupe cliquée dans la barre du bas
            	private Troupe getTroupeFromBar(int mouseX, int mouseY) {

            	    int avatarSize = 50;
            	    int spacing = 80;
            	    int startX = 20;
            	    int y = getHeight() - 80;

            	    int index = 0;

            	    for (Troupe t : partie.getTroupes()) {

            	        int x = startX + index * spacing;

            	        if (mouseX >= x && mouseX <= x + avatarSize &&
            	            mouseY >= y && mouseY <= y + avatarSize) {
            	            return t;
            	        }

            	        index++;
            	    }

            	    return null;
            	}
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            dessinerGrille(g2);
            dessinerHotelDeVille(g2);
            dessinerDefenses(g2);
            dessinerResultat(g2);
            dessinerChrono(g2);

            // Dessine la barre en bas de l’écran
            g.setColor(new Color(50, 50, 50));
            g.fillRect(0, getHeight() - 100, getWidth(), 100);

            dessinerTroupes(g);
            drawAvatars(g);
        }

        // Méthode qui retourne la troupe située à la position du clic de la souris.
        // Parcourir la liste des troupes et vérifier si les coordonnées de la souris
        // Si une troupe est trouvée, la retourner, sinon retourner null.
        private Troupe getTroupeAtPosition(int mouseX, int mouseY) {
            for (Troupe t : partie.getTroupes()) {
                int tx = t.getX();
                int ty = t.getY();

                if (mouseX >= tx && mouseX <= tx + 40 &&
                        mouseY >= ty && mouseY <= ty + 40) {
                    return t;
                }
            }
            return null;
        }

        // Méthode qui dessine toutes les troupes sur la carte.
        // Parcourir la liste des troupes et afficher l'image correspondant au type
        // de troupe (Barbare, Sorcier, Pekka) à sa position.
        private void dessinerTroupes(Graphics g) {
            for (Troupe t : partie.getTroupes()) {
                if (t instanceof Barbare) {
                    g.drawImage(barbareImg, t.getX(), t.getY(), 40, 40, Affichage.this);
                } else if (t instanceof Sorcier) {
                    g.drawImage(sorcierImg, t.getX(), t.getY(), 40, 40, Affichage.this);
                } else if (t instanceof Pekka) {
                    g.drawImage(pekkaImg, t.getX(), t.getY(), 40, 40, Affichage.this);
                }

                // Met en évidence la troupe sélectionnée
                if (t == troupeSelectionnee) {
                    g.setColor(Color.YELLOW);
                    g.drawRect(t.getX(), t.getY(), 40, 40);
                }
            }
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
                g2.setColor(touchee ? new Color(220, 50, 50) : new Color(160, 60, 60));
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
        
     // Retourne le bâtiment situé sous le clic de la souris
        private Batiment getBatimentAtPosition(int mouseX, int mouseY) {

            final int DEF_SIZE = 30;
            final int HOTEL_SIZE = 50;

            // Vérifie d’abord les défenses
            for (Defense d : partie.getDefenses()) {
                int bx = d.getX() - DEF_SIZE / 2;
                int by = d.getY() - DEF_SIZE / 2;

                if (mouseX >= bx && mouseX <= bx + DEF_SIZE &&
                        mouseY >= by && mouseY <= by + DEF_SIZE) {
                    return d;
                }
            }

            // Vérifie ensuite l’hôtel de ville
            Batiment hdv = partie.getHotelDeVille();
            int bx = hdv.getX() - HOTEL_SIZE / 2;
            int by = hdv.getY() - HOTEL_SIZE / 2;

            if (mouseX >= bx && mouseX <= bx + HOTEL_SIZE &&
                    mouseY >= by && mouseY <= by + HOTEL_SIZE) {
                return hdv;
            }

            return null;
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

    public void demarrerChrono() {

        // Vérifie si le chrono est déjà lancé
        // Cela évite de démarrer plusieurs timers en même temps
        if (!chrono.isRunning()) {

            // Démarre le Timer → il va exécuter l'action toutes les secondes
            chrono.start();
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

         // Si c'est la troupe sélectionnée → contour jaune
         if (t == troupeSelectionnee) {
             g.setColor(Color.YELLOW);
             g.drawRect(x, y, avatarSize, avatarSize);
         }

         index++;
         g.setColor(Color.WHITE);
         g.drawString("PV:" + t.getHealth(), x, y + 75);
     }
 }
}
