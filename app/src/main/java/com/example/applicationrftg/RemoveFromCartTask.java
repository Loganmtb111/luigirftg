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
 * AsyncTask pour supprimer un film du panier via l'API
 * POST /cart/remove - Supprime la location avec status_id = 2
 */
public class RemoveFromCartTask extends AsyncTask<Integer, Void, RemoveFromCartTask.Result> {

    private RemoveFromCartCallback callback;

    // Interface callback pour communiquer le resultat
    public interface RemoveFromCartCallback {
        void onRemoveSuccess(String message);
        void onRemoveError(String error);
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

    public RemoveFromCartTask(RemoveFromCartCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        Log.d("RemoveFromCartTask", ">>> Debut de la suppression du panier");
    }

    @Override
    protected Result doInBackground(Integer... params) {
        // params[0] = filmId
        // params[1] = customerId
        int filmId = params[0];
        int customerId = params[1];

        try {
            URL url = new URL(UrlManager.getURLConnexion() + "/cart/remove");
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

            Log.d("RemoveFromCartTask", ">>> POST /cart/remove - Body: " + jsonBody.toString());

            // Envoyer le JSON
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write(jsonBody.toString().getBytes());
            out.flush();
            out.close();

            int responseCode = connection.getResponseCode();
            Log.d("RemoveFromCartTask", ">>> POST /cart/remove - Code: " + responseCode);

            if (responseCode == 200) {
                InputStream in = new BufferedInputStream(connection.getInputStream());
                String result = readStream(in);
                in.close();
                Log.d("RemoveFromCartTask", ">>> Reponse: " + result);
                connection.disconnect();
                return new Result(true, "Film supprime du panier");
            } else {
                connection.disconnect();
                return new Result(false, "Erreur serveur (code " + responseCode + ")");
            }

        } catch (Exception e) {
            Log.e("RemoveFromCartTask", ">>> Exception: " + e.toString());
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
        Log.d("RemoveFromCartTask", ">>> onPostExecute - Success: " + result.success);

        if (callback != null) {
            if (result.success) {
                callback.onRemoveSuccess(result.message);
            } else {
                callback.onRemoveError(result.message);
            }
        }
    }
}
