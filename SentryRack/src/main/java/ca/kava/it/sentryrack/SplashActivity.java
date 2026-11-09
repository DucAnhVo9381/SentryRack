// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 3000;
    private static final long SPLASH_EXIT_DURATION_MS = 500;

    // Splash stays visible while this is true (set to false after 3 seconds)
    private volatile boolean keepSplash = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Must be installed BEFORE super.onCreate()
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Custom exit animation (Volk): the icon zooms out with a small overshoot
        splashScreen.setOnExitAnimationListener(screen -> {
            ObjectAnimator zoomX = ObjectAnimator.ofFloat(screen.getIconView(), View.SCALE_X, 0.8f, 0.0f);
            zoomX.setInterpolator(new OvershootInterpolator());
            zoomX.setDuration(SPLASH_EXIT_DURATION_MS);

            ObjectAnimator zoomY = ObjectAnimator.ofFloat(screen.getIconView(), View.SCALE_Y, 0.8f, 0.0f);
            zoomY.setInterpolator(new OvershootInterpolator());
            zoomY.setDuration(SPLASH_EXIT_DURATION_MS);

            zoomY.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    screen.remove();
                    openLoginScreen();
                }
            });

            zoomX.start();
            zoomY.start();
        });

        // Hold the splash for 3 seconds
        splashScreen.setKeepOnScreenCondition(() -> keepSplash);
        new Handler(Looper.getMainLooper()).postDelayed(() -> keepSplash = false, SPLASH_DURATION_MS);
    }

    /** Opens LoginActivity and closes Splash, so Back never returns here */
    private void openLoginScreen() {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }
}