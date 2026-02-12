package com.example.applicationrftg;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ListefilmsActivity extends AppCompatActivity {

    private ListView listeFilmsView;
    private ArrayAdapter<Film> adapter;
    private ArrayList<Film> films = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, ">>> onCreate - Démarrage de ListefilmsActivity");
        setContentView(R.layout.activity_listefilms);

        // Récupérer la ListView du layout XML
        listeFilmsView = findViewById(R.id.listeFilms);

        // Créer l'adaptateur (qui va remplir la ListView)
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, films);
        listeFilmsView.setAdapter(adapter);

        // Ajouter un listener pour détecter les clics sur les films
        listeFilmsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Récupérer le film cliqué
                Film filmClique = films.get(position);
                Log.d(TAG, ">>> Film cliqué : " + filmClique.getTitle() + " (ID: " + filmClique.getFilmId() + ")");

                // Lancer DetailfilmActivity en passant l'ID du film
                Intent intent = new Intent(ListefilmsActivity.this, DetailfilmActivity.class);
                intent.putExtra("film_id", filmClique.getFilmId());
                startActivity(intent);
            }
        });

        // Appeler le webservice de façon asynchrone
        URL urlAAppeler = null;
        try {
            urlAAppeler = new URL(UrlManager.getURLConnexion() + "/films");
            new Listefilmstasks(this).execute(urlAAppeler);
        } catch (MalformedURLException mue) {
            Log.e(TAG, ">>> onCreate - MalformedURLException : " + mue.toString());
        } finally {
            urlAAppeler = null;
        }
    }

    // Quand on clique sur le bouton "Panier"
    public void Ouvrir_Panier(View view) {
        Intent intent = new Intent(this, PanierActivity.class);
        startActivity(intent);
    }

    // Méthode appelée par Listefilmstasks après avoir reçu les données
    public void afficherResultat(String resultat) {
        Log.d(TAG, ">>> Résultat reçu dans l'activité : " + resultat);

        // Convertir le JSON en ArrayList<Film>
        ArrayList<Film> filmsRecus = convertirJsonEnFilms(resultat);

        // Mettre à jour la ListView avec les nouveaux films
        if (filmsRecus != null && !filmsRecus.isEmpty()) {
            films.clear();  // Vider la liste actuelle
            films.addAll(filmsRecus);  // Ajouter les nouveaux films
            adapter.notifyDataSetChanged();  // IMPORTANT : Dire à l'adaptateur que les données ont changé
            Log.d(TAG, ">>> " + films.size() + " films affichés");
        } else {
            Log.e(TAG, ">>> Aucun film reçu ou erreur de conversion");
        }
    }

    // Convertir le JSON en ArrayList<Film> (utilise la librairie Gson)
    private ArrayList<Film> convertirJsonEnFilms(String jsonFilms) {
        try {
            Gson gson = new Gson();
            Type filmListType = new TypeToken<ArrayList<Film>>(){}.getType();
            ArrayList<Film> filmArray = gson.fromJson(jsonFilms, filmListType);

            Log.d(TAG, ">>> Conversion JSON réussie - " + filmArray.size() + " films convertis");
            return filmArray;
        } catch (Exception e) {
            Log.e(TAG, ">>> Erreur lors de la conversion JSON : " + e.toString());
            return null;
        }
    }
}