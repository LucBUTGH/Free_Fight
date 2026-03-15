package model;

public class Batiment {
    protected String nom;
    protected int pv;
    protected int x;
    protected int y;

    public Batiment(String nom, int pv, int x, int y) {
        this.nom = nom;
        this.pv = pv;
        this.x = x;
        this.y = y;
    }

    public String getNom() { return nom; }
    public int getPv()     { return pv;  }
    public int getX()      { return x;   }
    public int getY()      { return y;   }

    @Override
    public String toString() {
        return nom + " [PV: " + pv + "] | Pos: (" + x + ", " + y + ")";
    }
}