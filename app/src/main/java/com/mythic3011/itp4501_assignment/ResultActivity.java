package com.mythic3011.itp4501_assignment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mythic3011.itp4501_assignment.databinding.ActivityResultBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Activity for displaying the game results to the user.
 * This activity is responsible for showing the results of a game, including the correct answer count,
 * game duration, and the date the game was played. It also provides options to save the result,
 * play a sound, and vibrate the device.
 */

public class ResultActivity extends AppCompatActivity {
    // Binding instance for interacting with the activity's views.
    private ActivityResultBinding binding;
    // Instance of FirebaseAnalytics for logging events.
    private FirebaseAnalytics firebaseAnalytics;
    // Helper for database operations related to game results.
    private DatabaseHelper dbHelper;
    // MediaPlayer for playing success sounds.
    private MediaPlayer mediaPlayer;
    // Vibrator for providing haptic feedback.
    private Vibrator vibrator;
    // The count of correct answers in the game.
    private int correctCount;
    // Duration of the game in milliseconds.
    private long duration;
    // Date and time when the game was played.
    private String date;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        dbHelper = new DatabaseHelper(this);
        mediaPlayer = MediaPlayer.create(this, R.raw.sound_system_success_sound);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        retrieveGameData();
        displayGameResult();
        setupSaveButton();
        createNotificationChannel();
    }

    /**
     * Retrieves the game data from the intent and stores it in the class variables.
     */

    private void retrieveGameData() {
        Intent intent = getIntent();
        correctCount = intent.getIntExtra("CORRECT_COUNT", 0);
        duration = intent.getLongExtra("DURATION", 0);
        date = intent.getStringExtra("DATE");
        if (date == null) {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        }
    }

    /**
     * Displays the game result to the user.
     */

    private void displayGameResult() {
        binding.tvCorrectCount.setText(getString(R.string.correct_count, correctCount));
        binding.tvDuration.setText(getString(R.string.duration, formatDuration(duration)));
        binding.tvDate.setText(getString(R.string.date, date));
    }

    /**
     * Sets up the save button and its click listener.
     */

    private void setupSaveButton() {
        binding.btnSaveResult.setOnClickListener(v -> saveResult());
    }

    /**
     * Saves the game result to the database and logs it to Firebase Analytics.
     */

    private void saveResult() {
        String playerName = binding.etPlayerName.getText().toString().trim();
        if (playerName.isEmpty()) {
            playerName = getString(R.string.guest);
        }

        showSpinner();

        long resultId = dbHelper.insertResult(playerName, correctCount, duration, date);
        if (resultId != -1) {
            logResultToFirebase(playerName);
            askToUploadResult();
            playSuccessSound();
            if (isVibrationEnabled()) {
                vibrate();
            }
            if (isNotificationEnabled()) {
                sendNotification();
            }
            showSnackbar(getString(R.string.result_saved));
            navigateToMainScreen();
        } else {
            showSnackbar(getString(R.string.save_error));
        }

        hideSpinner();
    }

    /**
     * Asks the user if they want to upload the game result to a server.
     */

    private void askToUploadResult() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.game_result)
                .setMessage(R.string.upload_result_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> uploadResult())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    /**
     * Uploads the game result to a server.
     */

    private void uploadResult() {
        // TODO: Implement server upload logic
        showSnackbar(getString(R.string.upload_success));
    }

    /**
     * Makes the progress bar visible to indicate a loading or processing action.
     */
    private void showSpinner() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the progress bar to indicate the end of a loading or processing action.
     */
    private void hideSpinner() {
        binding.progressBar.setVisibility(View.GONE);
    }

    /**
     * Displays a Snackbar message to the user.
     * This method is used to provide feedback or information to the user.
     *
     * @param message The message to be displayed in the Snackbar.
     */
    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG);
        snackbar.getView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        snackbar.show();
    }

    /**
     * Plays a success sound if audio feedback is enabled.
     * This method checks if audio feedback is enabled in the settings before playing the sound.
     */
    private void playSuccessSound() {
        if (isAudioEnabled() && mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    /**
     * Triggers device vibration for feedback.
     * This method checks if the device has a vibrator and triggers a one-time vibration.
     * For devices running Android O (API 26) and above, it uses the VibrationEffect class to create a one-shot vibration
     * of 500 milliseconds with the default amplitude. For older versions, it directly calls the vibrate method with a
     * duration of 500 milliseconds.
     */
    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
        }
    }

    /**
     * Sends a notification to the user's device.
     * This method constructs a notification with a small icon, a title, and text content. It sets the priority of the
     * notification to default and enables auto-cancel, which removes the notification when the user taps on it.
     * The notification is sent through the NotificationManager system service.
     */
    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "game_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.game_result))
                .setContentText(getString(R.string.notification_content, correctCount, formatDuration(duration)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    /**
     * Navigates the user to the main screen after a delay.
     * This method schedules a task to start the MainActivity after a 2-second delay,
     * ensuring a smooth transition for the user. Once the MainActivity is started,
     * it finishes the current activity to remove it from the back stack.
     */
    private void navigateToMainScreen() {
        new android.os.Handler().postDelayed(() -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }

    /**
     * Logs the game result to Firebase Analytics.
     * This method packages the game result details such as player name, correct count, duration, and date
     * into a Bundle and logs it as a "game_result" event in Firebase Analytics.
     *
     * @param playerName The name of the player.
     */
    private void logResultToFirebase(String playerName) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(System.currentTimeMillis()));
        bundle.putString("player_name", playerName);
        bundle.putInt("correct_count", correctCount);
        bundle.putLong("duration", duration);
        bundle.putString("date", date);
        firebaseAnalytics.logEvent("game_result", bundle);
    }

    /**
     * Formats the game duration from milliseconds to a minutes:seconds string.
     * This method converts the game duration in milliseconds to a formatted string showing minutes and seconds.
     *
     * @param millis The duration in milliseconds.
     * @return A formatted string representing the duration in minutes and seconds.
     */
    private String formatDuration(long millis) {
        return String.format(Locale.getDefault(), "%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
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
     * Checks if notifications are enabled in the preferences.
     * This method retrieves the notification setting from the shared preferences and returns its value.
     *
     * @return True if notifications are enabled, false otherwise.
     */
    private boolean isNotificationEnabled() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("notification_enabled", true);
    }

    /**
     * Creates a notification channel for the application.
     * This method checks if the device is running on Android Oreo (API level 26) or higher, and if so,
     * creates a notification channel with a specified name, description, and importance level.
     * Notification channels are necessary for delivering notifications on Android O and above.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("game_channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Cleans up resources when the activity is destroyed.
     * This method releases the MediaPlayer resource if it is not null and closes the database helper.
     * It is called automatically when the activity is being destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
