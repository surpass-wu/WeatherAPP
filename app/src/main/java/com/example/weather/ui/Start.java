package com.example.weather.ui;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weather.R;
import com.example.weather.logic.model.GPSUtils;
import com.example.weather.ui.placesearch.PlaceSearchActivity;

public class Start extends AppCompatActivity {
    int SPLASH_TIME = 5000;
    int DELAY_TIME = 500;
    int WAIT_TIME = 2000;
    ImageView logoImageView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        logoImageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);

        ImageView imageView = findViewById(R.id.imageView2);

        logoImageView.setVisibility(ImageView.INVISIBLE);
        textView.setVisibility(TextView.INVISIBLE);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(imageView, "alpha", 0f, 1f);
        alphaAnimator.setDuration(SPLASH_TIME);

        alphaAnimator.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showOtherComponents();
            }
        }, SPLASH_TIME);
    }

    private void showOtherComponents() {
        logoImageView.setVisibility(ImageView.VISIBLE);
        textView.setVisibility(TextView.VISIBLE);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(logoImageView, "alpha", 0f, 1f);
        alphaAnimator.setDuration(DELAY_TIME);
        alphaAnimator.start();
        ObjectAnimator alphaAnimator1 = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
        alphaAnimator1.setDuration(DELAY_TIME);
        alphaAnimator1.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mySuperIntent;
                mySuperIntent = new Intent(Start.this, Gold.class);
                startActivity(mySuperIntent);
                finish();
            }
        }, WAIT_TIME);
    }
}