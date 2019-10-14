package edu.illinois.cs.cs125.fall2019.mp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

/**
 * Code for Log In screen.
 */
public class LaunchActivity extends AppCompatActivity {
    /**
       We will be using Google’s Firebase Authentication service to display a login flow
       and manage credentials. We want the user to be sent directly to the main app if
       they’re already logged in, but if not we will start the login process.
     */
    private int rcSignIn = 1;
    /**
     * Here we initialize a log in screen in order for a user to fill out his/her credentials.
     * @param savedInstanceState Last seen screen.
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) { // see below discussion
            // launch MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // start login activity for result - see below discussion
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build());
            // Create and launch sign-in intent

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    rcSignIn);
        }
        Button button = findViewById(R.id.goLogin);
        button.setOnClickListener(v -> {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build());
            // Create and launch sign-in intent

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    rcSignIn);

        });
    }

    /**
     * Determines whether to direct user back to the login screen.
     * @param requestCode code to be passed.
     * @param resultCode code to send out.
     * @param data Intent to be passed.
     */

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == rcSignIn) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Button button = findViewById(R.id.goLogin);
                button.setVisibility(View.VISIBLE);
            }
        }
    }
}
