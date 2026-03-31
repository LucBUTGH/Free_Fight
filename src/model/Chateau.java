package model;

import java.util.ArrayList;
import java.util.List;

public class Chateau extends Defense {

    private boolean aSpawn = false;

    public Chateau(String nom, int pv, int portee, int degats, int cadenceTir, int x, int y) {
        super(nom, pv, portee, degats, cadenceTir, x, y);
    }

    public List<Troupe> spawnDefense() {
        List<Troupe> nouvelles = new ArrayList<>();

        Barbare b1 = new Barbare(getX() + 30, getY());
        Pekka b2 = new Pekka(getX() - 30, getY());
        Sorcier b3 = new Sorcier(getX(), getY() + 30);

        b1.setCamp(Camp.ENNEMI);
        b2.setCamp(Camp.ENNEMI);
        b3.setCamp(Camp.ENNEMI);

        nouvelles.add(b1);
        nouvelles.add(b2);
        nouvelles.add(b3);

        return nouvelles;
    }

    public boolean hasSpawn() {
        return aSpawn;
    }

    public void setSpawn(boolean value) {
        this.aSpawn = value;
    }
}