// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.controller;

import ca.kava.it.sentryrack.model.InputValidator;
import ca.kava.it.sentryrack.model.UserData;

public class LoginController implements AuthController.Callback {
    // Used by the login activity (view) to determine which field should display an error
    public enum Field {
        EMAIL,
        PASSWORD
    }

    // Callbacks sent back to LoginActivity
    public interface Callback {
        void onValidationError(Field field, String errorMessage);
        void onLoading();
        void onSuccess(UserData userData);
        void onError(String errorMessage);
        void onActionMessage(String message);
    }

    private final AuthController authController;
    private final Callback callback;

    public LoginController(Callback callback) {
        this.callback = callback;
        this.authController = new AuthController(this);     // LoginController receives AuthController events and forwards them to LoginActivity
    }

    // Check if a user is already signed in
    public boolean shouldSkipLogin(boolean isFromLogout) {
        return !isFromLogout && authController.getCurrentUser() != null;
    }

    // Validate login credentials before sending them to Firebase
    public void loginWithEmail(String email, String password) {
        String emailInput = email != null ? email.trim() : "";
        String passwordInput = password != null ? password : "";
        boolean hasError = false;

        if (!InputValidator.isNotBlank(emailInput)) {
            sendValidationError(Field.EMAIL, "Email is required.");
            hasError = true;
        } else if (!InputValidator.isValidEmail(emailInput)) {
            sendValidationError(Field.EMAIL, "Please enter a valid email address.");
            hasError = true;
        }

        if (!InputValidator.isNotBlank(passwordInput)) {
            sendValidationError(Field.PASSWORD, "Password is required.");
            hasError = true;
        }

        if(hasError) return;

        authController.loginWithEmail(emailInput, passwordInput);
    }

    // Google Sign-In already provides a valid token
    public void loginWithGoogle(String idToken) {
        authController.loginWithGoogle(idToken);
    }

    // Validate email before requesting a password reset
    public void sendPasswordResetEmail(String email) {
        String cleanEmail = email != null ? email.trim() : "";

        if (!InputValidator.isNotBlank(cleanEmail)) {
            sendValidationError(Field.EMAIL, "Email is required.");
            return;
        }
        if (!InputValidator.isValidEmail(cleanEmail)) {
            sendValidationError(Field.EMAIL, "Please enter a valid email address.");
            return;
        }

        authController.sendPasswordResetEmail(cleanEmail);
    }

    // Helper method to report validation errors back to the View
    private void sendValidationError(Field field, String message) {
        if (callback != null) {
            callback.onValidationError(field, message);
        }
    }

    // AuthController.Callback
    // These methods simply forward AuthController events to LoginActivity
    @Override
    public void onLoading() {
        if (callback != null) callback.onLoading();
    }

    @Override
    public void onSuccess(UserData userData) {
        if (callback != null) callback.onSuccess(userData);
    }

    @Override
    public void onError(String errorMessage) {
        if (callback != null) callback.onError(errorMessage);
    }

    @Override
    public void onActionMessage(String message) {
        if (callback != null) callback.onActionMessage(message);
    }
}