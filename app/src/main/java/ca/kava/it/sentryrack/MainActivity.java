// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
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

public class MainActivity extends AppCompatActivity {


    // Drawer and bottom bar are fields so every method can use them
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Splash screen (Volk) moved to LoginActivity.setupSplashScreen() so it shows
        // before the Login screen (Deliverable 2 step 11) - Anvesh, 2026-10-01

        setContentView(R.layout.activity_main);

        /** Setup toolbar as the app bar
         * @author Krushna
         * @date 2026-09-28
         */
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

            // if/else is used instead of switch because resource IDs are not final in newer Gradle versions
            if (id == R.id.nav_racks) {
                fragment = new RacksFragment();
            } else if (id == R.id.nav_alerts) {
                fragment = new AlertsFragment();
            } else if (id == R.id.nav_settings) {
                fragment = new SettingsFragment();
            } else {
                fragment = new HomeFragment();
            }

            showFragment(fragment);
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
            bottomNav.setSelectedItemId(R.id.nav_settings);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // About is drawer-only, so it opens the screen directly
        drawerAbout.setOnClickListener(v -> {
            showFragment(new AboutFragment());
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
    }

    /** Replaces the screen shown in mainContent with the given fragment
     * @author Krushna
     * @date 2026-09-28
     */
    private void showFragment(Fragment fragment) {
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
    // @author Anvesh

    /** Loads the toolbar menu (Refresh, Alerts, Help, Settings, Log out)
     * @author Anvesh
     * @date 2026-10-01
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

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

    /** Runs the action for the toolbar option that was tapped
     * (if/else instead of switch because resource IDs are not final in newer Gradle versions)
     * @author Anvesh
     * @date 2026-10-01
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            showRefreshMessage();
            return true;
        } else if (id == R.id.action_alerts) {
            bottomNav.setSelectedItemId(R.id.nav_alerts);
            return true;
        } else if (id == R.id.action_help) {
            showHelpDialog();
            return true;
        } else if (id == R.id.action_settings) {
            bottomNav.setSelectedItemId(R.id.nav_settings);
            return true;
        } else if (id == R.id.action_logout) {
            confirmLogout();
            return true;
        }
        // Anything else (e.g. the hamburger icon) is handled by Android as before
        return super.onOptionsItemSelected(item);
    }

    /** Refresh: shows a message above the bottom navigation
     * @author Anvesh
     * @date 2026-10-01
     */
    private void showRefreshMessage() {
        Snackbar.make(findViewById(R.id.mainContent), R.string.menu_refreshed_msg, Snackbar.LENGTH_SHORT)
                .setAnchorView(bottomNav)
                .show();
    }
    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_help_title)
                .setMessage(R.string.menu_help_msg)
                .setPositiveButton(R.string.menu_ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    /** Log out: asks first, so the user does not log out by accident
     * @author Anvesh
     * @date 2026-10-01
     */
    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_logout_title)
                .setMessage(R.string.menu_logout_msg)
                .setPositiveButton(R.string.menu_logout, (dialog, which) -> logout())
                .setNegativeButton(R.string.menu_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    /** Goes back to the Login screen and clears the back stack,
     * so pressing Back on Login does not return to the main screen
     * @author Anvesh
     * @date 2026-10-01
     */
    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_FROM_LOGOUT, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}