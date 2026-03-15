package test;
import model.*;
import view.Affichage;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {

        // Création troupes pour le test
        List<Troupe> troupes = new ArrayList<>();

        // Ajout de différentes troupes
        troupes.add(new Barbare(50, 500));
        troupes.add(new Sorcier(120, 500));
        troupes.add(new Pekka(190, 500));

        // Création de la fenêtre d'affichage
        new Affichage(troupes);
    }
}