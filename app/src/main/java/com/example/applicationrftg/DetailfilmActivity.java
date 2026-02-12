package com.example.applicationrftg;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;

public class DetailfilmActivity extends AppCompatActivity implements AddToCartTask.AddToCartCallback {

    private int filmId;
    private TextView titreFilmView;
    private TextView descriptionFilmView;
    private TextView anneeFilmView;
    private TextView dureeFilmView;
    private TextView ratingFilmView;
    private TextView extrasFilmView;
    private Film filmActuel;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailfilm);

        Log.d("mydebug", ">>> onCreate DetailfilmActivity");

        // Récupérer l'ID du film passé en Intent
        Intent intent = getIntent();
        filmId = intent.getIntExtra("film_id", -1);
        Log.d("mydebug", ">>> Film ID reçu : " + filmId);

        // Récupérer les TextViews du layout
        titreFilmView = findViewById(R.id.titreFilm);
        descriptionFilmView = findViewById(R.id.descriptionFilm);
        anneeFilmView = findViewById(R.id.anneeFilm);
        dureeFilmView = findViewById(R.id.dureeFilm);
        ratingFilmView = findViewById(R.id.ratingFilm);
        extrasFilmView = findViewById(R.id.extrasFilm);

        // Appeler le webservice pour récupérer les détails du film
        URL urlAAppeler = null;
        try {
            // Construire l'URL avec l'ID du film
            urlAAppeler = new URL(UrlManager.getURLConnexion() + "/films/" + filmId);
            new Detailfilmstasks(this).execute(urlAAppeler);
        } catch (MalformedURLException mue) {
            Log.e("mydebug", ">>> MalformedURLException : " + mue.toString());
        }
    }

    // Méthode appelée par Detailfilmstasks après avoir reçu les détails
    public void afficherDetailFilm(String resultat) {
        Log.d("mydebug", ">>> Détails du film reçus : " + resultat);

        // Convertir le JSON en objet Film
        Film filmRecu = convertirJsonEnFilm(resultat);

        if (filmRecu != null) {
            filmActuel = filmRecu;
            // Mettre à jour l'affichage avec tous les détails
            titreFilmView.setText(filmRecu.getTitle());
            descriptionFilmView.setText(filmRecu.getDescription());
            anneeFilmView.setText(String.valueOf(filmRecu.getReleaseYear()));
            dureeFilmView.setText(filmRecu.getLength() + " min");
            ratingFilmView.setText(filmRecu.getRating() != null ? filmRecu.getRating() : "Non classé");
            extrasFilmView.setText(filmRecu.getSpecialFeatures() != null ? filmRecu.getSpecialFeatures() : "Aucun");

            Log.d("mydebug", ">>> Tous les détails affichés : " + filmRecu.getTitle());
        } else {
            Log.e("mydebug", ">>> Erreur lors de la conversion du film");
        }
    }

    // Convertir le JSON en objet Film (utilise la librairie Gson)
    private Film convertirJsonEnFilm(String jsonFilm) {
        try {
            Gson gson = new Gson();
            Film film = gson.fromJson(jsonFilm, Film.class);

            Log.d("mydebug", ">>> Conversion JSON réussie - Film : " + film.getTitle());
            return film;
        } catch (Exception e) {
            Log.e("mydebug", ">>> Erreur lors de la conversion JSON : " + e.toString());
            return null;
        }
    }

    // Quand on clique sur "Commander ce film"
    public void Commander_Film(View view) {
        if (filmActuel == null) {
            Log.e("mydebug", ">>> Erreur : filmActuel est null");
            afficherPopupCommande("Erreur : impossible d'ajouter le film au panier");
            return;
        }

        Log.d("mydebug", ">>> Clic sur Commander - Film : " + filmActuel.getTitle());

        // Recuperer le customerId depuis SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int customerId = prefs.getInt("customerId", -1);

        if (customerId == -1) {
            Log.e("mydebug", ">>> Erreur : customerId non trouve");
            afficherPopupCommande("Erreur : vous devez etre connecte pour commander");
            return;
        }

        Log.d("mydebug", ">>> CustomerId recupere : " + customerId);

        // Ajouter le film au panier local
        PanierManager panierManager = PanierManager.getInstance(this);
        panierManager.ajouterFilm(filmActuel);

        // Appeler l'API pour creer la location
        AddToCartTask task = new AddToCartTask(this);
        task.execute(filmId, customerId);
    }

    // Callback quand l'ajout au panier API reussit
    @Override
    public void onSuccess(String message) {
        Log.d("mydebug", ">>> API Success : " + message);
        PanierManager panierManager = PanierManager.getInstance(this);
        String fullMessage = "Le film \"" + filmActuel.getTitle() + "\" a ete ajoute au panier !\n\n" +
                "Location creee avec succes.\n\n" +
                "Vous avez " + panierManager.getNombreArticles() + " film(s) dans votre panier.";
        afficherPopupCommande(fullMessage);
    }

    // Callback quand l'ajout au panier API echoue
    @Override
    public void onError(String error) {
        Log.e("mydebug", ">>> API Error : " + error);
        // Retirer du panier local car l'API a echoue
        PanierManager panierManager = PanierManager.getInstance(this);
        panierManager.decrementerFilm(filmActuel.getFilmId());
        afficherPopupCommande("Erreur : " + error);
    }

    // Afficher la pop-up de confirmation
    public void afficherPopupCommande(String message) {
        Log.d("mydebug", ">>> Affichage de la pop-up : " + message);

        // Créer le layout de la pop-up
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = layoutInflater.inflate(R.layout.popup_commande, null);

        // Récupérer les éléments de la pop-up
        TextView messagePopup = customView.findViewById(R.id.messagePopup);
        Button closePopupBtn = customView.findViewById(R.id.closePopupBtn);

        // Mettre à jour le message
        messagePopup.setText(message);

        // Créer la pop-up
        popupWindow = new PopupWindow(
                customView,
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );

        // Afficher la pop-up
        View rootView = findViewById(android.R.id.content);
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);

        // Fermer la pop-up quand on clique sur le bouton
        closePopupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    // Retourner à la liste des films
    public void retourListe(View view) {
        Intent intent = new Intent(this, ListefilmsActivity.class);
        startActivity(intent);
    }
}