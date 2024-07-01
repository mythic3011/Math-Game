package com.mythic3011.itp4501_assignment;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private TextView tvQuestion, tvTimer, tvResult;
    private EditText etAnswer;
    private Button btnDone, btnNext;

    private int currentQuestion = 0;
    private int correctCount = 0;
    private long startTime;
    private Handler timerHandler = new Handler();
    private Random random = new Random();

    // Database
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        dbHelper = new DatabaseHelper(this);

        // Set up Game
        tvQuestion = findViewById(R.id.tvQuestion);
        tvTimer = findViewById(R.id.tvTimer);
        tvResult = findViewById(R.id.tvResult);
        etAnswer = findViewById(R.id.etAnswer);
        btnDone = findViewById(R.id.btnDone);
        btnNext = findViewById(R.id.btnNext);

        btnDone.setOnClickListener(v -> checkAnswer());
        btnNext.setOnClickListener(v -> nextQuestion());

        startGame();
    }

    private void startGame() {
        currentQuestion = 0;
        correctCount = 0;
        startTime = System.currentTimeMillis();
        nextQuestion();
        startTimer();
    }

    private void startTimer() {
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                tvTimer.setText("Time: " + seconds + " seconds");
                timerHandler.postDelayed(this, 1000);
            }
        }, 0);
    }

    private void nextQuestion() {
        if (currentQuestion < 10) {
            int operand1 = random.nextInt(100) + 1;
            int operand2 = random.nextInt(100) + 1;
            String[] operators = {"+", "-", "*", "/"};
            String operator = operators[random.nextInt(operators.length)];

            // Ensure division results in an integer and subtraction results in a non-negative number
            if (operator.equals("/")) {
                operand1 = (operand1 / operand2) * operand2;
            } else if (operator.equals("-") && operand2 > operand1) {
                int temp = operand1;
                operand1 = operand2;
                operand2 = temp;
            }

            tvQuestion.setText(operand1 + " " + operator + " " + operand2 + " = ?");
            etAnswer.setText("");
            btnDone.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.GONE);
            tvResult.setVisibility(View.GONE);
            currentQuestion++;
        } else {
            endGame();
        }
    }

    private void checkAnswer() {
        String questionText = tvQuestion.getText().toString();
        String[] parts = questionText.split(" ");
        int operand1 = Integer.parseInt(parts[0]);
        int operand2 = Integer.parseInt(parts[2]);
        String operator = parts[1];

        int correctAnswer = 0;
        switch (operator) {
            case "+":
                correctAnswer = operand1 + operand2;
                break;
            case "-":
                correctAnswer = operand1 - operand2;
                break;
            case "*":
                correctAnswer = operand1 * operand2;
                break;
            case "/":
                correctAnswer = operand1 / operand2;
                break;
        }

        boolean isCorrect;
        try{
            int userAnswer = Integer.parseInt(etAnswer.getText().toString());
            isCorrect = userAnswer == correctAnswer;
        }catch(NumberFormatException e){
            isCorrect = false;
        }


        if (isCorrect) {
            correctCount++;
            tvResult.setText("Correct!");
        } else {
            tvResult.setText("Incorrect. The correct answer is " + correctAnswer);
        }

        tvResult.setVisibility(View.VISIBLE);
        btnDone.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
    }

    // isInvalidInvalid
    public boolean isInvalidInvalid(String input){
        try{
            Integer.parseInt(input);
            return false;
        }catch(NumberFormatException e){
            return true;
        }
    }

    private void endGame() {
        long endTime = System.currentTimeMillis();
        int duration = (int) ((endTime - startTime) / 1000);

        tvQuestion.setText("Game Over!");
        tvResult.setText("You got " + correctCount + " out of 10 correct in " + duration + " seconds.");
        tvResult.setVisibility(View.VISIBLE);
        btnNext.setText("Continue");
        btnNext.setOnClickListener(v -> finish()); // Return to main menu

        // TODO: Save game result to local database
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        dbHelper.addGameResult(currentDate, currentTime, duration, correctCount);
    }

//    private void syncWithRemoteDatabase() {
//        // TODO: Sync with remote database
//        List<GameResult> unsyncedResults = dbHelper.getUnsyncedGameResults();
//        if (!unsyncedResults.isEmpty()) {
//            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
//            Call<Void> call = apiService.syncGameResults(unsyncedResults);
//            call.enqueue(new Callback<Void>() {
//                @Override
//                public void onResponse(Call<Void> call, Response<Void> response) {
//                    if (response.isSuccessful()) {
//                        for (GameResult result : unsyncedResults) {
//                            dbHelper.markAsSynced(result.getId());
//                        }
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<Void> call, Throwable t) {
//                    // Handle failure
//                }
//            });
//        }
//    }

}
