// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ca.kava.it.sentryrack.R;
import ca.kava.it.sentryrack.controller.RackListAdapter;
import ca.kava.it.sentryrack.model.UserData;
import ca.kava.it.sentryrack.controller.AuthController;
import ca.kava.it.sentryrack.controller.DashboardRackController;
import ca.kava.it.sentryrack.model.RackStatus;

public class DashboardFragment extends Fragment implements DashboardRackController.Callback {

    private DashboardRackController rackController;
    private RackListAdapter attentionAdapter;
    private List<RackStatus> currentRacks = new ArrayList<>();
    private TextView totalRackCount;
    private TextView healthyCount;
    private TextView warningCount;
    private TextView criticalCount;

    private static final long STALE_CHECK_INTERVAL_MS = 20_000;
    private final Handler staleHandler = new Handler(Looper.getMainLooper());
    private final Runnable staleRunnable = new Runnable() {
        @Override
        public void run() {
            renderRacks();
            staleHandler.postDelayed(this, STALE_CHECK_INTERVAL_MS);
        }
    };

    public DashboardFragment() {
        // Required empty public constructor
    }

    // MAIN LIFECYCLE METHOD
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dashboard_fragment, container, false);

        AuthController authController = new AuthController();
        UserData currentUserData = authController.getCurrentUser();
        String displayName = (currentUserData != null && currentUserData.getDisplayName() != null) ? currentUserData.getDisplayName() : getString(R.string.user_name_default);

        TextView dashboardGreeting = view.findViewById(R.id.dashboardGreeting);
        dashboardGreeting.setText(getGreetingMessage(displayName));

        totalRackCount = view.findViewById(R.id.dashboardTotalRackCount);
        healthyCount = view.findViewById(R.id.dashboardHealthyCount);
        warningCount = view.findViewById(R.id.dashboardWarningCount);
        criticalCount = view.findViewById(R.id.dashboardCriticalCount);
        RecyclerView attentionRecycler = view.findViewById(R.id.dashboardRecyclerView);
        attentionAdapter = new RackListAdapter();
        attentionRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        attentionRecycler.setAdapter(attentionAdapter);
        rackController = new DashboardRackController(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        UserData currentUserData = new AuthController().getCurrentUser();
        if (currentUserData == null) {
            currentRacks.clear();
            renderRacks();
            return;
        }
        rackController.start(currentUserData.getUid());
        staleHandler.postDelayed(staleRunnable, STALE_CHECK_INTERVAL_MS);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (rackController != null) rackController.stop();
        staleHandler.removeCallbacks(staleRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        totalRackCount = null;
        healthyCount = null;
        warningCount = null;
        criticalCount = null;
        attentionAdapter = null;
    }

    @Override
    public void onRacksChanged(List<RackStatus> racks) {
        currentRacks = new ArrayList<>(racks);
        renderRacks();
    }

    @Override
    public void onError(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void renderRacks() {
        int healthy = 0;
        int warning = 0;
        int critical = 0;
        List<RackStatus> attention = new ArrayList<>();

        for (RackStatus rack : currentRacks) {
            RackStatus.Condition condition = rack.getCondition(RackStatus.STALE_AFTER_MS);
            if (condition == RackStatus.Condition.CRITICAL) {
                critical++;
                attention.add(rack);
            } else if (condition == RackStatus.Condition.WARNING) {
                warning++;
                attention.add(rack);
            } else {
                healthy++;
            }
        }

        if (totalRackCount != null && isAdded()) {
            totalRackCount.setText(getResources().getQuantityString(
                    R.plurals.racks_total_count, currentRacks.size(), currentRacks.size()));
            healthyCount.setText(String.valueOf(healthy));
            warningCount.setText(String.valueOf(warning));
            criticalCount.setText(String.valueOf(critical));
            attentionAdapter.submitList(attention);
        }
    }

    // Greeting logic method
    private String getGreetingMessage(String userName) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour < 12) {
            greeting = getString(R.string.good_morning);
        }
        else if (hour < 17) {
            greeting = getString(R.string.good_afternoon);
        }
        else {
            greeting = getString(R.string.good_evening);
        }

        return greeting + ", " + userName;
    }
}