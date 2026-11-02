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

import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends Fragment {

    // Settings are saved in Shared Preference (a small private file on the phone)
    private static final String PREFS_NAME = "settings_user_prefs";
    private static final String KEY_LOCK_PORTRAIT = "lock_portrait";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_TEXT_SIZE = "text_size";
    private static final int[] THEME_OPTIONS = {R.string.theme_system, R.string.theme_light, R.string.theme_dark};
    private static final int[] TEXT_SIZE_OPTIONS = {R.string.text_small, R.string.text_normal, R.string.text_large};
    private static final float[] TEXT_SCALES = {0.85f, 1.0f, 1.15f};

    public SettingsFragment() {
        // Required empty public constructor
    }

    // Loads the settings layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        setupPortraitLock(view);
        setupKeepScreenOn(view);
        setupTheme(view);
        setupTextSize(view);

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
    public static void applySavedOrientation(Activity activity) {
        activity.setRequestedOrientation(isPortraitLocked(activity)
                ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                : ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }


    // Applies the saved theme when the app starts (called by LoginActivity and MainActivity).
    // Never changed in Settings -> follows the phone (system default).
    public static void applySavedTheme(Context context) {
        SharedPreferences prefs = getPrefs(context);
        int index = prefs.contains(KEY_THEME)
                ? prefs.getInt(KEY_THEME, 0)
                : (prefs.getBoolean(KEY_DARK_MODE, false) ? 2 : 0);
        AppCompatDelegate.setDefaultNightMode(nightModeFor(index));
    }
    //--- REUSABLE SETTING HELPERS (DRY principle + Observer pattern) ---

    interface SwitchListener {
        void onChanged(boolean isOn);
    }

    interface ChoiceListener {
        void onChosen(int index);
    }
    private void bindSwitch(View root, int switchId, String key, boolean defaultValue,
                            @NonNull SwitchListener listener) {
        MaterialSwitch settingSwitch = root.findViewById(switchId);

        // Show the saved value first (before adding the listener)
        settingSwitch.setChecked(getPrefs(requireContext()).getBoolean(key, defaultValue));

        settingSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            getPrefs(requireContext()).edit().putBoolean(key, isChecked).apply();
            listener.onChanged(isChecked);
        });
    }
    private void showChoiceDialog(View root, @StringRes int titleRes, @StringRes int messageRes,
                                  @StringRes int[] options, String key, int valueTextId,
                                  ChoiceListener previewListener, @NonNull ChoiceListener listener) {
        String[] labels = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            labels[i] = getString(options[i]);
        }
        final int[] selected = {getSavedIndex(requireContext(), key)};

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(titleRes)
                .setSingleChoiceItems(labels, selected[0], (dialog, which) -> {
                    selected[0] = which;
                    if (previewListener != null) {
                        previewListener.onChosen(which);
                    }
                })
                .setNegativeButton(R.string.setting_cancel, null)
                .setPositiveButton(R.string.setting_ok, (dialog, which) -> {
                    getPrefs(requireContext()).edit().putInt(key, selected[0]).apply();
                    showValue(root, valueTextId, options, selected[0]);
                    listener.onChosen(selected[0]);
                });

        // Small explanation under the title (0 = no message)
        if (messageRes != 0) {
            builder.setMessage(messageRes);
        }
        builder.show();
    }
    static int getSavedIndex(Context context, String key) {
        return getPrefs(context).getInt(key, KEY_TEXT_SIZE.equals(key) ? 1 : 0);
    }

    private void showValue(View root, int valueTextId, @StringRes int[] options, int index) {
        TextView valueText = root.findViewById(valueTextId);
        valueText.setText(options[index]);
    }

    private void setupKeepScreenOn(View root) {
        bindSwitch(root, R.id.switchKeepScreenOn, KEY_KEEP_SCREEN_ON, false,
                isOn -> applyKeepScreenOn(requireActivity()));
    }
    public static void applyKeepScreenOn(Activity activity) {
        if (getPrefs(activity).getBoolean(KEY_KEEP_SCREEN_ON, false)) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
    private void setupTheme(View root) {
        showValue(root, R.id.valueTheme, THEME_OPTIONS, getSavedIndex(requireContext(), KEY_THEME));

        root.findViewById(R.id.rowTheme).setOnClickListener(v ->
                showChoiceDialog(root, R.string.setting_theme, R.string.setting_theme_msg,
                        THEME_OPTIONS, KEY_THEME, R.id.valueTheme, null,
                        index -> AppCompatDelegate.setDefaultNightMode(nightModeFor(index))));
    }

    private static int nightModeFor(int index) {
        if (index == 1) {
            return AppCompatDelegate.MODE_NIGHT_NO;
        } else if (index == 2) {
            return AppCompatDelegate.MODE_NIGHT_YES;
        }
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }

    private void setupTextSize(View root) {
        showValue(root, R.id.valueTextSize, TEXT_SIZE_OPTIONS, getSavedIndex(requireContext(), KEY_TEXT_SIZE));

        root.findViewById(R.id.rowTextSize).setOnClickListener(v ->
                showChoiceDialog(root, R.string.setting_text_size, 0,
                        TEXT_SIZE_OPTIONS, KEY_TEXT_SIZE, R.id.valueTextSize, null,
                        index -> requireActivity().recreate()));
    }
    public static float getTextScale(Context context) {
        return TEXT_SCALES[getSavedIndex(context, KEY_TEXT_SIZE)];
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