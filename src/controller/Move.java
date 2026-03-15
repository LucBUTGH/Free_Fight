package controller;

public class Move {
    int x;
    int y;
    int speed;

        // Vérifie si la position actuelle est avant ou après la position cible sur l’axe X
        // Si x est plus petit que targetX → on avance vers la droite
        // Si x est plus grand que targetX → on recule vers la gauche

    public Move(int x, int y, int speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
    }

    public void bouger(int TargetX, int TargetY){

        if (x < TargetX) {
            x += speed; // déplacement vers l'avant
        } else if (x > TargetX) {
            x -= speed; // déplacement vers l'arrière
        }

        
        // Même logique pour l’axe Y
        // Si y est plus petit que targetY → on descend (ou monte selon ton repère)
        // Si y est plus grand que targetY → on remonte

        if (y < TargetY) {
            y += speed;
        } else if (y > TargetY) {
            y -= speed;
        }       

    }


    
}