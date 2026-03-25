package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class Affichage extends JPanel {

    private final List<Defense> defenses = new ArrayList<>();
    private final Batiment hotelDeVille = new Batiment("Hôtel de Ville", 1500, 375, 290);
    private Point clickPoint = null;
    private final List<Defense> defensesEnPortee = new ArrayList<>();

    private List<Troupe> troupes;

    private Troupe troupeSelectionnee = null;

	// Nombre de secondes restantes pour le combat
    private int secondesRestantes = 120;

	// Timer Swing utilisé pour faire fonctionner le chronomètre.
	// Il exécute une action automatiquement toutes les X millisecondes.
	private Timer chrono;

    private Image barbareImg;
    private Image sorcierImg;
    private Image pekkaImg;
    
    private static final long serialVersionUID = 1L;

    public Affichage(List<Troupe> troupes) {

    	setLayout(new BorderLayout());
    	
        defenses.add(new Defense("Canon",       200, 150, 200, 200));
        defenses.add(new Defense("Tour Archer", 100, 220, 500, 280));
        defenses.add(new Defense("Mortier",     300, 180, 360, 430));

        this.setPreferredSize(new Dimension(1280, 720));

        barbareImg = new ImageIcon("res/barbare.png").getImage();
        sorcierImg = new ImageIcon("res/sorcier.png").getImage();
        pekkaImg = new ImageIcon("res/pekka.png").getImage();

        MapPanel mapPanel = new MapPanel();
        //mapPanel.setLayout(null);

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

        add(mapPanel,BorderLayout.CENTER);
        
        this.troupes = troupes;

	    // Création du chronomètre
        chrono = new Timer(1000, e -> {

            // Tant qu'il reste du temps, on décrémente le chrono
            if (secondesRestantes > 0) {
                secondesRestantes--;  // enlève 1 seconde

                // repaint() force le redessin de la fenêtre
                // Cela permet d'actualiser l'affichage du chrono à l'écran
                repaint();

            } else {

                // Lorsque le temps atteint 0, on arrête le Timer
                // sinon il continuerait à tourner inutilement
                chrono.stop();

                // On redessine une dernière fois l'écran pour afficher "Temps écoulé"
                repaint();
            }
        });

        // Timer déplacement troupes
        Timer timer = new Timer(40, e -> {
            for (Troupe t : troupes) {
                t.agir(defenses, hotelDeVille, new ArrayList<Batiment>());
            }
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

                    Troupe t = getTroupeAtPosition(mx, my);
                    if (t != null) {
                        troupeSelectionnee = t;
                    }

                    // Test portée défenses
                    clickPoint = e.getPoint();
                    defensesEnPortee.clear();
                    for (Defense d : defenses) {
                        if (d.estAPortee(e.getX(), e.getY())) {
                            defensesEnPortee.add(d);
                        }
                    }
                    repaint();
                }
            });
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            dessinerGrille(g2);
            dessinerHotelDeVille(g2);
            dessinerDefenses(g2);
            dessinerResultat(g2);
            dessinerChrono(g2);

            g.setColor(new Color(50, 50, 50));
            g.fillRect(0, getHeight() - 100, getWidth(), 100);

            dessinerTroupes(g);
            drawAvatars(g);
        }

        private Troupe getTroupeAtPosition(int mouseX, int mouseY) {
            for (Troupe t : troupes) {
                int tx = t.getX();
                int ty = t.getY();
                if (mouseX >= tx && mouseX <= tx + 40 &&
                        mouseY >= ty && mouseY <= ty + 40) {
                    return t;
                }
            }
            return null;
        }

        private void dessinerTroupes(Graphics g) {
            for (Troupe t : troupes) {
                if (t instanceof Barbare) {
                    g.drawImage(barbareImg, t.getX(), t.getY(), 40, 40, Affichage.this);
                } else if (t instanceof Sorcier) {
                    g.drawImage(sorcierImg, t.getX(), t.getY(), 40, 40, Affichage.this);
                } else if (t instanceof Pekka) {
                    g.drawImage(pekkaImg, t.getX(), t.getY(), 40, 40, Affichage.this);
                }
                if (t == troupeSelectionnee) {
                    g.setColor(Color.YELLOW);
                    g.drawRect(t.getX(), t.getY(), 40, 40);
                }
            }
        }

        private void dessinerGrille(Graphics2D g2) {
            g2.setColor(new Color(0, 80, 0, 90));
            for (int x = 0; x < getWidth(); x += CELL) g2.drawLine(x, 0, x, getHeight());
            for (int y = 0; y < getHeight(); y += CELL) g2.drawLine(0, y, getWidth(), y);
        }

        private void dessinerDefenses(Graphics2D g2) {
            for (Defense d : defenses) {
                if (d.estDetruit()) continue;

                boolean touchee = defensesEnPortee.contains(d);

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

                int bx = d.getX() - DEF_SIZE / 2;
                int by = d.getY() - DEF_SIZE / 2;
                g2.setColor(touchee ? new Color(220, 50, 50) : new Color(160, 60, 60));
                g2.fillRect(bx, by, DEF_SIZE, DEF_SIZE);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(bx, by, DEF_SIZE, DEF_SIZE);
                g2.setStroke(new BasicStroke(1));

                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String label = d.getNom();
                g2.setColor(Color.BLACK);
                g2.drawString(label, d.getX() - fm.stringWidth(label) / 2 + 1, d.getY() + DEF_SIZE / 2 + 15);
                g2.setColor(Color.WHITE);
                g2.drawString(label, d.getX() - fm.stringWidth(label) / 2, d.getY() + DEF_SIZE / 2 + 14);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                fm = g2.getFontMetrics();
                String info = "PV:" + d.getPv() + "  P:" + d.getPortee();
                g2.setColor(new Color(220, 220, 220));
                g2.drawString(info, d.getX() - fm.stringWidth(info) / 2, d.getY() + DEF_SIZE / 2 + 25);
            }
        }

        private void dessinerHotelDeVille(Graphics2D g2) {
            if (hotelDeVille.estDetruit()) return;

            final int SIZE = 50;
            int x = hotelDeVille.getX();
            int y = hotelDeVille.getY();
            int bx = x - SIZE / 2;
            int by = y - SIZE / 2;

            g2.setColor(new Color(218, 165, 32));
            g2.fillRect(bx, by, SIZE, SIZE);
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(3));
            g2.drawRect(bx, by, SIZE, SIZE);
            g2.setStroke(new BasicStroke(1));

            int[] xs = {bx, bx + SIZE / 2, bx + SIZE};
            int[] ys = {by, by - 18, by};
            g2.setColor(new Color(160, 82, 45));
            g2.fillPolygon(xs, ys, 3);
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(2));
            g2.drawPolygon(xs, ys, 3);
            g2.setStroke(new BasicStroke(1));

            g2.setColor(new Color(255, 255, 200));
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x - 10, y, x + 10, y);
            g2.drawLine(x, y - 10, x, y + 10);
            g2.setStroke(new BasicStroke(1));

            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            String label = hotelDeVille.getNom();
            g2.setColor(Color.BLACK);
            g2.drawString(label, x - fm.stringWidth(label) / 2 + 1, y + SIZE / 2 + 17);
            g2.setColor(new Color(255, 240, 150));
            g2.drawString(label, x - fm.stringWidth(label) / 2, y + SIZE / 2 + 16);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            fm = g2.getFontMetrics();
            String info = "PV:" + hotelDeVille.getPv();
            g2.setColor(new Color(220, 220, 220));
            g2.drawString(info, x - fm.stringWidth(info) / 2, y + SIZE / 2 + 27);
        }

        public void dessinerChrono(Graphics2D g2) {

            // Conversion des secondes restantes en minutes + secondes
            // Exemple : 125 secondes → 2 minutes 05 secondes
            int minutes = secondesRestantes / 60;
            int secondes = secondesRestantes % 60;

            // Formatage de l'affichage (ex : 2:05)
            String temps = String.format("%d:%02d", minutes, secondes);

            // Etat du chrono
            boolean urgent = secondesRestantes <= 30; // passe en mode urgence sous 30s
            boolean fini   = secondesRestantes == 0;   // chrono terminé

            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(temps) + 28;
            int x = getWidth() / 2 - w / 2;

            // Choix de la couleur du fond selon l'état du chrono
            Color fond = fini   ? new Color(180, 0, 0, 210) :   // rouge si terminé
                         urgent ? new Color(180, 80, 0, 200) :  // orange si moins de 30s
                                  new Color(0, 0, 0, 180);      // noir normal
            g2.setColor(fond);
            g2.fillRoundRect(x, 8, w, 32, 10, 10);

            g2.setColor(fini   ? new Color(255, 100, 100) :
                        urgent ? new Color(255, 200, 80)  :
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

            g2.setColor(Color.CYAN);
            g2.fillOval(clickPoint.x - 6, clickPoint.y - 6, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(clickPoint.x - 6, clickPoint.y - 6, 12, 12);
            g2.setStroke(new BasicStroke(1));

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

    private void drawAvatars(Graphics g) {
        int avatarSize = 50;
        int spacing = 120;
        int totalWidth = spacing * 2;
        int startX = (getWidth() - totalWidth) / 2;
        int y = getHeight() - 80;
        g.setColor(Color.WHITE);

        g.drawImage(barbareImg, startX, y, avatarSize, avatarSize, this);
        g.drawString("Barbare", startX, y - 5);

        g.drawImage(sorcierImg, startX + spacing, y, avatarSize, avatarSize, this);
        g.drawString("Sorcier", startX + spacing, y - 5);

        g.drawImage(pekkaImg, startX + spacing * 2, y, avatarSize, avatarSize, this);
        g.drawString("Pekka", startX + spacing * 2, y - 5);
    }
}
