package edu.northeastern.authenticator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Objects;

/**
 * This activity will display a message to the user if they successfully completed the mobile authentication process,
 * so that they can log in to the web application.
 * @author James Bebarski
 */
public class AuthenticationCompleteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication_complete);

        ImageView gifImageView = findViewById(R.id.gifImageView);
        TextView countdownTextView = findViewById(R.id.countdownTextView);

        // loading my stupid gif into the ImageView
        Glide.with(this)
                .load(R.drawable.funny_gif)
                .into(gifImageView);

        // Start a countdown timer from 6 seconds, updating the TextView every second
        new android.os.CountDownTimer(6000, 1000) {

            public void onTick(long millisUntilFinished) {
                countdownTextView.setText("Redirecting in: " + millisUntilFinished / 1000 + " seconds");
            }
            public void onFinish() {
                Intent intent = new Intent(AuthenticationCompleteActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }.start();
    }

    // Lifecycle methods
    // Note To Self: I'm still unsure if quitting the looper is necessary here in onDestroy, onStop, onPause.
    @Override
    protected void onDestroy() { super.onDestroy(); Objects.requireNonNull(Looper.myLooper()).quit(); Log.d("AuthenticationComplete", "onDestroy"); }

    @Override
    protected void onStop() { super.onStop(); Objects.requireNonNull(Looper.myLooper()).quit(); Log.d("AuthenticationComplete", "onStop"); }

    @Override
    protected void onPause() { super.onPause(); Objects.requireNonNull(Looper.myLooper()).quit(); }

    @Override
    protected void onResume() { super.onResume(); Log.d("AuthenticationComplete", "onResume"); }

    @Override
    protected void onStart() { super.onStart(); Log.d("AuthenticationComplete", "onStart"); }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Objects.requireNonNull(Looper.myLooper()).quit();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Objects.requireNonNull(Looper.myLooper()).quit();
    }

}
