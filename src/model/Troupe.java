package model;

public abstract class Troupe { // car c'est une base commune à toutes les troupes
    protected int x;    // position x de la troupe
    protected int y;    // position y de la troupe
    protected int health;   // points de vie de la troupe
    protected int damage;   // dégâts que la troupe inflige
    protected int speed;    // vitesse de déplacement de la troupe

    public Troupe(int x, int y, int health, int damage, int speed) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.damage = damage;
        this.speed = speed;
    }

    // public abstract void move(); // chaque troupe a une façon différente de se déplacer

    // public abstract void attack(Troupe target); // chaque troupe a une façon différente d'attaquer

    /* public void takeDamage(int damage) {
        health -= damage; // la troupe perd des points de vie lorsqu'elle subit des dégâts
        if (health < 0) {
            health = 0; // la santé ne peut pas être négative
        }
    }
    
    public boolean isAlive() {
        return health > 0; // la troupe est vivante si elle a plus de 0 points de vie
    }
        */

    // Getters
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getHealth() {
        return health;
    }
}