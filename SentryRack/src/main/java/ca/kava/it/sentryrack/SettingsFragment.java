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
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.Map;

import ca.kava.it.sentryrack.controller.AuthController;
import ca.kava.it.sentryrack.model.SensorTemplateRepository;

public class SettingsFragment extends Fragment {

    // Settings are saved in Shared Preference (a small private file on the phone)
    private static final String PREFS_NAME = "settings_user_prefs";
    private static final String KEY_LOCK_PORTRAIT = "lock_portrait";
    private static final String KEY_DARK_MODE = "dark_mode";
    private final TextInputLayout[] templateLayouts = new TextInputLayout[6];
    private final TextInputEditText[] templateInputs = new TextInputEditText[6];
    private MaterialSwitch templateLeakSwitch;
    private MaterialSwitch templateDoorSwitch;
    private MaterialButton templateSaveButton;
    private SensorTemplateRepository templateRepository;

    public SettingsFragment() {
        // Required empty public constructor
    }

    // Loads the settings layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        setupPortraitLock(view);
        setupDarkMode(view);   // Dark theme switch
        setupSensorTemplate(view);

        return view;
    }

    private void setupSensorTemplate(View root) {
        int[] layoutIds = {
                R.id.sensorTemplateCoolantLayout,
                R.id.sensorTemplateCurrentLayout,
                R.id.sensorTemplatePowerLayout,
                R.id.sensorTemplateAqiLayout,
                R.id.sensorTemplateTvocLayout,
                R.id.sensorTemplateEco2Layout
        };
        int[] inputIds = {
                R.id.sensorTemplateCoolantInput,
                R.id.sensorTemplateCurrentInput,
                R.id.sensorTemplatePowerInput,
                R.id.sensorTemplateAqiInput,
                R.id.sensorTemplateTvocInput,
                R.id.sensorTemplateEco2Input
        };
        for (int i = 0; i < templateInputs.length; i++) {
            templateLayouts[i] = root.findViewById(layoutIds[i]);
            templateInputs[i] = root.findViewById(inputIds[i]);
        }

        templateLeakSwitch = root.findViewById(R.id.sensorTemplateLeakSwitch);
        templateDoorSwitch = root.findViewById(R.id.sensorTemplateDoorSwitch);
        templateSaveButton = root.findViewById(R.id.sensorTemplateSaveButton);
        templateRepository = new SensorTemplateRepository(new SensorTemplateRepository.Callback() {
            @Override
            public void onLoaded(DataSnapshot template) {
                if (!isAdded()) return;
                setInput(0, template.child("coolantLevel").child("value"));
                setInput(1, template.child("current").child("value"));
                setInput(2, template.child("current").child("powerW"));
                setInput(3, template.child("airQuality").child("aqi"));
                setInput(4, template.child("airQuality").child("tvoc"));
                setInput(5, template.child("airQuality").child("eco2"));
                templateLeakSwitch.setChecked(Boolean.TRUE.equals(
                        template.child("coolantLevel").child("leak").getValue(Boolean.class)));
                templateDoorSwitch.setChecked(Boolean.TRUE.equals(
                        template.child("door").child("open").getValue(Boolean.class)));
                templateSaveButton.setEnabled(true);
            }

            @Override
            public void onSaved() {
                if (!isAdded()) return;
                templateSaveButton.setEnabled(true);
                Toast.makeText(requireContext(), R.string.sensor_template_saved,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                templateSaveButton.setEnabled(true);
                Toast.makeText(requireContext(),
                        getString(R.string.sensor_template_save_failed, message),
                        Toast.LENGTH_LONG).show();
            }
        });
        templateSaveButton.setOnClickListener(v -> saveSensorTemplate());

        if (new AuthController().getCurrentUser() == null) {
            templateSaveButton.setEnabled(false);
            Toast.makeText(requireContext(), R.string.register_rack_login_required,
                    Toast.LENGTH_SHORT).show();
        } else {
            templateSaveButton.setEnabled(false);
            templateRepository.load();
        }
    }

    private void setInput(int index, DataSnapshot value) {
        Object raw = value.getValue();
        templateInputs[index].setText(raw == null ? "" : String.valueOf(raw));
    }

    private void saveSensorTemplate() {
        clearTemplateErrors();
        if (new AuthController().getCurrentUser() == null) {
            Toast.makeText(requireContext(), R.string.register_rack_login_required,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Double coolant = readDecimal(0);
        Double current = readDecimal(1);
        Double power = readDecimal(2);
        Integer aqi = readInteger(3);
        Integer tvoc = readInteger(4);
        Integer eco2 = readInteger(5);
        if (coolant == null || current == null || power == null
                || aqi == null || tvoc == null || eco2 == null) return;

        Map<String, Object> coolantValues = new HashMap<>();
        coolantValues.put("value", coolant);
        coolantValues.put("leak", templateLeakSwitch.isChecked());

        Map<String, Object> currentValues = new HashMap<>();
        currentValues.put("value", current);
        currentValues.put("powerW", power);

        Map<String, Object> doorValues = new HashMap<>();
        doorValues.put("open", templateDoorSwitch.isChecked());

        Map<String, Object> airQualityValues = new HashMap<>();
        airQualityValues.put("aqi", aqi);
        airQualityValues.put("tvoc", tvoc);
        airQualityValues.put("eco2", eco2);

        Map<String, Object> template = new HashMap<>();
        template.put("coolantLevel", coolantValues);
        template.put("current", currentValues);
        template.put("door", doorValues);
        template.put("airQuality", airQualityValues);

        templateSaveButton.setEnabled(false);
        templateRepository.save(template);
    }

    private Double readDecimal(int index) {
        String text = inputText(index);
        try {
            double value = Double.parseDouble(text);
            if (Double.isFinite(value) && value >= 0) return value;
        } catch (NumberFormatException ignored) {
        }
        templateLayouts[index].setError(getString(R.string.sensor_template_invalid_number));
        return null;
    }

    private Integer readInteger(int index) {
        String text = inputText(index);
        try {
            int value = Integer.parseInt(text);
            if (value >= 0) return value;
        } catch (NumberFormatException ignored) {
        }
        templateLayouts[index].setError(getString(R.string.sensor_template_invalid_integer));
        return null;
    }

    private String inputText(int index) {
        return templateInputs[index].getText() == null
                ? "" : templateInputs[index].getText().toString().trim();
    }

    private void clearTemplateErrors() {
        for (TextInputLayout layout : templateLayouts) {
            layout.setError(null);
        }
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

    //--- DARK THEME FUNCTIONALITY ---
    private void setupDarkMode(View root) {
        MaterialSwitch darkSwitch = root.findViewById(R.id.switchDarkMode);

        // Show the current theme first (before adding the listener)
        darkSwitch.setChecked(
                // Is dark mode on
                getPrefs(requireContext()).getBoolean(KEY_DARK_MODE,false)
        );
        darkSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            // Save dark mode using shared preferences
            getPrefs(requireContext()).edit().putBoolean(KEY_DARK_MODE, isChecked).apply();

            // Android redraws every open screen with the new colours
            AppCompatDelegate.setDefaultNightMode(isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        });
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