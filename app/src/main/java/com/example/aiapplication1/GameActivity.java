package com.example.aiapplication1;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import android.widget.*;


import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.util.concurrent.FutureCallback;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private String[] words = new String[]{"Dog", "Elephant", "Dolphin", "Bear"};
    private Random random = new Random();
    private String word;
    private String prompt;
    private EditText etAnswer;
    private Button btnSend, btnRestart;
    private TextView tvDesc,tvChat;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        tvDesc = findViewById(R.id.tvDesc);
        etAnswer = findViewById(R.id.etAnswer);
        btnSend = findViewById(R.id.btnSend);
        tvChat = findViewById(R.id.tvChat);
        btnRestart = findViewById(R.id.btnRestart);
        scrollView = findViewById(R.id.scrollView);

        btnSend.setOnClickListener(this::onSend);
        btnRestart.setOnClickListener(this::onRestart);

        newWord();

    }

    private void onRestart(View view) {
        newWord();
        btnSend.setVisibility(TextView.VISIBLE);
    }

    private void onSend(View view) {
        String answer = etAnswer.getText().toString();
        if (answer.toLowerCase().equals(word)) {
            tvChat.append("\nYou guessed right!\n");
            btnSend.setVisibility(TextView.INVISIBLE);
        }
        else {
            tvChat.append("\n" + answer + " is the wrong answer! Try again...\n\n");
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        }
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        etAnswer.setText("");
    }

    private void newWord() {
        tvChat.setText("");
        word = words[random.nextInt(words.length)].toLowerCase();
        prompt="Given a target word (" + word + "), return a list of 4-6 words that identify the target word easily. Separate the words with a comma.";
        // הודעה לגמיני ויוצר רק מופע אחד עמ שלא יקרה מצב של יצירת 2 פניות שונות ל 2 גמיני שונים או יותרשולח
        GeminiManager.getInstance().sendMessage(prompt, new FutureCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d("MYLOG", "newWord: response is: " + response);
                runOnUiThread(() -> onDescReady(response));//גמיני רץ על תהליכון נפרד
            }

            @Override
            public void onFailure(Throwable throwable) {
                runOnUiThread(() -> tvDesc.setText("error: " + throwable.getMessage()));
            }

        });
    }

    private void onDescReady(String response) {
        //String[] avoidies = response.split(",");
        prompt="Please describe in only one short sentance the word "+word+".\n" +
                " You cannot mention the word "+word+" or any of the additional forbidden words.\n"+
                " The forbidden words are: " + response;
        GeminiManager.getInstance().sendMessage(prompt, new FutureCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d("MYLOG", "onDescReady: response is: " + response);
                runOnUiThread(() -> tvDesc.setText(response));
            }

            @Override
            public void onFailure(Throwable throwable) {
                runOnUiThread(() -> tvDesc.setText("error: " + throwable.getMessage()));
            }

        });
    }
}