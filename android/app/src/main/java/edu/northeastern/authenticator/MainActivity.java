package edu.northeastern.authenticator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.firebase.auth.FirebaseAuth;

/**
 * This is the main point of entry for the application.
 * It should check if the user has already completed the application setup, and if so, it will
 * redirect the user to the PinEntryActivity, where the user can initiate the authentication process for their web application login attempt.
 * If the user has not completed the mobile authentication apps setup, it will redirect the user to the LoginActivity,
 * where they can enter their standard login information, and subsequently be redirected to provide their backup codes,
 * then their biometric information, and finally their PIN.
 * @author James Bebarski
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handleAppFlow();
    }

    /**
     * This is a helper method to help the application determine if you need to be redirected to the PinEntryActivity or the LoginActivity.
     * It will also check to see if a user is actually connected to the internet or if they are connected to a cellular network first.
     * <p>
     * Note: It was funny, during the recent outages when I had no internet I stumbled into the connectivity check issue.
     *       I was certain that I was entering the correct PIN, and that I have attempted a login on the web application.
     *       However, I was just getting my generic error message at the time, and had no idea why.
     *       Simply enough, I was just not connected to the internet, and I didn't think to check that first.
     */
    private void handleAppFlow() {
        // Check if the user is connected to the internet or cellular network. If not, redirect them to the NoConnectionActivity
        if (checkForConnectivity()) { Log.e("MainActivity", "No network connection"); goToNoConnectionActivity(); return; }

        // Check if the current user is authenticated
        if (FirebaseAuth.getInstance().getCurrentUser() != null) { Log.d("MainActivity", "User found and authenticated");

            // if the user has already completed the setup, redirect them to the PinEntryActivity
            if (isSetupComplete()) { goToPinEntryActivity(); }

            // if the user has not completed the authenticator setup, redirect them to the LoginActivity
            else { goToLoginActivity(); }

        } else { goToLoginActivity(); Log.e("MainActivity", "User was not found or not authenticated"); }
    }

    /**
     * Check if the user has already completed the application setup.
     * This is done by checking if the user has already set up their PIN.
     *
     * @return - true if the user has already completed the setup, false otherwise
     */
    private boolean isSetupComplete() {
        try {
            // Check if the EncryptedSharedPreferences contains the "pin" key, will return true if it does, false otherwise
            Log.d("MainActivity", "Checking if setup is complete");
            return EncryptedSharedPreferences.create(
                    "secure_preferences",
                    MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM).contains("pin");

        } catch (Exception e) { Log.e("MainActivity", "Error checking setup completion", e);
            return false;
        }
    }

    /**
     * Redirect the user to the PinEntryActivity.
     * This is the activity where the user will enter their PIN to authenticate themselves.
     */
    private void goToPinEntryActivity() { Intent intent = new Intent(this, AuthenticationActivity.class); startActivity(intent); finish(); }

    /**
     * Redirect the user to the LoginActivity.
     * This is the activity where the user will enter their standard login information.
     */
    private void goToLoginActivity() { Intent intent = new Intent(this, LoginActivity.class); startActivity(intent); finish(); }

    /**
     * Redirect the user to the NoConnectionActivity.
     * This is the activity where the user will be informed that they are not connected to the internet or cellular network.
     * It's a work in progress, currently I'm using deprecated methods to check for connectivity,
     * I'm not entirely sure it functions correctly on devices lower thant API 29.
     */
    private void goToNoConnectionActivity() { Intent intent = new Intent(this, NoConnectionActivity.class); startActivity(intent); finish(); }

    /**
     * Check if the user is connected to the internet or cellular network.
     * This is done by checking the network information.
     * This method is using some deprecated methods,
     * and I'm still unsure if it works correctly on devices lower than API 29.
     *
     * @return - true if the user is not connected, false otherwise
     */
    private boolean checkForConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo == null || !networkInfo.isConnected();
    }

    // Lifecycle methods
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MainActivity", "onStart");
        if (checkForConnectivity()) {
            Log.e("MainActivity", "No network connection");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume");
        if (checkForConnectivity()) {
            Log.e("MainActivity", "No network connection");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause");

        // Save the current state of the application
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isSetupComplete", isSetupComplete());
        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity", "onStop");

        // Save the current state of the application
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isSetupComplete", isSetupComplete());
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "onDestroy");
    }

    // save the current state of the application
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); outState.putBoolean("isSetupComplete", isSetupComplete()); }

    // restore the current state of the application
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean isSetupComplete = savedInstanceState.getBoolean("isSetupComplete");
        Log.d("MainActivity", "Is setup complete? " + isSetupComplete);
    }

}