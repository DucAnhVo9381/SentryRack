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

public class MainActivity extends AppCompatActivity {
    // variable determines splash remains visible or disappears
    private volatile boolean keepSplash = true;

    // Drawer and bottom bar are fields so every method can use them
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** Setup splashscreen
         * @author Volk
         * @date 2026-09-25
         */
        // Activates the splash screen API - Android creates splash screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        // Custom exit animation
        splashScreen.setOnExitAnimationListener(screen -> {
            ObjectAnimator zoomX = ObjectAnimator.ofFloat(
                    screen.getIconView(),
                    View.SCALE_X,
                    0.8f,
                    0.0f
            );
            zoomX.setInterpolator(new OvershootInterpolator());
            zoomX.setDuration(500);

            ObjectAnimator zoomY = ObjectAnimator.ofFloat(
                    screen.getIconView(),
                    View.SCALE_Y,
                    0.8f,
                    0.0f
            );
            zoomY.setInterpolator(new OvershootInterpolator());
            zoomY.setDuration(500);

            zoomY.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    screen.remove();
                }
            });

            zoomX.start();
            zoomY.start();
        });

        // Code controls when to remove it
        // True keep the splash screen indefinitely
        splashScreen.setKeepOnScreenCondition(() -> keepSplash);
        // keepSplash becomes false after 5 seconds and Android removes the splash screen
        findViewById(android.R.id.content).postDelayed(() -> keepSplash = false, 5000);

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
}