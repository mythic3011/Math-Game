package com.mythic3011.itp4501_assignment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.Locale;

public class HowToPlay extends AppCompatActivity {

    private TextView tvTitle;
    private Button btnStartGame;
    private MathIconView mathIconView;
    private CardView[] instructionCards;
    private TextView[] instructionTexts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLanguageSetting();
        setContentView(R.layout.activity_how_to_play);

        initViews();
        setInstructions();
        setupListeners();
        startAnimations();
    }

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

    private void setupListeners() {
        btnStartGame.setOnClickListener(v -> startGame());
    }

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

    private void startGame() {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void applyLanguageSetting() {
        SharedPreferences sharedPreferences = getSharedPreferences("GameSettings", MODE_PRIVATE);
        int languagePosition = sharedPreferences.getInt("language", 0);
        String[] languageCodes = {"en", "zh-TW", "zh-CN", "ja"};

        if (languagePosition < languageCodes.length) {
            setLocale(languageCodes[languagePosition]);
        }
    }

    private void setLocale(String languageCode) {
        Locale locale;
        if (languageCode.contains("-")) {
            String[] parts = languageCode.split("-");
            locale = new Locale(parts[0], parts[1]);
        } else {
            locale = new Locale(languageCode);
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}