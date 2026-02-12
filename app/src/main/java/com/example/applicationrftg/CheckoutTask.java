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
 * AsyncTask pour valider le panier via l'API
 * POST /cart/checkout - Change status_id de 2 a 3
 */
public class CheckoutTask extends AsyncTask<Integer, Void, CheckoutTask.Result> {

    private CheckoutCallback callback;

    // Interface callback pour communiquer le resultat
    public interface CheckoutCallback {
        void onCheckoutSuccess(int itemsCount);
        void onCheckoutError(String error);
    }

    // Classe pour encapsuler le resultat
    public static class Result {
        boolean success;
        int itemsCount;
        String errorMessage;

        Result(boolean success, int itemsCount, String errorMessage) {
            this.success = success;
            this.itemsCount = itemsCount;
            this.errorMessage = errorMessage;
        }
    }

    public CheckoutTask(CheckoutCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        Log.d("CheckoutTask", ">>> Debut du checkout");
    }

    @Override
    protected Result doInBackground(Integer... params) {
        // params[0] = customerId
        int customerId = params[0];

        try {
            URL url = new URL(UrlManager.getURLConnexion() + "/cart/checkout");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.e30.jg2m4pLbAlZv1h5uPQ6fU38X23g65eXMX8q-SXuIPDg");
            connection.setDoOutput(true);

            // Creer le JSON avec customerId
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("customerId", customerId);

            Log.d("CheckoutTask", ">>> POST /cart/checkout - Body: " + jsonBody.toString());

            // Envoyer le JSON
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write(jsonBody.toString().getBytes());
            out.flush();
            out.close();

            int responseCode = connection.getResponseCode();
            Log.d("CheckoutTask", ">>> POST /cart/checkout - Code: " + responseCode);

            if (responseCode == 200) {
                InputStream in = new BufferedInputStream(connection.getInputStream());
                String result = readStream(in);
                in.close();
                Log.d("CheckoutTask", ">>> Reponse: " + result);

                // Parser la reponse pour obtenir le nombre d'items
                JSONObject jsonResponse = new JSONObject(result);
                int itemsCount = jsonResponse.optInt("itemsCount", 0);

                connection.disconnect();
                return new Result(true, itemsCount, null);
            } else {
                connection.disconnect();
                return new Result(false, 0, "Erreur serveur (code " + responseCode + ")");
            }

        } catch (Exception e) {
            Log.e("CheckoutTask", ">>> Exception: " + e.toString());
            return new Result(false, 0, "Erreur de connexion au serveur");
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
        Log.d("CheckoutTask", ">>> onPostExecute - Success: " + result.success);

        if (callback != null) {
            if (result.success) {
                callback.onCheckoutSuccess(result.itemsCount);
            } else {
                callback.onCheckoutError(result.errorMessage);
            }
        }
    }
}
