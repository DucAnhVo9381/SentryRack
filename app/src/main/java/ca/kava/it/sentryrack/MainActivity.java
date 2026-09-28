// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.widget.TextView;




public class MainActivity extends AppCompatActivity {
    // variable determines splash remains visible or disappears
    private volatile boolean keepSplash = true;

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
        // Setup navigation drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.open_drawer,
                R.string.close_drawer
        );

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        TextView drawerHome = findViewById(R.id.drawerHome);
        TextView drawerSettings = findViewById(R.id.drawerSettings);
        TextView drawerAbout = findViewById(R.id.drawerAbout);

        drawerHome.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainContent, new HomeFragment())
                    .commit();

            drawerLayout.closeDrawer(GravityCompat.START);
        });

        drawerSettings.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainContent, new SettingsFragment())
                    .commit();

            drawerLayout.closeDrawer(GravityCompat.START);
        });

        drawerAbout.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainContent, new AboutFragment())
                    .commit();

            drawerLayout.closeDrawer(GravityCompat.START);
        });

        /** Setup Dialogbox on backpress intercept
         * @Author: Andres
         * @Date: 2028-09-26
         */
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true){
            @Override
            public void handleOnBackPressed(){
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setTitle(R.string.closing_app);
                alertBuilder.setMessage(R.string.are_you_sure_you_want_to_close_sentryrack);
                alertBuilder.setIcon(R.drawable.animated_logo);
                alertBuilder.setCancelable(false)
                        .setPositiveButton(R.string.yes, (dialog, which) ->{
                                finish();
                        }).setNegativeButton(R.string.no, (dialog, which) ->{
                                dialog.dismiss();
                        });
                alertBuilder.show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);

        if (drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
        } else {
            drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
        }

        return true;
    }
}