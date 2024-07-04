package com.mythic3011.itp4501_assignment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applySettings();

        setupEdgeToEdgeDisplay();
        initializeButtons();
        initializeDatabase();
        initializeFirebase();
    }

    private void applySettings() {
        loadSettings();
        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(prefs.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));

        int language = prefs.getInt("language", 0);
        if (language != 0) {
            Locale.setDefault(Locale.ENGLISH);
            Locale.setDefault(Locale.forLanguageTag("en"));
        }
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);

        if (prefs.contains("language") && !(prefs.getAll().get("language") instanceof String)) {
            prefs.edit().remove("language").apply();
        }
    }

    private void setupEdgeToEdgeDisplay() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void initializeButtons() {
        setupButton(R.id.btnHowToPlay, HowToPlay.class);
        setupButton(R.id.btnAbout, AboutActivity.class);
        setupButton(R.id.btnRanking, RankingActivity.class);
        setupButton(R.id.btnGame, GameActivity.class);
        setupButton(R.id.btnSettings, SettingsActivity.class);
    }

    private void setupButton(int buttonId, Class<?> activityClass) {
        MaterialButton button = findViewById(buttonId);
        button.setOnClickListener(v -> startActivity(new Intent(this, activityClass)));
    }

    private void initializeDatabase() {
        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error initializing database: " + e.getMessage());
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initializeFirebase() {
        FirebaseApp.initializeApp(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}