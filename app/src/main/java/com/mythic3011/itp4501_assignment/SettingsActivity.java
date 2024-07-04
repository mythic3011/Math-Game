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

public class SettingsActivity extends AppCompatActivity {

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

    @Override
    public boolean onSupportNavigateUp() {
        // Navigate up to parent activity
        NavUtils.navigateUpFromSameTask(this);
        return true;
    }


    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        private ActivityResultLauncher<Intent> exportLauncher;
        private ActivityResultLauncher<Intent> importLauncher;
        private SharedPreferences sharedPreferences;

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

        // this method is called when the user Toasts the app from the Settings screen
        private void showToast(int messageResId) {
            Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show();
        }

        private void showToast(String message) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

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
        private void updateLanguage(String languageCode) {
            setLocale(languageCode);
            requireActivity().recreate();}

        private void updateSound(boolean enabled) {
            SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
            editor.putBoolean("sound", enabled).apply();
            showSettingChangedSnackbar(enabled ? R.string.sound_enabled : R.string.sound_disabled);
        }

        private void updateNotifications(boolean enabled) {
            SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
            editor.putBoolean("notifications", enabled).apply();
            showSettingChangedSnackbar(enabled ? R.string.notifications_enabled : R.string.notifications_disabled);
        }

        private void updateVibration(boolean enabled) {
            SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
            editor.putBoolean("vibration", enabled).apply();
            showSettingChangedSnackbar(enabled ? R.string.vibration_enabled : R.string.vibration_disabled);
        }

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

        private void showSettingChangedSnackbar(int messageResId) {
            View rootView = getView();
            if (rootView != null) {
                Snackbar.make(rootView, messageResId, Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}