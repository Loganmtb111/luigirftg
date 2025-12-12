package com.example.applicationrftg;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Listefilmstasks extends AsyncTask<URL, Integer, String> {
    private volatile ListefilmsActivity screen;  // référence à l'écran

    public Listefilmstasks(ListefilmsActivity s) {
        this.screen = s;
    }

    @Override
    protected void onPreExecute() {
        // Prétraitement de l'appel (avant de lancer la requête)
        Log.d("mydebug", ">>> onPreExecute - Début de l'appel au service");
    }

    @Override
    protected String doInBackground(URL... urls) {
        // Cette méthode s'exécute en ARRIÈRE-PLAN (thread parallèle)
        String sResultatAppel = null;
        URL urlAAppeler = urls[0];
        Log.d("mydebug", ">>> doInBackground - Appel du webservice : " + urlAAppeler);
        sResultatAppel = appelerServiceRestHttp(urlAAppeler);
        return sResultatAppel;
    }

    @Override
    protected void onPostExecute(String resultat) {
        // Cette méthode s'exécute dans le THREAD PRINCIPAL (UI)
        System.out.println(">>> onPostExecute / resultat=" + resultat);
        Log.d("mydebug", ">>> onPostExecute - Données reçues : " + resultat);

        // IMPORTANT : On appelle la méthode de mise à jour de l'activity
        if (screen != null) {
            screen.afficherResultat(resultat);
        }
    }

    // Méthode privée qui fait l'appel HTTP GET
    private String appelerServiceRestHttp(URL urlAAppeler) {
        String sResultatAppel = "";
        HttpURLConnection urlConnection = null;
        int responseCode = -1;
        try {
            // Ouvrir la connexion
            urlConnection = (HttpURLConnection) urlAAppeler.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"));
            urlConnection.setRequestProperty("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.e30.jg2m4pLbAlZv1h5uPQ6fU38X23g65eXMX8q-SXuIPDg");

            // Récupérer le code de réponse HTTP
            responseCode = urlConnection.getResponseCode();
            Log.d("mydebug", ">>> Code de réponse HTTP : " + responseCode);

            // Lire la réponse
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            // Lire caractère par caractère et construire la chaîne de résultat
            int codeCaractere = -1;
            while ((codeCaractere = in.read()) != -1) {
                sResultatAppel = sResultatAppel + (char) codeCaractere;
            }
            in.close();
            Log.d("mydebug", ">>> Résultat obtenu : " + sResultatAppel.substring(0, Math.min(100, sResultatAppel.length())));
        } catch (IOException ioe) {
            Log.d("mydebug", ">>> IOException : " + ioe.toString());
        } catch (Exception e) {
            Log.d("mydebug", ">>> Exception : " + e.toString());
        } finally {
            // Fermer la connexion
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return sResultatAppel;
    }
}