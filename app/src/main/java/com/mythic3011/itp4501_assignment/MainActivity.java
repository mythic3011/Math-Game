package com.mythic3011.itp4501_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    // Activity Main

    private Button btnAbout;
    private Button btnRanking;
    private Button btnGame;
    private Button btnHistory;
    private Button btnSettings;

    // Database
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize buttons
        Button btnHowToPlay = findViewById(R.id.btnHowToPlay);
        btnAbout = findViewById(R.id.btnAbout);
        btnRanking = findViewById(R.id.btnRanking);
        btnGame = findViewById(R.id.btnGame);
        btnHistory = findViewById(R.id.btnHistory);
        btnSettings = findViewById(R.id.btnSettings);

        // Set click listeners
        btnHowToPlay.setOnClickListener(this::howToPlay);
        btnAbout.setOnClickListener(this::about);
        btnRanking.setOnClickListener(this::ranking);
        btnGame.setOnClickListener(this::game);
        btnHistory.setOnClickListener(this::history);
        btnSettings.setOnClickListener(this::settings);

        // Initialize database
        dbHelper = new DatabaseHelper(this);
    }

    // create a Database and table on create the

    // ranking page
    public void ranking(View view) {
        Intent intent = new Intent(this, RankingActivity.class);
        startActivity(intent);
    }

    // game page
    public void game(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    // history page
    public void history(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    // settings page
    public void settings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    // how to play page
    public void howToPlay(View view) {
        Intent intent = new Intent(this, HowToPlay.class);
        startActivity(intent);
    }

    // about page
    public void about(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}