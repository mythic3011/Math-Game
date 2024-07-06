package com.mythic3011.itp4501_assignment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import java.util.Locale;

/**
 * Activity class that displays the "How to Play" instructions for the game.
 * It initializes the UI components, sets up listeners, and applies user settings such as language and theme.
 */
public class HowToPlay extends AppCompatActivity {

    private TextView tvTitle; // TextView for the title of the "How to Play" section
    private Button btnStartGame; // Button to start the game
    private MathIconView mathIconView; // Custom view for displaying math-related icons
    private CardView[] instructionCards; // Array of CardViews for instruction steps
    private TextView[] instructionTexts; // Array of TextViews for instruction texts

    /**
     * Called when the activity is starting.
     * This method performs initial setup including applying settings, initializing views, setting instructions,
     * setting up listeners, and starting animations.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySettings(); // Apply user settings such as theme and language
        setContentView(R.layout.activity_how_to_play); // Set the UI layout for this activity

        initViews(); // Initialize UI components
        setInstructions(); // Set instruction texts
        setupListeners(); // Setup event listeners
        startAnimations(); // Start UI animations
    }

    /**
     * Initializes UI components by finding them by their ID.
     */
    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        btnStartGame = findViewById(R.id.btnStartGame);
        mathIconView = findViewById(R.id.mathIconView);
        instructionCards = new CardView[]{
                findViewById(R.id.card1),
                findViewById(R.id.card2),
                findViewById(R.id.card3),
                findViewById(R.id.card4)
        };
        instructionTexts = new TextView[]{
                findViewById(R.id.tvCardText1),
                findViewById(R.id.tvCardText2),
                findViewById(R.id.tvCardText3),
                findViewById(R.id.tvCardText4)
        };
    }

    /**
     * Sets the instruction texts for each instruction card.
     * The texts are retrieved from string resources.
     */
    private void setInstructions() {
        String[] instructions = {
                getString(R.string.instruction1),
                getString(R.string.instruction2),
                getString(R.string.instruction3),
                getString(R.string.instruction4)
        };

        for (int i = 0; i < instructionTexts.length; i++) {
            instructionTexts[i].setText(instructions[i]);
        }
    }

    /**
     * Sets up event listeners for UI components.
     * Currently, it only sets a click listener for the start game button.
     */
    private void setupListeners() {
        btnStartGame.setOnClickListener(v -> startGame());
    }

    /**
     * Starts animations for the UI components.
     * This includes fading in the title, sliding and fading in the instruction cards, and fading in the start game button.
     */
    private void startAnimations() {
        // Animate title
        tvTitle.setAlpha(0f);
        tvTitle.animate().alpha(1f).setDuration(1000).start();

        // Animate instruction cards
        for (int i = 0; i < instructionCards.length; i++) {
            CardView card = instructionCards[i];
            card.setTranslationX(-1000f);
            card.setAlpha(0f);
            card.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(500)
                    .setStartDelay(i * 200L)
                    .start();
        }

        // Animate start game button
        btnStartGame.setAlpha(0f);
        btnStartGame.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(1500)
                .start();
    }

    /**
     * Starts the game activity and applies a transition animation.
     */
    private void startGame() {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    /**
     * Applies user-defined settings for the application.
     * This method loads settings from SharedPreferences, applies the night mode theme based on user preference,
     * and sets the application's default locale if a language preference is specified.
     */
    private void applySettings() {
        loadSettings(); // Load settings from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);
        // Apply the night mode theme based on user preference
        AppCompatDelegate.setDefaultNightMode(prefs.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));

        // Set the application's default locale if a language preference is specified
        int language = prefs.getInt("language", 0);
        if (language != 0) {
            Locale.setDefault(Locale.ENGLISH);
            Locale.setDefault(Locale.forLanguageTag("en"));
        }
    }

    /**
     * Loads settings from SharedPreferences and performs data validation.
     * This method checks if the "language" setting exists and is not a String type, and if so, removes it.
     * This cleanup ensures that the application settings are in a valid state before they are applied.
     */
    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);

        // Check and remove the "language" setting if it exists and is not a String type
        if (prefs.contains("language") && !(prefs.getAll().get("language") instanceof String)) {
            prefs.edit().remove("language").apply();
        }
    }
}