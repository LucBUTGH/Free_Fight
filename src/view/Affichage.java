package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import model.*;


public class Affichage extends JFrame {

    private final List<Defense> defenses = new ArrayList<>();
    private final Batiment hotelDeVille = new Batiment("Hôtel de Ville", 1500, 375, 290);
    private Point clickPoint = null;
    private final List<Defense> defensesEnPortee = new ArrayList<>();
  
    private List<Troupe> troupes;



    private Image barbareImg;
    private Image sorcierImg;
    private Image pekkaImg;
  

    public Affichage(List<Troupe> troupes) {

        // Défenses de test placées sur la map
        defenses.add(new Defense("Canon",        200, 150, 200, 200));
        defenses.add(new Defense("Tour Archer",  100, 220, 500, 280));
        defenses.add(new Defense("Mortier",      300, 180, 360, 430));

        setTitle("FreeFight – Test Portée Défenses");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        barbareImg = new ImageIcon("res/barbare.png").getImage();
        sorcierImg = new ImageIcon("res/sorcier.png").getImage();
        pekkaImg = new ImageIcon("res/pekka.png").getImage();

        add(new MapPanel());
        setVisible(true);
        this.troupes = troupes;
        setBackground(new Color(34,139,34)); // vert terrain
    }

    // ---------------------------------------------------------------
    private class MapPanel extends JPanel {

        private static final int CELL = 50;
        private static final int DEF_SIZE = 30;

        MapPanel() {
            setBackground(new Color(34, 139, 34));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
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
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            dessinerGrille(g2);
            dessinerHotelDeVille(g2);
            dessinerDefenses(g2);
            dessinerResultat(g2);
            // Dessine la bare en bas de l'écran
            g.setColor(new Color(50, 50, 50));
            g.fillRect(0, getHeight() - 100, getWidth(), 100);

            // Dessine les troupes
            drawAvatars(g);
        }

        private void dessinerGrille(Graphics2D g2) {
            g2.setColor(new Color(0, 80, 0, 90));
            for (int x = 0; x < getWidth(); x += CELL)  g2.drawLine(x, 0, x, getHeight());
            for (int y = 0; y < getHeight(); y += CELL) g2.drawLine(0, y, getWidth(), y);
        }

        private void dessinerDefenses(Graphics2D g2) {
            for (Defense d : defenses) {
                boolean touchee = defensesEnPortee.contains(d);

                // Zone de portée (cercle)
                Color fillPortee   = touchee ? new Color(255, 50,  50,  55) : new Color(255, 200, 0, 40);
                Color strokePortee = touchee ? new Color(255, 50,  50, 160) : new Color(255, 200, 0, 140);
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

                // Bâtiment (carré)
                int bx = d.getX() - DEF_SIZE / 2;
                int by = d.getY() - DEF_SIZE / 2;
                g2.setColor(touchee ? new Color(220, 50, 50) : new Color(160, 60, 60));
                g2.fillRect(bx, by, DEF_SIZE, DEF_SIZE);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(bx, by, DEF_SIZE, DEF_SIZE);
                g2.setStroke(new BasicStroke(1));

                // Étiquette nom
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

        private void dessinerHotelDeVille(Graphics2D g2) {
            final int SIZE = 50;
            int x = hotelDeVille.getX();
            int y = hotelDeVille.getY();
            int bx = x - SIZE / 2;
            int by = y - SIZE / 2;

            // Corps du bâtiment (doré)
            g2.setColor(new Color(218, 165, 32));
            g2.fillRect(bx, by, SIZE, SIZE);
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(3));
            g2.drawRect(bx, by, SIZE, SIZE);
            g2.setStroke(new BasicStroke(1));

            // Toit triangulaire
            int[] xs = { bx, bx + SIZE / 2, bx + SIZE };
            int[] ys = { by, by - 18, by };
            g2.setColor(new Color(160, 82, 45));
            g2.fillPolygon(xs, ys, 3);
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(2));
            g2.drawPolygon(xs, ys, 3);
            g2.setStroke(new BasicStroke(1));

            // Étoile centrale (croix simple)
            g2.setColor(new Color(255, 255, 200));
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x - 10, y, x + 10, y);
            g2.drawLine(x, y - 10, x, y + 10);
            g2.setStroke(new BasicStroke(1));

            // Étiquette nom
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            String label = hotelDeVille.getNom();
            g2.setColor(Color.BLACK);
            g2.drawString(label, x - fm.stringWidth(label) / 2 + 1, y + SIZE / 2 + 17);
            g2.setColor(new Color(255, 240, 150));
            g2.drawString(label, x - fm.stringWidth(label) / 2, y + SIZE / 2 + 16);

            // Info PV
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            fm = g2.getFontMetrics();
            String info = "PV:" + hotelDeVille.getPv();
            g2.setColor(new Color(220, 220, 220));
            g2.drawString(info, x - fm.stringWidth(info) / 2, y + SIZE / 2 + 27);
        }

        public void dessinerResultat(Graphics2D g2) {
            if (clickPoint == null) {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                g2.drawString("Cliquez sur la carte pour tester la portée des défenses", 10, 25);
                return;
            }

            // Point de clic
            g2.setColor(Color.CYAN);
            g2.fillOval(clickPoint.x - 6, clickPoint.y - 6, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(clickPoint.x - 6, clickPoint.y - 6, 12, 12);
            g2.setStroke(new BasicStroke(1));

            // Bandeau résultat
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
            g2.fillRoundRect(8, 8, w, 28, 10, 10);
            g2.setColor(defensesEnPortee.isEmpty() ? new Color(100, 230, 100) : new Color(255, 100, 100));
            g2.drawString(msg, 20, 27);
        }
    }

    private void drawAvatars(Graphics g) {

        int avatarSize = 50;         
        int spacing = 120;
        int totalWidth = spacing * 2;
        int startX = (getWidth() - totalWidth) / 2;
        int y = getHeight() - 80;     // Affichage en bas de l'écran
        g.setColor(Color.WHITE);

        // Barbare
        g.drawImage(barbareImg, startX, y, avatarSize, avatarSize, this);
        g.drawString("Barbare", startX, y - 5);

        // Sorcier
        g.drawImage(sorcierImg, startX + spacing, y, avatarSize, avatarSize, this);
        g.drawString("Sorcier", startX + spacing, y - 5);

        // Pekka
        g.drawImage(pekkaImg, startX + spacing * 2, y, avatarSize, avatarSize, this);
        g.drawString("Pekka", startX + spacing * 2, y - 5);
    }
}