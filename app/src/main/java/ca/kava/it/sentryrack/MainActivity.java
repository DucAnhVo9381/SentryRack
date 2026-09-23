// Andres Zambrano N01495324 ONC --TODO add name student # + Section CODE!
package ca.kava.it.sentryrack;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                alertBuilder.setTitle("Closing App");
                alertBuilder.setMessage("Are you sure you want to SentryRack?");
                //--Todo CHANGE ALERT DIALOG ICON
                alertBuilder.setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                alertBuilder.show();
            }
        });
    }
}