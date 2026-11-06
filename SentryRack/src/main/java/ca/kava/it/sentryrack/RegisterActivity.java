// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Sign up screen (D3 #26-28).
 * - Fields: full name, phone, email, password, confirm password (all validated)
 * - Password rules turn green while the user types
 * - Account is created with Firebase Authentication: the email is the unique ID,
 *   so the same email can never be registered twice
 * - Name and phone are saved in Realtime Database under users/{uid}
 * @author Anvesh Rachakonda
 * @date 2026-11-06
 */
public class RegisterActivity extends AppCompatActivity {

    /** Sent back to Login so the new email is already filled in. */
    public static final String EXTRA_EMAIL = "registered_email";

    private static final String NODE_USERS = "users";
    private static final int MIN_NAME = 2;
    private static final int MIN_PASSWORD = 8;
    private static final int PHONE_DIGITS = 10;

    private TextInputLayout nameLayout, phoneLayout, emailLayout, passwordLayout, confirmLayout;
    private TextInputEditText nameInput, phoneInput, emailInput, passwordInput, confirmInput;
    private TextView ruleLength, ruleUpper, ruleNumber, ruleSymbol;
    private MaterialCheckBox agreeCheck;
    private MaterialButton registerButton;

    // Same text size as the rest of the app (Settings > Text size)
    @Override
    protected void attachBaseContext(Context newBase) {
        Configuration override = new Configuration();
        override.fontScale = newBase.getResources().getConfiguration().fontScale
                * SettingsFragment.getTextScale(newBase);
        applyOverrideConfiguration(override);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsFragment.applySavedOrientation(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        bindViews();
        setupPasswordRules();
        setupClearErrors();

        findViewById(R.id.registerBackButton).setOnClickListener(v -> finish());
        findViewById(R.id.registerSignInButton).setOnClickListener(v -> finish());
        findViewById(R.id.registerPrivacyButton).setOnClickListener(v -> showPrivacyPolicy());
        registerButton.setOnClickListener(v -> attemptRegister());

        // Settings > Font style
        SettingsFragment.applyFont(findViewById(android.R.id.content),
                SettingsFragment.getAppTypeface(this));
    }

    private void bindViews() {
        nameLayout = findViewById(R.id.registerNameLayout);
        phoneLayout = findViewById(R.id.registerPhoneLayout);
        emailLayout = findViewById(R.id.registerEmailLayout);
        passwordLayout = findViewById(R.id.registerPasswordLayout);
        confirmLayout = findViewById(R.id.registerConfirmLayout);

        nameInput = findViewById(R.id.registerNameInput);
        phoneInput = findViewById(R.id.registerPhoneInput);
        emailInput = findViewById(R.id.registerEmailInput);
        passwordInput = findViewById(R.id.registerPasswordInput);
        confirmInput = findViewById(R.id.registerConfirmInput);

        ruleLength = findViewById(R.id.ruleLength);
        ruleUpper = findViewById(R.id.ruleUpper);
        ruleNumber = findViewById(R.id.ruleNumber);
        ruleSymbol = findViewById(R.id.ruleSymbol);

        agreeCheck = findViewById(R.id.registerAgreeCheck);
        registerButton = findViewById(R.id.registerButton);
    }

    //--- LIVE PASSWORD RULES ---

    // Every key press re-checks the 4 rules and turns each one green or grey
    private void setupPasswordRules() {
        passwordInput.addTextChangedListener(new SimpleWatcher(() -> {
            String password = text(passwordInput);
            markRule(ruleLength, hasLength(password));
            markRule(ruleUpper, hasUpper(password));
            markRule(ruleNumber, hasNumber(password));
            markRule(ruleSymbol, hasSymbol(password));
        }));
    }

    private void markRule(TextView rule, boolean met) {
        @DrawableRes int icon = met ? R.drawable.register_rule_done : R.drawable.register_rule_todo;
        rule.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0);
        rule.setTextColor(ContextCompat.getColor(this,
                met ? R.color.register_rule_ok : R.color.login_on_bg_muted));
    }

    private static boolean hasLength(String p) { return p.length() >= MIN_PASSWORD; }
    private static boolean hasUpper(String p)  { return p.matches(".*[A-Z].*"); }
    private static boolean hasNumber(String p) { return p.matches(".*\\d.*"); }
    private static boolean hasSymbol(String p) { return p.matches(".*[^A-Za-z0-9].*"); }

    private static boolean isStrongPassword(String p) {
        return hasLength(p) && hasUpper(p) && hasNumber(p) && hasSymbol(p);
    }

    //--- VALIDATION ---

    // The red error under a field disappears as soon as the user types again
    private void setupClearErrors() {
        clearErrorOnType(nameInput, nameLayout);
        clearErrorOnType(phoneInput, phoneLayout);
        clearErrorOnType(emailInput, emailLayout);
        clearErrorOnType(passwordInput, passwordLayout);
        clearErrorOnType(confirmInput, confirmLayout);
    }

    private void clearErrorOnType(TextInputEditText input, TextInputLayout layout) {
        input.addTextChangedListener(new SimpleWatcher(() -> layout.setError(null)));
    }

    /** Checks every field (so the user sees all errors at once), then creates the account. */
    private void attemptRegister() {
        String name = text(nameInput).trim();
        String phone = digitsOnly(text(phoneInput));
        String email = text(emailInput).trim().toLowerCase();
        String password = text(passwordInput);
        String confirm = text(confirmInput);

        boolean ok = check(nameLayout, name.length() >= MIN_NAME && name.matches("[\\p{L} .'-]+"),
                R.string.register_error_name);
        ok &= check(phoneLayout, phone.length() == PHONE_DIGITS, R.string.register_error_phone);
        ok &= check(emailLayout, Patterns.EMAIL_ADDRESS.matcher(email).matches(),
                R.string.register_error_email);
        ok &= check(passwordLayout, isStrongPassword(password), R.string.register_error_password);
        ok &= check(confirmLayout, !confirm.isEmpty() && confirm.equals(password),
                R.string.user_pw_error_match);

        if (!agreeCheck.isChecked()) {
            showMessage(R.string.register_error_agree);
            ok = false;
        }
        if (ok) {
            createAccount(name, phone, email, password);
        }
    }

    // Shows or clears the red error under one field; returns true when the field is valid
    private boolean check(TextInputLayout layout, boolean valid, @StringRes int error) {
        layout.setError(valid ? null : getString(error));
        return valid;
    }

    //--- FIREBASE ---

    /** Firebase Authentication makes the email the unique ID of the account. */
    private void createAccount(String name, String phone, String email, String password) {
        setLoading(true);
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (isFinishing() || isDestroyed()) return;

            FirebaseUser user = auth.getCurrentUser();
            if (task.isSuccessful() && user != null) {
                // Display name is used by the Account screen and the drawer header
                user.updateProfile(new UserProfileChangeRequest.Builder()
                        .setDisplayName(name).build());
                saveProfile(user.getUid(), name, phone, email);
                return;
            }

            setLoading(false);
            Exception error = task.getException();
            if (error instanceof FirebaseAuthUserCollisionException) {
                emailLayout.setError(getString(R.string.register_error_email_used));
            } else if (error instanceof FirebaseNetworkException) {
                showMessage(R.string.register_error_network);
            } else {
                showMessage(R.string.register_error_failed);
            }
        });
    }

    /** Saves name + phone in Realtime Database (users/{uid}); the password is never stored here. */
    private void saveProfile(String uid, String name, String phone, String email) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", name);
        profile.put("phone", phone);
        profile.put("email", email);
        profile.put("createdAt", ServerValue.TIMESTAMP);

        FirebaseDatabase.getInstance(getString(R.string.firebase_db_url))
                .getReference(NODE_USERS).child(uid)
                .setValue(profile)
                .addOnCompleteListener(task -> {
                    if (isFinishing() || isDestroyed()) return;
                    // The account exists even if this write failed, so always continue
                    FirebaseAuth.getInstance().signOut();   // user signs in on the Login screen
                    setLoading(false);
                    showSuccess(name, email);
                });
    }

    //--- UI HELPERS ---

    private void setLoading(boolean loading) {
        registerButton.setEnabled(!loading);
        registerButton.setText(loading ? R.string.register_creating : R.string.register_button);
    }

    /** "Account created!" dialog, then back to Login with the email filled in. */
    private void showSuccess(String name, String email) {
        new MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.register_rule_done)
                .setTitle(R.string.register_success_title)
                .setMessage(getString(R.string.register_success_msg, name))
                .setCancelable(false)
                .setPositiveButton(R.string.register_sign_in, (dialog, which) -> {
                    setResult(RESULT_OK, new Intent().putExtra(EXTRA_EMAIL, email));
                    finish();
                })
                .show();
    }

    private void showPrivacyPolicy() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.setting_privacy)
                .setMessage(HtmlCompat.fromHtml(getString(R.string.privacy_text).replace("\n", "<br>"),
                        HtmlCompat.FROM_HTML_MODE_LEGACY))
                .setPositiveButton(R.string.setting_ok, (dialog, which) -> agreeCheck.setChecked(true))
                .show();
    }

    private void showMessage(@StringRes int message) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(message)
                .setPositiveButton(R.string.setting_ok, null)
                .show();
    }

    private static String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    private static String digitsOnly(String value) {
        return value.replaceAll("\\D", "");
    }

    /** Small TextWatcher that only needs "after text changed" (avoids 3 empty methods each time). */
    private static class SimpleWatcher implements TextWatcher {
        private final Runnable action;

        SimpleWatcher(@NonNull Runnable action) {
            this.action = action;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
        @Override public void afterTextChanged(Editable s) { action.run(); }
    }
}