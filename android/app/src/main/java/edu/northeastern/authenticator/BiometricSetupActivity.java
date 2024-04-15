package edu.northeastern.authenticator;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

/**
 * This activity represents the third phase of this authenticator applications set up,
 * the user must provide their biometric information to authenticate themselves in the future.
 * the fingerprint and the pin will be used to authenticate the user in the future by default.
 * @author James Bebarski
 */
public class BiometricSetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometric_setup);
        initializeBiometricManager();

    }

    /**
     * Initialize the BiometricManager to check if the device supports biometric authentication.
     * There are a number of cases that the biometric manager requires to check if the device supports biometric authentication.
     * See the android docs for more details:
     * <a href="https://developer.android.com/reference/android/hardware/biometrics/BiometricManager">...</a>
     */
    private void initializeBiometricManager() {
        // Check if the device supports biometric authentication
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                promptForFingerprintEnrollment(); break; // will prompt the user to enroll their fingerprint, for future authentication
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                showToast("No biometric features available on this device."); break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                showToast("Biometric features are currently unavailable."); break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                showToast("Please enroll your fingerprint.");
                startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS)); break;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                showToast("A security update is required for biometric authentication."); break;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                showToast("Biometric authentication is not supported."); break;
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                showToast("The biometric status is unknown."); break;
        }
    }

    /**
     * Helper method to just keep the biometric setup activity a bit more clean.
     * @param toastMessage - The message to display in the toast.
     */
    private void showToast(String toastMessage) { Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show(); }

    /**
     * Prompt the user to enroll their fingerprint.
     *
     */
    private void promptForFingerprintEnrollment() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Register your fingerprint")
                .setSubtitle("You will use this to authenticate in the future")
                .setNegativeButtonText("Cancel")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(BiometricSetupActivity.this,
                ContextCompat.getMainExecutor(this), new BiometricPrompt.AuthenticationCallback() {

            /**
             * Part of the BiometricPrompt.AuthenticationCallback class.
             * This method is called when some error occurs during the authentication process.
             *
             * @param errorCode An integer ID associated with the error.
             * @param errString A human-readable string that describes the error.
             */
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                showToast("Authentication error: " + errString);
            }

            /**
             * Part of the BiometricPrompt.AuthenticationCallback class.
             * This method is called when the user had successfully authenticated.
             * @param result An object containing authentication-related data.
             */
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                showToast("Authentication succeeded!");
                Log.d("BiometricSetupActivity", "Biometric setup successful. Proceeding to PinSetupActivity.");
                Intent intent = new Intent(BiometricSetupActivity.this, PinSetupActivity.class);
                startActivity(intent);
            }

            /**
             * Part of the BiometricPrompt.AuthenticationCallback class.
             * This method is called when the user failed to authenticate.
             * Potentially an invalid fingerprint or other problem with the biometric sensor?
             */
            @Override
            public void onAuthenticationFailed() { super.onAuthenticationFailed();
                showToast("Authentication failed");
            }
        });
        biometricPrompt.authenticate(promptInfo);
    }

    // Lifecycle methods
    @Override
    protected void onStart() { super.onStart(); Log.d("BiometricSetupActivity", "onStart"); }

    @Override
    protected void onStop() { super.onStop(); Log.d("BiometricSetupActivity", "onStop"); }

    @Override
    protected void onResume() { super.onResume(); Log.d("BiometricSetupActivity", "onResume"); }

    @Override
    protected void onPause() { super.onPause(); Log.d("BiometricSetupActivity", "onPause"); }

    @Override
    protected void onDestroy() { super.onDestroy(); Log.d("BiometricSetupActivity", "onDestroy"); }

    // Save the current state of the application
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); Log.d("BiometricSetupActivity", "onSaveInstanceState"); }

    // Restore the current state of the application
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) { super.onRestoreInstanceState(savedInstanceState); Log.d("BiometricSetupActivity", "onRestoreInstanceState"); }

}