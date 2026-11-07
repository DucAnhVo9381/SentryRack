// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ca.kava.it.sentryrack.model.LocationRepository;

public class RegisterRackFragment extends Fragment {
    private static final String ARG_ROOM_ID = "roomId";

    private TextInputEditText rackNameInput;
    private TextInputEditText floorInput;
    private TextInputEditText roomInput;
    private MaterialButton registerButton;
    private final LocationRepository repository = new LocationRepository();

    public RegisterRackFragment() {
    }

    public static RegisterRackFragment newInstance(String roomId) {
        RegisterRackFragment fragment = new RegisterRackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_ID, roomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_rack_fragment, container, false);

        rackNameInput = view.findViewById(R.id.registerRackNameInput);
        floorInput = view.findViewById(R.id.registerRackFloorInput);
        roomInput = view.findViewById(R.id.registerRackRoomInput);
        registerButton = view.findViewById(R.id.registerRackButton);

        registerButton.setOnClickListener(v -> registerRack());

        return view;
    }

    private void registerRack() {
        String rackName = rackNameInput.getText() == null ? "" : rackNameInput.getText().toString().trim();
        String floorName = floorInput.getText() == null ? "" : floorInput.getText().toString().trim();
        String roomName = roomInput.getText() == null ? "" : roomInput.getText().toString().trim();

        if (TextUtils.isEmpty(rackName)) {
            rackNameInput.setError("Rack name is required");
            rackNameInput.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(floorName)) {
            floorInput.setError("Floor is required");
            floorInput.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(roomName)) {
            roomInput.setError("Room is required");
            roomInput.requestFocus();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please sign in before registering a rack.", Toast.LENGTH_SHORT).show();
            return;
        }

        registerButton.setEnabled(false);
        repository.registerRackHierarchy(rackName, floorName, roomName, currentUser.getUid(), "admin",
                new LocationRepository.Callback() {
                    @Override
                    public void onSuccess(String id) {
                        if (!isAdded()) {
                            return;
                        }
                        registerButton.setEnabled(true);
                        Toast.makeText(requireContext(), "Rack registered successfully.", Toast.LENGTH_SHORT).show();
                        rackNameInput.setText("");
                        floorInput.setText("");
                        roomInput.setText("");
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded()) {
                            return;
                        }
                        registerButton.setEnabled(true);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}