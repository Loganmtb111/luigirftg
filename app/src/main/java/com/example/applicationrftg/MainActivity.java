package com.example.applicationrftg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String[] listeURLs;
    private EditText editTextURL;
    private EditText editTextLogin;
    private EditText editTextPassword;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Récupérer les références des vues
        editTextLogin = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextURL = findViewById(R.id.editTextURL);
        buttonLogin = findViewById(R.id.button);

        // Configuration du Spinner
        listeURLs = getResources().getStringArray(R.array.listeURLs);
        Spinner spinnerURLs = findViewById(R.id.spinnerURLs);
        spinnerURLs.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapterListeURLs = ArrayAdapter.createFromResource(
                this, R.array.listeURLs, android.R.layout.simple_spinner_item);
        adapterListeURLs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerURLs.setAdapter(adapterListeURLs);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seConnecter();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        editTextURL.setText(listeURLs[position]);
        UrlManager.setURLConnexion(listeURLs[position]);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void seConnecter() {
        String email = editTextLogin.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Sauvegarder l'URL saisie manuellement
        String urlSaisie = editTextURL.getText().toString().trim();
        if (!urlSaisie.isEmpty()) {
            UrlManager.setURLConnexion(urlSaisie);
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Veuillez saisir un email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Veuillez saisir un mot de passe", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("mydebug", "Connexion sur: " + UrlManager.getURLConnexion());
        new ConnexionTask(this).execute(email, password);
    }

    public void onConnexionTerminee(String resultat) {
        Log.d("mydebug", "Résultat connexion JSON: " + resultat);

        try {
            JSONObject jsonResponse = new JSONObject(resultat);
            int customerId = jsonResponse.getInt("customerId");

            if (customerId > 0) {
                // Sauvegarder le customerId dans SharedPreferences
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                prefs.edit().putInt("customerId", customerId).apply();

                Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show();
                ouvrirPage(null);
            } else {
                Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_LONG).show();
                editTextPassword.setText("");
            }

        } catch (Exception e) {
            Log.e("mydebug", "Erreur parsing JSON connexion: " + e.toString());
            Toast.makeText(this, "Erreur de connexion au serveur", Toast.LENGTH_LONG).show();
        }
    }

    public void ouvrirPage(View view) {
        Intent intent = new Intent(MainActivity.this, ListefilmsActivity.class);
        startActivity(intent);
    }
}