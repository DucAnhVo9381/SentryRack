// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ca.kava.it.sentryrack.controller.AuthController;
import ca.kava.it.sentryrack.controller.FeedbackController;
import ca.kava.it.sentryrack.model.DeviceInfo;
import ca.kava.it.sentryrack.model.User;

/**
 * Customer Feedback screen (Deliverable 4 #30, #42-52).
 * View in the team's MVC design: validation and saving are done by FeedbackController
 * (FeedbackRepository writes feedback/{id} to Firebase with the device model).
 *
 * - Errors under each wrong field, nothing is sent (#43)
 * - Progress overlay for at least 5 seconds while sending (#44)
 * - AlertDialog with OK only after Firebase confirms the save (#45)
 * - Device model read in code, not on the form (#46)
 * - Form cleared, one feedback per 24 hours, greyed button with h/min countdown (#50, #51)
 *
 * @author Anvesh Rachakonda
 * @date 2026-11-16
 */
public class FeedbackFragment extends Fragment implements FeedbackController.Callback {

    private static final long MIN_SENDING_MS = 5_000;              // D4 #44: 5-second delay
    private static final long LIMIT_MS = 24L * 60 * 60 * 1000;     // D4 #50: once per 24 hours
    private static final long TIMER_TICK_MS = 30_000;              // countdown refresh
    private static final int PHONE_DIGITS = 10;
    private static final int[] RATING_LABELS = {R.string.fb_rating_1, R.string.fb_rating_2,
            R.string.fb_rating_3, R.string.fb_rating_4, R.string.fb_rating_5};

    // Kept apart from Settings, so "Reset all settings" cannot skip the 24-hour limit
    private static final String PREFS_NAME = "feedback_prefs";
    private static final String KEY_LAST_SENT = "last_sent_";       // + user id

    private TextInputLayout nameLayout, phoneLayout, emailLayout, commentLayout;
    private TextInputEditText nameInput, phoneInput, emailInput, commentInput;
    private RatingBar ratingBar;
    private TextView ratingText, noteText;
    private MaterialButton submitButton;
    private View progressOverlay;
    private LinearProgressIndicator progressBar;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private CountDownTimer limitTimer;

    // A send finishes when BOTH the 5 seconds are over AND Firebase has answered
    private boolean minDelayDone;
    private Boolean saveSucceeded;       // null = Firebase has not answered yet
    private String saveError;
    private int sentStars;

    public FeedbackFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.feedback_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        prefillFromAccount();

        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> showRatingLabel((int) rating));
        submitButton.setOnClickListener(v -> attemptSubmit());

        updateLimitUi();
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        if (limitTimer != null) {
            limitTimer.cancel();
        }
        super.onDestroyView();
    }

    private void bindViews(View root) {
        nameLayout = root.findViewById(R.id.fbNameLayout);
        phoneLayout = root.findViewById(R.id.fbPhoneLayout);
        emailLayout = root.findViewById(R.id.fbEmailLayout);
        commentLayout = root.findViewById(R.id.fbCommentLayout);
        nameInput = root.findViewById(R.id.fbNameInput);
        phoneInput = root.findViewById(R.id.fbPhoneInput);
        emailInput = root.findViewById(R.id.fbEmailInput);
        commentInput = root.findViewById(R.id.fbCommentInput);
        ratingBar = root.findViewById(R.id.fbRatingBar);
        ratingText = root.findViewById(R.id.fbRatingText);
        noteText = root.findViewById(R.id.fbNote);
        submitButton = root.findViewById(R.id.fbSubmitButton);
        progressOverlay = root.findViewById(R.id.fbProgressOverlay);
        progressBar = root.findViewById(R.id.fbProgressBar);
    }

    // Name and email of the signed-in user are filled in (they can still be changed)
    private void prefillFromAccount() {
        User user = new AuthController().getCurrentUser();
        if (user == null) return;
        if (user.getDisplayName() != null) nameInput.setText(user.getDisplayName());
        if (user.getEmail() != null) emailInput.setText(user.getEmail());
    }

    //--- SUBMIT ---

    private void attemptSubmit() {
        clearErrors();

        // Extra rule on top of the controller: exactly 10 digits (D4 #43)
        if (text(phoneInput).length() != PHONE_DIGITS) {
            phoneLayout.setError(getString(R.string.fb_phone_error));
            return;
        }

        sentStars = (int) ratingBar.getRating();
        FeedbackController controller = new FeedbackController(this);
        boolean started = controller.submit(text(nameInput), text(phoneInput), text(emailInput),
                text(commentInput), sentStars, DeviceInfo.from(appVersion()));

        if (started) {
            showSending();
        }
    }

    /** Overlay + progress bar that fills up over 5 seconds. */
    private void showSending() {
        minDelayDone = false;
        saveSucceeded = null;
        saveError = null;

        progressOverlay.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        ObjectAnimator.ofInt(progressBar, "progress", 0, 100)
                .setDuration(MIN_SENDING_MS)
                .start();

        handler.postDelayed(() -> {
            minDelayDone = true;
            finishSendingIfReady();
        }, MIN_SENDING_MS);
    }

    // FeedbackController.Callback - Firebase confirmed the save
    @Override
    public void onSuccess() {
        saveSucceeded = true;
        finishSendingIfReady();
    }

    // FeedbackController.Callback - Firebase refused the save
    @Override
    public void onError(String message) {
        saveSucceeded = false;
        saveError = message;
        finishSendingIfReady();
    }

    private void finishSendingIfReady() {
        if (!isAdded() || !minDelayDone || saveSucceeded == null) return;
        progressOverlay.setVisibility(View.GONE);

        if (saveSucceeded) {
            saveLastSent(System.currentTimeMillis());
            clearForm();
            updateLimitUi();
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.fb_done_title)
                    .setMessage(getString(R.string.fb_done_msg, sentStars))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        } else {
            Toast.makeText(requireContext(), getString(R.string.feedback_failed, saveError),
                    Toast.LENGTH_LONG).show();
        }
    }

    // FeedbackController.Callback - wrong input: show the error, nothing is sent
    @Override
    public void onFieldError(FeedbackController.Field field, int errorResId) {
        String error = getString(errorResId);
        switch (field) {
            case FULL_NAME: nameLayout.setError(error); break;
            case PHONE:     phoneLayout.setError(error); break;
            case EMAIL:     emailLayout.setError(error); break;
            case COMMENT:   commentLayout.setError(error); break;
            case RATING:
                ratingText.setText(error);
                ratingText.setTextColor(ContextCompat.getColor(requireContext(), R.color.fb_error));
                break;
        }
    }

    // FeedbackController.Callback
    @Override
    public void onNotLoggedIn() {
        Toast.makeText(requireContext(), R.string.feedback_login_required, Toast.LENGTH_LONG).show();
    }

    //--- 24-HOUR LIMIT ---

    /** Greys out Submit with "Next feedback in 23 h 59 min" until 24 h have passed. */
    private void updateLimitUi() {
        if (limitTimer != null) {
            limitTimer.cancel();
        }
        long remaining = lastSent() + LIMIT_MS - System.currentTimeMillis();

        if (remaining <= 0) {
            submitButton.setEnabled(true);
            submitButton.setText(R.string.fb_submit);
            noteText.setText(R.string.fb_note_device);
            return;
        }

        submitButton.setEnabled(false);
        noteText.setText(R.string.fb_note_limit);
        showRemaining(remaining);
        limitTimer = new CountDownTimer(remaining, TIMER_TICK_MS) {
            @Override
            public void onTick(long millisLeft) {
                showRemaining(millisLeft);
            }

            @Override
            public void onFinish() {
                if (isAdded()) updateLimitUi();
            }
        }.start();
    }

    private void showRemaining(long millisLeft) {
        long minutes = (millisLeft + 59_999) / 60_000;   // round up: never shows 0 min while locked
        submitButton.setText(getString(R.string.fb_next_in, minutes / 60, minutes % 60));
    }

    private SharedPreferences prefs() {
        return requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // One limit per signed-in user on this phone
    private String limitKey() {
        User user = new AuthController().getCurrentUser();
        return KEY_LAST_SENT + (user == null ? "guest" : user.getUid());
    }

    private long lastSent() {
        return prefs().getLong(limitKey(), 0);
    }

    private void saveLastSent(long time) {
        prefs().edit().putLong(limitKey(), time).apply();
    }

    //--- SMALL HELPERS ---

    private void showRatingLabel(int stars) {
        ratingText.setTextColor(ContextCompat.getColor(requireContext(), R.color.fb_star_text));
        ratingText.setText(stars >= 1 && stars <= 5 ? RATING_LABELS[stars - 1] : R.string.fb_rating_tap);
    }

    private void clearErrors() {
        nameLayout.setError(null);
        phoneLayout.setError(null);
        emailLayout.setError(null);
        commentLayout.setError(null);
        showRatingLabel((int) ratingBar.getRating());
    }

    // D4 #50: clear the user input after a successful submit
    private void clearForm() {
        nameInput.setText("");
        phoneInput.setText("");
        emailInput.setText("");
        commentInput.setText("");
        ratingBar.setRating(0);
        clearErrors();
    }

    private static String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private String appVersion() {
        try {
            return requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0";
        }
    }
}
