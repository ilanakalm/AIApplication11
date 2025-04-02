package com.example.aiapplication1;

public interface GeminiCallback {
    void onError(Throwable exception);
    void onSuccess(String text);
}
