// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;

import ca.kava.it.sentryrack.R;
import ca.kava.it.sentryrack.model.ProfileRepository;

/**
 * Controller for the Account screen (MVC, same style as FeedbackController).
 * Checks the input, then asks ProfileRepository to change Firebase.
 * The screen (AccountFragment) only shows what this controller reports.
 * @author Anvesh Rachakonda
 * @date 2026-11-21
 */
public class ProfileController {

    public enum Field { NAME, CURRENT_PASSWORD, NEW_PASSWORD, CONFIRM_PASSWORD, DELETE_PASSWORD }

    /** Stable keys saved in Firebase (users/{uid}/role) - labels come from strings.xml. */
    public static final String[] ROLE_KEYS = {"technician", "network_engineer",
            "facility_manager", "it_admin"};
    public static final int[] ROLE_LABELS = {R.string.role_technician, R.string.role_network,
            R.string.role_facility, R.string.role_it_admin};

    // D4 #28: 9+ characters, lower case, upper case, a digit and a special character
    private static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{9,}$";
    // 2-50 letters, spaces, dots, apostrophes and hyphens (any language)
    private static final String NAME_REGEX = "^[\\p{L} .'-]{2,50}$";

    private static final int PHOTO_SIZE = 256;       // px, square
    private static final int PHOTO_QUALITY = 80;     // JPEG quality

    public interface Callback {
        void onProfileLoaded(String name, String email, @Nullable String roleKey,
                             @Nullable Bitmap customPhoto, @Nullable Uri googlePhoto,
                             boolean isGoogleAccount, boolean hasPassword);
        void onFieldError(Field field, @StringRes int errorResId);
        void onSaved(@StringRes int messageResId);
        void onError(String message);
        void onAccountDeleted();
        void onReloginRequired();
    }

    private final ProfileRepository repository = new ProfileRepository();
    private final Callback callback;

    public ProfileController(@NonNull Callback callback) {
        this.callback = callback;
    }

    //--- LOAD ---

    /** Reads the profile; the screen is told once everything is ready. */
    public void load() {
        FirebaseUser user = repository.getUser();
        if (user == null) return;

        repository.loadProfile((name, roleKey, photo) -> {
            String shownName = name != null ? name
                    : user.getDisplayName() != null ? user.getDisplayName() : "";
            callback.onProfileLoaded(shownName, user.getEmail(), roleKey, decodePhoto(photo),
                    repository.getGooglePhotoUrl(), repository.isGoogleAccount(),
                    repository.hasPassword());
        });
    }

    //--- NAME ---

    public boolean changeName(String name) {
        String clean = name == null ? "" : name.trim();
        if (!isValidName(clean)) {
            callback.onFieldError(Field.NAME, R.string.account_name_error);
            return false;
        }
        repository.updateName(clean, result(R.string.account_name_saved));
        return true;
    }

    //--- ROLE ---

    public void changeRole(int index) {
        if (index < 0 || index >= ROLE_KEYS.length) return;
        repository.setField(ProfileRepository.FIELD_ROLE, ROLE_KEYS[index],
                result(R.string.account_role_saved));
    }

    /** Position of a saved role in the list (-1 = not set). */
    public static int roleIndex(@Nullable String roleKey) {
        for (int i = 0; i < ROLE_KEYS.length; i++) {
            if (ROLE_KEYS[i].equals(roleKey)) return i;
        }
        return -1;
    }

    //--- PHOTO ---

    /** Shrinks the picked image to a 256 px square JPEG and saves it as text. */
    public void changePhoto(@NonNull Bitmap picked) {
        repository.setField(ProfileRepository.FIELD_PHOTO, encodePhoto(picked),
                result(R.string.account_photo_saved));
    }

    /** Removes the custom photo (Google accounts then show their Google photo again). */
    public void removePhoto() {
        repository.setField(ProfileRepository.FIELD_PHOTO, null,
                result(R.string.account_photo_removed));
    }

    //--- PASSWORD ---

    /** Checks all 3 fields (every error is shown), then changes the password in Firebase. */
    public boolean changePassword(String current, String newPassword, String confirm) {
        boolean valid = true;
        if (isEmpty(current)) {
            callback.onFieldError(Field.CURRENT_PASSWORD, R.string.user_pw_error_empty);
            valid = false;
        }
        if (!isStrongPassword(newPassword)) {
            callback.onFieldError(Field.NEW_PASSWORD, R.string.account_pw_rules);
            valid = false;
        } else if (newPassword.equals(current)) {
            callback.onFieldError(Field.NEW_PASSWORD, R.string.user_pw_error_same);
            valid = false;
        }
        if (isEmpty(confirm) || !confirm.equals(newPassword)) {
            callback.onFieldError(Field.CONFIRM_PASSWORD, R.string.user_pw_error_match);
            valid = false;
        }
        if (!valid) return false;

        repository.changePassword(current, newPassword, (success, error) -> {
            if (success) {
                callback.onSaved(R.string.user_pw_saved_msg);
            } else {
                callback.onFieldError(Field.CURRENT_PASSWORD, R.string.user_pw_error_current);
            }
        });
        return true;
    }

    //--- DELETE ACCOUNT ---

    /** Email accounts must type their password; Google accounts confirm with the button only. */
    public boolean deleteAccount(@Nullable String password) {
        if (repository.hasPassword() && isEmpty(password)) {
            callback.onFieldError(Field.DELETE_PASSWORD, R.string.user_pw_error_empty);
            return false;
        }
        repository.deleteAccount(password, (success, error) -> {
            if (success) {
                callback.onAccountDeleted();
            } else if (ProfileRepository.ERROR_RELOGIN.equals(error)) {
                callback.onReloginRequired();
            } else if (repository.hasPassword()) {
                callback.onFieldError(Field.DELETE_PASSWORD, R.string.user_pw_error_current);
            } else {
                callback.onError(error);
            }
        });
        return true;
    }

    public boolean needsPasswordToDelete() {
        return repository.hasPassword();
    }

    //--- RULES (plain Java, easy to unit test) ---

    public static boolean isValidName(@Nullable String name) {
        return name != null && name.trim().matches(NAME_REGEX);
    }

    public static boolean isStrongPassword(@Nullable String password) {
        return password != null && password.matches(PASSWORD_REGEX);
    }

    //--- HELPERS ---

    // Same answer for every save: message on success, error text on failure
    private ProfileRepository.ResultCallback result(@StringRes int savedMessage) {
        return (success, error) -> {
            if (success) {
                callback.onSaved(savedMessage);
            } else {
                callback.onError(error);
            }
        };
    }

    private static boolean isEmpty(@Nullable String value) {
        return value == null || value.isEmpty();
    }

    /** Centre square, 256 x 256, JPEG -> Base64 text (about 15-30 KB). */
    static String encodePhoto(Bitmap source) {
        int side = Math.min(source.getWidth(), source.getHeight());
        Bitmap square = Bitmap.createBitmap(source, (source.getWidth() - side) / 2,
                (source.getHeight() - side) / 2, side, side);
        Bitmap small = Bitmap.createScaledBitmap(square, PHOTO_SIZE, PHOTO_SIZE, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        small.compress(Bitmap.CompressFormat.JPEG, PHOTO_QUALITY, out);
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }

    @Nullable
    static Bitmap decodePhoto(@Nullable String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        try {
            byte[] bytes = Base64.decode(base64, Base64.NO_WRAP);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException e) {
            return null;   // damaged text -> show the default picture
        }
    }

    //Empty callback: screens that only need the profile (e.g. the drawer header),override onProfileLoaded and ignore the rest.
    public abstract static class SimpleCallback implements Callback {
        @Override public void onFieldError(Field field, int errorResId) { }
        @Override public void onSaved(int messageResId) { }
        @Override public void onError(String message) { }
        @Override public void onAccountDeleted() { }
        @Override public void onReloginRequired() { }
    }
}
