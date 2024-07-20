package com.mythic3011.itp4501_assignment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Locale;
import java.util.Objects;

/**
 * The SettingsActivity class extends AppCompatActivity and is responsible for displaying and managing the settings UI.
 * It initializes the settings screen layout and toolbar, and handles navigation up to the parent activity.
 */
public class SettingsActivity extends AppCompatActivity {

    /**
     * Initializes the activity's UI components and settings fragment.
     * It sets the content view to the activity_settings layout, initializes the toolbar, and if the activity is
     * being created for the first time (i.e., savedInstanceState is null), it replaces the settings_container
     * with a new instance of SettingsFragment.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this
     *                           Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
    }

    /**
     * Handles navigation up to the parent activity when the support action bar's up button is pressed.
     *
     * @return boolean True if up navigation completed successfully and this Activity was finished, false otherwise.
     */
    @Override
    public boolean onSupportNavigateUp() {
        // Navigate up to parent activity
        NavUtils.navigateUpFromSameTask(this);
        return true;
    }

    /**
     * The SettingsFragment class extends PreferenceFragmentCompat and implements SharedPreferences.OnSharedPreferenceChangeListener.
     * It is responsible for initializing preference UI from a preference resource, handling preference changes,
     * and managing import/export of database through activity results.
     */
    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        private ActivityResultLauncher<Intent> exportLauncher;
        private ActivityResultLauncher<Intent> importLauncher;
        private SharedPreferences sharedPreferences;

        /**
         * Initializes the preference hierarchy for this fragment from the specified XML resource and sets up
         * preference change listeners and activity result launchers for import/export functionality.
         *
         * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
         * @param rootKey            If non-null, this preference fragment should be rooted at the PreferenceScreen with this key.
         */
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

            registerLaunchers();
            setupLanguagePreference();
            setupExportPreference();
            setupImportPreference();
            setupThemePreference();
            setupLanguagePreference();
            setupNotificationsSwitch();
            setupSoundSwitch();
            setupVibrationSwitch();
        }

        /**
         * Registers activity result launchers for exporting and importing databases.
         * <p>
         * This method registers two activity result launchers: `exportLauncher` and `importLauncher`.
         * The `exportLauncher` is used to start an activity for result and handle the result by exporting the database if the result code is `Activity.RESULT_OK`.
         * The `importLauncher` is used to start an activity for result and handle the result by importing the database if the result code is `Activity.RESULT_OK`.
         *
         */
        private void registerLaunchers() {
            exportLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                exportDatabase(data.getData());
                            }
                        }
                    });

            importLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                importDatabase(data.getData());
                            }
                        }
                    });
        }

        /**
         * Sets up the theme preference by finding the ListPreference with the key "theme"
         * and attaching an OnPreferenceChangeListener to it.
         *
         * @return true if the change was successfully handled
         */
        private void setupThemePreference() {
            ListPreference themePreference = findPreference("theme");
            if (themePreference != null) {
                themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    String themeOption = (String) newValue;
                    updateTheme(themeOption);
                    return true;
                });
            }
        }


        /**
         * Sets up the language preference by finding the ListPreference with the key "language"
         * and setting an OnPreferenceChangeListener to it.
         *
         * @return void
         */
        private void setupLanguagePreference() {
            ListPreference languagePreference = findPreference("language");
            if (languagePreference != null) {
                languagePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    setLocale((String) newValue);
                    requireActivity().recreate();
                    return true;
                });
            }
        }

        /**
         * Sets up the export preference by adding an OnPreferenceClickListener to it.
         * When the preference is clicked, it launches an intent to create a new document
         * with the type "application/octet-stream" and the title "game_saves.db".
         *
         * @return void
         */
        private void setupExportPreference() {
            Preference exportPreference = findPreference("export_database");
            if (exportPreference != null) {
                exportPreference.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/octet-stream");
                    intent.putExtra(Intent.EXTRA_TITLE, "game_saves.db");
                    exportLauncher.launch(intent);
                    return true;
                });
            }
        }

        /**
         * Sets up the import preference by adding an OnPreferenceClickListener to it.
         * When the preference is clicked, it launches an intent to open a document.
         * The intent is configured to open a document with the type "application/octet-stream".
         *
         * @return void
         */
        private void setupImportPreference() {
            Preference importPreference = findPreference("import_database");
            if (importPreference != null) {
                importPreference.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/octet-stream");
                    importLauncher.launch(intent);
                    return true;
                });
            }
        }

        /**
         * Sets the locale of the application based on the given language code.
         *
         * @param languageCode the language code to set the locale to
         */
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
            requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());
        }

        /**
         * Exports the database to the specified URI.
         *
         * @param uri The URI where the database will be exported.
         * @throws IOException If an I/O error occurs during the export process.
         */
        private void exportDatabase(Uri uri) {
            try {
                File dbFile = requireContext().getDatabasePath("MathGameDB");
                if (!dbFile.exists()) {
                    showToast(R.string.database_not_found);
                    return;
                }

                try (FileInputStream fis = new FileInputStream(dbFile);
                     FileOutputStream fos = (FileOutputStream) requireContext().getContentResolver().openOutputStream(uri);
                     FileChannel src = fis.getChannel();
                     FileChannel dst = fos.getChannel()) {

                    dst.transferFrom(src, 0, src.size());
                    showToast(R.string.export_success);
                }
            } catch (IOException e) {
                e.printStackTrace();
                showToast(getString(R.string.export_error, e.getMessage()));
            }
        }

        /**
         * Imports the database from the specified URI.
         *
         * @param uri The URI from which the database will be imported.
         */
        private void importDatabase(Uri uri) {
            try {
                File dbFile = requireContext().getDatabasePath("MathGameDB");
                try (FileInputStream fis = (FileInputStream) requireContext().getContentResolver().openInputStream(uri);
                     FileOutputStream fos = new FileOutputStream(dbFile);
                     FileChannel src = fis.getChannel();
                     FileChannel dst = fos.getChannel()) {

                    dst.transferFrom(src, 0, src.size());
                    showToast(R.string.import_success);
                }
            } catch (IOException e) {
                e.printStackTrace();
                showToast(getString(R.string.import_error, e.getMessage()));
            }
        }

        /**
         * Displays a toast message with the specified message resource ID.
         *
         * @param messageResId the resource ID of the message to display
         */
        private void showToast(int messageResId) {
            Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show();
        }

        /**
         * Displays a toast message with the specified message.
         *
         * @param message the message to be displayed in the toast
         */
        private void showToast(String message) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }

        /**
         * Resumes the activity and registers a listener for changes in the preference screen's shared preferences.
         * <p>
         * This method is called when the activity is resumed after it has been paused. It first calls the
         * super.onResume() method to ensure that any necessary setup is performed by the parent class.
         * Then, it registers a listener for changes in the preference screen's shared preferences by
         * calling the registerOnSharedPreferenceChangeListener() method on the preference screen's
         * shared preferences object. The listener is set to this object, so that it will receive
         * callbacks when the preferences change.
         */
        @Override
        public void onResume() {
            super.onResume();
            Objects.requireNonNull(getPreferenceScreen().getSharedPreferences()).registerOnSharedPreferenceChangeListener(this);
        }

        /**
         * Called when the fragment is no longer in use.
         * It unregisters the OnSharedPreferenceChangeListener to avoid memory leaks.
         */
        @Override
        public void onPause() {
            super.onPause();
            Objects.requireNonNull(getPreferenceScreen().getSharedPreferences()).unregisterOnSharedPreferenceChangeListener(this);
        }

        /**
         * A method to handle shared preference changes and update corresponding settings.
         *
         * @param sharedPreferences The SharedPreferences instance for accessing preferences
         * @param key               The key of the preference that has changed
         */
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case "theme":
                    updateTheme(sharedPreferences.getString(key, "system"));
                    break;
                case "language":
                    updateLanguage(sharedPreferences.getString(key, "en"));
                    break;
                case "notifications":
                    updateNotifications(sharedPreferences.getBoolean(key, true));
                    break;
                case "sound":
                    updateSound(sharedPreferences.getBoolean(key, true));
                    break;
                case "vibration":
                    updateVibration(sharedPreferences.getBoolean(key, true));
                    break;
            }
        }

        /**
         * Updates the application's theme based on the user's selection.
         * This method maps the selected theme to its corresponding resource ID and night mode setting.
         * It then saves the selected theme to SharedPreferences, applies the night mode, sets the theme,
         * and recreates the activity to reflect the changes.
         *
         * @param theme The theme selected by the user. Expected values are "light", "dark", "pixel",
         *              "cloudflare", "cloudflare_dark", "tailwind", or "system". Any other value defaults
         *              to the system theme.
         */
        private void updateTheme(String theme) {
            int themeResId;
            int nightMode;

            switch (theme) {
                case "light":
                    themeResId = R.style.Theme_MathGame_Light;
                    nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                    break;
                case "dark":
                    themeResId = R.style.Theme_MathGame_Dark;
                    nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                    break;
                case "pixel":
                    themeResId = R.style.Theme_MathGame_Pixel;
                    nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                    break;
                case "cloudflare":
                    themeResId = R.style.Theme_MathGame_Cloudflare;
                    nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                    break;
                case "cloudflare_dark":
                    themeResId = R.style.Theme_MathGame_CloudflareDark;
                    nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                    break;
                case "tailwind":
                    themeResId = R.style.Theme_MathGame_Tailwind;
                    nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                    break;
                case "system":
                    themeResId = R.style.Theme_MathGame;
                    nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    break;
                default:
                    themeResId = R.style.Theme_MathGame;
                    nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    break;
            }

            // Save the selected theme
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("theme", theme);
            editor.apply();

            // Set the night mode
            AppCompatDelegate.setDefaultNightMode(nightMode);

            // Apply the theme
            requireActivity().setTheme(themeResId);

            // Recreate the activity
            requireActivity().recreate();
        }

        /**
         * Updates the language of the application based on the given language code.
         *
         * @param languageCode the language code to set the locale to
         */
        private void updateLanguage(String languageCode) {
            setLocale(languageCode);
            requireActivity().recreate();
        }

        /**
         * Updates the sound setting in the shared preferences and shows a snackbar indicating the new state.
         *
         * @param enabled true if sound is enabled, false otherwise
         */
        private void updateSound(boolean enabled) {
            SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
            editor.putBoolean("sound", enabled).apply();
            showSettingChangedSnackbar(enabled ? R.string.sound_enabled : R.string.sound_disabled);
        }

        /**
         * Updates the notifications setting in the shared preferences and shows a snackbar indicating the new state.
         *
         * @param enabled true if notifications are enabled, false otherwise
         */
        private void updateNotifications(boolean enabled) {
            SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
            editor.putBoolean("notifications", enabled).apply();
            showSettingChangedSnackbar(enabled ? R.string.notifications_enabled : R.string.notifications_disabled);
        }

        /**
         * Updates the vibration setting in the shared preferences and shows a snackbar indicating the new state.
         *
         * @param enabled true if vibration is enabled, false otherwise
         * @return description of return value
         */
        private void updateVibration(boolean enabled) {
            SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
            editor.putBoolean("vibration", enabled).apply();
            showSettingChangedSnackbar(enabled ? R.string.vibration_enabled : R.string.vibration_disabled);
        }

        /**
         * Sets up the notifications switch and attaches an OnPreferenceChangeListener to it.
         */
        private void setupNotificationsSwitch() {
            SwitchPreferenceCompat notificationsSwitch = findPreference("notifications");
            if (notificationsSwitch != null) {
                notificationsSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean isChecked = (Boolean) newValue;
                    updateNotifications(isChecked);
                    return true;
                });
            }
        }

        /**
         * Sets up the sound switch and attaches an OnPreferenceChangeListener to it.
         *
         * @return description of return value
         */
        private void setupSoundSwitch() {
            SwitchPreferenceCompat soundSwitch = findPreference("sound");
            if (soundSwitch != null) {
                soundSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean isChecked = (Boolean) newValue;
                    updateSound(isChecked);
                    return true;
                });
            }
        }

        /**
         * Sets up the vibration switch and attaches an OnPreferenceChangeListener to it.
         *
         * @return description of return value
         */
        private void setupVibrationSwitch() {
            SwitchPreferenceCompat vibrationSwitch = findPreference("vibration");
            if (vibrationSwitch != null) {
                vibrationSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean isChecked = (Boolean) newValue;
                    updateVibration(isChecked);
                    return true;
                });
            }
        }

        /**
         * Shows a Snackbar with the specified message resource ID at the top of the current fragment's layout.
         *
         * @param messageResId the resource ID of the string to display in the Snackbar
         */
        private void showSettingChangedSnackbar(int messageResId) {
            View rootView = getView();
            if (rootView != null) {
                Snackbar.make(rootView, messageResId, Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}