package com.mythic3011.itp4501_assignment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final int EXPORT_REQUEST_CODE = 1;
    private static final int IMPORT_REQUEST_CODE = 2;

    private CheckBox checkBoxSaveRecords;
    private Button btnExportSaves, btnImportSaves;
    private Spinner spinnerTheme, spinnerLanguage;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences("GameSettings", MODE_PRIVATE);

        checkBoxSaveRecords = findViewById(R.id.checkBoxSaveRecords);
        btnExportSaves = findViewById(R.id.btnExportSaves);
        btnImportSaves = findViewById(R.id.btnImportSaves);
        spinnerTheme = findViewById(R.id.spinnerTheme);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);

        setupSaveRecordsCheckBox();
        setupExportButton();
        setupImportButton();
        setupThemeSpinner();
        setupLanguageSpinner();
    }

    private void setupSaveRecordsCheckBox() {
        boolean saveRecords = sharedPreferences.getBoolean("SaveRecords", true);
        checkBoxSaveRecords.setChecked(saveRecords);
        checkBoxSaveRecords.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("SaveRecords", isChecked);
            editor.apply();
        });
    }

    private void setupExportButton() {
        btnExportSaves.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/octet-stream");
            intent.putExtra(Intent.EXTRA_TITLE, "game_saves.db");
            startActivityForResult(intent, EXPORT_REQUEST_CODE);
        });
    }

    private void setupImportButton() {
        btnImportSaves.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/octet-stream");
            startActivityForResult(intent, IMPORT_REQUEST_CODE);
        });
    }

    private void setupThemeSpinner() {
        if (spinnerTheme == null) {
            Log.e("SettingsActivity", "Theme spinner is null");
            return;
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.themes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(adapter);

        String currentTheme = sharedPreferences.getString("Theme", "Default");
        int position = adapter.getPosition(currentTheme);
        spinnerTheme.setSelection(position);

        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTheme = parent.getItemAtPosition(position).toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Theme", selectedTheme);
                editor.apply();
                applyTheme(selectedTheme);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void applyTheme(String selectedTheme) {
        switch (selectedTheme) {
            case "Dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "Light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
        recreate();
    }

    private void applyLanguage(String selectedLanguage) {
        Locale locale;
        switch (selectedLanguage) {
            case "Traditional Chinese":
                locale = Locale.TRADITIONAL_CHINESE;
                break;
            case "Simplified Chinese":
                locale = Locale.SIMPLIFIED_CHINESE;
                break;
            case "Japanese":
                locale = Locale.JAPANESE;
                break;
            default:
                locale = Locale.ENGLISH;
                break;
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        recreate();
    }


    private void setupLanguageSpinner() {
        if (spinnerLanguage == null) {
            Log.e("SettingsActivity", "Language spinner is null");
            return;
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        String currentLanguage = sharedPreferences.getString("Language", "English");
        int position = adapter.getPosition(currentLanguage);
        spinnerLanguage.setSelection(position);

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = parent.getItemAtPosition(position).toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Language", selectedLanguage);
                editor.apply();
                applyLanguage(selectedLanguage);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == EXPORT_REQUEST_CODE) {
                exportDatabase(data.getData());
            } else if (requestCode == IMPORT_REQUEST_CODE) {
                importDatabase(data.getData());
            }
        }
    }

    private void exportDatabase(Uri uri) {
        try {
            File dbFile = getDatabasePath("MathGameDB");
            FileInputStream fis = new FileInputStream(dbFile);
            FileOutputStream fos = (FileOutputStream) getContentResolver().openOutputStream(uri);
            FileChannel src = fis.getChannel();
            FileChannel dst = fos.getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            Toast.makeText(this, "Database exported successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting database", Toast.LENGTH_SHORT).show();
        }
    }

    private void importDatabase(Uri uri) {
        try {
            File dbFile = getDatabasePath("MathGameDB");
            FileInputStream fis = (FileInputStream) getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(dbFile);
            FileChannel src = fis.getChannel();
            FileChannel dst = fos.getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            Toast.makeText(this, "Database imported successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error importing database", Toast.LENGTH_SHORT).show();
        }
    }
}
