package com.mythic3011.itp4501_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.DateFormat;
import java.util.Date;

public class ResultActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView tvResultTitle, tvResult;
    private TextInputLayout tilName;
    private EditText etName;
    private Button btnSave, btnUpload, btnBackToMain;
    private DatabaseHelper dbHelper;
    private FirebaseAnalytics mFirebaseAnalytics;
    private int duration;
    private int correctCount;
    private String currentDate;
    private String currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        initViews();
        setupListeners();
        loadResultData();
        animateViews();
    }

    private void initViews() {
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResult = findViewById(R.id.tvResult);
        tilName = findViewById(R.id.tilName);
        etName = findViewById(R.id.etName);
        btnSave = findViewById(R.id.btnSave);
        btnUpload = findViewById(R.id.btnUpload);
        btnBackToMain = findViewById(R.id.btnBackToMain);

        dbHelper = new DatabaseHelper(this);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveResult());
        btnUpload.setOnClickListener(v -> uploadResult());
        btnBackToMain.setOnClickListener(v -> navigateToMain());
    }

    private void loadResultData() {
        Intent intent = getIntent();
        correctCount = intent.getIntExtra("CORRECT_COUNT", 0);
        duration = intent.getIntExtra("DURATION", 0);
        currentDate = intent.getStringExtra("DATE");
        currentTime = intent.getStringExtra("TIME");

        String resultText = getString(R.string.result_format, correctCount, 10, duration);
        tvResult.setText(resultText);
    }

    private void animateViews() {
        fadeInView(tvResultTitle, 0);
        fadeInView(tvResult, 300);
        fadeInView(tilName, 600);
        fadeInView(btnSave, 900);
        fadeInView(btnUpload, 1200);
        fadeInView(btnBackToMain, 1500);
        scaleView(tvResult);
    }

    private void fadeInView(View view, int delay) {
        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void scaleView(View view) {
        view.setScaleX(0.5f);
        view.setScaleY(0.5f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void saveResult() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            tilName.setError(getString(R.string.name_required));
            shakeView(tilName);
            return;
        }

        // Save result to the database
        dbHelper.addGameResult(currentDate, currentTime, duration, correctCount, name);
        navigateToMain();
        showSuccessAnimation();
    }

    private void uploadResult() {
        // TODO: Implement upload result logic here (using Firebase Realtime Database)
        showLoadingAnimation();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void shakeView(View view) {
        view.animate()
                .translationX(20f)
                .setDuration(50)
                .setInterpolator(new CycleInterpolator(4))
                .start();
    }

    private void showSuccessAnimation() {
        ImageView checkmark = new ImageView(this);
        checkmark.setImageResource(R.drawable.ic_checkmark);
        checkmark.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ViewGroup rootView = findViewById(android.R.id.content);
        rootView.addView(checkmark);

        checkmark.setX((rootView.getWidth() - checkmark.getWidth()) / 2f);
        checkmark.setY((rootView.getHeight() - checkmark.getHeight()) / 2f);

        checkmark.setScaleX(0f);
        checkmark.setScaleY(0f);
        checkmark.setAlpha(0f);

        checkmark.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(500)
                .withEndAction(() -> {
                    rootView.removeView(checkmark);
                    Snackbar.make(rootView, R.string.save_success, Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(ContextCompat.getColor(this, R.color.colorPrimary))
                            .setTextColor(ContextCompat.getColor(this, R.color.colorOnPrimary))
                            .show();
                })
                .start();
    }

    private void showLoadingAnimation() {
        if (progressBar == null) {
            tilName.removeView(progressBar);
            progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
            progressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            progressBar.setIndeterminate(true);
        }

        ViewGroup rootView = findViewById(android.R.id.content);
        rootView.addView(progressBar);

        progressBar.setX((rootView.getWidth() - progressBar.getWidth()) / 2f);
        progressBar.setY((rootView.getHeight() - progressBar.getHeight()) / 2f);

        progressBar.setAlpha(0f);
        progressBar.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        new Handler().postDelayed(() -> {
            hideLoadingAnimation();
            Snackbar.make(rootView, R.string.upload_success, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setTextColor(ContextCompat.getColor(this, R.color.colorOnPrimary))
                    .show();
        }, 2000);
    }

    private void hideLoadingAnimation() {
        if (progressBar != null && progressBar.getParent() != null) {
            progressBar.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        ViewGroup rootView = findViewById(android.R.id.content);
                        rootView.removeView(progressBar);
                    })
                    .start();
        }
    }
}