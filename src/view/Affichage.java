package view;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import model.*;
import controller.GameController;

/**
 * Vue principale du jeu.
 *
 * Responsabilités (uniquement) :
 *  - Dessiner l'état du jeu (troupes, bâtiments, grille, chrono, barre du bas)
 *  - Exposer getMapPanel() pour que le contrôleur y branche son listener souris
 *  - Recevoir de l'extérieur les données d'état visuel (sélection, portée)
 *
 * Ce qu'elle NE fait PAS :
 *  - Timers (délégués au contrôleur)
 *  - Interprétation des clics (délégués au contrôleur)
 *  - Création / modification du modèle
 */
public class Affichage extends JPanel {

    private static final long serialVersionUID = 1L;

    // ── Références ───────────────────────────────────────────────────────────
    private final Partie           partie;
    private final GameController   controller;
    private final MapPanel         mapPanel;

    // ── État visuel (fourni par le contrôleur via setClickInfo) ─────────────
    private Point         clickPoint       = null;
    private List<Defense> defensesEnPortee = new ArrayList<>();

    // ── Images ───────────────────────────────────────────────────────────────
    private final Image barbareImg;
    private final Image sorcierImg;
    private final Image pekkaImg;

    // ────────────────────────────────────────────────────────────────────────

    public Affichage(Partie partie, GameController controller) {
        this.partie     = partie;
        this.controller = controller;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 720));

        barbareImg = new ImageIcon("res/barbare.png").getImage();
        sorcierImg = new ImageIcon("res/sorcier.png").getImage();
        pekkaImg   = new ImageIcon("res/pekka.png").getImage();

        mapPanel = new MapPanel();
        add(mapPanel, BorderLayout.CENTER);
    }

    /** Exposé pour que le contrôleur branche son MouseListener. */
    public MapPanel getMapPanel() {
        return mapPanel;
    }

    /**
     * Mis à jour par le contrôleur après un clic libre (test de portée).
     */
    public void setClickInfo(int x, int y, List<Defense> enPortee) {
        this.clickPoint       = new Point(x, y);
        this.defensesEnPortee = enPortee;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Panneau de carte (dessin uniquement)
    // ════════════════════════════════════════════════════════════════════════

    public class MapPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        private static final int CELL     = 50;
        private static final int DEF_SIZE = 30;

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
            dessinerDefenses(g2);
            dessinerResultatPortee(g2);
            dessinerChrono(g2);

            // Barre du bas
            g.setColor(new Color(50, 50, 50));
            g.fillRect(0, getHeight() - 100, getWidth(), 100);

            dessinerTroupesSurCarte(g);
            dessinerAvatarsBarre(g);
        }

        // ── Grille ───────────────────────────────────────────────────────────

        private void dessinerGrille(Graphics2D g2) {
            g2.setColor(new Color(0, 80, 0, 90));
            for (int x = 0; x < getWidth(); x += CELL)  g2.drawLine(x, 0, x, getHeight());
            for (int y = 0; y < getHeight(); y += CELL) g2.drawLine(0, y, getWidth(), y);
        }

        // ── Hôtel de ville ───────────────────────────────────────────────────

        private void dessinerHotelDeVille(Graphics2D g2) {
            if (partie.getHotelDeVille().estDetruit()) return;

            final int SIZE = 50;
            int x  = partie.getHotelDeVille().getX();
            int y  = partie.getHotelDeVille().getY();
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
            String label = partie.getHotelDeVille().getNom();
            g2.setColor(Color.BLACK);
            g2.drawString(label, x - fm.stringWidth(label) / 2 + 1, y + SIZE / 2 + 17);
            g2.setColor(new Color(255, 240, 150));
            g2.drawString(label, x - fm.stringWidth(label) / 2, y + SIZE / 2 + 16);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            fm = g2.getFontMetrics();
            String info = "PV:" + partie.getHotelDeVille().getPv();
            g2.setColor(new Color(220, 220, 220));
            g2.drawString(info, x - fm.stringWidth(info) / 2, y + SIZE / 2 + 27);
        }

        // ── Défenses ─────────────────────────────────────────────────────────

        private void dessinerDefenses(Graphics2D g2) {
            for (Defense d : partie.getDefenses()) {
                if (d.estDetruit()) continue;

                boolean touchee = defensesEnPortee.contains(d);

                Color fillPortee   = touchee ? new Color(255,  50,  50,  55) : new Color(255, 200,   0,  40);
                Color strokePortee = touchee ? new Color(255,  50,  50, 160) : new Color(255, 200,   0, 140);
                int rx = d.getX() - d.getPortee();
                int ry = d.getY() - d.getPortee();
                int rd = d.getPortee() * 2;

                g2.setColor(fillPortee);
                g2.fillOval(rx, ry, rd, rd);
                g2.setColor(strokePortee);
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6, 4}, 0));
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
                g2.drawString(label, d.getX() - fm.stringWidth(label) / 2,     d.getY() + DEF_SIZE / 2 + 14);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                fm = g2.getFontMetrics();
                String info = "PV:" + d.getPv() + "  P:" + d.getPortee();
                g2.setColor(new Color(220, 220, 220));
                g2.drawString(info, d.getX() - fm.stringWidth(info) / 2, d.getY() + DEF_SIZE / 2 + 25);
            }
        }

        // ── Résultat test portée ──────────────────────────────────────────────

        private void dessinerResultatPortee(Graphics2D g2) {
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

        // ── Chronomètre ──────────────────────────────────────────────────────

        private void dessinerChrono(Graphics2D g2) {
            int sec     = partie.getSecondesRestantes();
            String temps = String.format("%d:%02d", sec / 60, sec % 60);

            boolean urgent = sec <= 30;
            boolean fini   = sec == 0;

            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(temps) + 28;
            int x = getWidth() / 2 - w / 2;

            Color fond = fini   ? new Color(180,   0,   0, 210) :
                         urgent ? new Color(180,  80,   0, 200) :
                                  new Color(  0,   0,   0, 180);
            g2.setColor(fond);
            g2.fillRoundRect(x, 8, w, 32, 10, 10);

            g2.setColor(fini   ? new Color(255, 100, 100) :
                        urgent ? new Color(255, 200,  80) :
                                 Color.WHITE);
            g2.drawString(temps, x + 14, 30);

            if (fini) {
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                String msg = "Temps écoulé !";
                g2.setColor(new Color(255, 80, 80));
                g2.drawString(msg, getWidth() / 2 - g2.getFontMetrics().stringWidth(msg) / 2, 55);
            }
        }

        // ── Troupes sur la carte ──────────────────────────────────────────────

        private void dessinerTroupesSurCarte(Graphics g) {
            Troupe selectionnee = controller.getTroupeSelectionnee();
            for (Troupe t : partie.getTroupes()) {
                Image img = imageFor(t);
                if (img != null) g.drawImage(img, t.getX(), t.getY(), 40, 40, Affichage.this);
                if (t == selectionnee) {
                    g.setColor(Color.YELLOW);
                    g.drawRect(t.getX(), t.getY(), 40, 40);
                }
            }
        }

        // ── Avatars dans la barre du bas ──────────────────────────────────────

        private void dessinerAvatarsBarre(Graphics g) {
            Troupe selectionnee = controller.getTroupeSelectionnee();
            int avatarSize = 50, spacing = 80, startX = 20;
            int y = getHeight() - 80;
            int index = 0;

            for (Troupe t : partie.getTroupes()) {
                int x = startX + index * spacing;
                Image img = imageFor(t);
                if (img != null) g.drawImage(img, x, y, avatarSize, avatarSize, Affichage.this);
                if (t == selectionnee) {
                    g.setColor(Color.YELLOW);
                    g.drawRect(x, y, avatarSize, avatarSize);
                }
                g.setColor(Color.WHITE);
                g.drawString("PV:" + t.getHealth(), x, y + 75);
                index++;
            }
        }

        // ── Utilitaire image ──────────────────────────────────────────────────

        private Image imageFor(Troupe t) {
            if (t instanceof Barbare) return barbareImg;
            if (t instanceof Sorcier) return sorcierImg;
            if (t instanceof Pekka)   return pekkaImg;
            return null;
        }
    }
}
