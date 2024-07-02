package com.mythic3011.itp4501_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private TextView tvResult;
    private EditText etName;
    private Button btnSave, btnUpload, btnBackToMain;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        dbHelper = new DatabaseHelper(this);

        tvResult = findViewById(R.id.tvResult);
        etName = findViewById(R.id.etName);
        btnSave = findViewById(R.id.btnSave);
        btnUpload = findViewById(R.id.btnUpload);
        btnBackToMain = findViewById(R.id.btnBackToMain);

        // Get data from intent
        int correctCount = getIntent().getIntExtra("CORRECT_COUNT", 0);
        int duration = getIntent().getIntExtra("DURATION", 0);
        String date = getIntent().getStringExtra("DATE");
        String time = getIntent().getStringExtra("TIME");

        // Display result
        tvResult.setText(String.format(Locale.getDefault(), "You got %d out of 10 correct in %d seconds.", correctCount, duration));

        btnSave.setOnClickListener(v -> saveResult(correctCount, duration, date, time));
        btnUpload.setOnClickListener(v -> uploadResult(correctCount, duration, date, time));
        btnBackToMain.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void saveResult(int correctCount, int duration, String date, String time) {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Since you didn't enter your name, we'll use \"Anonymous\" instead", Toast.LENGTH_SHORT).show();
            name = "Anonymous";
        }

        long resultId = dbHelper.addGameResult(date, time, duration, correctCount, name);
        if (resultId != -1) {
            Toast.makeText(this, "Game result saved successfully", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(false);
        } else {
            Toast.makeText(this, "Error saving game result", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadResult(int correctCount, int duration, String date, String time) {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Since you didn't enter your name, we'll use \"Anonymous\" instead", Toast.LENGTH_SHORT).show();
            name = "Anonymous";
        }

        // TODO: Implement the API call to upload the result to the server
        // post it to the fast api server
        // local server is at http://localhost:8999/sync_game_results

        long resultId = dbHelper.addGameResult(date, time, duration, correctCount, name);
        if (resultId == -1) {
            Toast.makeText(this, "Error saving game result", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Result uploaded successfully", Toast.LENGTH_SHORT).show();
        btnUpload.setEnabled(false);
    }
}
