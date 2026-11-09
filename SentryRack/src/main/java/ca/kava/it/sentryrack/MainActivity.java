// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import ca.kava.it.sentryrack.controller.AuthController;
import ca.kava.it.sentryrack.model.User;
import ca.kava.it.sentryrack.controller.FeedbackController;
import ca.kava.it.sentryrack.model.DeviceInfo;

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
                        showSnackbar(getString(R.string.permission_denied_opening_settings), getString(R.string.drawer_settings), v -> {
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
        SettingsFragment.applySavedTheme(this);
        SettingsFragment.applySavedOrientation(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.drawer_nav_view);

        View header = navigationView.getHeaderView(0);
        ImageButton closeButton = header.findViewById(R.id.btnCloseDrawer);
        closeButton.setOnClickListener(view -> drawerLayout.closeDrawer(GravityCompat.START));

        AuthController authController = new AuthController();
        User currentUser = authController.getCurrentUser();
        if (currentUser != null) {
            TextView txtUserName = header.findViewById(R.id.txtUserName);
            TextView txtUserEmail = header.findViewById(R.id.txtUserEmail);

            if (txtUserEmail != null && currentUser.getEmail() != null) {
                txtUserEmail.setText(currentUser.getEmail());
            }
            if (txtUserName != null && currentUser.getDisplayName() != null) {
                txtUserName.setText(currentUser.getDisplayName());
            }
        }

        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bottomNav = findViewById(R.id.bottomNavigation);

        bottomNav.setOnItemSelectedListener(item -> {
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

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.dashboard);
            requestLocation();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }else if (current instanceof RegisterRackFragment) {
                        setCurrentFragment(new RacksFragment());
                } else if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof DashboardFragment)) {
                    bottomNav.getMenu().findItem(R.id.dashboard).setChecked(true);
                    bottomNav.setSelectedItemId(R.id.dashboard);
                } else {
                    showCustomConfirmationDialog(null, null, v -> moveTaskToBack(true));
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.toolbar_alerts) {
            setCurrentFragment(new AlertsFragment());
            return true;
        } else if (id == R.id.action_help_center) {
            setCurrentFragment(new HelpCenterFragment());
            return true;
        } else if (id == R.id.action_contact) {
            showContactSupportSheet();
            return true;
        } else if (id == R.id.overflow_about) {
            showSendFeedbackDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        if (itemId == R.id.drawer_account) {
            setCurrentFragment(new AccountFragment());
        } else if (itemId == R.id.drawer_settings) {
            setCurrentFragment(new SettingsFragment());
        } else if (itemId == R.id.drawer_about) {
            setCurrentFragment(new AboutFragment());
        } else if (itemId == R.id.drawer_logout) {
            confirmLogout();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void confirmLogout() {
        showCustomConfirmationDialog(
                getString(R.string.logout_dialog_title),
                getString(R.string.logout_dialog_msg),
                v -> logout()
        );
    }

    private void logout() {
        AuthController authController = new AuthController();
        authController.logout();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.FROM_LOGOUT, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showContactSupportSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.overflow_sheet_contact_support, null);
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

    private void showSendFeedbackDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.overflow_dialog_send_feedback, null);
        TextInputLayout nameLayout = dialogView.findViewById(R.id.feedbackNameLayout);
        EditText nameInput = dialogView.findViewById(R.id.feedbackNameInput);
        TextInputLayout phoneLayout = dialogView.findViewById(R.id.feedbackPhoneLayout);
        EditText phoneInput = dialogView.findViewById(R.id.feedbackPhoneInput);
        TextInputLayout emailLayout = dialogView.findViewById(R.id.feedbackEmailLayout);
        EditText emailInput = dialogView.findViewById(R.id.feedbackEmailInput);
        TextInputLayout messageLayout = dialogView.findViewById(R.id.feedbackMessageLayout);
        EditText messageInput = dialogView.findViewById(R.id.feedbackMessageInput);
        RatingBar ratingBar = dialogView.findViewById(R.id.feedbackRatingBar);
        TextView ratingError = dialogView.findViewById(R.id.feedbackRatingError);
        TextView deviceInfoText = dialogView.findViewById(R.id.feedbackDeviceInfo);

        deviceInfoText.setText(getString(R.string.feedback_device_info,
                getAppVersion(), Build.VERSION.RELEASE));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.feedback_title)
                .setView(dialogView)
                .setNegativeButton(R.string.feedback_cancel, null)
                .setPositiveButton(R.string.feedback_send, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(button -> {
                    nameLayout.setError(null);
                    phoneLayout.setError(null);
                    emailLayout.setError(null);
                    messageLayout.setError(null);
                    ratingError.setVisibility(View.GONE);

                    String comment = textOf(messageInput);

                    FeedbackController controller = new FeedbackController(new FeedbackController.Callback() {
                        @Override
                        public void onFieldError(FeedbackController.Field field, int errorResId) {
                            String error = getString(errorResId);
                            switch (field) {
                                case FULL_NAME:
                                    nameLayout.setError(error);
                                    break;
                                case PHONE:
                                    phoneLayout.setError(error);
                                    break;
                                case EMAIL:
                                    emailLayout.setError(error);
                                    break;
                                case COMMENT:
                                    messageLayout.setError(error);
                                    break;
                                case RATING:
                                    ratingError.setText(error);
                                    ratingError.setVisibility(View.VISIBLE);
                                    break;
                            }
                        }

                        @Override
                        public void onNotLoggedIn() {
                            Toast.makeText(MainActivity.this, R.string.feedback_login_required,
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onSuccess() {
                            Toast.makeText(MainActivity.this, R.string.feedback_success,
                                    Toast.LENGTH_SHORT).show();
                            sendFeedbackEmail(comment.trim());
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.feedback_failed, message),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    boolean started = controller.submit(
                            textOf(nameInput),
                            textOf(phoneInput),
                            textOf(emailInput),
                            comment,
                            (int) ratingBar.getRating(),
                            DeviceInfo.from(getAppVersion()));

                    if (started) {
                        dialog.dismiss();
                    }
                }));

        dialog.show();
    }

    private String textOf(EditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    private void sendFeedbackEmail(String comment) {
        String deviceDetails = getString(R.string.app_name) + " " + getAppVersion()
                + " · Android " + Build.VERSION.RELEASE
                + " · " + Build.MANUFACTURER + " " + Build.MODEL;

        Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.contact_email_address)});
        email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
        email.putExtra(Intent.EXTRA_TEXT, comment + "\n\n---\n" + deviceDetails);
        startExternalApp(email);
    }

    private String getAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0";
        }
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasNotificationPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

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

    private void showCustomConfirmationDialog(String title, String message, View.OnClickListener onYesClick) {
        View dialogView = getLayoutInflater().inflate(R.layout.confirm_dialog, null);

        TextView txtTitle = dialogView.findViewById(R.id.confirmDialogTitle);
        TextView txtMessage = dialogView.findViewById(R.id.confirmDialogMessage);

        if (txtTitle != null && title != null) {
            txtTitle.setText(title);
        }
        if (txtMessage != null && message != null) {
            txtMessage.setText(message);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Button btnNo = dialogView.findViewById(R.id.confirmDialogBtnNo);
        Button btnYes = dialogView.findViewById(R.id.confirmDialogBtnYes);

        btnNo.setOnClickListener(v -> dialog.dismiss());

        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            if (onYesClick != null) {
                onYesClick.onClick(v);
            }
        });

        dialog.show();
    }
}