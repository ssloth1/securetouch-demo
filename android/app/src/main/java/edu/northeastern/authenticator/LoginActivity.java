package edu.northeastern.authenticator;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * This activity represents the first phase of the authenticator applications set up,
 * they must provide their standard log in information that they would enter on the web application side.
 * This activity will then take the user to the VerifyBackupCodesActivity where they will be able to enter their backup codes.
 * @author James Bebarski
 */
public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.editTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextPassword);
        findViewById(R.id.setupButton).setOnClickListener(v -> attemptSetup());
    }

    /**
     * Method to attempt to log in the user with the provided email and password.
     * If the user is successfully authenticated, they will be taken to the VerifyBackupCodesActivity.
     * If the user is not authenticated, a toast message will be displayed.
     */
    private void attemptSetup() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d("LoginActivity", "signInWithEmail:success, will now navigate to VerifyBackupCodesActivity.");
                            goToVerifyBackupCodesActivity();
                        } else {
                            Log.w("LoginActivity", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(LoginActivity.this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method to take the user to the VerifyBackupCodesActivity.
     */
    private void goToVerifyBackupCodesActivity() { startActivity(new Intent(LoginActivity.this, VerifyBackupCodesActivity.class)); finish(); }

    // Lifecycle methods
    @Override
    protected void onStart() { super.onStart(); Log.d("LoginActivity", "onStart"); }

    @Override
    protected void onStop() { super.onStop(); Log.d("LoginActivity", "onStop"); }

    @Override
    protected void onResume() { super.onResume(); Log.d("LoginActivity", "onResume"); }

    @Override
    protected void onPause() { super.onPause(); Log.d("LoginActivity", "onPause"); }

    @Override
    protected void onDestroy() { super.onDestroy(); Log.d("LoginActivity", "onDestroy"); }

    // Save the current state of the application
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); Log.d("LoginActivity", "onSaveInstanceState"); }

    // Restore the current state of the application
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) { super.onRestoreInstanceState(savedInstanceState); Log.d("LoginActivity", "onRestoreInstanceState"); }
}
