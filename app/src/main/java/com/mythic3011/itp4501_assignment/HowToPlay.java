package com.mythic3011.itp4501_assignment;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HowToPlay extends AppCompatActivity {

    private TextView tvInstructions;
    private Button btnStartGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);

        tvInstructions = findViewById(R.id.tvInstructions);
        btnStartGame = findViewById(R.id.btnStartGame);

        setInstructions();

        btnStartGame.setOnClickListener(v -> startGame());
    }

    private void setInstructions() {
        String instructions = "Welcome to the Mathematics Game!\n\n" +
                "Here's how to play:\n\n" +
                "1. The game consists of 10 math questions.\n\n" +
                "2. Each question is randomly generated and includes two operands and one operator.\n\n" +
                "3. Operands are integers between 1 and 100.\n\n" +
                "4. Operators can be addition (+), subtraction (-), multiplication (*), or division (/).\n\n" +
                "5. For division, the result will always be an integer.\n\n" +
                "6. For subtraction, the result will always be zero or positive.\n\n" +
                "7. Enter your answer in the provided text box and tap 'Done'.\n\n" +
                "8. The game will show if your answer is correct or not.\n\n" +
                "9. Tap 'Next' to move to the next question.\n\n" +
                "10. The timer starts when you begin the game and stops when you finish all 10 questions.\n\n" +
                "11. At the end of the game, you'll see your score (number of correct answers) and the time taken.\n\n" +
                "12. Try to answer correctly and quickly to get the best score!\n\n" +
                "Good luck and have fun!";

        tvInstructions.setText(instructions);
    }

    private void startGame() {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
        finish();
    }
}