package com.example.applicationrftg;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class PanierActivity extends AppCompatActivity implements PanierAdapter.OnPanierChangeListener, CheckoutTask.CheckoutCallback, RemoveFromCartTask.RemoveFromCartCallback {

    private ListView listePanier;
    private TextView textePanierVide;
    private Button btnValiderCommande;
    private Button btnViderPanier;
    private PanierManager panierManager;
    private PanierAdapter panierAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panier);

        Log.d("mydebug", ">>> onCreate PanierActivity");

        // Initialiser le gestionnaire de panier
        panierManager = PanierManager.getInstance(this);

        // Récupérer les vues
        listePanier = findViewById(R.id.listePanier);
        textePanierVide = findViewById(R.id.textePanierVide);
        btnValiderCommande = findViewById(R.id.btnValiderCommande);
        btnViderPanier = findViewById(R.id.btnViderPanier);

        // Charger et afficher le panier
        chargerPanier();

        // Configurer le bouton "Valider la commande"
        btnValiderCommande.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validerCommande();
            }
        });

        // Configurer le bouton "Vider le panier"
        btnViderPanier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viderPanier();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger le panier quand on revient sur cette activité
        chargerPanier();
    }

    /**
     * Charge les articles du panier et met à jour l'affichage
     */
    private void chargerPanier() {
        List<PanierItem> articlesPanier = panierManager.getArticlesPanier();

        Log.d("mydebug", ">>> Chargement du panier : " + articlesPanier.size() + " article(s)");

        if (articlesPanier.isEmpty()) {
            // Panier vide
            listePanier.setVisibility(View.GONE);
            textePanierVide.setVisibility(View.VISIBLE);
            btnValiderCommande.setEnabled(false);
            btnViderPanier.setEnabled(false);
        } else {
            // Panier contient des articles
            listePanier.setVisibility(View.VISIBLE);
            textePanierVide.setVisibility(View.GONE);
            btnValiderCommande.setEnabled(true);
            btnViderPanier.setEnabled(true);

            // Créer ou mettre à jour l'adapter
            if (panierAdapter == null) {
                panierAdapter = new PanierAdapter(this, articlesPanier, this);
                listePanier.setAdapter(panierAdapter);
            } else {
                panierAdapter.mettreAJourDonnees(articlesPanier);
            }
        }
    }

    /**
     * Appelée quand le panier est modifié (suppression d'un article)
     */
    @Override
    public void onPanierChange() {
        Log.d("mydebug", ">>> Panier modifié, rechargement...");
        chargerPanier();
    }

    /**
     * Valide la commande - appelle l'API pour passer status de 2 a 3
     */
    private void validerCommande() {
        if (panierManager.estVide()) {
            Toast.makeText(this, "Votre panier est vide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recuperer le customerId depuis SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int customerId = prefs.getInt("customerId", -1);

        if (customerId == -1) {
            Toast.makeText(this, "Erreur : vous devez etre connecte", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("mydebug", ">>> Validation de la commande pour customerId: " + customerId);

        // Appeler l'API /cart/checkout
        new CheckoutTask(this).execute(customerId);
    }

    // Callback quand le checkout reussit
    @Override
    public void onCheckoutSuccess(int itemsCount) {
        Log.d("mydebug", ">>> Checkout reussi : " + itemsCount + " article(s)");

        String message = "Commande validee !\n" + itemsCount + " film(s) commande(s)";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Vider le panier local apres validation
        panierManager.viderPanier();
        chargerPanier();
    }

    // Callback quand le checkout echoue
    @Override
    public void onCheckoutError(String error) {
        Log.e("mydebug", ">>> Checkout echoue : " + error);
        Toast.makeText(this, "Erreur : " + error, Toast.LENGTH_LONG).show();
    }

    /**
     * Appelée quand on supprime un film du panier - appelle l'API pour supprimer de la BDD
     */
    @Override
    public void onSupprimerFilm(int filmId) {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int customerId = prefs.getInt("customerId", -1);

        if (customerId == -1) {
            Log.e("mydebug", ">>> Erreur : customerId non trouve pour suppression");
            return;
        }

        Log.d("mydebug", ">>> Suppression API du film ID " + filmId + " pour customerId " + customerId);
        new RemoveFromCartTask(this).execute(filmId, customerId);
    }

    // Callback quand la suppression API reussit
    @Override
    public void onRemoveSuccess(String message) {
        Log.d("mydebug", ">>> Suppression API reussie : " + message);
    }

    // Callback quand la suppression API echoue
    @Override
    public void onRemoveError(String error) {
        Log.e("mydebug", ">>> Suppression API echouee : " + error);
        Toast.makeText(this, "Erreur suppression serveur : " + error, Toast.LENGTH_SHORT).show();
    }

    /**
     * Vide complètement le panier
     */
    private void viderPanier() {
        if (panierManager.estVide()) {
            Toast.makeText(this, "Votre panier est déjà vide", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("mydebug", ">>> Vidage du panier");

        panierManager.viderPanier();
        chargerPanier();

        Toast.makeText(this, "Le panier a été vidé", Toast.LENGTH_SHORT).show();
    }

    /**
     * Retourne à la liste des films
     **/
    public void retourListeFilms(View view) {
        Intent intent = new Intent(this, ListefilmsActivity.class);
        startActivity(intent);
    }
}