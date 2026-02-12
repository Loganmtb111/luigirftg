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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * AsyncTask pour la connexion via l'API /customers/verify
 */
@SuppressWarnings("deprecation")
public class ConnexionTask extends AsyncTask<String, Integer, String> {

    private volatile MainActivity screen;

    public ConnexionTask(MainActivity s) {
        this.screen = s;
    }

    @Override
    protected void onPreExecute() {
        Log.d("mydebug", "Début de la connexion");
    }

    @Override
    protected String doInBackground(String... params) {
        String email = params[0];
        String password = params[1];

        // Afficher le mot de passe hashé en MD5 dans les logs
        String passwordMD5 = hashMD5(password);
        Log.d("mydebug", "Mot de passe MD5: " + passwordMD5);

        try {
            URL url = new URL(UrlManager.getURLConnexion() + "/customers/verify");
            return appelerServiceRestHttp(url, email, password);
        } catch (Exception e) {
            Log.e("mydebug", "Erreur connexion: " + e.toString());
            return "{\"customerId\": -1}";
        }
    }

    private String hashMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e("mydebug", "Erreur MD5: " + e.toString());
            return null;
        }
    }

    private String appelerServiceRestHttp(URL url, String email, String password) {
        String result = "";
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            // Configuration de la requête POST
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoOutput(true);

            // Création du JSON avec mot de passe crypté en MD5
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            String passwordCrypte = hashMD5(password);
            jsonBody.put("password", passwordCrypte);

            // Log du JSON et de l'URL pour debug
            Log.d("mydebug", ">>> URL: " + url.toString());
            Log.d("mydebug", ">>> JSON envoyé: " + jsonBody.toString());

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
                int codeCaractere = -1;
                while ((codeCaractere = in.read()) != -1) {
                    result = result + (char) codeCaractere;
                }
                in.close();
                Log.d("mydebug", ">>> Réponse reçue : " + result);
            } else {
                // Lire la réponse d'erreur pour debug
                InputStream errorStream = urlConnection.getErrorStream();
                if (errorStream != null) {
                    String errorResult = "";
                    int c;
                    while ((c = errorStream.read()) != -1) {
                        errorResult = errorResult + (char) c;
                    }
                    errorStream.close();
                    Log.e("mydebug", ">>> Erreur serveur " + responseCode + " : " + errorResult);
                }
            }

            urlConnection.disconnect();

        } catch (Exception e) {
            Log.e("mydebug", ">>> Exception lors de la connexion : " + e.toString());
            return "{\"customerId\": -1}";
        }

        return result.isEmpty() ? "{\"customerId\": -1}" : result;
    }

    @Override
    protected void onPostExecute(String resultat) {
        screen.onConnexionTerminee(resultat);
    }
}
