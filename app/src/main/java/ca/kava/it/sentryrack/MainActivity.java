// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.snackbar.Snackbar;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.EditText;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import androidx.core.view.MenuCompat;

public class MainActivity extends AppCompatActivity {
    // Drawer and bottom bar are fields so every method can use them
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;

    // Runtime permission variables
    private FusedLocationProviderClient fusedLocationClient;
    private int locationPermissionDenied = 0;

    private static final String CHANNEL_ID = "location_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final String FULL_NAME = "Kava";

    // Create fragments


    // Redirect user to settings if location permisison denied > 2
    // Redirect user to settings if location permission denied > 2
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fineGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                boolean coarseGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                if (fineGranted || coarseGranted) {
                    locationPermissionDenied = 0;
                    fetchDeviceLocation();
                } else {
                    locationPermissionDenied++;
                    if (locationPermissionDenied > 2) {
                        showSnackbar(getString(R.string.permission_denied_opening_settings), getString(R.string.settings), v -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                            startActivity(intent);
                        });
                    } else {
                        showSnackbar(getString(R.string.location_permission_denied), getString(R.string.dismiss), null);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Settings > "Lock screen to portrait" - apply the saved choice (Anvesh, 2026-10-01)
        SettingsFragment.applySavedOrientation(this);

        // Splash screen (Volk) moved to LoginActivity.setupSplashScreen() so it shows
        // before the Login screen (Deliverable 2 step 11) - Anvesh, 2026-10-01

        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();

        // Setup toolbar
        // The toolbar lives inside the layout (not the window), so the drawer can slide over it
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup navigation drawer
        drawerLayout = findViewById(R.id.drawerLayout);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.open_drawer,
                R.string.close_drawer
        );

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        /** Fix drawer overlapping the status bar (edge-to-edge)
         * @author Krushna
         * @date 2026-09-28
         */
        // Adds padding equal to the status bar and gesture bar heights so drawer items are not hidden
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.navigationView), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, bars.top, 0, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        /** Setup bottom navigation
         * @author Krushna
         * @date 2026-09-28
         */
        bottomNav = findViewById(R.id.bottomNavigation);

        // Opens the matching screen when a bottom tab is tapped
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment;

            if (id == R.id.nav_racks) {
                fragment = new RacksFragment();
            } else if (id == R.id.nav_user) {
                fragment = new UserFragment();
            } else {
                fragment = new HomeFragment();
            }

            setCurrentFragment(fragment);
            return true;
        });

        // Show Home when the app first opens (not again after screen rotation)
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        /** Setup drawer items and keep them in sync with the bottom navigation
         * @author Krushna
         * @date 2026-09-28
         */
        TextView drawerHome = findViewById(R.id.drawerHome);
        TextView drawerRacks = findViewById(R.id.drawerRacks);
        TextView drawerSettings = findViewById(R.id.drawerSettings);
        TextView drawerAbout = findViewById(R.id.drawerAbout);

        // Home, Racks and Settings select the bottom tab, which also opens the screen
        drawerHome.setOnClickListener(v -> {
            bottomNav.setSelectedItemId(R.id.nav_home);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        drawerRacks.setOnClickListener(v -> {
            bottomNav.setSelectedItemId(R.id.nav_racks);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        drawerSettings.setOnClickListener(v -> {
            // Settings is no longer a bottom tab, so open it directly - Anvesh, 2026-10-07
            setCurrentFragment(new SettingsFragment());
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // About is drawer-only, so it opens the screen directly
        drawerAbout.setOnClickListener(v -> {
            setCurrentFragment(new AboutFragment());
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        /** Setup Dialogbox on backpress intercept
         * @Author: Andres
         * @Date: 2028-09-26
         */
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setTitle(R.string.closing_app);
                alertBuilder.setMessage(R.string.are_you_sure_you_want_to_close_sentryrack);
                alertBuilder.setIcon(R.drawable.animated_logo);
                alertBuilder.setCancelable(false)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            finish();
                        }).setNegativeButton(R.string.no, (dialog, which) -> {
                            dialog.dismiss();
                        });
                alertBuilder.show();
            }
        });

        if (savedInstanceState == null) {
            requestLocation();
        }
    }

    /** Replaces the screen shown in mainContent with the given fragment
     * @author Krushna
     * @date 2026-09-28
     */
    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContent, fragment)
                .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
    }

    // Toolbar (action bar) menu
    /** Loads the toolbar menu (Refresh, Alerts, Help, Settings, Log out)
     * @author Anvesh
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        // Divider line between overflow groups (like Chrome's menu)
        MenuCompat.setGroupDividerEnabled(menu, true);

        // Step 18: Refresh must show its image AND its text, so it gets a custom button
        MenuItem refreshItem = menu.findItem(R.id.action_refresh);
        refreshItem.setActionView(createRefreshButton(refreshItem));
        return true;
    }

    // Builds the Refresh toolbar button with icon + text (colors follow light/dark theme)

    private MaterialButton createRefreshButton(MenuItem refreshItem) {
        MaterialButton button = new MaterialButton(this, null,
                androidx.appcompat.R.attr.borderlessButtonStyle);
        int color = MaterialColors.getColor(button, com.google.android.material.R.attr.colorOnSurface);

        button.setText(R.string.menu_refresh);
        button.setTextColor(color);
        button.setIconResource(R.drawable.ic_menu_refresh);
        button.setIconTint(ColorStateList.valueOf(color));
        button.setOnClickListener(v -> onOptionsItemSelected(refreshItem));
        return button;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            showRefreshMessage();
            return true;
        } else if (id == R.id.action_alerts) {
            setCurrentFragment(new AlertsFragment());
            return true;
        } else if (id == R.id.action_help_center) {
            setCurrentFragment(new HelpCenterFragment());
            return true;
        } else if (id == R.id.action_contact) {
            showContactSupportSheet();
            return true;
        } else if (id == R.id.action_feedback) {
            showSendFeedbackDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRefreshMessage() {
        Snackbar.make(findViewById(R.id.mainContent), R.string.menu_refreshed_msg, Snackbar.LENGTH_SHORT)
                .setAnchorView(bottomNav)
                .show();
    }
    public void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_logout_title)
                .setMessage(R.string.menu_logout_msg)
                .setPositiveButton(R.string.menu_logout, (dialog, which) -> logout())
                .setNegativeButton(R.string.menu_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_FROM_LOGOUT, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showContactSupportSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.ar_sheet_contact_support, null);
        sheet.setContentView(sheetView);

        sheetView.findViewById(R.id.contactEmailRow).setOnClickListener(v -> {
            sheet.dismiss();
            openSupportEmail();
        });
        sheetView.findViewById(R.id.contactCallRow).setOnClickListener(v -> {
            sheet.dismiss();
            openSupportDialer();
        });
        sheetView.findViewById(R.id.contactWebRow).setOnClickListener(v -> {
            sheet.dismiss();
            openSupportWebsite();
        });

        sheet.show();
    }

    private void openSupportEmail() {
        Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.contact_email_address)});
        email.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.app_name) + " - " + getString(R.string.contact_title));
        startExternalApp(email);
    }

    private void openSupportDialer() {
        Intent dial = new Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:" + getString(R.string.contact_phone_number)));
        startExternalApp(dial);
    }

    private void openSupportWebsite() {
        Intent web = new Intent(Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.contact_web_url)));
        startExternalApp(web);
    }
    private void startExternalApp(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(findViewById(R.id.mainContent), R.string.contact_no_app, Snackbar.LENGTH_SHORT)
                    .setAnchorView(bottomNav)
                    .show();
        }
    }

    /** Opens the "Send feedback" dialog (from the 3-dot menu).
     * The Send button checks the message first - the dialog only closes
     */
    private void showSendFeedbackDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.ar_dialog_send_feedback, null);
        ChipGroup typeGroup = dialogView.findViewById(R.id.feedbackTypeGroup);
        TextInputLayout messageLayout = dialogView.findViewById(R.id.feedbackMessageLayout);
        EditText messageInput = dialogView.findViewById(R.id.feedbackMessageInput);
        TextView deviceInfo = dialogView.findViewById(R.id.feedbackDeviceInfo);

        // "App version 1.0 · Android 14 added automatically"
        deviceInfo.setText(getString(R.string.feedback_device_info,
                getAppVersion(), Build.VERSION.RELEASE));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.feedback_title)
                .setView(dialogView)
                .setNegativeButton(R.string.menu_cancel, null)
                .setPositiveButton(R.string.feedback_send, null)   // real click set below
                .create();

        // Replace the Send click so an empty message does NOT close the dialog
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(button -> {
                    String message = messageInput.getText() == null
                            ? "" : messageInput.getText().toString().trim();

                    if (message.isEmpty()) {
                        messageLayout.setError(getString(R.string.feedback_empty_error));
                        return;
                    }
                    messageLayout.setError(null);
                    dialog.dismiss();
                    sendFeedbackEmail(getSelectedFeedbackType(typeGroup), message);
                }));

        dialog.show();
    }

    // Returns the text of the selected chip: "Bug", "Idea" or "Other"
    private String getSelectedFeedbackType(ChipGroup typeGroup) {
        Chip selected = typeGroup.findViewById(typeGroup.getCheckedChipId());
        return selected != null ? selected.getText().toString() : getString(R.string.feedback_other);
    }

    /** Opens the email app with the feedback filled in:
     * subject = "SentryRack feedback: Bug", body = message + device details.
     */
    private void sendFeedbackEmail(String type, String message) {
        String deviceDetails = getString(R.string.app_name) + " " + getAppVersion()
                + " · Android " + Build.VERSION.RELEASE
                + " · " + Build.MANUFACTURER + " " + Build.MODEL;

        Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.contact_email_address)});
        email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject, type));
        email.putExtra(Intent.EXTRA_TEXT, message + "\n\n---\n" + deviceDetails);
        startExternalApp(email);
    }
    private String getAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0";
        }
    }
    //Misc snackbar method REUSEABLE
    private void showSnackbar(String message, String actionText, View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.mainContent), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(actionText, v -> {
            if (listener != null) {
                listener.onClick(v);
            }
            snackbar.dismiss();
        });
        snackbar.show();
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasNotificationPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    // Get perms
    private void requestLocation() {
        if (hasLocationPermission() && hasNotificationPermission()) {
            fetchDeviceLocation();
            return;
        }
        String[] permissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS}
                : new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        locationPermissionLauncher.launch(permissions);
    }

    private void fetchDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // getCurrentLocation
        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String locationText = getString(R.string.lat) + latitude + ", " + getString(R.string.lon) + longitude;

                        showSnackbar(locationText, getString(R.string.dismiss), null);
                        triggerNotification(latitude, longitude);
                    } else {
                        showSnackbar(getString(R.string.location_could_not_be_determined), getString(R.string.dismiss), null);
                    }
                })
                .addOnFailureListener(this, e ->
                        showSnackbar(getString(R.string.location_error) + e.getLocalizedMessage(), getString(R.string.dismiss), null));
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.location_channel),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(getString(R.string.channel_for_location_notifications));
            channel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void triggerNotification(double lat, double lng) {
        if (!hasNotificationPermission()) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentTitle(FULL_NAME + getString(R.string.location_determined))
                .setContentText(getString(R.string.lat) + lat + ", " + getString(R.string.lon) + lng)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}