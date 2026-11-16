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
import android.widget.TextView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;
import ca.kava.it.sentryrack.model.SensorUnitConverter;

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
        setupSensorTemplate(view);
        setupSensorUnits(view);   // Sensor units
        setupDecimals(view);      // Decimal places

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


    // Saved in SharedPreferences. Firebase always keeps the base unit (mm, A, W, ppb, ppm);
    // only the value SHOWN on screen is converted (SensorUnitConverter).

    private static final String KEY_UNIT_PREFIX = "sensor_unit_";   // + COOLANT, CURRENT, ...
    private static final String KEY_DECIMALS = "decimal_places";
    private static final SensorUnitConverter.Sensor[] UNIT_SENSORS = {
            SensorUnitConverter.Sensor.COOLANT, SensorUnitConverter.Sensor.CURRENT,
            SensorUnitConverter.Sensor.POWER, SensorUnitConverter.Sensor.TVOC,
            SensorUnitConverter.Sensor.ECO2};
    private static final int[] UNIT_ROWS = {R.id.rowUnitCoolant, R.id.rowUnitCurrent,
            R.id.rowUnitPower, R.id.rowUnitTvoc, R.id.rowUnitEco2};
    private static final int[] UNIT_VALUES = {R.id.valueUnitCoolant, R.id.valueUnitCurrent,
            R.id.valueUnitPower, R.id.valueUnitTvoc, R.id.valueUnitEco2};
    private static final int[] UNIT_TITLES = {R.string.setting_unit_coolant, R.string.setting_unit_current,
            R.string.setting_unit_power, R.string.setting_unit_tvoc, R.string.setting_unit_eco2};
    private static final double[] SAMPLE_VALUES = {124.5, 3.2468, 1532.5, 245, 612};
    private static final int SAMPLE_CURRENT = 1;   // decimal places example uses the current reading

    private void setupSensorUnits(View root) {
        for (int i = 0; i < UNIT_SENSORS.length; i++) {
            final int index = i;
            showUnitValue(root, index);
            root.findViewById(UNIT_ROWS[index]).setOnClickListener(v -> showUnitDialog(root, index));
        }
    }

    private void showUnitValue(View root, int index) {
        TextView value = root.findViewById(UNIT_VALUES[index]);
        value.setText(getUnit(requireContext(), UNIT_SENSORS[index]).symbol);
    }

    private void showUnitDialog(View root, int index) {
        Context context = requireContext();
        SensorUnitConverter.Sensor sensor = UNIT_SENSORS[index];
        List<SensorUnitConverter.Unit> units = SensorUnitConverter.unitsFor(sensor);
        int decimals = getDecimals(context);

        String[] labels = new String[units.size()];
        for (int i = 0; i < labels.length; i++) {
            SensorUnitConverter.Unit unit = units.get(i);
            labels[i] = getString(unitNameRes(unit)) + " (" + unit.symbol + ")  ·  "
                    + SensorUnitConverter.format(SAMPLE_VALUES[index], unit, decimals);
        }
        final int[] selected = {units.indexOf(getUnit(context, sensor))};

        new MaterialAlertDialogBuilder(context)
                .setTitle(UNIT_TITLES[index])
                .setSingleChoiceItems(labels, selected[0], (dialog, which) -> selected[0] = which)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    getPrefs(context).edit()
                            .putString(KEY_UNIT_PREFIX + sensor.name(), units.get(selected[0]).name())
                            .apply();
                    showUnitValue(root, index);
                    showDecimalsValue(root);   // its example uses the current unit
                })
                .show();
    }

    /** Decimal places row: 0 to 3 digits after the point. */
    private void setupDecimals(View root) {
        showDecimalsValue(root);
        root.findViewById(R.id.rowDecimals).setOnClickListener(v -> showDecimalsDialog(root));
    }

    private void showDecimalsValue(View root) {
        Context context = requireContext();
        int decimals = getDecimals(context);
        TextView value = root.findViewById(R.id.valueDecimals);
        TextView summary = root.findViewById(R.id.summaryDecimals);
        value.setText(String.valueOf(decimals));
        summary.setText(getString(R.string.setting_decimals_example,
                formatSample(context, SAMPLE_CURRENT, decimals)));
    }

    /** "2  ·  3.25 A" - every option shows how a reading will look. */
    private void showDecimalsDialog(View root) {
        Context context = requireContext();
        int count = SensorUnitConverter.MAX_DECIMALS - SensorUnitConverter.MIN_DECIMALS + 1;
        String[] labels = new String[count];
        for (int i = 0; i < count; i++) {
            int decimals = SensorUnitConverter.MIN_DECIMALS + i;
            labels[i] = decimals + "  ·  " + formatSample(context, SAMPLE_CURRENT, decimals);
        }
        final int[] selected = {getDecimals(context) - SensorUnitConverter.MIN_DECIMALS};

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.setting_decimals)
                .setSingleChoiceItems(labels, selected[0], (dialog, which) -> selected[0] = which)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    getPrefs(context).edit()
                            .putInt(KEY_DECIMALS, SensorUnitConverter.MIN_DECIMALS + selected[0])
                            .apply();
                    showDecimalsValue(root);
                })
                .show();
    }

    // Example reading in the saved unit with the given decimals
    private static String formatSample(Context context, int index, int decimals) {
        return SensorUnitConverter.format(SAMPLE_VALUES[index],
                getUnit(context, UNIT_SENSORS[index]), decimals);
    }

    // Full unit name for the dialog (translated in values-fr)
    private static int unitNameRes(SensorUnitConverter.Unit unit) {
        switch (unit) {
            case CENTIMETRE:  return R.string.unit_centimetre;
            case INCH:        return R.string.unit_inch;
            case AMPERE:      return R.string.unit_ampere;
            case MILLIAMPERE: return R.string.unit_milliampere;
            case WATT:        return R.string.unit_watt;
            case KILOWATT:    return R.string.unit_kilowatt;
            case PPB:         return R.string.unit_ppb;
            case PPM_TVOC:
            case PPM:         return R.string.unit_ppm;
            case PERCENT:     return R.string.unit_percent;
            default:          return R.string.unit_millimetre;
        }
    }

    /** Saved unit for one sensor (default = base unit). Used by the Racks list. */
    public static SensorUnitConverter.Unit getUnit(Context context, SensorUnitConverter.Sensor sensor) {
        return SensorUnitConverter.unitFromName(sensor,
                getPrefs(context).getString(KEY_UNIT_PREFIX + sensor.name(), null));
    }

    /** Saved decimal places (0-3, default 1). */
    public static int getDecimals(Context context) {
        return SensorUnitConverter.clampDecimals(
                getPrefs(context).getInt(KEY_DECIMALS, SensorUnitConverter.DEFAULT_DECIMALS));
    }

    /** A base-unit reading ready for the screen, e.g. 124.5 mm -> "12.45 cm". */
    public static String formatReading(Context context, SensorUnitConverter.Sensor sensor, double baseValue) {
        return SensorUnitConverter.format(baseValue, getUnit(context, sensor), getDecimals(context));
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