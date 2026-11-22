// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.view;

import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

import ca.kava.it.sentryrack.R;
import ca.kava.it.sentryrack.controller.ProfileController;

/**
 * Account screen v2 (View in MVC): name, role, photo, password, log out, delete account.
 * All checks and Firebase work are done by ProfileController -> ProfileRepository.
 * Photo: custom photo (users/{uid}/photo) > Google photo (Firebase Auth) > default picture.
 * Name, role and photo changes are shown INSTANTLY (here and in the drawer) and saved to
 * Firebase in the background; if Firebase refuses, the screen goes back to the saved profile.
 * @author Anvesh Rachakonda
 * @date 2026-11-22
 */
public class AccountFragment extends Fragment implements ProfileController.Callback {

    private static final int MAX_PICK_SIZE = 1024;   // px, big photos are shrunk while reading

    private ProfileController controller;

    private ShapeableImageView photoView;
    private TextView nameText, emailText, googleBadge, roleChip;
    private TextView nameValue, roleValue, passwordSummary;
    private View passwordRow, passwordChevron;

    // Latest profile (filled by onProfileLoaded)
    private String currentName = "";
    private String currentEmail = "";
    private int currentRole = -1;
    @Nullable private Bitmap customPhoto;
    @Nullable private Uri googlePhoto;
    private boolean isGoogleAccount;
    private boolean hasPassword;

    // The dialog that is waiting for Firebase (closed in onSaved / errors shown in onFieldError)
    @Nullable private AlertDialog activeDialog;
    @Nullable private View activeDialogView;

    // Android photo picker: no storage permission needed
    private final ActivityResultLauncher<PickVisualMediaRequest> photoPicker =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::onPhotoPicked);

    public AccountFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.account_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        controller = new ProfileController(this);

        view.findViewById(R.id.rowAccountName).setOnClickListener(v -> showNameDialog());
        view.findViewById(R.id.rowAccountRole).setOnClickListener(v -> showRoleDialog());
        view.findViewById(R.id.rowAccountPhoto).setOnClickListener(v -> showPhotoSheet());
        view.findViewById(R.id.accountPhotoFrame).setOnClickListener(v -> showPhotoSheet());
        passwordRow.setOnClickListener(v -> showPasswordDialog());
        view.findViewById(R.id.rowAccountDelete).setOnClickListener(v -> showDeleteDialog());
        // Log out reuses the same confirm dialog as the drawer (MainActivity)
        view.findViewById(R.id.btnLogout).setOnClickListener(v ->
                ((MainActivity) requireActivity()).confirmLogout());

        controller.load();
    }

    private void bindViews(View root) {
        photoView = root.findViewById(R.id.accountPhoto);
        nameText = root.findViewById(R.id.accountName);
        emailText = root.findViewById(R.id.accountEmail);
        googleBadge = root.findViewById(R.id.accountGoogleBadge);
        roleChip = root.findViewById(R.id.accountRoleChip);
        nameValue = root.findViewById(R.id.valueAccountName);
        roleValue = root.findViewById(R.id.valueAccountRole);
        passwordRow = root.findViewById(R.id.rowAccountPassword);
        passwordSummary = root.findViewById(R.id.summaryAccountPassword);
        passwordChevron = root.findViewById(R.id.chevronAccountPassword);
    }

    //--- SHOW THE PROFILE ---

    @Override
    public void onProfileLoaded(String name, String email, @Nullable String roleKey,
                                @Nullable Bitmap photo, @Nullable Uri google,
                                boolean isGoogle, boolean canChangePassword) {
        if (!isAdded()) return;
        currentName = name;
        currentEmail = email == null ? "" : email;
        currentRole = ProfileController.roleIndex(roleKey);
        customPhoto = photo;
        googlePhoto = google;
        isGoogleAccount = isGoogle;
        hasPassword = canChangePassword;
        render();
    }

    /** Shows the current values on this screen AND in the drawer header (no waiting). */
    private void render() {
        nameText.setText(currentName);
        nameValue.setText(currentName);
        emailText.setText(currentEmail);
        googleBadge.setVisibility(isGoogleAccount ? View.VISIBLE : View.GONE);

        if (currentRole >= 0) {
            String role = getString(ProfileController.ROLE_LABELS[currentRole]);
            roleChip.setText(role);
            roleChip.setVisibility(View.VISIBLE);
            roleValue.setText(role);
        } else {
            roleChip.setVisibility(View.GONE);
            roleValue.setText(R.string.account_role_none);
        }

        // Photo: custom > Google (loaded from its web link with Glide) > default
        if (customPhoto != null) {
            photoView.setImageBitmap(customPhoto);
        } else if (googlePhoto != null) {
            Glide.with(this).load(googlePhoto)
                    .placeholder(R.drawable.account_img_user_avatar)
                    .error(R.drawable.account_img_user_avatar)
                    .into(photoView);
        } else {
            photoView.setImageResource(R.drawable.account_img_user_avatar);
        }

        // Google accounts have no password in SentryRack
        passwordRow.setEnabled(hasPassword);
        passwordChevron.setVisibility(hasPassword ? View.VISIBLE : View.GONE);
        passwordSummary.setText(hasPassword
                ? R.string.account_password_summary : R.string.account_password_google);

        String roleKey = currentRole >= 0 ? ProfileController.ROLE_KEYS[currentRole] : null;
        ((MainActivity) requireActivity()).showDrawerProfile(currentName, currentEmail, roleKey,
                customPhoto, googlePhoto);
    }

    //--- NAME ---

    private void showNameDialog() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.account_dialog_name, null);
        TextInputEditText input = view.findViewById(R.id.accountNameInput);
        input.setText(currentName);

        openDialog(view, R.string.account_name_title, R.string.user_pw_save, () -> {
            ((TextInputLayout) view.findViewById(R.id.accountNameLayout)).setError(null);
            String name = textOf(input).trim();
            if (controller.changeName(name)) {   // valid: show now, Firebase saves in the background
                currentName = name;
                closeActiveDialog();
                render();
            }
        });
    }

    //--- ROLE ---

    private void showRoleDialog() {
        String[] labels = new String[ProfileController.ROLE_LABELS.length];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = getString(ProfileController.ROLE_LABELS[i]);
        }
        final int[] selected = {currentRole};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.account_role_title)
                .setSingleChoiceItems(labels, currentRole, (dialog, which) -> selected[0] = which)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.user_pw_save, (dialog, which) -> {
                    if (selected[0] < 0) return;
                    currentRole = selected[0];
                    render();                         // show now
                    controller.changeRole(selected[0]);
                })
                .show();
    }

    //--- PHOTO ---

    private void showPhotoSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.account_sheet_photo, null);
        sheet.setContentView(view);

        // "Use my Google photo" only for Google accounts that chose another photo
        view.findViewById(R.id.photoOptionGoogle)
                .setVisibility(isGoogleAccount && customPhoto != null ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.photoOptionRemove)
                .setVisibility(customPhoto != null ? View.VISIBLE : View.GONE);

        view.findViewById(R.id.photoOptionGallery).setOnClickListener(v -> {
            sheet.dismiss();
            photoPicker.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
        // Removing the custom photo brings back the Google photo (or the default one)
        View.OnClickListener remove = v -> {
            sheet.dismiss();
            customPhoto = null;
            render();                 // show now
            controller.removePhoto();
        };
        view.findViewById(R.id.photoOptionGoogle).setOnClickListener(remove);
        view.findViewById(R.id.photoOptionRemove).setOnClickListener(remove);
        sheet.show();
    }

    private void onPhotoPicked(@Nullable Uri uri) {
        if (uri == null || !isAdded()) return;   // user closed the picker
        try {
            ImageDecoder.Source source =
                    ImageDecoder.createSource(requireContext().getContentResolver(), uri);
            Bitmap bitmap = ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);   // needed to resize it
                int longest = Math.max(info.getSize().getWidth(), info.getSize().getHeight());
                if (longest > MAX_PICK_SIZE) {
                    decoder.setTargetSampleSize(longest / MAX_PICK_SIZE);
                }
            });
            customPhoto = bitmap;
            render();                           // show it straight away
            controller.changePhoto(bitmap);
        } catch (IOException e) {
            showMessage(getString(R.string.account_error, e.getMessage()));
        }
    }

    //--- PASSWORD ---

    private void showPasswordDialog() {
        if (!hasPassword) return;
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.account_dialog_change_password, null);

        openDialog(view, R.string.user_pw_title, R.string.user_pw_save, () -> {
            clearErrors(view, R.id.currentPasswordLayout, R.id.newPasswordLayout,
                    R.id.confirmPasswordLayout);
            controller.changePassword(
                    textOf(view.findViewById(R.id.currentPasswordInput)),
                    textOf(view.findViewById(R.id.newPasswordInput)),
                    textOf(view.findViewById(R.id.confirmPasswordInput)));
        });
    }

    //--- DELETE ACCOUNT ---

    private void showDeleteDialog() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.account_dialog_delete, null);
        boolean needsPassword = controller.needsPasswordToDelete();
        view.findViewById(R.id.accountDeletePasswordLayout)
                .setVisibility(needsPassword ? View.VISIBLE : View.GONE);

        openDialog(view, R.string.account_delete_title, R.string.account_delete_confirm, () -> {
            clearErrors(view, R.id.accountDeletePasswordLayout);
            controller.deleteAccount(textOf(view.findViewById(R.id.accountDeletePasswordInput)));
        });
    }

    @Override
    public void onAccountDeleted() {
        if (!isAdded()) return;
        closeActiveDialog();
        Toast.makeText(requireContext(), R.string.account_deleted, Toast.LENGTH_LONG).show();
        ((MainActivity) requireActivity()).goToLogin();   // same path as Log out
    }

    @Override
    public void onReloginRequired() {
        closeActiveDialog();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.account_delete_title)
                .setMessage(R.string.account_relogin)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    //--- RESULTS FROM THE CONTROLLER ---

    @Override
    public void onFieldError(ProfileController.Field field, @StringRes int errorResId) {
        if (activeDialogView == null) return;
        int layoutId;
        switch (field) {
            case NAME:             layoutId = R.id.accountNameLayout; break;
            case CURRENT_PASSWORD: layoutId = R.id.currentPasswordLayout; break;
            case NEW_PASSWORD:     layoutId = R.id.newPasswordLayout; break;
            case CONFIRM_PASSWORD: layoutId = R.id.confirmPasswordLayout; break;
            default:               layoutId = R.id.accountDeletePasswordLayout; break;
        }
        TextInputLayout layout = activeDialogView.findViewById(layoutId);
        if (layout != null) layout.setError(getString(errorResId));
    }

    @Override
    public void onSaved(@StringRes int messageResId) {
        if (!isAdded()) return;
        closeActiveDialog();                 // e.g. Change password waits for Firebase
        showMessage(getString(messageResId));
    }

    @Override
    public void onError(String message) {
        if (!isAdded()) return;
        showMessage(getString(R.string.account_error, message));
        controller.load();                   // Firebase refused: show the saved profile again
    }

    //--- SMALL HELPERS (DRY) ---

    /** Dialog whose main button runs the action but stays open until Firebase answers. */
    private void openDialog(View view, @StringRes int title, @StringRes int button, Runnable action) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(button, null)   // click set below, so the dialog does not close
                .create();
        dialog.setOnDismissListener(d -> {
            if (activeDialog == dialog) {
                activeDialog = null;
                activeDialogView = null;
            }
        });
        dialog.show();
        activeDialog = dialog;
        activeDialogView = view;
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> action.run());
    }

    private void closeActiveDialog() {
        if (activeDialog != null) activeDialog.dismiss();
    }

    private static void clearErrors(View root, int... layoutIds) {
        for (int id : layoutIds) {
            ((TextInputLayout) root.findViewById(id)).setError(null);
        }
    }

    private void showMessage(String message) {
        if (getView() != null) Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
    }

    private static String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}
