// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.controller;

import android.util.Patterns;

import ca.kava.it.sentryrack.R;
import ca.kava.it.sentryrack.model.DeviceInfo;
import ca.kava.it.sentryrack.model.Feedback;
import ca.kava.it.sentryrack.model.FeedbackRepository;
import ca.kava.it.sentryrack.model.User;

public class FeedbackController {

    public enum Field { FULL_NAME, PHONE, EMAIL, COMMENT, RATING }

    public interface Callback {
        void onFieldError(Field field, int errorResId);
        void onNotLoggedIn();
        void onSuccess();
        void onError(String message);
    }

    private static final int PHONE_DIGITS = 10;   // D4: valid 10-digit phone

    private final FeedbackRepository repository = new FeedbackRepository();
    private final AuthController authController = new AuthController();
    private final Callback callback;

    public FeedbackController(Callback callback) {
        this.callback = callback;
    }

    /**
     * Validates the input and saves it. Returns true when the input was valid
     * and the save was started; the result then arrives through the callback.
     */
    public boolean submit(String fullName, String phone, String email, String comment,
                          double stars, DeviceInfo deviceInfo) {
        String name = clean(fullName);
        String phoneNumber = clean(phone);
        String mail = clean(email);
        String text = clean(comment);

        boolean valid = true;
        if (name.isEmpty()) {
            callback.onFieldError(Field.FULL_NAME, R.string.feedback_name_error);
            valid = false;
        }
        if (!isValidPhone(phoneNumber)) {
            callback.onFieldError(Field.PHONE, R.string.feedback_phone_error);
            valid = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            callback.onFieldError(Field.EMAIL, R.string.feedback_email_error);
            valid = false;
        }
        if (text.isEmpty()) {
            callback.onFieldError(Field.COMMENT, R.string.feedback_empty_error);
            valid = false;
        }
        if (stars < 0.5 || stars > 5) {
            callback.onFieldError(Field.RATING, R.string.feedback_rating_error);
            valid = false;
        }
        if (!valid) {
            return false;
        }

        User user = authController.getCurrentUser();
        if (user == null) {
            callback.onNotLoggedIn();
            return false;
        }

        Feedback feedback = new Feedback(name, phoneNumber, mail, text, stars, deviceInfo);
        repository.submitFeedback(user.getUid(), feedback, new FeedbackRepository.Callback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
        return true;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    // Digits, spaces and + - ( ) only, with at least 7 digits
    // Exactly 10 digits, e.g. 4165550123 (spaces, + - ( ) are ignored) - Anvesh 2026-11-18
    private boolean isValidPhone(String phone) {
        if (!phone.matches("[0-9 +\\-()]+")) return false;
        int digits = 0;
        for (char c : phone.toCharArray()) {
            if (Character.isDigit(c)) digits++;
        }
        return digits == PHONE_DIGITS;
    }
}