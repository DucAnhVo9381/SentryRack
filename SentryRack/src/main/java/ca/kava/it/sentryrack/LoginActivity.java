// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ca.kava.it.sentryrack.model.AuthState;
import ca.kava.it.sentryrack.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {
    //--- CONSTANTS ---
    // Set by MainActivity on Log out, so the 3-second splash is not shown again
    public static final String FROM_LOGOUT = "FROM_LOGOUT";
    private static final long LOGO_ANIMATION = 1000;
    private static final long CARD_ANIMATION_DELAY = 150;

    //--- VIEWS ---
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private View loginLogo;
    private View loginCard;
    private MaterialButton loginButton;
    private MaterialButton googleButton;
    private MaterialButton signUpButton;

    //--- VIEWMODEL ---
    private AuthViewModel authViewModel;

    // Shows the splash (first launch only), then loads the login layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply the saved light/dark theme before the screen is drawn - @author Anvesh @date 2026-10-15
        SettingsFragment.applySavedTheme(this);
        // Settings > "Lock screen to portrait" - apply the saved choice (Anvesh, 2026-10-01)
        SettingsFragment.applySavedOrientation(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Bind views: Connects the Java fields to the views in activity_login.xml / login_form_card.xml
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginLogo = findViewById(R.id.loginLogo);
        loginCard = findViewById(R.id.loginCard);
        loginButton = findViewById(R.id.loginButton);
        googleButton = findViewById(R.id.googleButton);
        signUpButton = findViewById(R.id.signUpButton);

        // Initialize MVVM ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Check if user is already authenticated with Firebase
        boolean isFromLogout = getIntent().getBooleanExtra(FROM_LOGOUT, false);
        if (!isFromLogout && authViewModel.getCurrentUser() != null) {
            // Open main screen: Opens MainActivity and closes Login, so Back does not return to the login screen
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setupObservers();
        setupClickListeners();

        // Show splash only on first app launch, not after rotation or logout
        boolean playSplash = savedInstanceState == null && !isFromLogout;
        if (playSplash) {
            playEntranceAnimation(0);
        }
    }

    //--- OBSERVERS ---
    private void setupObservers() {
        authViewModel.getAuthState().observe(this, authState -> {
            if (authState == null) return;

            switch (authState.getStatus()) {
                case LOADING:
                    setUiLoading(true);
                    break;
                case SUCCESS:
                    setUiLoading(false);
                    showToast("Welcome " + authState.getUser().getDisplayName());
                    // Open main screen: Opens MainActivity and closes Login, so Back does not return to the login screen
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    break;
                case ERROR:
                    setUiLoading(false);
                    showToast(authState.getMessage());
                    break;
                case IDLE:
                    setUiLoading(false);
                    break;
            }
        });

        authViewModel.getResetEmailStatus().observe(this, statusMsg -> {
            if (statusMsg != null) {
                showToast(statusMsg);
            }
        });
    }

    //--- LOGIN HELPER METHODS ---
    // Wires up every button on the login screen
    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());

        // "Done" on the keyboard (after typing the password) also logs in
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
        signUpButton.setOnClickListener(v -> attemptSignUp());
    }

    // Opens the main screen if BOTH fields have text
    private void attemptLogin() {
        // Check both so the user sees every error at once
        boolean emailFilled = isFieldFilled(emailInput, emailLayout, R.string.error_email_empty);
        boolean passwordFilled = isFieldFilled(passwordInput, passwordLayout, R.string.error_password_empty);

        if (emailFilled && passwordFilled && emailInput.getText() != null && passwordInput.getText() != null) {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            authViewModel.loginWithEmail(email, password);
        }
    }

    private void attemptSignUp() {
        boolean emailFilled = isFieldFilled(emailInput, emailLayout, R.string.error_email_empty);
        boolean passwordFilled = isFieldFilled(passwordInput, passwordLayout, R.string.error_password_empty);

        if (emailFilled && passwordFilled && emailInput.getText() != null && passwordInput.getText() != null) {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            authViewModel.signUpWithEmail(email, password);
        }
    }

    private void attemptPasswordReset() {
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        if (email.isEmpty()) {
            emailLayout.setError(getString(R.string.error_email_empty));
            return;
        }
        emailLayout.setError(null);
        authViewModel.sendPasswordResetEmail(email);
    }

    // Google Sign-In using Credential Manager API
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
                                GoogleIdTokenCredential googleIdTokenCredential =
                                        GoogleIdTokenCredential.createFrom(credential.getData());
                                authViewModel.loginWithGoogle(googleIdTokenCredential.getIdToken());
                            } catch (Exception e) {
                                showToast("Google Sign-In failed to parse token.");
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        if (!e.getMessage().contains("cancel")) {
                            showToast("Google Sign-In error: " + e.getLocalizedMessage());
                        }
                    }
                }
        );
    }

    private void setUiLoading(boolean isLoading) {
        loginButton.setEnabled(!isLoading);
        googleButton.setEnabled(!isLoading);
        signUpButton.setEnabled(!isLoading);
    }

    // Shows a red error under the field if it is empty, clears it otherwise
    private boolean isFieldFilled(TextInputEditText input, TextInputLayout layout, @StringRes int error) {
        if (input.getText() == null || input.getText().toString().trim().isEmpty()) {
            layout.setError(getString(error));
            return false;
        }
        layout.setError(null);
        return true;
    }

    //--- ANIMATION METHODS ---
    /* Entrance animation: logo fades in from above, card slides up from below
     * @param startDelayMs wait this long first
     */
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

    //--- UTILITY/REUSABLE METHODS ---
    // Shows a short message at the bottom of the screen
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}