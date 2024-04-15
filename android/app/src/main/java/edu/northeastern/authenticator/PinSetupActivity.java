package edu.northeastern.authenticator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

/**
 * This activity represents the fourth and final phase of the authenticator application's setup.
 * After the user provides their biometric information, they must enter their PIN to authenticate themselves.
 * Going forward, when the user attempts to login on the web application, they will need to navigate to this authenticator app,
 * enter their PIN, and then authenticate themselves using their biometric information.
 * @author James Bebarski
 */
public class PinSetupActivity extends AppCompatActivity {

    private EditText editPin;
    private EditText editPinConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_setup);

        editPin = findViewById(R.id.et_pin);
        editPinConfirm = findViewById(R.id.et_confirmPin);
        Button btnSetPin = findViewById(R.id.btn_setPin);

        btnSetPin.setOnClickListener(v -> {
            String pin = editPin.getText().toString();
            String pinConfirm = editPinConfirm.getText().toString();

            // Validate the PIN and confirm PIN
            if (validatePin(pin) && pin.equals(pinConfirm)) {
                savePin(pin);
                proceedToMainActivity();
            } else if (!pin.equals(pinConfirm)) {
                // If the PIN and confirm PIN do not match, show a toast message
                Toast.makeText(PinSetupActivity.this, "PIN does not match. Please try again.", Toast.LENGTH_SHORT).show();
            } else {
                // If the PIN is invalid, show a toast message
                Toast.makeText(PinSetupActivity.this, "Invalid PIN. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Method to help validate the PIN,
     * my only requirement is that the PIN for the app should be 6 digits long, and only contain numbers between 0-9.
     * @param pin - the PIN to validate
     * @return true if the PIN is valid, false otherwise
     */
    private boolean validatePin(String pin) {
        return pin.length() == 6 && pin.matches("[0-9]+");
    }

    /**
     * Method to save the PIN securely using EncryptedSharedPreferences,
     * which is the best practice, because if you store the PIN in SharedPreferences without encryption,
     * the PIN can be easily easily accessed by anyone who has access to the device or to malicious apps.
     * <p>
     * Note: I'm not sure why you even need to specify the key encryption scheme,
     * the Android docs seem to only mention AES256_GCM_SPEC as the key spec.
     * <p>
     * For the actual implementation of shared preferences I followed the Android docs implementation:
     * <a href="https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences">...</a>
     * <p>
     * The PrefKeyEncryptionScheme encrypts the keys in the SharedPreferences,
     * and the PrefValueEncryptionScheme encrypts the values themselves.
     * <p>
     * @param pin - the PIN to save
     */
    private void savePin(String pin) {
        try {
            // Generate or retrieve the master key alias
            // I am using AES256_GCM_SPEC for the master key spec
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            // Create the EncryptedSharedPreferences
            // I am using AES256_SIV for the key encryption scheme and AES256_GCM for the value encryption scheme
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    "secure_preferences",
                    masterKeyAlias,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // Save the PIN in EncryptedSharedPreferences
            sharedPreferences.edit().putString("pin", pin).apply();
            Log.d("PinSetupActivity", "PIN saved securely");
            Toast.makeText(this, "PIN saved securely", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("PinSetupActivity", "Failed to save PIN securely", e);
            Toast.makeText(this, "Failed to save PIN securely", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method to proceed to the MainActivity.
     * This will be called after the user has successfully set up their PIN.
     * Essentially they will be redirected there, it will check if they are authenticated or not.
     * Since they should be if this method is called it will redirect them to the PIN entry activity.
     */
    private void proceedToMainActivity() { startActivity(new Intent(PinSetupActivity.this, MainActivity.class)); finish(); }
}
