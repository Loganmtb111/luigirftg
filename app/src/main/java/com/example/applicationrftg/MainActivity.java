package com.example.applicationrftg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void Ouvrir_Liste(View view) {
        // Récupération des champs email et password
        EditText editTextEmail = findViewById(R.id.editTextUsername);
        EditText editTextPassword = findViewById(R.id.editTextPassword);

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Vérification que les champs ne sont pas vides
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lancement de la tâche asynchrone pour vérifier les identifiants
        // Le password est envoyé en clair, l'API le cryptera côté serveur
        VerifyLoginTask task = new VerifyLoginTask(this);
        task.execute(email, password);
    }

    // Méthode appelée par VerifyLoginTask après vérification
    public void onLoginResult(int customerId) {
        if (customerId != -1) {
            // Connexion réussie
            Toast.makeText(this, "Connexion réussie ! Customer ID: " + customerId, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ListefilmsActivity.class);
            startActivity(intent);
        } else {
            // Connexion échouée
            Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_LONG).show();
        }
    }

    // ENCRYPTAGE EN MD5
    private String encrypterChaineMD5(String chaine) {
        byte[] chaineBytes = chaine.getBytes();
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance("MD5").digest(chaineBytes);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        StringBuffer hashString = new StringBuffer();
        for (int i=0; i<hash.length; ++i ) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) {
                hashString.append('0');
                hashString.append(hex.charAt(hex.length()-1));
            }
            else {
                hashString.append(hex.substring(hex.length()-2));
            }
        }
        return hashString.toString();
    }
}