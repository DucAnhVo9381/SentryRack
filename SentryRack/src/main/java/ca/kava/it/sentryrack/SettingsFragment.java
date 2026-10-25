// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;

/** Settings screen (Deliverable 2 steps 6 and 7).
 * Shows 7 settings; ONE is implemented: "Lock screen to portrait".
 * The other settings are design only for this deliverable.
 */
public class SettingsFragment extends Fragment {

    // Settings are saved in a small private file on the phone
    private static final String PREFS_NAME = "sentryrack_settings";
    private static final String KEY_LOCK_PORTRAIT = "lock_portrait";
    private static final String KEY_DARK_MODE = "dark_mode";

    public SettingsFragment() {
        // Required empty public constructor
    }

    // Loads the settings layout
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    // Connects the settings that work (only "Lock screen to portrait" for now)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupPortraitLock(view);
        setupDarkMode(view);   // Dark theme switch
    }

    /** Step 7: the working setting.
     * Shows the saved value on the switch; when flipped, saves it, shows a message
     * and locks/unlocks the screen rotation immediately.
     */
    private void setupPortraitLock(View root) {
        MaterialSwitch lockSwitch = root.findViewById(R.id.switchLockPortrait);

        // Show the saved choice first (before adding the listener)
        lockSwitch.setChecked(isPortraitLocked(requireContext()));

        lockSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            savePortraitLocked(requireContext(), isChecked);

            Snackbar.make(root,
                    isChecked ? R.string.setting_portrait_locked_msg : R.string.setting_portrait_unlocked_msg,
                    Snackbar.LENGTH_SHORT).show();

            // Applied last: the screen may rotate (and redraw) right after this
            applySavedOrientation(requireActivity());
        });
    }

    // Reads the saved "lock to portrait" choice (false = rotation allowed)
    public static boolean isPortraitLocked(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_LOCK_PORTRAIT, false);
    }

    // Saves the "lock to portrait" choice
    private static void savePortraitLocked(Context context, boolean locked) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_LOCK_PORTRAIT, locked).apply();
    }

    /** Applies the saved choice to any screen (used by Settings, MainActivity and LoginActivity)
     * Locked   -> always portrait
     * Unlocked -> follows the phone's rotation
     */
    public static void applySavedOrientation(Activity activity) {
        activity.setRequestedOrientation(isPortraitLocked(activity)
                ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                : ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    //--- DARK THEME FUNCTIONALITY ---
    private void setupDarkMode(View root) {
        MaterialSwitch darkSwitch = root.findViewById(R.id.switchDarkMode);

        // Show the current theme first (before adding the listener)
        darkSwitch.setChecked(isDarkModeOn(requireContext()));
        darkSwitch.setOnCheckedChangeListener((button, isChecked) -> {

            // Save dark mode using shared preferences
            SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();

            // Android redraws every open screen with the new colours
            AppCompatDelegate.setDefaultNightMode(isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    public static boolean isDarkModeOn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.contains(KEY_DARK_MODE)) {
            return prefs.getBoolean(KEY_DARK_MODE, false);
        }
        int nightMask = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightMask == Configuration.UI_MODE_NIGHT_YES;
    }

    // Applies the saved theme when the app starts (called by LoginActivity and MainActivity).
    // Never changed in Settings -> follows the phone (system default).
    public static void applySavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int mode;
        if (!prefs.contains(KEY_DARK_MODE)) {
            mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        } else if (prefs.getBoolean(KEY_DARK_MODE, false)) {
            mode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            mode = AppCompatDelegate.MODE_NIGHT_NO;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}