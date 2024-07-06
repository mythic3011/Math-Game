package com.mythic3011.itp4501_assignment;

import static com.mythic3011.itp4501_assignment.R.string.developer_names;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mythic3011.itp4501_assignment.databinding.ActivityAboutBinding;

/**
 * AboutActivity provides a user interface that displays information about the application,
 * including the version, developer(s), and options to contact or share the app.
 */
public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeFirebaseAnalytics();
        setupActionBar();
        displayAppInfo();
        setupButtons();
    }

    /**
     * Sets up the action bar with a back button and the title set to the string resource R.string.about.
     */
    private void setupActionBar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.about);
        }
    }

    /**
     * Initializes the database helper for storing and retrieving game results.
     */
    private void initializeFirebaseAnalytics() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    /**
     * Displays the application's information including the name, version, description, and developer(s).
     */
    private void displayAppInfo() {
        String versionInfo = getString(R.string.app_name) + " " +
                getString(R.string.version_number) + " " +
                getAppVersion();
        binding.tvGameTitle.setText(versionInfo);
        binding.tvGameDescription.setText(R.string.app_description);
        binding.tvDeveloperInfo.setText(getString(R.string.developed_by, getString(developer_names)));
    }

    /**
     * Sets up buttons for visiting the website, contacting support, and sharing the app.
     */
    private void setupButtons() {
        setupButton(binding.btnVisitWebsite, this::openWebsite);
        setupButton(binding.btnContactUs, this::sendEmail);
        setupButton(binding.btnShareApp, this::shareApp);
    }

    /**
     * Configures a MaterialButton to perform a specific action when clicked.
     *
     * @param button The button to configure.
     * @param action The action to perform when the button is clicked.
     */
    private void setupButton(MaterialButton button, Runnable action) {
        button.setOnClickListener(v -> action.run());
    }

    /**
     * Retrieves the application's version name from the package manager.
     *
     * @return The application version name as a string, or a string indicating the version is unknown if an error occurs.
     */
    private String getAppVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return getString(R.string.unknown);
        }
    }

    /**
     * Opens the developer's website in a web browser.
     */
    private void openWebsite() {
        logAnalyticsEvent("visit_website");
        openUrl();
    }

    /**
     * Initiates an email intent to contact the developer.
     */
    private void sendEmail() {
        logAnalyticsEvent("contact_us");
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:me@mythic3011.xyz"));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_feedback));
        startActivitySafely(intent);
    }

    /**
     * Shares the application via other apps that handle text content.
     */
    private void shareApp() {
        logAnalyticsEvent("share_app");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message, getString(R.string.app_name)));
        startActivitySafely(Intent.createChooser(shareIntent, getString(R.string.share_via)));
    }

    /**
     * Opens the developer's GitHub profile in a web browser.
     * This method creates an intent to view a URL and passes it to {@link #startActivitySafely(Intent)} to handle the action.
     */
    private void openUrl() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Mythic3011"));
        startActivitySafely(intent);
    }

    /**
     * Attempts to start an activity with the given intent and catches any exceptions.
     * If no application is available to handle the intent, it displays a toast message to the user.
     * This method ensures that the app does not crash due to an unhandled exception when starting an activity.
     *
     * @param intent The intent to start.
     */
    private void startActivitySafely(Intent intent) {
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.no_app_available, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles action bar item clicks.
     * Specifically, it listens for the home button press in the action bar to navigate back.
     * Overrides the default onOptionsItemSelected method to provide custom behavior for the home button.
     *
     * @param item The menu item that was selected.
     * @return boolean Returns true if the home button was pressed, otherwise calls the superclass implementation.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
}
