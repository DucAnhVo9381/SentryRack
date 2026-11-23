// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.controller;

import ca.kava.it.sentryrack.model.InputValidator;
import ca.kava.it.sentryrack.model.UserData;

public class SignUpController implements AuthController.Callback {
    public enum Field {NAME, PHONE, EMAIL, PASSWORD, CONFIRM_PASSWORD, PRIVACY}

    public interface Callback {
        void onValidationError(Field field, String errorMessage);
        void onLoading();
        void onSuccess(UserData userData);
        void onError(String errorMessage);
        void onActionMessage(String message);
    }

    private final AuthController authController;
    private final Callback callback;

    public SignUpController(SignUpController.Callback callback) {
        this.callback = callback;
        this.authController = new AuthController(this);     // LoginController receives AuthController events and forwards them to LoginActivity
    }

    public void signUp(String name, String phone, String email, String password, String confirmPassword, boolean privacyAccepted) {
        boolean hasError = false;

        if (InputValidator.isBlank(name)) {
            sendValidationError(Field.NAME, "Name is required");
            hasError = true;
        } else if (!InputValidator.isValidFullName(name)) {
            sendValidationError(Field.NAME, "Please enter a valid name");
            hasError = true;
        }

        if (InputValidator.isBlank(phone)) {
            sendValidationError(Field.PHONE, "Phone number is required");
            hasError = true;
        } else if (!InputValidator.isValidPhoneNumber(phone)) {
            sendValidationError(Field.PHONE, "Please enter a valid phone number");
            hasError = true;
        }

        if (InputValidator.isBlank(email)) {
            sendValidationError(Field.EMAIL, "Email is required");
            hasError = true;
        } else if (!InputValidator.isValidEmail(email)) {
            sendValidationError(Field.EMAIL, "Please enter a valid email address");
            hasError = true;
        }

        if (InputValidator.isBlank(password)) {
            sendValidationError(Field.PASSWORD, "Password is required.");
            hasError = true;
        } else if (!InputValidator.isValidPassword(password)) {
            sendValidationError(Field.PASSWORD, "Password must be at least 9 characters, an uppercase letter, a number, and a special character");
            hasError = true;
        }

        if (!InputValidator.passwordsMatch(password, confirmPassword)) {
            sendValidationError(Field.CONFIRM_PASSWORD, "Passwords do not match");
            hasError = true;
        }

        if (!privacyAccepted) {
            sendValidationError(Field.PRIVACY, "Please accept the privacy policy");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        authController.signUpWithEmail(email, password);
    }

    // Helper method to report validation errors back to the View
    private void sendValidationError(SignUpController.Field field, String message) {
        if (callback != null) {
            callback.onValidationError(field, message);
        }
    }

    @Override
    public void onLoading() {
        if (callback != null) {
            callback.onLoading();
        }
    }

    @Override
    public void onSuccess(UserData userData) {
        if (callback != null) {
            callback.onSuccess(userData);
        }
    }

    @Override
    public void onError(String errorMessage) {
        if (callback != null) {
            callback.onError(errorMessage);
        }
    }

    @Override
    public void onActionMessage(String message) {
        if (callback != null) {
            callback.onActionMessage(message);
        }
    }
}
