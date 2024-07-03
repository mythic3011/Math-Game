package com.mythic3011.itp4501_assignment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.analytics.FirebaseAnalytics;

public class GameActivity extends AppCompatActivity {

    private TextView tvQuestionNumber, tvQuestion, tvTimer, tvResult, tvScore;
    private EditText etAnswer;
    private Button btnDone, btnNext;
    private TextInputLayout tilAnswer;
    private FrameLayout pauseOverlay;
    private Button btnContinue, btnEndGame;
    private TextView tvPauseGame;

    private static final int MAX_VALUE = 100;
    private static final int MIN_VALUE = 1;
    private static final int TOTAL_QUESTIONS = 10;

    private int currentQuestion = 0;
    private int correctCount = 0;
    private long startTime;
    private Handler timerHandler = new Handler();
    private Random random = new Random();
    private DatabaseHelper dbHelper;
    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean isPaused = false;
    private Locale currentLocale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySettings();
        setContentView(R.layout.activity_game);
        initializeViews();
        setupListeners();
        initializeDatabase();
        initializeFirebaseAnalytics();
        startGame();
    }

    private void applySettings() {
        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);

        // Clear incorrect value if it exists
        if (prefs.contains("language") && !(prefs.getAll().get("language") instanceof String)) {
            prefs.edit().remove("language").apply();
        }

        String languageCode = prefs.getString("language", "en");
        currentLocale = new Locale(languageCode);
        Locale.setDefault(currentLocale);

        int theme = prefs.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(theme);
    }

    private void initializeDatabase() {
        dbHelper = new DatabaseHelper(this);
    }

    private void initializeFirebaseAnalytics() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    private void initializeViews() {
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvTimer = findViewById(R.id.tvTimer);
        tvResult = findViewById(R.id.tvResult);
        tvScore = findViewById(R.id.tvScore);
        etAnswer = findViewById(R.id.etAnswer);
        btnDone = findViewById(R.id.btnDone);
        btnNext = findViewById(R.id.btnNext);
        tilAnswer = findViewById(R.id.tilAnswer);
        pauseOverlay = findViewById(R.id.pauseOverlay);
        btnContinue = findViewById(R.id.btnContinue);
        btnEndGame = findViewById(R.id.btnEndGame);
        tvPauseGame = findViewById(R.id.tvPauseGame);
    }

    private void setupListeners() {
        btnDone.setOnClickListener(v -> checkAnswer());
        btnNext.setOnClickListener(v -> animateToNextQuestion());
        btnContinue.setOnClickListener(v -> resumeGame());
        btnEndGame.setOnClickListener(v -> endGame());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!isPaused) {
            pauseGame();
        } else {
            resumeGame();
        }
    }

    private void startGame() {
        currentQuestion = 0;
        correctCount = 0;
        startTime = System.currentTimeMillis();
        nextQuestion();
        startTimer();
        updateScore();
        animateQuestionElements();
    }

    private void nextQuestion() {
        currentQuestion++;
        if (currentQuestion > TOTAL_QUESTIONS) {
            endGame();
            return;
        }

        int num1 = random.nextInt(MAX_VALUE - MIN_VALUE + 1) + MIN_VALUE;
        int num2 = random.nextInt(MAX_VALUE - MIN_VALUE + 1) + MIN_VALUE;
        String operator = random.nextBoolean() ? "+" : "-";

        tvQuestionNumber.setText(getString(R.string.question_number, currentQuestion, TOTAL_QUESTIONS));
        tvQuestion.setText(getString(R.string.question_format, num1, operator, num2));
        etAnswer.setText("");
        tvResult.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.INVISIBLE);
        btnDone.setVisibility(View.VISIBLE);
    }

    private void checkAnswer() {
        String userAnswer = etAnswer.getText().toString().trim();
        if (userAnswer.isEmpty()) {
            tilAnswer.setError(getString(R.string.enter_answer));
            return;
        }

        int answer = Integer.parseInt(userAnswer);
        String[] parts = tvQuestion.getText().toString().split(" ");
        int num1 = Integer.parseInt(parts[0]);
        int num2 = Integer.parseInt(parts[2]);
        String operator = parts[1];

        int correctAnswer = operator.equals("+") ? num1 + num2 : num1 - num2;

        if (answer == correctAnswer) {
            correctCount++;
            showResultWithAnimation(getString(R.string.correct));
        } else {
            showResultWithAnimation(getString(R.string.incorrect, correctAnswer));
        }

        btnDone.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.VISIBLE);
        updateScore();
    }

    private void updateScore() {
        tvScore.setText(getString(R.string.score, correctCount, currentQuestion));
    }

    private void endGame() {
        try {
            // Stop the timer
            timerHandler.removeCallbacksAndMessages(null);
            long endTime = System.currentTimeMillis();
            int duration = (int) ((endTime - startTime) / 1000);

            // Format the current date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());
            String currentTime = timeFormat.format(new Date());

            // Create an intent to start the ResultActivity
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("CORRECT_COUNT", correctCount);
            intent.putExtra("DURATION", duration);
            intent.putExtra("DATE", currentDate);
            intent.putExtra("TIME", currentTime);

            // Start the ResultActivity
            startActivity(intent);
            finish(); // Close the GameActivity
        } catch (Exception e) {
            Toast.makeText(this, "Error ending game: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void pauseGame() {
        isPaused = true;
        timerHandler.removeCallbacksAndMessages(null);
        showPauseOverlay();
        logAnalyticsEvent("game_paused");
    }

    private void resumeGame() {
        isPaused = false;
        startTimer();
        hidePauseOverlay();
        logAnalyticsEvent("game_resumed");
    }

    private void showPauseOverlay() {
        pauseOverlay.setVisibility(View.VISIBLE);
        pauseOverlay.animate().alpha(1f).setDuration(300);
        blurBackground();
    }

    private void hidePauseOverlay() {
        pauseOverlay.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            pauseOverlay.setVisibility(View.GONE);
            unblurBackground();
        });
    }

    private void blurBackground() {

    }

    private void unblurBackground() {

    }

    private void logAnalyticsEvent(String eventName) {
        Bundle bundle = new Bundle();
        bundle.putString("event_name", eventName);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void startTimer() {
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    long millis = System.currentTimeMillis() - startTime;
                    int seconds = (int) (millis / 1000);
                    tvTimer.setText(String.format(Locale.getDefault(), "Time: %d seconds", seconds));
                    if (seconds % 5 == 0) {
                        pulseTimerAnimation();
                    }
                    timerHandler.postDelayed(this, 1000);
                } catch (Exception e) {
                    Toast.makeText(GameActivity.this, "Error updating timer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, 0);
    }

    private void animateToNextQuestion() {
        View[] elementsToFadeOut = {tvQuestion, tilAnswer, btnDone, tvResult};
        for (View element : elementsToFadeOut) {
            element.animate().alpha(0f).setDuration(300);
        }

        new Handler().postDelayed(() -> {
            nextQuestion();
            animateQuestionElements();
        }, 300);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacksAndMessages(null);
    }

    private void pulseTimerAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvTimer, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvTimer, "scaleY", 1f, 1.2f, 1f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(1000);
        animatorSet.start();
    }

    private void animateQuestionElements() {
        View[] elements = {tvQuestionNumber, tvTimer, tvQuestion, tilAnswer, btnDone, tvScore};
        for (View element : elements) {
            element.setAlpha(0f);
            element.animate().alpha(1f).setDuration(500).setStartDelay(100);
        }
    }

    private void showResultWithAnimation(String result) {
        tvResult.setText(result);
        tvResult.setVisibility(View.VISIBLE);
        tvResult.setScaleX(0f);
        tvResult.setScaleY(0f);
        tvResult.setAlpha(0f);
        tvResult.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(300);
    }
}
