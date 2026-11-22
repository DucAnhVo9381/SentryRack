// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import java.util.regex.Pattern;

public class InputValidator {
    private InputValidator() {
        // Prevent instantiation
    }

    public static final Pattern FULL_NAME = Pattern.compile(
            "^[\\p{L}\\p{M}]+" +                  // First name (1+ letters)
                    "([ '\\-.][\\p{L}\\p{M}]+)*" +      // One or more additional part (e.g., last name)
                    "$"
    );

    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );

    public static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+1[\\s.-]?)?" +                  // Optional +1 country code
                    "(\\([2-9]\\d{2}\\)|[2-9]\\d{2})" + // Area code: (416) or 416
                    "[\\s.-]?" +                  // Optional separator
                    "[2-9]\\d{2}" +               // 3-digit exchange code
                    "[\\s.-]?" +                  // Optional separator
                    "\\d{4}$"                     // 4-digit line number
    );

    public static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^" +
                    "(?=.*[0-9])" +               // At least 1 digit
                    "(?=.*[a-z])" +               // At least 1 lowercase letter
                    "(?=.*[A-Z])" +               // At least 1 uppercase letter
                    "(?=.*[@#$%^&+=!*?._-])" +    // At least 1 special character
                    "(?=\\S+$)" +                 // No whitespace allowed
                    ".{9,}" +                     // Minimum 8 characters
                    "$"
    );

    public static boolean isValidFullName(String name) {
        return name != null && FULL_NAME.matcher(name).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean passwordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
