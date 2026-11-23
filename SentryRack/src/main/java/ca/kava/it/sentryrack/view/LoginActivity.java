// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ca.kava.it.sentryrack.R;
import ca.kava.it.sentryrack.controller.LoginController;
import ca.kava.it.sentryrack.model.UserData;

public class LoginActivity extends AppCompatActivity implements LoginController.Callback {
    // CONSTANTS
    public static final String FROM_LOGOUT = "FROM_LOGOUT";
    private static final long LOGO_ANIMATION = 1000;
    private static final long CARD_ANIMATION_DELAY = 150;

    // FIELDS/VIEWS
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private View loginLogo;
    private View loginCard;
    private MaterialButton loginButton;
    private MaterialButton googleButton;
    private MaterialButton signUpButton;

    // Controller: receives validation and authentication events
    private LoginController loginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsFragment.applySavedTheme(this);
        SettingsFragment.applySavedOrientation(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Bind views
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginLogo = findViewById(R.id.loginLogo);
        loginCard = findViewById(R.id.loginCard);
        loginButton = findViewById(R.id.loginButton);
        googleButton = findViewById(R.id.googleButton);
        signUpButton = findViewById(R.id.signUpButton);

        // Initialize Controller
        loginController = new LoginController(this);

        // Check if user is already authenticated
        boolean isFromLogout = getIntent().getBooleanExtra(FROM_LOGOUT, false);
        if (loginController.shouldSkipLogin(isFromLogout)) {
            navigateToMain();
            return;
        }

        setupClickListeners();

        boolean playSplash = savedInstanceState == null && !isFromLogout;
        if (playSplash) {
            playEntranceAnimation(0);
        }
    }

    // UI SETUP AND USER ACTIONS
    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());

        passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        MaterialButton forgotButton = findViewById(R.id.forgotPasswordButton);
        forgotButton.setOnClickListener(v -> attemptPasswordReset());

        googleButton.setOnClickListener(v -> performGoogleSignIn());
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        // Clears old error messages
        emailLayout.setError(null);
        passwordLayout.setError(null);

        String email = emailInput.getText() != null ? emailInput.getText().toString() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";
        loginController.loginWithEmail(email, password);
    }

    private void attemptPasswordReset() {
        emailLayout.setError(null);

        String email = emailInput.getText() != null ? emailInput.getText().toString() : "";
        loginController.sendPasswordResetEmail(email);
    }

    // Launch Google Credential Manager and exchange the returned ID token for Firebase authentication
    private void performGoogleSignIn() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        CredentialManager credentialManager = CredentialManager.create(this);
        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential();
                        if (credential instanceof CustomCredential &&
                                credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                            try {
                                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                                loginController.loginWithGoogle(googleIdTokenCredential.getIdToken());
                            } catch (Exception e) {
                                showToast("Google Sign-In failed to parse token.");
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        String msg = e.getMessage();
                        if (msg != null && msg.toLowerCase().contains("no credentials")) {
                            showToast("Please sign in to a Google account on this device first.");
                            try {
                                Intent intent = new Intent(android.provider.Settings.ACTION_ADD_ACCOUNT);
                                intent.putExtra(android.provider.Settings.EXTRA_ACCOUNT_TYPES, new String[]{"com.google"});
                                startActivity(intent);
                            } catch (Exception ex) {
                                showToast("Could not open Account settings.");
                            }
                        } else if (msg != null && !msg.toLowerCase().contains("cancel")) {
                            showToast("Google Sign-In error: " + e.getLocalizedMessage());
                        }
                    }
                }
        );
    }

    // CALLBACKS FROM LoginController (LoginController.Callback)
    // Handles local validation errors
    @Override
    public void onValidationError(LoginController.Field field, String errorMessage) {
        if (field == LoginController.Field.EMAIL) {
            emailLayout.setError(errorMessage);
        } else if (field == LoginController.Field.PASSWORD) {
            passwordLayout.setError(errorMessage);
        }
    }

    @Override
    public void onLoading() {
        setUiLoading(true);
    }

    @Override
    public void onSuccess(UserData userData) {
        setUiLoading(false);
        showToast("Welcome " + userData.getDisplayName());
        navigateToMain();
    }

    // Handles authentication errors returned by Firebase
    @Override
    public void onError(String errorMessage) {
        setUiLoading(false);

        if("USER NOT FOUND".equals(errorMessage)) {
            emailLayout.setError("Incorrect email or password");
            passwordLayout.setError("Incorrect email or password");
        } else {
            showToast(errorMessage);
        }
    }

    @Override
    public void onActionMessage(String message) {
        showToast(message);
    }

    // UI HELPER/UTILITY METHODS
    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    // Disable buttons while waiting for Firebase to prevent duplicate login requests
    private void setUiLoading(boolean isLoading) {
        loginButton.setEnabled(!isLoading);
        loginButton.setText(isLoading ? "Signing In..." : "Sign In");

        googleButton.setEnabled(!isLoading);
        signUpButton.setEnabled(!isLoading);
    }

    private void playEntranceAnimation(long startDelayMs) {
        float distance = getResources().getDimension(R.dimen.login_space_large);
        loginLogo.setAlpha(0f);
        loginLogo.setTranslationY(-distance);
        loginLogo.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(startDelayMs)
                .setDuration(LOGO_ANIMATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        loginCard.setAlpha(0f);
        loginCard.setTranslationY(distance * 2);
        loginCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(startDelayMs + CARD_ANIMATION_DELAY)
                .setDuration(LOGO_ANIMATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}