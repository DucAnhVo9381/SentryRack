// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** User (profile) screen - 3rd bottom navigation tab.
 * Working features: Change password (dialog with Current, New, Confirm) and Log out.
 * Edit profile, My alerts and Activity history are design only for now.
 * Passwords are stored as a SHA-256 hash on the phone; Firebase will replace this later.
 * @author Anvesh
 * @date 2026-10-07
 */
public class AccountFragment extends Fragment {

    // Saved user data (only the password hash for now)
    private static final String PREFS_NAME = "sentryrack_user";
    private static final String KEY_PASSWORD_HASH = "password_hash";

    // Email saved by LoginActivity when "Remember me" is ticked
    private static final String LOGIN_PREFS_NAME = "sentryrack_login";
    private static final String KEY_REMEMBERED_EMAIL = "remembered_email";

    private static final int MIN_PASSWORD_LENGTH = 6;

    public AccountFragment() {
        // Required empty public constructor
    }

    /** Loads the User screen layout
     * @author Anvesh
     * @date 2026-10-07
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    /** Fills in the header and connects the rows and buttons
     * @author Anvesh
     * @date 2026-10-07
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showSavedEmail(view);
        setupRows(view);
    }

    /** Shows the email the user logged in with (if "Remember me" was ticked)
     * @author Anvesh
     * @date 2026-10-07
     */
    private void showSavedEmail(View root) {
        SharedPreferences loginPrefs =
                requireContext().getSharedPreferences(LOGIN_PREFS_NAME, Context.MODE_PRIVATE);
        String email = loginPrefs.getString(KEY_REMEMBERED_EMAIL, "");
        if (!email.isEmpty()) {
            TextView emailText = root.findViewById(R.id.userEmail);
            emailText.setText(email);
        }
    }

    /** Connects each row: Change password works, the others show "available soon"
     * @author Anvesh
     * @date 2026-10-07
     */
    private void setupRows(View root) {
        root.findViewById(R.id.rowChangePassword).setOnClickListener(v -> showChangePasswordDialog());

        View.OnClickListener comingSoon = v -> showMessage(R.string.user_soon_msg);
        root.findViewById(R.id.rowEditProfile).setOnClickListener(comingSoon);
        root.findViewById(R.id.rowMyAlerts).setOnClickListener(comingSoon);
        root.findViewById(R.id.rowHistory).setOnClickListener(comingSoon);

        // Log out reuses the same confirm dialog as the toolbar menu (MainActivity)
        root.findViewById(R.id.btnLogout).setOnClickListener(v ->
                ((MainActivity) requireActivity()).confirmLogout());
    }

    /** Opens the Change password dialog (Current, New, Confirm + Save / Cancel).
     * Save is checked first, so the dialog stays open if something is wrong.
     * @author Anvesh
     * @date 2026-10-07
     */
    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.ar_dialog_change_password, null);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.user_pw_title)
                .setView(dialogView)
                .setPositiveButton(R.string.user_pw_save, null)   // click handled below
                .setNegativeButton(R.string.user_pw_cancel, (d, which) -> d.dismiss())
                .create();

        dialog.show();

        // Replace the default Save click so the dialog only closes when the input is valid
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (validateAndSavePassword(dialogView)) {
                dialog.dismiss();
                showMessage(R.string.user_pw_saved_msg);
            }
        });
    }

    /** Checks the 3 fields and saves the new password if everything is correct
     * @author Anvesh
     * @date 2026-10-07
     * @return true if the password was saved
     */
    private boolean validateAndSavePassword(View dialogView) {
        TextInputLayout currentLayout = dialogView.findViewById(R.id.currentPasswordLayout);
        TextInputLayout newLayout = dialogView.findViewById(R.id.newPasswordLayout);
        TextInputLayout confirmLayout = dialogView.findViewById(R.id.confirmPasswordLayout);

        String current = textOf(dialogView.findViewById(R.id.currentPasswordInput));
        String newPassword = textOf(dialogView.findViewById(R.id.newPasswordInput));
        String confirm = textOf(dialogView.findViewById(R.id.confirmPasswordInput));

        // Clear old errors first
        currentLayout.setError(null);
        newLayout.setError(null);
        confirmLayout.setError(null);

        // 1. No empty fields
        boolean ok = true;
        if (current.isEmpty()) {
            currentLayout.setError(getString(R.string.user_pw_error_empty));
            ok = false;
        }
        if (newPassword.isEmpty()) {
            newLayout.setError(getString(R.string.user_pw_error_empty));
            ok = false;
        }
        if (confirm.isEmpty()) {
            confirmLayout.setError(getString(R.string.user_pw_error_empty));
            ok = false;
        }
        if (!ok) {
            return false;
        }

        // 2. If a password was saved before, the current one must match it
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedHash = prefs.getString(KEY_PASSWORD_HASH, "");
        if (!savedHash.isEmpty() && !savedHash.equals(hashPassword(current))) {
            currentLayout.setError(getString(R.string.user_pw_error_current));
            return false;
        }

        // 3. New password rules
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            newLayout.setError(getString(R.string.user_pw_error_short, MIN_PASSWORD_LENGTH));
            return false;
        }
        if (newPassword.equals(current)) {
            newLayout.setError(getString(R.string.user_pw_error_same));
            return false;
        }
        if (!newPassword.equals(confirm)) {
            confirmLayout.setError(getString(R.string.user_pw_error_match));
            return false;
        }

        // 4. All good - save only the hash, never the real password
        prefs.edit().putString(KEY_PASSWORD_HASH, hashPassword(newPassword)).apply();
        return true;
    }

    /** Turns a password into a SHA-256 hash (hex text), so the real password is not stored
     * @author Anvesh
     * @date 2026-10-07
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available on Android; this should never happen
            throw new IllegalStateException(e);
        }
    }

    /** Shows a short message at the bottom of the screen
     * @author Anvesh
     * @date 2026-10-07
     */
    private void showMessage(@StringRes int messageRes) {
        Snackbar.make(requireView(), messageRes, Snackbar.LENGTH_SHORT).show();
    }

    /** Small helper: returns the text in a field, or "" if it is empty
     * @author Anvesh
     * @date 2026-10-07
     */
    private static String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}