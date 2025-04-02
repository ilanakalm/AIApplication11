package com.example.aiapplication1;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.ai.client.generativeai.Chat;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.ImagePart;
import com.google.ai.client.generativeai.type.Part;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.TextPart;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.util.concurrent.FutureCallback;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class GeminiManager {//    private static  String SYSTEM_PROMPT = "אתה משמש כמאסטר המשחק (או קריין/ית הסיפור) עבור משחק טקסטואלי. סוג המשחק הוא אינטראקטיב בדיוני / בחר את ההרפתקה שלך / משחק בלשות / תעלומה. המשתמש ישחק את התפקיד של המשתתף.\n" +
//            "\n" +
//            "תפקידך הוא להציג את העולם, הדמויות, האתגרים וההשלכות של פעולות המשתמש באמצעות טקסט. כאשר מתאים, הצע למשתמש אפשרויות בחירה ברורות שיקדמו את הסיפור או יאפשרו לו/לה לבצע פעולות שונות.\n" +
//            "\n" +
//            "הגב לפעולות המשתמש בצורה תיאורית ומרתקת, תוך שמירה על עקביות הסיפור והעולם שבנית. זכור את הבחירות והפעולות הקודמות של המשתמש כדי להבטיח המשכיות. צור חוויה אינטראקטיבית ומהנה עבור המשתמש.";

    //    private static  String SYSTEM_PROMPT = "אתה משמש כמאסטר המשחק (או קריין/ית הסיפור) עבור משחק טקסטואלי. סוג המשחק הוא אינטראקטיב בדיוני / בחר את ההרפתקה שלך / משחק בלשות / תעלומה. המשתמש ישחק את התפקיד של המשתתף.\n" +
//            "\n" +
//            "תפקידך הוא להציג את העולם, הדמויות, האתגרים וההשלכות של פעולות המשתמש באמצעות טקסט. כאשר מתאים, הצע למשתמש אפשרויות בחירה ברורות שיקדמו את הסיפור או יאפשרו לו/לה לבצע פעולות שונות.\n" +
//            "\n" +
//            "הגב לפעולות המשתמש בצורה תיאורית ומרתקת, תוך שמירה על עקביות הסיפור והעולם שבנית. זכור את הבחירות והפעולות הקודמות של המשתמש כדי להבטיח המשכיות. צור חוויה אינטראקטיבית ומהנה עבור המשתמש.";

    private static GeminiManager geminiManagerInstance;
    private GenerativeModel gemini;
    private Chat chat;

    public GeminiManager(){
        //List<Part> parts= new ArrayList();
        //parts.add(new TextPart(SYSTEM_PROMPT));
        gemini = new GenerativeModel(
                /* model name */ "gemini-2.0-flash",
                /* apiKey */"AIzaSyA1AgNYOgBRJpyviy9LhjBMNQTqGZk8Jwg",
                /* generation config */ null,
                /* safety settings */ null,
                /* request options - HTTP params such timeout, TTL,... */ new RequestOptions(),
                /* tools - private functions that I can call */ null,
                /* tools config - private functions config */ null
                //   /* content */ new Content(parts)
        );
        startChat();
    }

    //singleton:יצירת מופע יחיד של האובייקט
    public static GeminiManager getInstance(){
        if (geminiManagerInstance == null){
            geminiManagerInstance = new GeminiManager();
        }
        return geminiManagerInstance;
    }

    // שליחת פרומפט וקבלת תגובה מגמיני
    public void sendMessage(String prompt, FutureCallback<String> callback) {
        gemini.generateContent(prompt, new Continuation<GenerateContentResponse>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NonNull Object result) {

                if (result instanceof Result.Failure) {
                    callback.onFailure(((Result.Failure) result).exception);
                } else {
                    callback.onSuccess(((GenerateContentResponse) result).getText());
                }

            }
        });
    }


    public void sendMessageWithPhoto(String prompt, Bitmap photo, FutureCallback<String> callback) {
        List<Part> parts= new ArrayList();
        parts.add(new TextPart(prompt));
        parts.add(new ImagePart(photo));
        Content[] content= new Content[1];
        content[0]=new Content(parts);

        gemini.generateContent(content, new Continuation<GenerateContentResponse>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NonNull Object result) {
                if (result instanceof Result.Failure) {
                    callback.onFailure(((Result.Failure) result).exception);
                } else {
                    callback.onSuccess(((GenerateContentResponse) result).getText());
                }
            }
        });
    }


    // בצ'אט שזוכר את תוכן השיחה שימוש
    public void sendChatMessage(String prompt, CoroutineContext coroutineContext, FutureCallback<String> callback) {
        chat.sendMessage(prompt, new Continuation<GenerateContentResponse>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return coroutineContext;
            }

            @Override
            public void resumeWith(@NonNull Object result) {
                if (result instanceof Result.Failure) {
                    callback.onFailure(((Result.Failure) result).exception);
                } else {
                    callback.onSuccess(((GenerateContentResponse) result).getText());
                }

            }
        });
    }

    private void startChat(){
        chat= gemini.startChat(Collections.emptyList());
    }

}