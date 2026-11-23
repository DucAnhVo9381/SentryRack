// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ca.kava.it.sentryrack.R;
import ca.kava.it.sentryrack.controller.SignUpController;
import ca.kava.it.sentryrack.model.UserData;

public class SignUpActivity extends AppCompatActivity implements SignUpController.Callback {
    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmLayout;
    private TextInputLayout phoneLayout;

    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmInput;
    private TextInputEditText phoneInput;

    private MaterialCheckBox agreeCheck;
    private MaterialButton registerButton;
    private MaterialButton signInButton;

    private SignUpController signUpController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);

        nameLayout = findViewById(R.id.registerNameLayout);
        emailLayout = findViewById(R.id.registerEmailLayout);
        passwordLayout = findViewById(R.id.registerPasswordLayout);
        confirmLayout = findViewById(R.id.registerConfirmLayout);
        phoneLayout = findViewById(R.id.registerPhoneLayout);

        nameInput = findViewById(R.id.registerNameInput);
        emailInput = findViewById(R.id.registerEmailInput);
        passwordInput = findViewById(R.id.registerPasswordInput);
        confirmInput = findViewById(R.id.registerConfirmInput);
        phoneInput = findViewById(R.id.registerPhoneInput);

        agreeCheck = findViewById(R.id.registerAgreeCheck);

        registerButton = findViewById(R.id.registerButton);
        signInButton = findViewById(R.id.signupSignInButton);

        // Initialize controller and register for callback events
        signUpController = new SignUpController(this);

        Utility.clearErrorOnInput(nameInput, nameLayout);
        Utility.clearErrorOnInput(phoneInput, phoneLayout);
        Utility.clearErrorOnInput(emailInput, emailLayout);
        Utility.clearErrorOnInput(passwordInput, passwordLayout);
        Utility.clearErrorOnInput(confirmInput, confirmLayout);

        registerButton.setOnClickListener(v -> attemptSignUp());
        signInButton.setOnClickListener(v -> finish());
    }

    // Collect user input and delegate validation/account creation to the controller
    private void attemptSignUp() {
        clearErrors();

        signUpController.signUp(
                Utility.getText(nameInput).trim(),
                Utility.getText(phoneInput).trim(),
                Utility.getText(emailInput).trim(),
                Utility.getText(passwordInput),
                Utility.getText(confirmInput),
                agreeCheck.isChecked()
        );
    }

    // Display field-specific validation errors returned by SignUpController
    @Override
    public void onValidationError(SignUpController.Field field, String errorMessage) {
        switch (field) {
            case NAME:
                nameLayout.setError(errorMessage);
                break;
            case PHONE:
                phoneLayout.setError(errorMessage);
                break;
            case EMAIL:
                emailLayout.setError(errorMessage);
                break;
            case PASSWORD:
                passwordLayout.setError(errorMessage);
                break;
            case CONFIRM_PASSWORD:
                confirmLayout.setError(errorMessage);
                break;
            case PRIVACY:
                Utility.showLongToast(this, errorMessage);
                break;
        }
    }

    @Override
    public void onLoading() {
        setUiLoading(true);
    }

    @Override
    public void onSuccess(UserData userData) {
        setUiLoading(false);
        Utility.showLongToast(this, getString(R.string.register_success));
        finish();
    }

    // Handles Firebase authentication/repository errors
    @Override
    public void onError(String errorMessage) {
        setUiLoading(false);
        Utility.showLongToast(this, errorMessage);
    }

    @Override
    public void onActionMessage(String message) {
        setUiLoading(false);
        Utility.showLongToast(this, message);
    }

    // UI HELPER/UTILITY METHODS
    private void clearErrors() {
        nameLayout.setError(null);
        phoneLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmLayout.setError(null);
    }

    // Prevent duplicate sign up requests while waiting for Firebase
    private void setUiLoading(boolean isLoading) {
        registerButton.setEnabled(!isLoading);
        signInButton.setEnabled(!isLoading);

        registerButton.setText(isLoading ? R.string.register_creating_account : R.string.register_button);
    }
}