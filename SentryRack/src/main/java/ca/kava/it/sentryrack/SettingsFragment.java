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

import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import android.graphics.Typeface;
import androidx.core.content.res.ResourcesCompat;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.VibratorManager;
import android.provider.Settings;
import androidx.core.text.HtmlCompat;

public class SettingsFragment extends Fragment {

    // Settings are saved in Shared Preference (a small private file on the phone)
    private static final String PREFS_NAME = "settings_user_prefs";
    private static final String KEY_LOCK_PORTRAIT = "lock_portrait";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_TEXT_SIZE = "text_size";
    private static final int[] THEME_OPTIONS = {R.string.theme_system, R.string.theme_light, R.string.theme_dark};
    private static final int[] TEXT_SIZE_OPTIONS = {R.string.text_small, R.string.text_normal, R.string.text_large};
    private static final float[] TEXT_SCALES = {0.85f, 1.0f, 1.15f};
    // Profile avatar colours (same order everywhere) - @author Anvesh @date 2026-11-02
    private static final String KEY_AVATAR = "avatar_colour";
    private static final int[] AVATAR_NAMES = {R.string.avatar_blue, R.string.avatar_pink,
            R.string.avatar_green, R.string.avatar_orange, R.string.avatar_purple, R.string.avatar_teal};
    private static final int[] AVATAR_BG = {R.color.avatar_bg_blue, R.color.avatar_bg_pink,
            R.color.avatar_bg_green, R.color.avatar_bg_orange, R.color.avatar_bg_purple, R.color.avatar_bg_teal};
    private static final int[] AVATAR_FG = {R.color.avatar_fg_blue, R.color.avatar_fg_pink,
            R.color.avatar_fg_green, R.color.avatar_fg_orange, R.color.avatar_fg_purple, R.color.avatar_fg_teal};
    // Font style - @author Anvesh @date 2026-11-04
    private static final String KEY_FONT = "font_style";        // 0 Default, 1 Rounded, 2 Serif
    private static final int[] FONT_OPTIONS = {R.string.font_default, R.string.font_rounded, R.string.font_serif};
    // App icon (same order as the 3 activity-aliases in the Manifest) - @author Anvesh @date 2026-11-04
    private static final String KEY_APP_ICON = "app_icon";
    private static final int[] ICON_NAMES = {R.string.icon_blue, R.string.icon_dark, R.string.icon_gold};
    private static final int[] ICON_PREVIEWS = {R.mipmap.homescreen_icon_light_round,
            R.mipmap.homescreen_icon_dark_round, R.mipmap.homescreen_icon_gold_round};
    private static final String[] ICON_ALIASES = {".IconBlue", ".IconDark", ".IconGold"};
    // Notifications - @author Anvesh @date 2026-11-04
    private static final String KEY_SOUND = "notification_sound";   // 0 Default, 1 Chime, 2 Alarm, 3 Silent
    private static final String KEY_VIBRATION = "vibration";
    private static final int[] SOUND_OPTIONS = {R.string.sound_default, R.string.sound_chime, R.string.sound_alarm, R.string.sound_silent};
    private static final long PREVIEW_MS = 2500;
    private static final long VIBRATE_MS = 200;
    // Sound preview (stopped after a few seconds or when the screen closes)
    private Ringtone previewRingtone;
    private final Handler previewHandler = new Handler(Looper.getMainLooper());
    public SettingsFragment() {
        // Required empty public constructor
    }

    // Loads the settings layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        setupPortraitLock(view);
        setupTheme(view);
        setupTextSize(view);
        setupProfileCard(view);   // Profile card
        setupAvatar(view);
        setupFont(view);   // Font style
        setupAppIcon(view);
        setupNotificationSound(view);   // Notification sound
        setupVibration(view);// Vibration
        setupAbout(view);   // About
        setupReset(view);
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

    private void setupTheme(View root) {
        showValue(root, R.id.valueTheme, THEME_OPTIONS, getSavedIndex(requireContext(), KEY_THEME));

        // No dialog message here: a message would hide the list of options
        root.findViewById(R.id.rowTheme).setOnClickListener(v ->
                showChoiceDialog(root, R.string.setting_theme, 0,
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
    //--- PROFILE CARD ---

    private void setupProfileCard(View root) {
        TextView email = root.findViewById(R.id.profileEmail);
        email.setText(AccountFragment.getDisplayEmail(requireContext()));
        applyAvatar(root.findViewById(R.id.profileAvatar));

        root.findViewById(R.id.cardProfile).setOnClickListener(v ->
                ((MainActivity) requireActivity()).openScreen(new AccountFragment()));
    }

    //--- PROFILE AVATAR ---

    private void setupAvatar(View root) {
        refreshAvatar(root);
        root.findViewById(R.id.rowAvatar).setOnClickListener(v -> showAvatarDialog(root));
    }

    private void refreshAvatar(View root) {
        applyAvatar(root.findViewById(R.id.profileAvatar));
        applyAvatar(root.findViewById(R.id.imgAvatarPreview));
        showValue(root, R.id.valueAvatar, AVATAR_NAMES, getSavedIndex(requireContext(), KEY_AVATAR));
    }


    private void showAvatarDialog(View root) {
        final int[] selected = {getSavedIndex(requireContext(), KEY_AVATAR)};
        int size = getResources().getDimensionPixelSize(R.dimen.settings_avatar_choice);
        int pad = getResources().getDimensionPixelSize(R.dimen.settings_avatar_choice_padding);
        int gap = getResources().getDimensionPixelSize(R.dimen.settings_padding);

        GridLayout grid = new GridLayout(requireContext());
        grid.setColumnCount(3);
        ImageView[] choices = new ImageView[AVATAR_NAMES.length];

        for (int i = 0; i < choices.length; i++) {
            ImageView choice = new ImageView(requireContext());
            styleAvatar(choice, i);
            choice.setPadding(pad, pad, pad, pad);
            choice.setContentDescription(getString(AVATAR_NAMES[i]));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(gap, gap, gap, gap);
            choice.setLayoutParams(params);

            final int index = i;
            choice.setOnClickListener(v -> {
                selected[0] = index;
                markSelected(choices, index);
            });
            choices[i] = choice;
            grid.addView(choice);
        }
        markSelected(choices, selected[0]);

        // Centre the grid inside the dialog
        FrameLayout wrapper = new FrameLayout(requireContext());
        wrapper.addView(grid, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.setting_avatar)
                .setMessage(R.string.setting_avatar_msg)
                .setView(wrapper)
                .setNegativeButton(R.string.setting_cancel, null)
                .setPositiveButton(R.string.setting_save, (dialog, which) -> {
                    getPrefs(requireContext()).edit().putInt(KEY_AVATAR, selected[0]).apply();
                    refreshAvatar(root);
                })
                .show();
    }

    private void markSelected(ImageView[] choices, int index) {
        for (int i = 0; i < choices.length; i++) {
            choices[i].setForeground(i == index
                    ? ContextCompat.getDrawable(requireContext(), R.drawable.settings_avatar_ring)
                    : null);
        }
    }

    private static void styleAvatar(ImageView image, int index) {
        Context context = image.getContext();
        image.setBackgroundResource(R.drawable.settings_avatar_bg);
        image.setImageResource(R.drawable.settings_ic_person);
        image.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, AVATAR_BG[index])));
        image.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, AVATAR_FG[index])));
    }
    public static void applyAvatar(ImageView image) {
        styleAvatar(image, getSavedIndex(image.getContext(), KEY_AVATAR));
    }
    //--- FONT STYLE ---

    //Font style row: reuses the choice dialog, then redraws the screen with the new font

    private void setupFont(View root) {
        showValue(root, R.id.valueFont, FONT_OPTIONS, getSavedIndex(requireContext(), KEY_FONT));

        root.findViewById(R.id.rowFont).setOnClickListener(v ->
                showChoiceDialog(root, R.string.setting_font, 0,
                        FONT_OPTIONS, KEY_FONT, R.id.valueFont, null,
                        index -> requireActivity().recreate()));
    }

    //The font the user picked, or null for the phone's normal font
    public static Typeface getAppTypeface(Context context) {
        int index = getSavedIndex(context, KEY_FONT);
        if (index == 1) {
            try {
                return ResourcesCompat.getFont(context, R.font.nunito);
            } catch (Exception e) {
                return null;   // font missing -> keep the normal font, never crash
            }
        } else if (index == 2) {
            return Typeface.SERIF;
        }
        return null;
    }
    public static void applyFont(View view, Typeface font) {
        if (font == null || view == null) {
            return;
        }
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            int style = text.getTypeface() != null ? text.getTypeface().getStyle() : Typeface.NORMAL;
            text.setTypeface(font, style);
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyFont(group.getChildAt(i), font);
            }
        }
    }
    //--- APP ICON ---

    // App icon row: shows the current icon + name, opens the picker sheet
    private void setupAppIcon(View root) {
        refreshAppIconRow(root);
        root.findViewById(R.id.rowAppIcon).setOnClickListener(v -> showAppIconSheet(root));
    }
    // Puts the current icon picture and name on the row
    private void refreshAppIconRow(View root) {
        int index = getSavedIndex(requireContext(), KEY_APP_ICON);
        ImageView preview = root.findViewById(R.id.imgAppIconPreview);
        preview.setImageResource(ICON_PREVIEWS[index]);
        showValue(root, R.id.valueAppIcon, ICON_NAMES, index);
    }
    // Bottom sheet with the 3 icons. The current one is highlighted with "Current".Tapping another one switches the home-screen icon.
    private void showAppIconSheet(View root) {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.settings_sheet_app_icon, null);
        sheet.setContentView(sheetView);

        int[] optionIds = {R.id.iconOptionBlue, R.id.iconOptionDark, R.id.iconOptionGold};
        int[] currentIds = {R.id.iconCurrentBlue, R.id.iconCurrentDark, R.id.iconCurrentGold};
        int current = getSavedIndex(requireContext(), KEY_APP_ICON);

        for (int i = 0; i < optionIds.length; i++) {
            View option = sheetView.findViewById(optionIds[i]);
            boolean isCurrent = (i == current);
            option.setBackgroundResource(isCurrent ? R.drawable.settings_icon_option_selected : 0);
            sheetView.findViewById(currentIds[i]).setVisibility(isCurrent ? View.VISIBLE : View.INVISIBLE);

            final int index = i;
            option.setOnClickListener(v -> {
                if (index != current) {
                    getPrefs(requireContext()).edit().putInt(KEY_APP_ICON, index).apply();
                    //setAppIcon(requireContext(), index);
                    refreshAppIconRow(root);
                    Toast.makeText(requireContext(),
                            getString(R.string.setting_app_icon_changed, getString(ICON_NAMES[index])),
                            Toast.LENGTH_LONG).show();
                }
                sheet.dismiss();
            });
        }
        sheet.show();
    }

    // Switches the home-screen icon: turns ON the chosen alias, then turns the others OFF(in that order, so there is never a moment with no icon).Also used by "Reset all settings" to go back to Blue.

    static void setAppIcon(Context context, int index) {
        PackageManager pm = context.getPackageManager();
        String pkg = context.getPackageName();

        pm.setComponentEnabledSetting(new ComponentName(context, pkg + ICON_ALIASES[index]),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        for (int i = 0; i < ICON_ALIASES.length; i++) {
            if (i != index) {
                pm.setComponentEnabledSetting(new ComponentName(context, pkg + ICON_ALIASES[i]),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
        }
    }
    /** Switches the home-screen icon to the saved choice, but only if it changed.
     * Called when the user LEAVES the app (MainActivity.onStop), because Androidcloses the app if its own launcher icon is switched while it is open.
     */
    public static void applySavedAppIcon(Context context) {
        int index = getSavedIndex(context, KEY_APP_ICON);
        PackageManager pm = context.getPackageManager();
        ComponentName chosen = new ComponentName(context, context.getPackageName() + ICON_ALIASES[index]);
        int state = pm.getComponentEnabledSetting(chosen);

        // Blue is ON by default in the Manifest, Dark and Gold are OFF
        boolean alreadyOn = state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                || (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && index == 0);
        if (!alreadyOn) {
            setAppIcon(context, index);
        }
    }
    //--- NOTIFICATION SOUND ---
    //Sound row: reuses the choice dialog. Every tap plays a short preview,OK saves the choice, the preview always stops by itself.
    private void setupNotificationSound(View root) {
        showValue(root, R.id.valueNotifSound, SOUND_OPTIONS, getSavedIndex(requireContext(), KEY_SOUND));

        root.findViewById(R.id.rowNotifSound).setOnClickListener(v ->
                showChoiceDialog(root, R.string.setting_notif_sound, 0,
                        SOUND_OPTIONS, KEY_SOUND, R.id.valueNotifSound,
                        this::previewSound,          // plays on every tap
                        index -> stopPreview()));    // OK pressed
    }

    // Plays a short preview of one sound option
    private void previewSound(int index) {
        stopPreview();
        previewRingtone = playSound(requireContext(), index);
        previewHandler.postDelayed(this::stopPreview, PREVIEW_MS);
    }

    // Stops the preview sound if it is still playing
    private void stopPreview() {
        previewHandler.removeCallbacksAndMessages(null);
        if (previewRingtone != null && previewRingtone.isPlaying()) {
            previewRingtone.stop();
        }
        previewRingtone = null;
    }

    // Plays one of the 4 sounds. Default and Alarm use the phone's own sounds,Chime is a short built-in tone, Silent plays nothing.Returns the Ringtone so it can be stopped (null for Chime / Silent).
    static Ringtone playSound(Context context, int index) {
        if (index == 3) {
            return null;                                   // Silent
        }
        if (index == 1) {                                  // Chime
            ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90);
            tone.startTone(ToneGenerator.TONE_PROP_ACK, 500);
            new Handler(Looper.getMainLooper()).postDelayed(tone::release, 700);
            return null;
        }
        Uri uri = (index == 2) ? Settings.System.DEFAULT_ALARM_ALERT_URI
                : Settings.System.DEFAULT_NOTIFICATION_URI;
        Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
        if (ringtone != null) {                            // some emulators have no sounds
            ringtone.play();
        }
        return ringtone;
    }

    //--- VIBRATION ---

    // Vibration switch: reuses bindSwitch, gives a short buzz when turned ON
    private void setupVibration(View root) {
        bindSwitch(root, R.id.switchVibration, KEY_VIBRATION, true, isOn -> {
            if (isOn) {
                vibrate(requireContext());
            }
        });
    }

    // One short vibration (uses the phone's VibratorManager, API 31+)
    static void vibrate(Context context) {
        VibratorManager manager =
                (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        if (manager != null) {
            manager.getDefaultVibrator().vibrate(
                    VibrationEffect.createOneShot(VIBRATE_MS, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    // For alerts anywhere in the app: plays the chosen sound and vibrates if allowed.e.g. SettingsFragment.playAlert(this); when a rack alert arrives.

    public static void playAlert(Context context) {
        playSound(context, getSavedIndex(context, KEY_SOUND));
        if (getPrefs(context).getBoolean(KEY_VIBRATION, true)) {
            vibrate(context);
        }
    }

    // Stops the sound preview when the user leaves Settings
    @Override
    public void onDestroyView() {
        stopPreview();
        super.onDestroyView();
    }
    //--- ABOUT ---

    /** About rows: app version (read from the app) and Privacy policy.
     * Help center, Send feedback and Rate were removed (team decision) - they live in the overflow menu.
     * @author Anvesh Rachakonda
     * @date 2026-11-06
     */
    private void setupAbout(View root) {
        MainActivity main = (MainActivity) requireActivity();

        TextView version = root.findViewById(R.id.valueAppVersion);
        version.setText(main.getAppVersion());

        root.findViewById(R.id.rowPrivacy).setOnClickListener(v -> showPrivacyPolicy());
    }

    /**
     * @author Anvesh Rachakonda
     * @date 2026-11-04
     * Red "Reset settings" row: asks first, then resets everything.
     */
    private void setupReset(View root) {
        root.findViewById(R.id.rowReset).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.reset_all_title)
                        .setMessage(R.string.reset_all_msg)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.reset_all_confirm, (d, w) -> resetAllSettings())
                        .show());
    }

    /**
     * Clears settings_user_prefs (login prefs are a different file, so the user
     * stays signed in), applies the defaults and refreshes the screen.
     */
    private void resetAllSettings() {
        Activity activity = requireActivity();
        stopPreview();

        getPrefs(activity).edit().clear().apply();

        Toast.makeText(activity, R.string.reset_all_done, Toast.LENGTH_SHORT).show();

        // App icon goes back to Blue when the app is closed (MainActivity.onStop)
        applySavedOrientation(activity);

        // Theme change recreates the activity by itself; recreate anyway for text size + font
        AppCompatDelegate.setDefaultNightMode(nightModeFor(0));
        activity.recreate();
    }

    //Privacy policy & terms: a simple in-app page (bold headings from the string)

    private void showPrivacyPolicy() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.setting_privacy)
                .setMessage(HtmlCompat.fromHtml(getString(R.string.privacy_text).replace("\n", "<br>"),
                        HtmlCompat.FROM_HTML_MODE_LEGACY))
                .setPositiveButton(R.string.setting_ok, null)
                .show();
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