package com.mythic3011.itp4501_assignment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
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

public class GameActivity extends AppCompatActivity {

    private TextView tvQuestionNumber, tvQuestion, tvTimer, tvResult, tvScore;
    private EditText etAnswer;
    private Button btnDone;
    private Button btnNext;
    private TextInputLayout tilAnswer;

    private int currentQuestion = 0;
    private int correctCount = 0;
    private long startTime;
    private Handler timerHandler = new Handler();
    private Random random = new Random();

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        try {
            dbHelper = new DatabaseHelper(this);

            tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
            tvQuestion = findViewById(R.id.tvQuestion);
            tvTimer = findViewById(R.id.tvTimer);
            tvResult = findViewById(R.id.tvResult);
            tvScore = findViewById(R.id.tvScore);
            etAnswer = findViewById(R.id.etAnswer);
            btnDone = findViewById(R.id.btnDone);
            btnNext = findViewById(R.id.btnNext);
            tilAnswer = findViewById(R.id.tilAnswer);
            btnDone.setOnClickListener(v -> checkAnswer());
            btnNext.setOnClickListener(v -> animateToNextQuestion());

            startGame();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing game: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startGame() {
        currentQuestion = 0;
        correctCount = 0;
        startTime = System.currentTimeMillis();
        nextQuestion();
        startTimer();
        updateScore();
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

    private void pulseTimerAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvTimer, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvTimer, "scaleY", 1f, 1.2f, 1f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(1000);
        animatorSet.start();
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

    private void nextQuestion() {
        try {
            if (currentQuestion < 10) {
                int operand1 = random.nextInt(100) + 1;
                int operand2 = random.nextInt(100) + 1;
                String[] operators = {"+", "-", "*", "/"};
                String operator = operators[random.nextInt(operators.length)];

                if (operator.equals("/")) {
                    operand1 = (operand1 / operand2) * operand2;
                } else if (operator.equals("-") && operand2 > operand1) {
                    int temp = operand1;
                    operand1 = operand2;
                    operand2 = temp;
                }

                tvQuestionNumber.setText(String.format(Locale.getDefault(), "Question %d/10", currentQuestion + 1));
                tvQuestion.setText(String.format(Locale.getDefault(), "%d %s %d = ?", operand1, operator, operand2));
                etAnswer.setText("");
                btnDone.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.GONE);
                tvResult.setVisibility(View.GONE);
                currentQuestion++;
                animateQuestionElements();
            } else {
                endGame();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error generating question: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAnswer() {
        try {
            String userInput = etAnswer.getText().toString().trim();
            if (TextUtils.isEmpty(userInput)) {
                Toast.makeText(this, "Please enter an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isInvalidInput(userInput)) {
                Toast.makeText(this, "Invalid input. Please enter a number.", Toast.LENGTH_SHORT).show();
                return;
            }

            String questionText = tvQuestion.getText().toString();
            String[] parts = questionText.split(" ");
            int operand1 = Integer.parseInt(parts[0]);
            int operand2 = Integer.parseInt(parts[2]);
            String operator = parts[1];

            int correctAnswer = calculateAnswer(operand1, operand2, operator);
            int userAnswer = Integer.parseInt(userInput);

            boolean isCorrect = userAnswer == correctAnswer;

            if (isCorrect) {
                correctCount++;
                showResultWithAnimation("Correct!");
            } else {
                showResultWithAnimation(String.format(Locale.getDefault(), "Incorrect. The correct answer is %d", correctAnswer));
            }

            btnDone.setVisibility(View.GONE);
            btnNext.setVisibility(View.VISIBLE);
            updateScore();
        } catch (Exception e) {
            Toast.makeText(this, "Error checking answer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int calculateAnswer(int operand1, int operand2, String operator) {
        switch (operator) {
            case "+": return operand1 + operand2;
            case "-": return operand1 - operand2;
            case "*": return operand1 * operand2;
            case "/": return operand1 / operand2;
            default: throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }

    private boolean isInvalidInput(String input) {
        try {
            Integer.parseInt(input);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private void updateScore() {
        tvScore.setText(String.format(Locale.getDefault(), "Score: %d/10", correctCount));
    }

    private void endGame() {
        try {
            // Stop the timer
            timerHandler.removeCallbacksAndMessages(null);

            long endTime = System.currentTimeMillis();
            int duration = (int) ((endTime - startTime) / 1000);

            // Create an intent to start the ResultActivity
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("CORRECT_COUNT", correctCount);
            intent.putExtra("DURATION", duration);
            intent.putExtra("DATE", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            intent.putExtra("TIME", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));

            // Start the ResultActivity
            startActivity(intent);
            finish(); // Close the GameActivity
        } catch (Exception e) {
            Toast.makeText(this, "Error ending game: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacksAndMessages(null);
    }
}
