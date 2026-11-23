// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class Utility {
    private Utility() { }

    public static String getText(TextInputEditText input) {
        return input != null && input.getText() != null ? input.getText().toString() : "";
    }

    public static void showLongToast(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    // Clear validation error message when focusing on a field or while typing inside it
    public static void clearErrorOnInput(TextInputEditText input, TextInputLayout layout) {
        if (input == null || layout == null) {
            return;
        }

        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && layout.getError() != null) {
                layout.setError(null);
            }
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (layout.getError() != null) {
                    layout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
}
