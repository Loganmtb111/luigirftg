package com.example.applicationrftg;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * AsyncTask pour ajouter un film au panier via l'API
 * POST /cart/add - Ajoute au panier avec status_id = 2
 */
public class AddToCartTask extends AsyncTask<Integer, Void, AddToCartTask.Result> {

    private AddToCartCallback callback;

    // Interface callback pour communiquer le resultat
    public interface AddToCartCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // Classe pour encapsuler le resultat
    public static class Result {
        boolean success;
        String message;

        Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public AddToCartTask(AddToCartCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        Log.d("AddToCartTask", ">>> Debut de l'ajout au panier");
    }

    @Override
    protected Result doInBackground(Integer... params) {
        // params[0] = filmId
        // params[1] = customerId
        int filmId = params[0];
        int customerId = params[1];

        try {
            // Une seule requete : POST /cart/add (status_id = 2)
            URL url = new URL(UrlManager.getURLConnexion() + "/cart/add");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.e30.jg2m4pLbAlZv1h5uPQ6fU38X23g65eXMX8q-SXuIPDg");
            connection.setDoOutput(true);

            // Creer le JSON avec filmId et customerId
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("customerId", customerId);
            jsonBody.put("filmId", filmId);

            Log.d("AddToCartTask", ">>> POST /cart/add - Body: " + jsonBody.toString());

            // Envoyer le JSON
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write(jsonBody.toString().getBytes());
            out.flush();
            out.close();

            int responseCode = connection.getResponseCode();
            Log.d("AddToCartTask", ">>> POST /cart/add - Code: " + responseCode);

            if (responseCode == 200) {
                InputStream in = new BufferedInputStream(connection.getInputStream());
                String result = readStream(in);
                in.close();
                Log.d("AddToCartTask", ">>> Reponse: " + result);
                connection.disconnect();
                return new Result(true, "Ajoute au panier avec succes !");
            } else if (responseCode == 404) {
                connection.disconnect();
                return new Result(false, "Ce film n'est plus disponible");
            } else {
                connection.disconnect();
                return new Result(false, "Erreur serveur (code " + responseCode + ")");
            }

        } catch (Exception e) {
            Log.e("AddToCartTask", ">>> Exception: " + e.toString());
            return new Result(false, "Erreur de connexion au serveur");
        }
    }

    /**
     * Lit un InputStream et retourne une String
     */
    private String readStream(InputStream in) throws Exception {
        StringBuilder result = new StringBuilder();
        int character;
        while ((character = in.read()) != -1) {
            result.append((char) character);
        }
        return result.toString();
    }

    @Override
    protected void onPostExecute(Result result) {
        Log.d("AddToCartTask", ">>> onPostExecute - Success: " + result.success);

        if (callback != null) {
            if (result.success) {
                callback.onSuccess(result.message);
            } else {
                callback.onError(result.message);
            }
        }
    }
}
