package controller;

import model.Partie;
import view.Affichage;

import javax.swing.SwingUtilities;

/**
 * Moteur d'exécution du jeu : gère les deux threads qui font avancer la partie.
 *
 * <p><b>threadJeu</b> (toutes les 40 ms) :</p>
 * <ol>
 *   <li>Appelle {@link Partie#update()} pour faire avancer la simulation d'un tick.</li>
 *   <li>Demande un repaint au thread graphique via {@link SwingUtilities#invokeLater}.</li>
 *   <li>Détecte la fin de partie et déclenche le callback.</li>
 * </ol>
 *
 * <p><b>threadChrono</b> (toutes les secondes) :</p>
 * <ol>
 *   <li>Décrémente le chronomètre via {@link Partie#decrementerTemps()}.</li>
 *   <li>Déclenche la fin de partie si le temps est écoulé.</li>
 * </ol>
 *
 * <p>Les deux threads partagent le flag {@code running} (volatile) pour s'arrêter
 * proprement. {@code finNotifiee} garantit que le callback de fin n'est appelé
 * qu'une seule fois, même si les deux threads détectent la fin simultanément.</p>
 *
 * <p>Ordre d'utilisation attendu :</p>
 * <pre>
 *   GameLoop loop = new GameLoop(partie);
 *   loop.setAffichage(affichage);
 *   loop.setFinPartieCallback(callback);
 *   loop.demarrer();
 * </pre>
 */
public class GameLoop {

    private final Partie partie;

    /** Vue à repeindre après chaque tick. Injectée après construction via {@link #setAffichage}. */
    private Affichage affichage;

    /** Boucle principale à 40 ms : simulation + rendu. */
    private final Thread threadJeu;

    /** Tick à 1 s : décrémente le chrono et détecte l'expiration du temps. */
    private final Thread threadChrono;

    /** Partagé entre les deux threads — mis à false pour les arrêter proprement. */
    private volatile boolean running = false;

    /** Verrou léger : empêche le callback de fin d'être appelé deux fois. */
    private volatile boolean finNotifiee = false;

    /** Rappelé sur le thread graphique quand la partie se termine (victoire, défaite ou temps écoulé). */
    private Runnable finPartieCallback;


    /**
     * Construit la boucle de jeu et prépare les deux threads sans les démarrer.
     *
     * @param partie  L'état du jeu à faire avancer à chaque tick.
     */
    public GameLoop(Partie partie) {
        this.partie = partie;

        threadJeu = new Thread(() -> {
            while (running) {
                partie.update();

                SwingUtilities.invokeLater(() -> {
                    if (affichage != null) affichage.repaint();
                });

                if (!finNotifiee && partie.estTerminee()) {
                    arreter();
                    SwingUtilities.invokeLater(this::notifierFin);
                    return;
                }

                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "thread-jeu");

        threadChrono = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                if (!partie.tempsEcoule()) {
                    partie.decrementerTemps();
                    SwingUtilities.invokeLater(() -> {
                        if (affichage != null) affichage.repaint();
                    });
                } else if (!finNotifiee) {
                    arreter();
                    SwingUtilities.invokeLater(this::notifierFin);
                    return;
                }
            }
        }, "thread-chrono");
    }


    /**
     * Injecte la vue à repeindre après chaque tick.
     * Doit être appelé avant {@link #demarrer()}.
     *
     * @param affichage  La vue principale du jeu.
     */
    public void setAffichage(Affichage affichage) {
        this.affichage = affichage;
    }

    /**
     * Enregistre le callback exécuté sur le thread graphique à la fin de la partie.
     * Doit être appelé avant {@link #demarrer()}.
     *
     * @param callback  Action à déclencher (ex. : afficher l'écran de fin).
     */
    public void setFinPartieCallback(Runnable callback) {
        this.finPartieCallback = callback;
    }

    /**
     * Démarre les deux threads.
     * Ne doit être appelé qu'une seule fois par instance.
     */
    public void demarrer() {
        running = true;
        threadJeu.start();
        threadChrono.start();
    }

    /**
     * Arrête les deux threads proprement.
     * Positionne {@code running} à false et envoie une interruption à chacun
     * pour sortir immédiatement du {@code sleep} en cours, puis demande un
     * dernier repaint pour mettre à jour l'affichage.
     */
    public void arreter() {
        running = false;
        threadJeu.interrupt();
        threadChrono.interrupt();
        SwingUtilities.invokeLater(() -> {
            if (affichage != null) affichage.repaint();
        });
    }

    /**
     * Exécuté sur le thread graphique à la fin de la partie.
     * Le flag {@code finNotifiee} garantit qu'un seul des deux threads
     * peut franchir ce point, même en cas de détection simultanée.
     */
    private void notifierFin() {
        finNotifiee = true;
        if (finPartieCallback != null) {
            finPartieCallback.run();
        }
    }
}
