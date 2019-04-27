package com.amsavarthan.hify.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.account.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;

public class Splash extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                if(FirebaseAuth.getInstance().getCurrentUser()==null){
                    finish();
                    startActivity(new Intent(Splash.this, LoginActivity.class));
                }else{
                    finish();
                    startActivity(new Intent(Splash.this, MainActivity.class));
                }

            }
        },1200);

    }
}
