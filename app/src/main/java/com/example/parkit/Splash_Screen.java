package com.example.parkit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

public class Splash_Screen extends AppCompatActivity {

    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        countDownTimer = new CountDownTimer(3000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {
                Intent intent = new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
                finish();
            }
        }.start();
    }
}
