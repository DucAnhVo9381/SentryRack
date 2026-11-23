// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC

package ca.kava.it.sentryrack.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.List;

import ca.kava.it.sentryrack.R;
import ca.kava.it.sentryrack.controller.AuthController;
import ca.kava.it.sentryrack.controller.DashboardRackController;
import ca.kava.it.sentryrack.controller.RackListAdapter;
import ca.kava.it.sentryrack.model.RackRepository;
import ca.kava.it.sentryrack.model.UserData;
import ca.kava.it.sentryrack.model.RackStatus;

public class RacksFragment extends Fragment implements DashboardRackController.Callback {

    private static final long STALE_CHECK_INTERVAL_MS = 20_000; // 20 seconds

    private final RackRepository rackRepository = new RackRepository();
    private RackListAdapter adapter;
    private DashboardRackController controller;

    // RackData data = dead RackData = OFFLINE
    private final Handler staleHandler = new Handler(Looper.getMainLooper());
    private final Runnable staleRunnable = new Runnable() {
        @Override
        public void run() {
            if (adapter != null && adapter.getItemCount() > 0) {
                adapter.notifyItemRangeChanged(0, adapter.getItemCount());
            }
            staleHandler.postDelayed(this, STALE_CHECK_INTERVAL_MS);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.racks_fragment, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.racksRecyclerView);

        adapter = new RackListAdapter();

        // Long click listener
        adapter.setOnItemLongClickListener(rack -> showDeleteDialog(rack));

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        controller = new DashboardRackController(this);

        view.findViewById(R.id.racksRegisterButton).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new RegisterRackFragment())
                        .commit());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        UserData currentUserData = new AuthController().getCurrentUser();
        if (currentUserData == null) {
            adapter.submitList(Collections.emptyList());
            return;
        }
        controller.start(currentUserData.getUid());
        staleHandler.postDelayed(staleRunnable, STALE_CHECK_INTERVAL_MS);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (controller != null) controller.stop();
        staleHandler.removeCallbacks(staleRunnable);
    }

    @Override
    public void onRacksChanged(List<RackStatus> racks) {
        if (adapter == null) return;
        adapter.submitList(racks);
    }

    @Override
    public void onError(String message) {
        if (getContext() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // Show delete dialog for long click listener
    private void showDeleteDialog(RackStatus rack) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Rack")
                .setMessage("Delete " + rack.name + "?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    rackRepository.deleteRack(rack.rackId, new RackRepository.DeleteRackCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(requireContext(), rack.name + " deleted", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .show();
    }
}