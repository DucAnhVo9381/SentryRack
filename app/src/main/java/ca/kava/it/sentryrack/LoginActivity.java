// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

// Login screen : Splash (3s) -> Login -> Main
public class LoginActivity extends AppCompatActivity {
    //--- CONSTANTS ---
    // Hardcoded login credential (remove later)
    private static final String EMAIL = "aaa@bbb.com";
    private static final String PASSWORD = "Admin101!";

    // Set by MainActivity on Log out, so the 3-second splash is not shown again
    public static final String FROM_LOGOUT = "FROM_LOGOUT";
    private static final long SPLASH_DURATION = 3000;
    private static final long SPLASH_EXIT_DURATION = 500;
    private static final long LOGO_ANIMATION = 1000;
    private static final long CARD_ANIMATION_DELAY = 150;

    // Splash stays visible while this is true (set to false after 3 seconds)
    private volatile boolean keepSplash = true;

    //--- VIEWS ---
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private View loginLogo;
    private View loginCard;

    // Shows the splash (first launch only), then loads the login layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Fresh app launch = not a screen rotation
        boolean freshLaunch = savedInstanceState == null && !getIntent().getBooleanExtra(FROM_LOGOUT,false);

        // The splash screen must be installed BEFORE super.onCreate()
        setupSplashScreen(freshLaunch);

        // Apply the saved light/dark theme before the screen is drawn - @author Anvesh @date 2026-10-15
        SettingsFragment.applySavedTheme(this);
        // Settings > "Lock screen to portrait" - apply the saved choice (Anvesh, 2026-10-01)
        SettingsFragment.applySavedOrientation(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Bind views: Connects the Java fields to the views in activity_login.xml / login_form_card.xml
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginLogo = findViewById(R.id.loginLogo);
        loginCard = findViewById(R.id.loginCard);

        setupClickListeners();

        // Show splash only on first app launch, not after rotation or logout
        if (freshLaunch) {
            playEntranceAnimation(SPLASH_DURATION);
        }
    }

    //--- LOGIN HELPER METHODS ---
    // Wires up every button on the login screen
    private void setupClickListeners() {
        MaterialButton loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> attemptLogin());

        // "Done" on the keyboard (after typing the password) also logs in
        passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        // These features come in a later deliverable - show a friendly message for now
        MaterialButton forgotButton = findViewById(R.id.forgotPasswordButton);
        forgotButton.setOnClickListener(v -> showToast(R.string.login_forgot_password_msg));

        MaterialButton googleButton = findViewById(R.id.googleButton);
        googleButton.setOnClickListener(v -> showToast(R.string.login_google_soon));

        MaterialButton signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(v -> showToast(R.string.login_sign_up_soon));
    }

    // Opens the main screen if BOTH fields have text (anything is accepted for now)
    private void attemptLogin() {
        // Check both so the user sees every error at once
        boolean emailFilled = isFieldFilled(emailInput, emailLayout, R.string.error_email_empty);
        boolean passwordFilled = isFieldFilled(passwordInput, passwordLayout, R.string.error_password_empty);

        // Hardcoded login credentials
        if (emailFilled && passwordFilled) {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (EMAIL.equals(email) && PASSWORD.equals(password)) {
                // Open main screen: Opens MainActivity and closes Login, so Back does not return to the login screen
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                showToast(R.string.app_name);
            }
        }
    }

    // Shows a red error under the field if it is empty, clears it otherwise
    private boolean isFieldFilled(TextInputEditText input, TextInputLayout layout, @StringRes int error) {
        if (input.getText().toString().trim().isEmpty()) {
            layout.setError(getString(error));
            return false;
        }
        layout.setError(null);
        return true;
    }

    //--- ANIMATION METHODS ---
    /* Setup splashscreen (moved here from MainActivity so the splash appears before Login)
       Original splash code and zoom-out exit animation by Volk (2026-09-25) */
    private void setupSplashScreen(boolean holdSplash) {
        // Activates the splash screen API - Android creates splash screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        // Custom exit animation (Volk): the icon zooms out with a small overshoot
        splashScreen.setOnExitAnimationListener(screen -> {
            ObjectAnimator zoomX = ObjectAnimator.ofFloat(screen.getIconView(), View.SCALE_X, 0.8f, 0.0f);
            zoomX.setInterpolator(new OvershootInterpolator());
            zoomX.setDuration(SPLASH_EXIT_DURATION);

            ObjectAnimator zoomY = ObjectAnimator.ofFloat(screen.getIconView(), View.SCALE_Y, 0.8f, 0.0f);
            zoomY.setInterpolator(new OvershootInterpolator());
            zoomY.setDuration(SPLASH_EXIT_DURATION);

            zoomY.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    screen.remove();
                }
            });

            zoomX.start();
            zoomY.start();
        });

        // Keep the splash for 3 seconds on a fresh launch only (not after rotation)
        if (holdSplash) {
            splashScreen.setKeepOnScreenCondition(() -> keepSplash);
            new Handler(Looper.getMainLooper()).postDelayed(() -> keepSplash = false, SPLASH_DURATION);
        }
    }

    /* Entrance animation: logo fades in from above, card slides up from below
     * @param startDelayMs wait this long first (so it plays after the splash screen)
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
    private void showToast(@StringRes int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}