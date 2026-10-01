// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/** Login screen (Deliverable 2 step 11): Splash (3 s) -> Login -> Main.
 * This is the app's launcher screen, so the splash screen is installed here.
 * For this deliverable any email and password are accepted,
 * but both fields must be filled in. Real accounts come in a later deliverable.
 * @author Anvesh
 * @date 2026-10-01
 */
public class LoginActivity extends AppCompatActivity {

    // Remember me is saved in a small private settings file on the phone
    private static final String PREFS_NAME = "sentryrack_login";
    private static final String KEY_REMEMBERED_EMAIL = "remembered_email";
    //Set by MainActivity on Log out, so the 3-second splash is not shown again
    public static final String EXTRA_FROM_LOGOUT = "ca.kava.it.sentryrack.FROM_LOGOUT";
    /** Deliverable 2 step 8b: splash screen stays for 3 seconds */
    private static final long SPLASH_DURATION_MS = 3000;
    private static final long SPLASH_EXIT_DURATION_MS = 500;
    private static final long ANIMATION_DURATION_MS = 600;
    private static final long CARD_ANIMATION_DELAY_MS = 150;

    // Splash stays visible while this is true (set to false after 3 seconds)
    private volatile boolean keepSplash = true;

    private View loginRoot;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialCheckBox rememberMeCheck;

    /** Shows the splash (first launch only), then loads the login layout
     * @author Anvesh
     * @date 2026-10-01
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Fresh app launch = not a screen rotation
        boolean freshLaunch = savedInstanceState == null
                && !getIntent().getBooleanExtra(EXTRA_FROM_LOGOUT, false);

        // The splash screen must be installed BEFORE super.onCreate()
        setupSplashScreen(freshLaunch);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bindViews();
        handleSystemBarsAndKeyboard();
        loadRememberedEmail();
        setupClickListeners();

        // Play the entrance animation right after the splash disappears
        if (freshLaunch) {
            playEntranceAnimation(SPLASH_DURATION_MS);
        }
    }

    /** Setup splashscreen (moved here from MainActivity so the splash appears before Login)
     * Original splash code and zoom-out exit animation by Volk (2026-09-25).
     * Changed: 5 s -> 3 s (Deliverable 2 step 8b) and only held on a fresh launch.
     * @author Anvesh
     * @date 2026-10-01
     */
    private void setupSplashScreen(boolean holdSplash) {
        // Activates the splash screen API - Android creates splash screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        // Custom exit animation (Volk): the icon zooms out with a small overshoot
        splashScreen.setOnExitAnimationListener(screen -> {
            ObjectAnimator zoomX = ObjectAnimator.ofFloat(screen.getIconView(), View.SCALE_X, 0.8f, 0.0f);
            zoomX.setInterpolator(new OvershootInterpolator());
            zoomX.setDuration(SPLASH_EXIT_DURATION_MS);

            ObjectAnimator zoomY = ObjectAnimator.ofFloat(screen.getIconView(), View.SCALE_Y, 0.8f, 0.0f);
            zoomY.setInterpolator(new OvershootInterpolator());
            zoomY.setDuration(SPLASH_EXIT_DURATION_MS);

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
            new Handler(Looper.getMainLooper()).postDelayed(() -> keepSplash = false, SPLASH_DURATION_MS);
        }
    }

    /** Connects the Java fields to the views in activity_login.xml / login_form_card.xml
     * @author Anvesh
     * @date 2026-10-01
     */
    private void bindViews() {
        loginRoot = findViewById(R.id.loginRoot);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        rememberMeCheck = findViewById(R.id.rememberMeCheck);
    }

    /** The app draws behind the status bar and navigation bar (edge-to-edge).
     * This pads the screen so nothing is hidden behind them,
     * and pushes the form up when the keyboard opens.
     * @author Anvesh
     * @date 2026-10-01
     */
    private void handleSystemBarsAndKeyboard() {
        ViewCompat.setOnApplyWindowInsetsListener(loginRoot, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout()
                    | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /** Wires up every button on the login screen
     * @author Anvesh
     * @date 2026-10-01
     */
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
        forgotButton.setOnClickListener(v -> showMessage(R.string.login_forgot_password_msg));

        MaterialButton googleButton = findViewById(R.id.googleButton);
        googleButton.setOnClickListener(v -> showMessage(R.string.login_google_soon));

        MaterialButton signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(v -> showMessage(R.string.login_sign_up_soon));
    }

    /** Opens the main screen if BOTH fields have text (anything is accepted for now)
     * @author Anvesh
     * @date 2026-10-01
     */
    private void attemptLogin() {
        // Check both so the user sees every error at once
        boolean emailFilled = isFieldFilled(emailInput, emailLayout, R.string.error_email_empty);
        boolean passwordFilled = isFieldFilled(passwordInput, passwordLayout, R.string.error_password_empty);

        if (emailFilled && passwordFilled) {
            saveRememberedEmail(textOf(emailInput).trim());
            openMainScreen();
        }
    }

    /** Shows a red error under the field if it is empty, clears it otherwise
     * @author Anvesh
     * @date 2026-10-01
     * @return true if the field has text
     */
    private boolean isFieldFilled(TextInputEditText input, TextInputLayout layout, @StringRes int errorRes) {
        if (textOf(input).trim().isEmpty()) {
            layout.setError(getString(errorRes));
            return false;
        }
        layout.setError(null);
        return true;
    }

    /** Remember me: fills in the saved email when the screen opens
     * @author Anvesh
     * @date 2026-10-01
     */
    private void loadRememberedEmail() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedEmail = prefs.getString(KEY_REMEMBERED_EMAIL, "");
        if (!savedEmail.isEmpty()) {
            emailInput.setText(savedEmail);
            rememberMeCheck.setChecked(true);
        }
    }

    /** Remember me: saves the email if the box is ticked, otherwise forgets it
     * @author Anvesh
     * @date 2026-10-01
     */
    private void saveRememberedEmail(String email) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_REMEMBERED_EMAIL, rememberMeCheck.isChecked() ? email : "")
                .apply();
    }

    /** Opens MainActivity and closes Login, so Back does not return to the login screen
     * @author Anvesh
     * @date 2026-10-01
     */
    private void openMainScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /** Shows a short message at the bottom of the screen
     * @author Anvesh
     * @date 2026-10-01
     */
    private void showMessage(@StringRes int messageRes) {
        Snackbar.make(loginRoot, messageRes, Snackbar.LENGTH_LONG).show();
    }

    /** Entrance animation: logo fades in from above, card slides up from below
     * @author Anvesh
     * @date 2026-10-01
     * @param startDelayMs wait this long first (so it plays after the splash screen)
     */
    private void playEntranceAnimation(long startDelayMs) {
        View logo = findViewById(R.id.loginLogo);
        View card = findViewById(R.id.loginCard);
        float distance = getResources().getDimension(R.dimen.login_space_large);

        logo.setAlpha(0f);
        logo.setTranslationY(-distance);
        logo.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(startDelayMs)
                .setDuration(ANIMATION_DURATION_MS)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        card.setAlpha(0f);
        card.setTranslationY(distance * 2);
        card.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(startDelayMs + CARD_ANIMATION_DELAY_MS)
                .setDuration(ANIMATION_DURATION_MS)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /** Small helper: returns the text in a field, or "" if it is empty
     * @author Anvesh
     * @date 2026-10-01
     */
    private static String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}