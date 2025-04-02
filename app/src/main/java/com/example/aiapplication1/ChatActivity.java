package com.example.aiapplication1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.util.concurrent.FutureCallback;

import java.util.Collections;

import kotlin.coroutines.EmptyCoroutineContext;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnSend;
    EditText etQuestion;
    TextView tvAnswer;
    GeminiManager geminiManager;
    private String prompt;
    ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);
        btnSend = findViewById(R.id.btnSend);

        etQuestion = findViewById(R.id.etQuestion);
        tvAnswer = findViewById(R.id.tvAnswer);
        scrollView = findViewById(R.id.scrollView);

        String question = etQuestion.getText().toString();
        geminiManager = GeminiManager.getInstance();
        btnSend.setOnClickListener(this);



    }




    @Override
    public void onClick(View view) {
        String message = etQuestion.getText().toString();
        etQuestion.setText("");
        geminiMessage(message);
        // סגירת המקלדת
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etQuestion.getWindowToken(), 0);
    }


    // פעולת שליחת הודעות לצ'אט לגמיניזימון
    private void geminiMessage(String message) {
        ProgressDialog pd = new ProgressDialog(ChatActivity.this);
        pd.setTitle("Connecting");
        pd.setMessage("Wait to Gemini...");
        pd.show();
        String prompt = message.trim();

        GeminiManager.getInstance().sendChatMessage(
                prompt,
                EmptyCoroutineContext.INSTANCE, new FutureCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        pd.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displayChatMessages(tvAnswer, message, s);
                            }
                        });
                    }
                    @Override
                    public void onFailure(Throwable throwable) {
                        tvAnswer.append("\n" + "Error: " + throwable.getMessage() + "\n");
                    }
                });

    }
    //הצ'אט בצבע שונה באותו textviewפעולה אשר מציגה את טקסט
    public void displayChatMessages(TextView textView, String userMessage, String botMessage) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        // הודעה מהמשתמש (צבע סגול)
        builder.append("\n"+userMessage);
        builder.setSpan(new ForegroundColorSpan(Color.parseColor("#683fb5")), 0, userMessage.length(), 0);
        builder.append("\n"); // מעבר שורה

        // הודעה מהבוט (צבע ירוק)
        builder.append("\n"+botMessage);
        builder.setSpan(new ForegroundColorSpan(Color.parseColor("#315041")), builder.length() - botMessage.length(), builder.length(), 0);
        builder.append("\n"); // מעבר שורה

        textView.append(builder);
        // גלילה אוטומטית לתחתית
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}