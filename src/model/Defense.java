package model;

public class Defense extends Batiment {
    private int portee;

    public Defense(String nom, int pv, int portee, int x, int y) {
        super(nom, pv, x, y);
        this.portee = portee;
    }

    public int getPortee() { return portee; }

    /**
     * Retourne vrai si le point (cx, cy) est dans le rayon de portée de cette défense.
     */
    public boolean estAPortee(int cx, int cy) {
        double distance = Math.sqrt(Math.pow(cx - x, 2) + Math.pow(cy - y, 2));
        return distance <= portee;
    }

    @Override
    public String toString() {
        return super.toString() + " | Portée: " + portee;
    }
}
