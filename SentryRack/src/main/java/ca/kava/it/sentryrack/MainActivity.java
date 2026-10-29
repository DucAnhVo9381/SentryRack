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
import android.widget.Button;
import android.widget.ImageButton;import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Bottom nav and drawer fields
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;

    // Runtime permission variables
    private FusedLocationProviderClient fusedLocationClient;
    private int locationPermissionDenied = 0;

    private static final String CHANNEL_ID = "location_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final String FULL_NAME = "Kava";

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
        // Apply the saved light/dark theme before the screen is drawn - @author Anvesh @date 2026-10-15
        SettingsFragment.applySavedTheme(this);
        // Settings > "Lock screen to portrait" - apply the saved choice (Anvesh, 2026-10-01)
        SettingsFragment.applySavedOrientation(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();

        // Setup toolbar - The toolbar lives inside the layout (not the window), so the drawer can slide over it
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup navigation drawer
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.drawer_nav_view);

        View header = navigationView.getHeaderView(0);
        ImageButton closeButton = header.findViewById(R.id.btnCloseDrawer);
        closeButton.setOnClickListener(view -> drawerLayout.closeDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup bottom navigation
        bottomNav = findViewById(R.id.bottomNavigation);

        bottomNav.setOnItemSelectedListener(item -> {
            clearSelection(navigationView.getMenu());

            int id = item.getItemId();
            if (id == R.id.dashboard) {
                setCurrentFragment(new DashboardFragment());
                return true;
            } else if (id == R.id.racks) {
                setCurrentFragment(new RacksFragment());
                return true;
            } else if (id == R.id.activity) {
                setCurrentFragment(new ActivityFragment());
                return true;
            }
            return false;
        });

        // First-time startup actions
        if (savedInstanceState == null) {
            // Set the main screen as default screen
            bottomNav.setSelectedItemId(R.id.dashboard);
            // Request location when app first launch
            requestLocation();
        }

        // Android back press handler
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    // Close drawer if open
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof DashboardFragment)) {
                    // If the currently visible screen is not the main screen, jump back to dashboard
                    bottomNav.getMenu().findItem(R.id.dashboard).setChecked(true);  // Forces highlight
                    bottomNav.setSelectedItemId(R.id.dashboard);
                }
                else {
                    // Only show exit dialog if user strictly on dashboard screen with no where else to go
                    showConfirmExitDialog();
                }
            }
        });
    }

    //---NAVIGATION HELPER METHODS---
    // Enable the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        // Divider line between overflow groups (like Chrome's menu)
        MenuCompat.setGroupDividerEnabled(menu,true);
        return true;
    }

    // Handle toolbar items clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.toolbar_alerts) {
            clearSelection(bottomNav.getMenu());
            clearSelection(navigationView.getMenu());
            setCurrentFragment(new AlertsFragment());
            return true;
        } else if (id == R.id.action_help_center) {
            clearSelection(bottomNav.getMenu());
            clearSelection(navigationView.getMenu());
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

    // Handle drawer menu item clicks
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        clearSelection(bottomNav.getMenu());

        int itemId = menuItem.getItemId();
        if (itemId == R.id.drawer_account) {
            setCurrentFragment(new AccountFragment());
        } else if (itemId == R.id.drawer_settings) {
            setCurrentFragment(new SettingsFragment());
        } else if (itemId == R.id.drawer_about) {
            setCurrentFragment(new AboutFragment());
        } else if (itemId == R.id.nav_logout) {

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Replaces the screen shown in fragment container with the given fragment
    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // Clears the selected/highlighted state from all items in the given menu
    private void clearSelection(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setChecked(false);
        }
    }

    //---MENU ITEMS FUNCTIONALITY METHODS---
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
        intent.putExtra(LoginActivity.FROM_LOGOUT, true);
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
            Snackbar.make(findViewById(R.id.fragment_container), R.string.contact_no_app, Snackbar.LENGTH_SHORT)
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

    //---LOCATION & PERMISSIONS & NOTIFICATIONS---
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

    //--- EXIT DIALOG HELPER METHODS ---
    // Inflate the custom exit dialog and its buttons behavior when Android back button is pressed
    private void showConfirmExitDialog() {
        // Inflate the custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.exit_dialog, null);

        // Build the AlertDialog with a transparent background to respect CardView rounded corners
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Set click handlers for NO and YES buttons
        Button btnNo = dialogView.findViewById(R.id.exitDialogBtnNo);
        Button btnYes = dialogView.findViewById(R.id.exitDialogBtnYes);

        btnNo.setOnClickListener(v -> dialog.dismiss());

        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            // Moves app to background safely instead of killing state/session
            moveTaskToBack(true);
        });

        dialog.show();
    }

    //---REUSABLE/UTILITY METHODS---
    //Misc snack bar method REUSABLE
    private void showSnackbar(String message, String actionText, View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.fragment_container), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(actionText, v -> {
            if (listener != null) {
                listener.onClick(v);
            }
            snackbar.dismiss();
        });
        snackbar.show();
    }
}