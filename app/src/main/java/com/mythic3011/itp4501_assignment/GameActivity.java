package com.mythic3011.itp4501_assignment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * GameActivity is responsible for managing the game's UI and logic.
 * It handles question generation, answer validation, and navigation between game states.
 */
public class GameActivity extends AppCompatActivity {

    // Constants for game configuration
    private static final int MAX_VALUE = 100;
    private static final int MIN_VALUE = 1;
    private static final int TOTAL_QUESTIONS = 10;
    private static final String[] OPERATORS = {"+", "-", "*", "/"};
    private static final String GAME_SETTINGS = "GameSettings";
    private static final String LANGUAGE_KEY = "language";
    private static final String THEME_KEY = "theme";
    private static final String SOUND_KEY = "sound";
    private static final String VIBRATION_KEY = "vibration";
    private static final long TIMER_INTERVAL = 1000;
    private static final int VIBRATION_DURATION = 350;

    // Handler for managing time-related tasks
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    // UI components
    private TextView tvQuestionNumber, tvQuestion, tvTimer, tvResult, tvScore;
    private EditText etAnswer;
    private Button btnDone, btnNext;
    private TextInputLayout tilAnswer;
    private FrameLayout pauseOverlay;
    private Button btnContinue, btnEndGame;
    private TextView tvPauseGame;

    // Media player for playing game sounds
    private MediaPlayer mediaPlayer;
    private MediaPlayer music;
    private boolean isPaused = false;
    private int currentQuestion = 0;
    private int correctCount = 0;
    private long startTime;

    // Database helper for storing and retrieving game results
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySettings();
        setContentView(R.layout.activity_game);
        initializeViews();
        setupListeners();
        initializeFirebaseAnalytics();
        startGame();

        // Handle back press to pause or resume the game
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isPaused) {
                    resumeGame();
                } else {
                    pauseGame();
                }
            }
        });
    }

    /**
     * Applies user settings such as language and theme from SharedPreferences.
     */
    private void applySettings() {
        SharedPreferences prefs = getSharedPreferences(GAME_SETTINGS, MODE_PRIVATE);
        String languageCode = prefs.getString(LANGUAGE_KEY, "en");
        Locale currentLocale = new Locale(languageCode);
        Locale.setDefault(currentLocale);
        int theme = prefs.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(theme);
    }

    /**
     * Initializes the database helper for storing and retrieving game results.
     */
    private void initializeFirebaseAnalytics() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    /**
     * Initializes Firebase Analytics for tracking game events.
     */
    private void initializeViews() {
        // Bind views to their respective UI components
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

    /**
     * Sets up listeners for UI components to handle user interactions.
     */
    private void setupListeners() {
        btnDone.setOnClickListener(v -> checkAnswer());
        btnNext.setOnClickListener(v -> animateToNextQuestion());
        btnContinue.setOnClickListener(v -> resumeGame());
        btnEndGame.setOnClickListener(v -> endGame());
    }

    /**
     * Starts the game by initializing game state variables, loading the first question,
     * starting the game timer, updating the score display, animating question elements,
     * logging the game start event in Firebase Analytics, and managing sound settings.
     */
    private void startGame() {
        currentQuestion = 0; // Reset the current question index to 0.
        correctCount = 0; // Reset the count of correctly answered questions to 0.
        startTime = System.currentTimeMillis(); // Record the game start time for timing purposes.
        nextQuestion(); // Load the first question.
        startTimer(); // Start the game timer to track elapsed time.
        updateScore(); // Update the score display based on the initial state.
        animateQuestionElements(); // Animate the appearance of the first question.
        logAnalyticsEvent("game_started"); // Log the game start event in Firebase Analytics.
        playGameMusic(); // Start playing background music for the game, if sound is enabled.
        playGameStartSound(); // Play a sound to indicate the game has started, if sound is enabled.
    }

    /**
     * Plays the game's background music if sound is enabled.
     * This method should contain logic to initialize and play music.
     */
    private void playGameMusic() {
        if (isAudioEnabled()) {
            music = MediaPlayer.create(this, R.raw.song_game);
            music.setVolume(0.3f, 0.3f);
            music.setLooping(true);
            music.start();
        }
    }

    /**
     * Logs a specific game event to Firebase Analytics.
     *
     * @param eventName The name of the event to log.
     *                  This method packages the event name into a Bundle and logs it using Firebase Analytics.
     */
    private void logAnalyticsEvent(String eventName) {
        Bundle bundle = new Bundle();
        bundle.putString("event_name", eventName);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    /**
     * Plays a sound to indicate the game has started, or vibrates if sound is disabled but vibration is enabled.
     * This method checks if sound is enabled to play the game start sound; otherwise, it checks if vibration is enabled to trigger a vibration.
     */
    private void playGameStartSound() {
        if (isAudioEnabled()) {
            playSound(R.raw.sound_game_start);
        } else if (isVibrationEnabled()) {
            vibrate(VIBRATION_DURATION);
        }
    }

    /**
     * Checks if vibration feedback is enabled in the preferences.
     * This method retrieves the vibration setting from the shared preferences and returns its value.
     *
     * @return True if vibration is enabled, false otherwise.
     */
    private boolean isVibrationEnabled() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("vibration_enabled", true);
    }

    /**
     * Plays a sound based on the provided resource ID.
     * This method releases any existing MediaPlayer instance before creating a new one,
     * ensuring that only one sound is played at a time. After setting up the MediaPlayer
     * with the sound resource, it sets an OnCompletionListener to release the MediaPlayer
     * once the sound has finished playing, then starts the playback.
     *
     * @param soundResId The resource ID of the sound file to be played.
     */
    private void playSound(int soundResId) {
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Release any existing MediaPlayer to prevent resource leaks.
        }
        mediaPlayer = MediaPlayer.create(this, soundResId); // Create a new MediaPlayer instance with the sound resource.
        mediaPlayer.setOnCompletionListener(MediaPlayer::release); // Set listener to release the MediaPlayer once the sound has finished playing.
        mediaPlayer.start(); // Start playback of the sound.
    }

    /**
     * Triggers device vibration for the specified duration.
     * This method checks if the device's vibrator service is available and,
     * if so, uses it to make the device vibrate for the given duration in milliseconds.
     *
     * @param duration The duration in milliseconds for which the device should vibrate.
     */
    private void vibrate(long duration) {
        if (isVibrationEnabled()) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(duration);
            }
        }
    }

    /**
     * Prepares and displays the next question in the game.
     * This method increments the current question counter and checks if the game should end based on the total number of questions.
     * If the game continues, it generates a new question by randomly selecting two numbers and an operator.
     * The method ensures that the generated question is valid according to the game's rules.
     * Once a valid question is generated, it updates the UI to display the new question and resets relevant fields for the user to input their answer.
     * Additionally, it triggers the sound effect for moving to the next question.
     */
    private void nextQuestion() {
        currentQuestion++;
        if (currentQuestion > TOTAL_QUESTIONS) {
            endGame();
            return;
        }

        int num1, num2;
        String operator;
        int result;

        do {
            num1 = random.nextInt(MAX_VALUE - MIN_VALUE + 1) + MIN_VALUE; // Generate a random number within the game's range for the first operand.
            num2 = random.nextInt(MAX_VALUE - MIN_VALUE + 1) + MIN_VALUE; // Generate a random number within the game's range for the second operand.
            operator = OPERATORS[random.nextInt(OPERATORS.length)]; // Randomly select an operator from the available operators.
            result = calculateResult(num1, num2, operator); // Calculate the result of the operation to ensure the question is valid.
        } while (!isValidQuestion(num1, num2, operator, result)); // Repeat if the generated question is not valid.

        tvQuestionNumber.setText(getString(R.string.question_number, currentQuestion, TOTAL_QUESTIONS)); // Update the question number display.
        tvQuestion.setText(getString(R.string.question_format, num1, operator, num2)); // Display the new question.
        etAnswer.setText(""); // Clear any previous answer.
        tvResult.setVisibility(View.INVISIBLE); // Hide the result view.
        btnNext.setVisibility(View.INVISIBLE); // Hide the "Next" button until the answer is checked.
        pauseTimer();
        // Show the "Done" button for the user to submit their answer. and if Done button is not clicked, it will be stop the timer
        btnDone.setVisibility(View.VISIBLE);
        playQuestionSound(); // Play the sound effect for moving to the next question.
        resumeTimer();
    }

    /**
     * Plays a sound for moving to the next question or vibrates if sound is disabled but vibration is enabled.
     * This method checks the game settings for sound and vibration preferences and acts accordingly.
     */
    private void playQuestionSound() {
        if (isAudioEnabled()) {
            playSound(R.raw.sound_game_next_question);
        } else if (isVibrationEnabled()) {
            vibrate(VIBRATION_DURATION);
        }
    }

    /**
     * Validates the generated question based on the game's rules.
     * This method ensures that division results in whole numbers and subtraction does not result in negative numbers.
     *
     * @param num1     The first number in the question.
     * @param num2     The second number in the question.
     * @param operator The operator used in the question.
     * @param result   The calculated result of the question.
     * @return true if the question is valid according to the game's rules, false otherwise.
     */
    private boolean isValidQuestion(int num1, int num2, String operator, int result) {
        if (operator.equals("/")) {
            return num1 % num2 == 0 && result > 0;
        }
        if (operator.equals("-")) {
            return result >= 0;
        }
        return true;
    }

    /**
     * Calculates the result of the operation between two numbers.
     * This method supports basic arithmetic operations: addition, subtraction, multiplication, and division.
     *
     * @param num1     The first operand.
     * @param num2     The second operand.
     * @param operator The operator indicating the type of arithmetic operation to perform.
     * @return The result of the arithmetic operation.
     * @throws IllegalArgumentException If an invalid operator is passed.
     */
    private int calculateResult(int num1, int num2, String operator) {
        switch (operator) {
            case "+":
                return num1 + num2;
            case "-":
                return num1 - num2;
            case "*":
                return num1 * num2;
            case "/":
                return num1 / num2;
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }

    /**
     * Checks the user's answer against the correct answer.
     * This method retrieves the user's answer from the EditText, validates it for non-emptiness,
     * parses it into an integer, and compares it with the correct answer calculated by {@link #calculateResult(int, int, String)}.
     * If the answer is correct, it increments the correct answer count, displays a correct answer animation,
     * and plays a correct answer sound or vibration. If the answer is incorrect, it displays an incorrect answer animation
     * and plays a wrong answer sound or vibration. Finally, it updates the UI to show the next question button and updates the score.
     */
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

        int correctAnswer = calculateResult(num1, num2, operator);

        if (answer == correctAnswer) {
            correctCount++;
            showResultWithAnimation(getString(R.string.correct));
            playCorrectSound();
        } else {
            showResultWithAnimation(getString(R.string.incorrect, correctAnswer));
            playWrongSound();
        }

        btnDone.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.VISIBLE);
        updateScore();
    }

    /**
     * Pauses the game timer by removing all callbacks and messages for the timer handler.
     * This method is used to stop the timer when the game is paused or when moving to the next question.
     */
    private void pauseTimer() {
        timerHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Resumes the game timer by posting a delayed runnable task that updates the timer display.
     * The method calculates the elapsed time since the game started and updates the timer TextView every second.
     * It checks if the game is not paused before updating the timer to ensure the timer only runs during active game play.
     */
    private void resumeTimer() {
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss", Locale.getDefault());
                    tvTimer.setText(sdf.format(new Date(elapsedTime)));
                    timerHandler.postDelayed(this, TIMER_INTERVAL);
                }
            }
        }, TIMER_INTERVAL);
    }


    /**
     * Plays a sound indicating the user has answered correctly, if sound is enabled.
     * If sound is disabled but vibration is enabled, it triggers a vibration instead.
     */
    private void playCorrectSound() {
        if (isAudioEnabled()) {
            playSound(R.raw.sound_game_correct);
        } else if (isVibrationEnabled()) {
            vibrate(VIBRATION_DURATION);
        }
    }

    /**
     * Plays a sound indicating the user has answered incorrectly, if sound is enabled.
     * If sound is disabled but vibration is enabled, it triggers a vibration instead.
     */
    private void playWrongSound() {
        if (isAudioEnabled()) {
            playSound(R.raw.sound_game_wrong);
        } else if (isVibrationEnabled()) {
            vibrate(VIBRATION_DURATION);
        }
    }

    /**
     * Displays the result message with a fade-in and fade-out animation.
     * This method sets the text of the tvResult TextView to the provided message,
     * makes it visible, and then applies a fade-in followed by a fade-out animation.
     *
     * @param message The result message to be displayed.
     */
    private void showResultWithAnimation(String message) {
        pauseTimer();
        tvResult.setText(message);
        tvResult.setVisibility(View.VISIBLE);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(tvResult, "alpha", 0f, 1f);
        fadeIn.setDuration(500);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(tvResult, "alpha", 1f, 0f);
        fadeOut.setDuration(500);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(fadeIn, fadeOut);
        animatorSet.start();
        resumeTimer();
    }

    /**
     * Updates the score display on the UI.
     * This method formats the score string with the current correct count and total questions,
     * then sets this formatted string as the text of the tvScore TextView.
     */
    private void updateScore() {
        tvScore.setText(getString(R.string.score_format, correctCount, TOTAL_QUESTIONS));
    }

    /**
     * Starts the game timer.
     * This method schedules a runnable task that updates the tvTimer TextView with the elapsed time
     * since the game started, formatted as mm:ss. This task is executed repeatedly with a fixed interval.
     */
    private void startTimer() {
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss", Locale.getDefault());
                    tvTimer.setText(sdf.format(new Date(elapsedTime)));
                    timerHandler.postDelayed(this, TIMER_INTERVAL);
                }
            }
        }, TIMER_INTERVAL);
    }

    /**
     * Pauses the game.
     * This method sets the game state to paused, makes the pause overlay visible,
     * sets the pause game text, and triggers the pause sound or vibration based on user settings.
     */
    private void pauseGame() {
        isPaused = true;
        pauseOverlay.setVisibility(View.VISIBLE);
        tvPauseGame.setText(R.string.game_paused);
        playPauseSound();
    }

    /**
     * Plays a sound or vibrates when the game is paused, based on user settings.
     * If sound is enabled, it plays the pause sound. If sound is disabled but vibration is enabled, it triggers a vibration.
     */
    private void playPauseSound() {
        if (isAudioEnabled()) {
            //playSound(R.raw.sound_game_pause);
            music.pause();
        } else if (isVibrationEnabled()) {
            vibrate(VIBRATION_DURATION);
        }
    }

    /**
     * Resumes the game from a paused state.
     * This method sets the game state to not paused, hides the pause overlay, clears the pause game text,
     * plays the resume sound or vibration based on user settings, and restarts the game timer.
     */
    private void resumeGame() {
        isPaused = false;
        pauseOverlay.setVisibility(View.GONE);
        tvPauseGame.setText("");
        playResumeSound();
        startTimer();
    }

    /**
     * Plays a sound or vibrates when the game is resumed, based on user settings.
     * If sound is enabled, it plays the resume sound. If sound is disabled but vibration is enabled, it triggers a vibration.
     */
    private void playResumeSound() {
        if (isAudioEnabled()) {
            //playSound(R.raw.sound_game_resume);
            music.start();
        } else if (isVibrationEnabled()) {
            vibrate(VIBRATION_DURATION);
        }
    }

    /**
     * Ends the current game session.
     * This method logs the game results, displays a game over message, and navigates to the ResultActivity with game details.
     * It calculates the game duration, formats the current date and time, and passes these details along with the correct answer count
     * and total questions to the ResultActivity. Finally, it finishes the current activity.
     */
    private void endGame() {
        logGameResults();
        Toast.makeText(this, getString(R.string.game_over), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, ResultActivity.class);
        long endTime = System.currentTimeMillis();
        int duration = (int) ((endTime - startTime) / 1000);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        String currentTime = timeFormat.format(new Date());
        intent.putExtra("CORRECT_COUNT", correctCount);
        intent.putExtra("DURATION", duration);
        intent.putExtra("DATE", currentDate);
        intent.putExtra("TIME", currentTime);
        intent.putExtra("total_questions", TOTAL_QUESTIONS);
        startActivity(intent);
        finish();
    }

    /**
     * Logs the game results to Firebase Analytics.
     * This method creates a bundle with the correct answer count and total questions, then logs this information
     * as a "game_results" event in Firebase Analytics.
     */
    private void logGameResults() {
        Bundle bundle = new Bundle();
        bundle.putInt("correct_count", correctCount);
        bundle.putInt("total_questions", TOTAL_QUESTIONS);
        mFirebaseAnalytics.logEvent("game_results", bundle);
    }

    /**
     * Animates to the next question in the game.
     * This method increments the current question counter and checks if the game should end based on the total number of questions.
     * If there are more questions, it calls nextQuestion() to prepare and display the next question, updates the score,
     * and animates the question elements. If there are no more questions, it ends the game.
     */
    private void animateToNextQuestion() {
        if (currentQuestion > TOTAL_QUESTIONS) {
            endGame();
        } else {
            nextQuestion();
            updateScore();
            animateQuestionElements();
        }
    }

    /**
     * Animates the question elements on the UI.
     * This method applies a fade-in animation to the tvQuestion TextView, making the question appear smoothly on the screen.
     */
    private void animateQuestionElements() {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(tvQuestion, "alpha", 0f, 1f);
        fadeIn.setDuration(500);
        fadeIn.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        if (music != null) {
            music.release();
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
}
