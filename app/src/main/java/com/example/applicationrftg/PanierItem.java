package com.example.applicationrftg;

/**
 * Représente un article dans le panier
 */
public class PanierItem {
    private Film film;
    private int quantite;

    public PanierItem() {
        this.quantite = 1;
    }

    public PanierItem(Film film) {
        this.film = film;
        this.quantite = 1;
    }

    public PanierItem(Film film, int quantite) {
        this.film = film;
        this.quantite = quantite;
    }

    // Getters et setters
    public Film getFilm() {
        return film;
    }

    public void setFilm(Film film) {
        this.film = film;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    // Méthode pour augmenter la quantité
    public void incrementerQuantite() {
        this.quantite++;
    }

    // Méthode pour diminuer la quantité
    public void decrementerQuantite() {
        if (this.quantite > 0) {
            this.quantite--;
        }
    }

    // Calcul du prix total pour cet article
    public double getPrixTotal() {
        return film.getRentalRate() * quantite;
    }

    @Override
    public String toString() {
        return "PanierItem{" +
                "film=" + (film != null ? film.getTitle() : "null") +
                ", quantite=" + quantite +
                ", prixTotal=" + getPrixTotal() +
                '}';
    }
}
