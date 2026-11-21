// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.view;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ca.kava.it.sentryrack.R;
import ca.kava.it.sentryrack.controller.AuthController;
import ca.kava.it.sentryrack.model.UserData;

public class SignUpActivity extends AppCompatActivity implements AuthController.Callback {
    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmLayout;

    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmInput;

    private MaterialCheckBox agreeCheck;
    private MaterialButton registerButton;
    private MaterialButton signInButton;

    private AuthController authController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);

        nameLayout = findViewById(R.id.registerNameLayout);
        emailLayout = findViewById(R.id.registerEmailLayout);
        passwordLayout = findViewById(R.id.registerPasswordLayout);
        confirmLayout = findViewById(R.id.registerConfirmLayout);

        nameInput = findViewById(R.id.registerNameInput);
        emailInput = findViewById(R.id.registerEmailInput);
        passwordInput = findViewById(R.id.registerPasswordInput);
        confirmInput = findViewById(R.id.registerConfirmInput);

        agreeCheck = findViewById(R.id.registerAgreeCheck);

        registerButton = findViewById(R.id.registerButton);
        signInButton = findViewById(R.id.signupSignInButton);

        authController = new AuthController(this);

        registerButton.setOnClickListener(v -> attemptSignUp());
        signInButton.setOnClickListener(v -> finish());
    }

    private void attemptSignUp() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirm = confirmInput.getText().toString();

        if (name.isEmpty()) {
            nameLayout.setError("Name is required");
            return;
        }

        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            return;
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            return;
        }

        if (!password.equals(confirm)) {
            confirmLayout.setError("Passwords do not match");
            return;
        }

        if (!agreeCheck.isChecked()) {
            Toast.makeText(this,"Please accept the privacy policy", Toast.LENGTH_SHORT).show();
            return;
        }

        authController.signUpWithEmail(email, password);
    }

    @Override
    public void onLoading() {

    }

    @Override
    public void onSuccess(UserData userData) {
        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActionMessage(String message) {

    }
}