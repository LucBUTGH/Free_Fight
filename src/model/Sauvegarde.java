package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Sauvegarde {

    private static final String FICHIER = "sauvegarde.dat";
    public static final int NOMBRE_NIVEAUX = 4;

    private int orTotal;
    private int niveauDebloque;

    public Sauvegarde() {
        orTotal = Ameliorations.OR_DEPART;
        niveauDebloque = 1;
    }

    public void charger() {
        try {
            if (!Files.exists(Paths.get(FICHIER))) return;
            String contenu = new String(Files.readAllBytes(Paths.get(FICHIER))).trim();
            String[] lignes = contenu.split("\n");
            if (lignes.length >= 2) {
                orTotal        = Integer.parseInt(lignes[0].trim());
                niveauDebloque = Math.min(Math.max(1, Integer.parseInt(lignes[1].trim())), NOMBRE_NIVEAUX);
            }
        } catch (Exception ignored) {}
    }

    public void sauvegarder() {
        try {
            Files.write(Paths.get(FICHIER), (orTotal + "\n" + niveauDebloque).getBytes());
        } catch (IOException ignored) {}
    }

    public int  getOrTotal()        { return orTotal;        }
    public int  getNiveauDebloque() { return niveauDebloque; }

    public void ajouterOr(int montant) { orTotal += montant; }

    // Retourne true si un nouveau niveau vient d'être débloqué
    public boolean debloquerNiveauSuivant(int niveauActuel) {
        int suivant = niveauActuel + 1;
        if (suivant <= NOMBRE_NIVEAUX && suivant > niveauDebloque) {
            niveauDebloque = suivant;
            return true;
        }
        return false;
    }
}
