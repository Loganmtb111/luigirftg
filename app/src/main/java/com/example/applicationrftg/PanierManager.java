package com.example.applicationrftg;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire du panier utilisant SharedPreferences pour la persistance des données
 */
public class PanierManager {
    private static final String PREFS_NAME = "PanierPreferences";
    private static final String PANIER_KEY = "panier_items";

    private static PanierManager instance;
    private Context context;
    private Gson gson;

    private PanierManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
    }

    // Singleton pour avoir une seule instance du gestionnaire de panier
    public static synchronized PanierManager getInstance(Context context) {
        if (instance == null) {
            instance = new PanierManager(context);
        }
        return instance;
    }

    /**
     * Récupère tous les articles du panier
     */
    public List<PanierItem> getArticlesPanier() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(PANIER_KEY, null);

        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<ArrayList<PanierItem>>(){}.getType();
            List<PanierItem> items = gson.fromJson(json, type);
            return items != null ? items : new ArrayList<>();
        } catch (Exception e) {
            Log.e("PanierManager", "Erreur lors de la récupération du panier", e);
            return new ArrayList<>();
        }
    }

    /**
     * Sauvegarde le panier dans SharedPreferences
     */
    private void sauvegarderPanier(List<PanierItem> articlesPanier) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String json = gson.toJson(articlesPanier);
        editor.putString(PANIER_KEY, json);
        editor.apply();

        Log.d("PanierManager", "Panier sauvegardé : " + articlesPanier.size() + " article(s)");
    }

    /**
     * Ajoute un film au panier
     * Si le film existe déjà, augmente la quantité
     */
    public void ajouterFilm(Film film) {
        List<PanierItem> articlesPanier = getArticlesPanier();

        // Vérifier si le film existe déjà dans le panier
        boolean filmExiste = false;
        for (PanierItem item : articlesPanier) {
            if (item.getFilm().getFilmId() == film.getFilmId()) {
                item.incrementerQuantite();
                filmExiste = true;
                Log.d("PanierManager", "Film déjà dans le panier, quantité augmentée : " + film.getTitle());
                break;
            }
        }

        // Si le film n'existe pas, l'ajouter
        if (!filmExiste) {
            PanierItem nouvelItem = new PanierItem(film, 1);
            articlesPanier.add(nouvelItem);
            Log.d("PanierManager", "Nouveau film ajouté au panier : " + film.getTitle());
        }

        sauvegarderPanier(articlesPanier);
    }

    /**
     * Supprime un film du panier
     */
    public void supprimerFilm(int filmId) {
        List<PanierItem> articlesPanier = getArticlesPanier();

        for (int i = 0; i < articlesPanier.size(); i++) {
            if (articlesPanier.get(i).getFilm().getFilmId() == filmId) {
                articlesPanier.remove(i);
                Log.d("PanierManager", "Film supprimé du panier : ID " + filmId);
                break;
            }
        }

        sauvegarderPanier(articlesPanier);
    }

    /**
     * Met à jour la quantité d'un film dans le panier
     */
    public void mettreAJourQuantite(int filmId, int nouvelleQuantite) {
        if (nouvelleQuantite <= 0) {
            supprimerFilm(filmId);
            return;
        }

        List<PanierItem> articlesPanier = getArticlesPanier();

        for (PanierItem item : articlesPanier) {
            if (item.getFilm().getFilmId() == filmId) {
                item.setQuantite(nouvelleQuantite);
                Log.d("PanierManager", "Quantité mise à jour pour film ID " + filmId + " : " + nouvelleQuantite);
                break;
            }
        }

        sauvegarderPanier(articlesPanier);
    }

    /**
     * Vide complètement le panier
     */
    public void viderPanier() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PANIER_KEY);
        editor.apply();
        Log.d("PanierManager", "Panier vidé");
    }

    /**
     * Retourne le nombre total d'articles dans le panier
     */
    public int getNombreArticles() {
        List<PanierItem> items = getArticlesPanier();
        int count = 0;
        for (PanierItem item : items) {
            count += item.getQuantite();
        }
        return count;
    }

    /**
     * Calcule le prix total du panier
     */
    public double getPrixTotal() {
        List<PanierItem> items = getArticlesPanier();
        double total = 0.0;
        for (PanierItem item : items) {
            total += item.getPrixTotal();
        }
        return total;
    }

    /**
     * Vérifie si le panier est vide
     */
    public boolean estVide() {
        return getArticlesPanier().isEmpty();
    }
}
