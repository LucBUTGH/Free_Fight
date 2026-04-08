package model;

/**
 * Enumération représentant le camp d'une troupe.
 * 
 * JOUEUR : troupe contrôlée par le joueur (attaque le village)
 * ENNEMI : troupe défensive spawned par le Château de Clan (défend le village)
 * 
 * Utilisé pour que les troupes ne s'attaquent pas entre elles
 * si elles sont du même camp.
 */
public enum Camp {
    JOUEUR,
    ENNEMI
}