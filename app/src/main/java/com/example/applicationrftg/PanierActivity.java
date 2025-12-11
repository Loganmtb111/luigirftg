package com.example.applicationrftg;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class PanierActivity extends AppCompatActivity implements PanierAdapter.OnPanierChangeListener {

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
     * Valide la commande
     */
    private void validerCommande() {
        if (panierManager.estVide()) {
            Toast.makeText(this, "Votre panier est vide", Toast.LENGTH_SHORT).show();
            return;
        }

        int nbArticles = panierManager.getNombreArticles();

        Log.d("mydebug", ">>> Validation de la commande : " + nbArticles + " article(s)");

        // Afficher un message de confirmation
        String message = "Commande validée !\n" + nbArticles + " film(s) commandé(s)";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Vider le panier après validation
        panierManager.viderPanier();
        chargerPanier();
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
}