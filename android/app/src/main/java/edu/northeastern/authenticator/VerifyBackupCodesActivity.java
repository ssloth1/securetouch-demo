package edu.northeastern.authenticator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This activity represents the second phase of this authenticator applications set up,
 * the user must provide their backup codes that they were told to record when they first set up their account.
 * @author James Bebarski
 */
public class VerifyBackupCodesActivity extends AppCompatActivity {

    private final EditText[] backupCodeEditTexts = new EditText[12];
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_backup_codes);

        // Check if user is actually authenticated
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            Log.e("VerifyBackupCodesActivity", "User was not found or not authenticated");
            return;
        }

        initializeEditTextFields();
        Button verifyCodesButton = findViewById(R.id.proceedToBioButton);
        verifyCodesButton.setOnClickListener(v -> verifyBackupCodes());
    }

    private void initializeEditTextFields() {
        backupCodeEditTexts[0] = findViewById(R.id.backupCode1);
        backupCodeEditTexts[1] = findViewById(R.id.backupCode2);
        backupCodeEditTexts[2] = findViewById(R.id.backupCode3);
        backupCodeEditTexts[3] = findViewById(R.id.backupCode4);
        backupCodeEditTexts[4] = findViewById(R.id.backupCode5);
        backupCodeEditTexts[5] = findViewById(R.id.backupCode6);
        backupCodeEditTexts[6] = findViewById(R.id.backupCode7);
        backupCodeEditTexts[7] = findViewById(R.id.backupCode8);
        backupCodeEditTexts[8] = findViewById(R.id.backupCode9);
        backupCodeEditTexts[9] = findViewById(R.id.backupCode10);
        backupCodeEditTexts[10] = findViewById(R.id.backupCode11);
        backupCodeEditTexts[11] = findViewById(R.id.backupCode12);
    }

    /**
     * Takes the word and salt and hashes them using SHA-256
     * I learned about message digests in Java and how they wrok with hashing algorithms from the following links:
     * <a href="https://www.geeksforgeeks.org/message-digest-in-information-security/">...</a>
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html">...</a>
     *
     * @param word - the word to hash
     * @param salt - the salt to hash the word with
     * @return the word + salt hashed with SHA-256
     */
    private String hashWordWithSalt(String word, String salt) {
       try {
           // Get the SHA-256 message digest instance, from the Java Security API
           MessageDigest md = MessageDigest.getInstance("SHA-256");
           return bytesToHex(md.digest((word + salt).getBytes(StandardCharsets.UTF_8)));
       } catch (NoSuchAlgorithmException e) {
                // The java security API does have a SHA-256 algo, so something would need to be very wrong for this to happen, I think.
              Log.e("VerifyBackupCodesActivity", "Error hashing word with salt", e);
              return null;
       }
    }

    /**
     * Converts the byte array to a hexadecimal string
     * This is necessary for the hashWordWithSalt method, as it returns a byte array
     * The byte array is converted to a hexadecimal string for comparison, as the stored backup codes are in hexadecimal format
     *
     * @param hash - the byte array to convert
     * @return the hexadecimal string
     */
    private static String bytesToHex(byte[] hash) {
        Log.d("VerifyBackupCodesActivity", "Hash is " + Arrays.toString(hash) + " with length " + hash.length);
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte thisByte : hash) {
            Log.d("VerifyBackupCodesActivity", "Byte: " + thisByte);
            // Convert each byte to a hexadecimal string, and append it to the StringBuilder
            String hex = Integer.toHexString(0xff & thisByte);
            if (hex.length() == 1) {
                // The java lang Integer API states that "This value is converted to a string of ASCII digits in hexadecimal (base 16) with no extra leading 0s
                hexString.append('0');
            }
            // Append the hex string to the StringBuilder
            hexString.append(hex);
            Log.d("VerifyBackupCodesActivity", "Hex string: " + hexString);
        }
        return hexString.toString();
    }

    /**
     * Verifies each of the backup codes entered by the user, against the stored backup codes and salts stored in firestore.
     * If the user has successfully verified all of their backup codes, they will be taken to the BiometricSetupActivity.
     * Otherwise, they will be prompted to try again.
     * <p>
     * In here I utilize my method, hashWordWithSalt, to hash the entered backup code with the stored salt.
     * I then compare the hashed entered code with the stored hash, if they match, the verification is successful.
     */
    private void verifyBackupCodes() {
        // Fetch the user's backup codes from the database
        firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {

            // Check if the user exists, and if they do, fetch their backup codes
            if (documentSnapshot.exists()) {

                // Check if the user actually has backup codes
                if (documentSnapshot.get("backupCodes") == null) {
                    Toast.makeText(VerifyBackupCodesActivity.this, "Your backup codes were not found.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Get the backup codes from firestore
                Object backupCodesObj = documentSnapshot.get("backupCodes");

                // just making sure that the backupCodesObj is a list of maps
                if (backupCodesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, String>> storedCodesWithSalts = (List<Map<String, String>>) backupCodesObj;

                    // Check if the user has 12 backup codes, as expected
                    if (storedCodesWithSalts.size() == 12) {
                        boolean allCodesVerified = true;

                        // Verify each backup code, one by one. If any code fails, the verification fails
                        for (int i = 0; i < backupCodeEditTexts.length; i++) {
                            String enteredCode = backupCodeEditTexts[i].getText().toString().trim();
                            Map<String, String> storedCodeWithSalt = storedCodesWithSalts.get(i);

                            // Get the stored hash and salt for the current backup code
                            String storedHash = storedCodeWithSalt.get("hash");
                            String salt = storedCodeWithSalt.get("salt");

                            // Hash the entered code with the stored salt, see my hashWordWithSalt method for more context.
                            String hashedEnteredCode = hashWordWithSalt(enteredCode, salt);

                            // If the hashed entered code does not match the stored hash, the verification fails
                            if (hashedEnteredCode == null || !hashedEnteredCode.equals(storedHash)) {
                                allCodesVerified = false;
                                Log.d("VerifyBackupCodesActivity", "Something made an oopsie at " + (i + 1));
                                break;
                            }
                        }
                        if (allCodesVerified) {
                            // Proceed to biometric setup, as backup codes were verified
                            proceedToBiometricSetup();
                        } else {
                            // Verification failed
                            Toast.makeText(VerifyBackupCodesActivity.this, "Verification failed. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Backup codes were not found
                        Toast.makeText(VerifyBackupCodesActivity.this, "Your backup codes were not found.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Invalid format for backup codes
                    Log.e("VerifyBackupCodesActivity", "Invalid format for backup codes");
                }
            } else {
                // User not found
                Toast.makeText(VerifyBackupCodesActivity.this, "User not found", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            // Error fetching user backup codes
            Log.e("VerifyBackupCodesActivity", "Error fetching user backup codes", e);
        });
    }

    /**
     * Proceed to the BiometricSetupActivity, where they will provide their fingerprint information, for future authentication
     * This method is called when the user has successfully verified their backup codes
     */
    private void proceedToBiometricSetup() { Log.d("VerifyBackupCodes", "Proceeding to BiometricSetupActivity"); startActivity(new Intent(VerifyBackupCodesActivity.this, BiometricSetupActivity.class)); }

    // Lifecycle methods
    @Override
    protected void onStart() { super.onStart();Log.d("VerifyBackupCodes", "onStart"); }

    @Override
    protected void onStop() { super.onStop(); Log.d("VerifyBackupCodes", "onStop"); }

    @Override
    protected void onResume() { super.onResume(); Log.d("VerifyBackupCodes", "onResume"); }

    @Override
    protected void onPause() { super.onPause(); Log.d("VerifyBackupCodes", "onPause"); }

    @Override
    protected void onDestroy() { super.onDestroy(); Log.d("VerifyBackupCodes", "onDestroy"); }

    // Save the current state of the application
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); Log.d("VerifyBackupCodes", "onSaveInstanceState"); }

    // Restore the current state of the application
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) { super.onRestoreInstanceState(savedInstanceState); Log.d("VerifyBackupCodes", "onRestoreInstanceState"); }
}
