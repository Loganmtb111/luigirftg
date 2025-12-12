package com.example.applicationrftg;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class VerifyLoginTask extends AsyncTask<String, Void, Integer> {
    private volatile MainActivity screen;

    public VerifyLoginTask(MainActivity s) {
        this.screen = s;
    }

    @Override
    protected void onPreExecute() {
        Log.d("mydebug", ">>> onPreExecute - Début de la vérification login");
    }

    @Override
    protected Integer doInBackground(String... params) {
        // params[0] = email
        // params[1] = password (déjà crypté en MD5)

        String email = params[0];
        String password = params[1];
        int customerId = -1;

        try {
            URL url = new URL("http://10.0.2.2:8180/customers/verify");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            // Configuration de la requête POST
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoOutput(true);

            // Création du JSON
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("password", password);

            // Envoi du JSON
            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(jsonBody.toString().getBytes());
            out.flush();
            out.close();

            // Récupération de la réponse
            int responseCode = urlConnection.getResponseCode();
            Log.d("mydebug", ">>> Code de réponse HTTP : " + responseCode);

            if (responseCode == 200) {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String result = "";
                int codeCaractere = -1;
                while ((codeCaractere = in.read()) != -1) {
                    result = result + (char) codeCaractere;
                }
                in.close();

                Log.d("mydebug", ">>> Réponse reçue : " + result);

                // Parse du JSON de réponse
                JSONObject jsonResponse = new JSONObject(result);
                customerId = jsonResponse.getInt("customerId");
            }

            urlConnection.disconnect();

        } catch (Exception e) {
            Log.d("mydebug", ">>> Exception lors de la vérification : " + e.toString());
            e.printStackTrace();
        }

        return customerId;
    }

    @Override
    protected void onPostExecute(Integer customerId) {
        Log.d("mydebug", ">>> onPostExecute - customerId reçu : " + customerId);

        if (screen != null) {
            screen.onLoginResult(customerId);
        }
    }
}