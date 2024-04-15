package edu.northeastern.authenticator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * This activity is just intended to inform the user that they are not connected to the internet or cellular network.
 * If the user is connected to the internet or cellular network, they will be redirected to the MainActivity.
 * This way the user can be informed of the lack of network connection, and then be redirected to the appropriate activity.
 * No sense in finding out after you've taken the time to enter your PIN.
 * @author James Bebarski
 */
public class NoConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_connection);
    }

    /**
     * Broadcast receiver to listen for network changes.
     * If the user is connected to the internet or cellular network, they will be redirected to the MainActivity.
     * I'm still using deprecated CONNECTIVITY_ACTION, and im unsure if this works on versions after API 28.
     */
    private final BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                if (checkForConnectivity(context)) {
                    Log.d("NoConnectionActivity", "Network connection reestablished");
                    goToMainActivity();
                }
            }
        }
    };

    /**
     * Helper method to check if the user is connected to the internet or cellular network
     * @param context - the context of the application, used to get the ConnectivityManager
     * @return true if the user is connected to the internet or cellular network, false otherwise
     */
    private boolean checkForConnectivity(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Helper method to redirect the user to the MainActivity
     * This will be called if the user is connected to the internet or cellular network
     */
    private void goToMainActivity() { startActivity(new Intent(NoConnectionActivity.this, MainActivity.class)); finish(); }

    // Lifecycle methods
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("NoConnectionActivity", "onStart");
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("NoConnectionActivity", "onStop");
        unregisterReceiver(networkChangeReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("NoConnectionActivity", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("NoConnectionActivity", "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("NoConnectionActivity", "onDestroy");
    }

    // Save the current state of the application
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("NoConnectionActivity", "onSaveInstanceState");
    }

    // Restore the current state of the application
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("NoConnectionActivity", "onRestoreInstanceState");
    }

}
