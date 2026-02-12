package com.example.applicationrftg;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter personnalisé pour afficher les articles du panier dans une ListView
 */
public class PanierAdapter extends BaseAdapter {

    private Context context;
    private List<PanierItem> articlesPanier;
    private PanierManager panierManager;
    private OnPanierChangeListener listener;

    // Interface pour notifier l'activité des changements
    public interface OnPanierChangeListener {
        void onPanierChange();
        void onSupprimerFilm(int filmId);
    }

    public PanierAdapter(Context context, List<PanierItem> articlesPanier, OnPanierChangeListener listener) {
        this.context = context;
        this.articlesPanier = articlesPanier;
        this.panierManager = PanierManager.getInstance(context);
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return articlesPanier.size();
    }

    @Override
    public Object getItem(int position) {
        return articlesPanier.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // Créer une nouvelle vue
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_panier, parent, false);

            holder = new ViewHolder();
            holder.titreView = convertView.findViewById(R.id.itemTitre);
            holder.descriptionView = convertView.findViewById(R.id.itemDescription);
            holder.btnSupprimer = convertView.findViewById(R.id.btnSupprimer);

            convertView.setTag(holder);
        } else {
            // Réutiliser une vue existante
            holder = (ViewHolder) convertView.getTag();
        }

        // Récupérer l'article actuel
        final PanierItem item = articlesPanier.get(position);
        final Film film = item.getFilm();

        // Remplir les données
        holder.titreView.setText(film.getTitle());

        // Description limitée
        String description = film.getDescription();
        if (description != null && description.length() > 100) {
            description = description.substring(0, 100) + "...";
        }
        holder.descriptionView.setText(description);

        // Gérer le clic sur le bouton Supprimer
        holder.btnSupprimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PanierAdapter", "Suppression du film : " + film.getTitle());

                // Supprimer l'article du panier local
                panierManager.supprimerFilm(film.getFilmId());

                // Mettre à jour la liste
                articlesPanier.remove(item);

                // Notifier l'adapter et l'activité
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onPanierChange();
                    // Appeler l'API pour supprimer de la base de donnees
                    listener.onSupprimerFilm(film.getFilmId());
                }
            }
        });

        return convertView;
    }

    // ViewHolder pattern pour optimiser les performances
    private static class ViewHolder {
        TextView titreView;
        TextView descriptionView;
        Button btnSupprimer;
    }

    // Méthode pour mettre à jour les données
    public void mettreAJourDonnees(List<PanierItem> nouveauxArticles) {
        this.articlesPanier = nouveauxArticles;
        notifyDataSetChanged();
    }
}
