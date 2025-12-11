package com.example.applicationrftg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void Ouvrir_Liste(View view) {
        Intent intent = new Intent(this, ListefilmsActivity.class);
        startActivity(intent);


        File monRepertoire = this.getDir("data", Context.MODE_PRIVATE);
        File listeFichiers[] = monRepertoire.listFiles();

        if (listeFichiers != null && listeFichiers.length > 0) {

            ArrayList<File> fileList = new ArrayList<>();

            for (File unFichier : listeFichiers) {
                if (unFichier.isDirectory()) {
                    fileList.add(unFichier);
                } else {
                    if (unFichier.isFile()) {
                        fileList.add(unFichier);
                    }
                }
            }
        }


    }
}