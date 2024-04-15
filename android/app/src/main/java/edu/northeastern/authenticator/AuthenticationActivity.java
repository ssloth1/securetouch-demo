package edu.northeastern.authenticator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * This activity represents the actual authenticator applications main functionality.
 * When a request is made in the web application to login, the user will then need to authenticate themselves using this app.
 * First they will need to give their PIN, then they will need to authenticate themselves using their biometric information.
 * If both of these are successful, the user will then be able to login to the web application.
 * @author James Bebarski
 */
public class AuthenticationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_entry);
        initializeViewComponents();
    }

    /**
     * Method to initialize the EditText and Button fields in the layout.
     * This method also sets an OnClickListener on the submitButton to handle the user's PIN entry.
     */
    private void initializeViewComponents() {
        findViewById(R.id.submitPinButton).setOnClickListener(v -> {
            EditText pinEntryEditText = findViewById(R.id.editTextEnterPin);
            checkForPendingLoginAttempts(pinEntryEditText.getText().toString());
        });
    }

    /**
     * Method to assist in validating the user's PIN.
     * This method retrieves the stored PIN from the EncryptedSharedPreferences and compares it to the entered PIN.
     * If the PIN matches, return true. Otherwise, return false.
     *
     * @param pin - the PIN entered by the user
     * @throws GeneralSecurityException - if an error occurs while creating the master key
     * @throws IOException - if an error occurs with EncryptedSharedPreferences
     */
    private boolean validatePin(String pin) throws GeneralSecurityException, IOException {
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                "secure_preferences",
                masterKeyAlias,
                this,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

        // Compare the entered PIN to the stored PIN, and return the result
        return pin.equals(sharedPreferences.getString("pin", ""));
    }

    /**
     * Method to check for any pending login attempts in Firestore.
     * If there are any pending login attempts, validate the entered PIN.
     * If the PIN is correct, authenticate the user using biometrics.
     * Otherwise, give a generic error message.
     *
     * @param enteredPin - the PIN entered by the user
     */
    private void checkForPendingLoginAttempts(String enteredPin) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        firestore.collection("login_attempts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        try {
                            if (validatePin(enteredPin)) {
                                String sessionId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                authenticateUserWithBiometrics(sessionId);
                            } else {
                                // If the PIN doesn't match, give a generic error message.
                                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                            }
                        } catch (GeneralSecurityException | IOException e) {
                            // If an error occurs while validating the PIN, log the error.
                            Log.e("PinEntryActivity", "Error validating PIN", e);
                        }
                    } else {
                        // No pending login attempts, give a generic message.
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("PinEntryActivity", "Failed to fetch login attempts", e));
    }

    /**
     * Method to handle authenticating the user using biometrics.
     * If the user successfully authenticates themselves, update the status of the login attempt to "authenticated".
     * Otherwise, handle any errors or failures that occur during the biometric authentication process.
     *
     * @param sessionId - the ID of the login attempt document in Firestore
     */
    private void authenticateUserWithBiometrics(String sessionId) {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {

            // If the user successfully authenticates themselves using biometrics, update the login attempt status to "authenticated".
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                updateLoginAttemptStatus(sessionId, "authenticated");
            }

            // Handle any errors that occur during the biometric authentication process.
            // do not want to update the login attempt status in these cases, since the user
            // might want to try authenticating themselves again.
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            // Handle any failures that occur during the biometric authentication process.
            // Similarly to onAuthenticationError, we do not want to update the login attempt status in these cases.
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Prompt the user to authenticate using biometrics,
        //The biometric data is stored in the device.
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Confirm your identity")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Method to handle updating the status of a login attempt in Firestore.
     * If the status is updated to "authenticated", proceed to the next activity.
     * Otherwise, do nothing.
     * @param sessionId - the ID of the login attempt document in Firestore
     * @param status - the new status to update the login attempt to ("authenticated").
     */
    private void updateLoginAttemptStatus(String sessionId, String status) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Query the Firestore database to update the status of the login attempt.
        firestore.collection("login_attempts").document(sessionId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Log.d("PinEntryActivity", "Login attempt status updated to " + status);
                    if ("authenticated".equals(status)) {
                        proceedToNextAuthenticationCompleteActivity();
                    }
                })
                .addOnFailureListener(e -> Log.e("PinEntryActivity", "Error updating login attempt status", e));
    }

    /**
     * Method to proceed to the AuthenticationCompleteActivity.
     * This should only be called if the user has successfully authenticated themselves.
     */
    private void proceedToNextAuthenticationCompleteActivity() {
        Log.d("PinEntryActivity", "Authentication successful. Proceeding to the AuthenticationCompleteActivity.");
        startActivity(new Intent(this, AuthenticationCompleteActivity.class));
    }

    // Lifecycle methods
    @Override
    protected void onStart() { super.onStart(); Log.d("PinEntryActivity", "onStart"); }

    @Override
    protected void onStop() { super.onStop(); Log.d("PinEntryActivity", "onStop"); }

    @Override
    protected void onResume() { super.onResume(); Log.d("PinEntryActivity", "onResume"); }

    @Override
    protected void onPause() { super.onPause(); Log.d("PinEntryActivity", "onPause"); }

    @Override
    protected void onDestroy() { super.onDestroy(); Log.d("PinEntryActivity", "onDestroy"); }

    // save the current state of the application
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); Log.d("PinEntryActivity", "onSaveInstanceState"); }

    // restore the current state of the application
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) { super.onRestoreInstanceState(savedInstanceState); Log.d("PinEntryActivity", "onRestoreInstanceState"); }
}
