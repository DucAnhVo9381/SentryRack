//--TODO add name student # + Section CODE!
// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
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

public class MainActivity extends AppCompatActivity {
    // variable determines splash remains visible or disappears
    private volatile boolean keepSplash = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** Setup splashscreen
         * @author Volk
         * @since 2026-09-25
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
        findViewById(android.R.id.content).postDelayed(() -> keepSplash = false, 10000);

        setContentView(R.layout.activity_main);

        /*  Deliverable1 pt41
        *   Intercepting user back press and displaying alert dialog
        *   Note: Can change the way this look and interacts in the future for a more user friendll/aesthetic UI
        *   -AZ
         */
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true){
            @Override
            public void handleOnBackPressed(){
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setTitle(R.string.closing_app);
                alertBuilder.setMessage(R.string.are_you_sure_you_want_to_close_sentryrack);
                //--Todo CHANGE ALERT DIALOG ICON
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
}