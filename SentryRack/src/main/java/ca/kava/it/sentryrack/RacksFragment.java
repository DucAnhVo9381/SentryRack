// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC

package ca.kava.it.sentryrack;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.kava.it.sentryrack.controller.RackListController;
import ca.kava.it.sentryrack.model.RackStatus;

public class RacksFragment extends Fragment implements RackListController.Callback {

    private static final long STALE_CHECK_INTERVAL_MS = 20_000; // 20 seconds
    private static final String PREFS_NAME = "sentryrack_prefs";
    private static final String KEY_LAST_ROOM_ID = "last_room_id";
    // Used until a room has been registered or chosen
    private static final String DEFAULT_ROOM_ID = "room_01";

    private RackStatusAdapter adapter;
    private RackListController controller;
    private TextView racksTotalCount;
    private String roomId;

    // Rack data = dead Rack = OFFLINE
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

    public static void saveLastRoomId(Context context, String roomId) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_LAST_ROOM_ID, roomId).apply();
    }

    public static void clearLastRoomId(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().remove(KEY_LAST_ROOM_ID).apply();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomId = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LAST_ROOM_ID, DEFAULT_ROOM_ID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.racks_fragment, container, false);

        racksTotalCount = view.findViewById(R.id.racksTotalCount);
        RecyclerView recyclerView = view.findViewById(R.id.racksRecyclerView);

        adapter = new RackStatusAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        controller = new RackListController(this);

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
        controller.start(roomId);
        staleHandler.postDelayed(staleRunnable, STALE_CHECK_INTERVAL_MS);
    }

    @Override
    public void onStop() {
        super.onStop();
        controller.stop();
        staleHandler.removeCallbacks(staleRunnable);
    }

    @Override
    public void onRacksChanged(List<RackStatus> racks) {
        adapter.submitList(racks);
        if (racksTotalCount != null && isAdded()) {
            racksTotalCount.setText(getResources().getQuantityString(
                    R.plurals.racks_total_count, racks.size(), racks.size()));
        }
    }

    @Override
    public void onError(String message) {
        if (getContext() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}