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

import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

public class SettingsFragment extends Fragment {

    // Settings are saved in Shared Preference (a small private file on the phone)
    private static final String PREFS_NAME = "settings_user_prefs";
    private static final String KEY_LOCK_PORTRAIT = "lock_portrait";
    private static final String KEY_DARK_MODE = "dark_mode";

    public SettingsFragment() {
        // Required empty public constructor
    }

    // Loads the settings layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        setupPortraitLock(view);

        return view;
    }

    //--- LOCK SCREEN LAYOUT FUNCTIONALITY ---
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

            showToast(isChecked ? R.string.setting_portrait_locked_msg : R.string.setting_portrait_unlocked_msg);

            // Applied last: the screen may rotate (and redraw) right after this
            applySavedOrientation(requireActivity());
        });
    }

    // Reads the saved "lock to portrait" choice (false = rotation allowed)
    public static boolean isPortraitLocked(Context context) {
        return getPrefs(context).getBoolean(KEY_LOCK_PORTRAIT, false);
    }

    // Saves the "lock to portrait" choice
    private static void savePortraitLocked(Context context, boolean locked) {
        getPrefs(context).edit().putBoolean(KEY_LOCK_PORTRAIT, locked).apply();
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


    // Applies the saved theme when the app starts (called by LoginActivity and MainActivity).
    // Never changed in Settings -> follows the phone (system default).
    public static void applySavedTheme(Context context) {
        boolean forceDark = getPrefs(context).getBoolean(KEY_DARK_MODE,false);

        AppCompatDelegate.setDefaultNightMode(
                forceDark
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        );
    }

    //--- UTILITY/REUSABLE METHODS ---
    private void showToast(int messageId) {
        Toast.makeText(requireContext(),messageId,Toast.LENGTH_LONG).show();
    }

    // Returns the app's SharedPreferences file used to store user settings
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
    }
}