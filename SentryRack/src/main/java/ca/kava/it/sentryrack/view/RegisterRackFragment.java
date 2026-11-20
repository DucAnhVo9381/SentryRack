// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ca.kava.it.sentryrack.R;
import ca.kava.it.sentryrack.controller.RegisterRackController;

public class RegisterRackFragment extends Fragment implements RegisterRackController.Callback {

    private TextInputLayout rackNameLayout;
    private TextInputLayout floorLayout;
    private TextInputLayout roomLayout;
    private TextInputEditText rackNameInput;
    private TextInputEditText floorInput;
    private TextInputEditText roomInput;
    private MaterialButton registerButton;
    private RegisterRackController controller;
    private String registeredRackName = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_rack_fragment, container, false);

        rackNameLayout = view.findViewById(R.id.registerRackNameLayout);
        floorLayout = view.findViewById(R.id.registerRackFloorLayout);
        roomLayout = view.findViewById(R.id.registerRackRoomLayout);
        rackNameInput = view.findViewById(R.id.registerRackNameInput);
        floorInput = view.findViewById(R.id.registerRackFloorInput);
        roomInput = view.findViewById(R.id.registerRackRoomInput);
        registerButton = view.findViewById(R.id.registerRackButton);

        controller = new RegisterRackController(this);
        registerButton.setOnClickListener(v -> registerRack());

        return view;
    }

    private void registerRack() {
        rackNameLayout.setError(null);
        floorLayout.setError(null);
        roomLayout.setError(null);

        registeredRackName = textOf(rackNameInput).trim();
        boolean started = controller.register(registeredRackName, textOf(floorInput), textOf(roomInput));
        registerButton.setEnabled(!started);
    }

    @Override
    public void onFieldError(RegisterRackController.Field field, int errorResId) {
        if (!isAdded()) return;
        registerButton.setEnabled(true);
        String error = getString(errorResId);
        switch (field) {
            case RACK_NAME:
                rackNameLayout.setError(error);
                break;
            case FLOOR:
                floorLayout.setError(error);
                break;
            case ROOM:
                roomLayout.setError(error);
                break;
        }
    }

    @Override
    public void onNotLoggedIn() {
        if (!isAdded()) return;
        registerButton.setEnabled(true);
        Toast.makeText(requireContext(), R.string.register_rack_login_required, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess(String rackId, String roomId) {
        if (!isAdded()) return;
        registerButton.setEnabled(true);
        showRegisteredDialog(rackId);
    }

    @Override
    public void onError(String message) {
        if (!isAdded()) return;
        registerButton.setEnabled(true);
        Toast.makeText(requireContext(),
                getString(R.string.register_rack_failed, message), Toast.LENGTH_SHORT).show();
    }

    // Shows the new rack's ID (the other application needs it), then opens the Racks screen
    private void showRegisteredDialog(String rackId) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.register_rack_success_title)
                .setMessage(getString(R.string.register_rack_success_message, registeredRackName, rackId))
                .setPositiveButton(R.string.register_rack_copy_id, (dialog, which) -> copyToClipboard(rackId))
                .setNegativeButton(R.string.register_rack_done, null)
                .setOnDismissListener(dialog -> openRacks())
                .show();
    }

    private void copyToClipboard(String rackId) {
        ClipboardManager clipboard =
                (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.register_rack_clip_label), rackId));
        Toast.makeText(requireContext(), R.string.register_rack_id_copied, Toast.LENGTH_SHORT).show();
    }

    private void openRacks() {
        if (!isAdded()) return;
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new RacksFragment())
                .commit();
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}