package com.mythic3011.itp4501_assignment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.mythic3011.itp4501_assignment.Class.DatabaseHelper;

import java.util.Locale;

/**
 * Main activity class that initializes the application's UI and functionality.
 * This class is responsible for setting up the main interface, applying user settings,
 * initializing the database and Firebase, and configuring the navigation buttons.
 */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper; // Helper object for database operations
    private MediaPlayer mediaPlayer; // Media player for playing background music

    /**
     * Called when the activity is starting.
     * This is where most initialization should go: calling setContentView(int) to inflate the activity's UI,
     * using findViewById(int) to programmatically interact with widgets in the UI, calling setup methods etc.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applySettings(); // Apply user-defined settings such as theme and language
        setupEdgeToEdgeDisplay(); // Setup UI to display in edge-to-edge mode
        initializeButtons(); // Initialize navigation buttons
        initializeDatabase(); // Initialize SQLite database
        initializeFirebase(); // Initialize Firebase
        playGameMusic(); // Play background music for the game
    }

    private void playGameMusic() {
        if (isAudioEnabled()) {
            mediaPlayer = MediaPlayer.create(this, R.raw.song_main);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    // nav to other page stop game music
    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }


    /**
     * Applies user-defined settings such as theme and language.
     * This method reads settings from SharedPreferences and applies them accordingly.
     */
    private void applySettings() {
        loadSettings(); // Load settings from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(prefs.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)); // Apply night mode theme

        int language = prefs.getInt("language", 0); // Apply language setting
        if (language != 0) {
            Locale.setDefault(Locale.ENGLISH);
            Locale.setDefault(Locale.forLanguageTag("en"));
        }
    }

    /**
     * Loads settings from SharedPreferences and performs data validation.
     * This method ensures that the settings are in a valid state before they are applied.
     */
    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);
        // Check and remove invalid "language" setting
        if (prefs.contains("language") && !(prefs.getAll().get("language") instanceof String)) {
            prefs.edit().remove("language").apply();
        }
    }

    /**
     * Checks if audio feedback is enabled in the preferences.
     * This method retrieves the audio setting from the shared preferences and returns its value.
     *
     * @return True if audio is enabled, false otherwise.
     */
    private boolean isAudioEnabled() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("audio_enabled", true);
    }

    /**
     * Sets up the UI to display in edge-to-edge mode, allowing the app's content to extend into the window insets.
     */
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

    /**
     * Initializes navigation buttons and sets their click listeners to start the respective activities.
     */
    private void initializeButtons() {
        setupButton(R.id.btnHowToPlay, HowToPlay.class);
        setupButton(R.id.btnAbout, AboutActivity.class);
        setupButton(R.id.btnRanking, RankingActivity.class);
        setupButton(R.id.btnGame, GameActivity.class);
        setupButton(R.id.btnSettings, SettingsActivity.class);
    }

    /**
     * Sets up a single button to start the specified activity when clicked.
     *
     * @param buttonId      The resource ID of the button to setup.
     * @param activityClass The class of the activity to start.
     */
    private void setupButton(int buttonId, Class<?> activityClass) {
        MaterialButton button = findViewById(buttonId);
        button.setOnClickListener(v -> startActivity(new Intent(this, activityClass)));
    }

    /**
     * Initializes the SQLite database helper and attempts to get a writable database.
     * This method creates an instance of the DatabaseHelper class, which is responsible for managing the application's database.
     * It then attempts to open the database in writable mode, which will trigger the creation or upgrade of the database if necessary.
     * If an exception occurs during database initialization, it logs the stack trace and displays an error message to the user.
     */
    private void initializeDatabase() {
        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Displays a toast message to the user.
     * This method creates and shows a toast message with the specified text. It is used throughout the MainActivity
     * to provide feedback to the user, such as error messages or notifications.
     *
     * @param message The message to be displayed in the toast.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Initializes Firebase services for the application.
     * This method calls FirebaseApp.initializeApp(context) to initialize the Firebase context with the default FirebaseApp settings.
     * It is essential for using Firebase services such as Firestore, Authentication, and Analytics.
     */
    private void initializeFirebase() {
        FirebaseApp.initializeApp(this);
    }

    /**
     * Cleans up resources when the activity is destroyed.
     * This method is called before the activity is destroyed, ensuring that the database helper is properly closed
     * to release SQLite database connections and other resources. It prevents memory leaks and ensures that the database
     * is cleanly shut down.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}