// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import ca.kava.it.sentryrack.R;
import ca.kava.it.sentryrack.model.RackStatus;

public class RackStatusAdapter extends ListAdapter<RackStatus, RackStatusAdapter.VH> {

    private static final long STALE_MS = 60_000;

    public RackStatusAdapter() {
        super(new DiffUtil.ItemCallback<RackStatus>() {
            @Override
            public boolean areItemsTheSame(@NonNull RackStatus a, @NonNull RackStatus b) {
                return a.rackId.equals(b.rackId);
            }

            @Override
            public boolean areContentsTheSame(@NonNull RackStatus a, @NonNull RackStatus b) {
                return a.equals(b);
            }
        });
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.racks_rv_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        RackStatus r = getItem(position);
        Context context = h.itemView.getContext();

        h.racksName.setText(r.name);

        boolean isOffline = r.isStale(STALE_MS);
        if (isOffline) {
            h.racksStatusChip.setText("OFFLINE");
            h.racksStatusChip.setBackgroundColor(ContextCompat.getColor(context, R.color.status_offline));
        } else if (r.leak) {
            h.racksStatusChip.setText("LEAK");
            h.racksStatusChip.setBackgroundColor(ContextCompat.getColor(context, R.color.status_critical));
        } else {
            h.racksStatusChip.setText("Healthy");
            h.racksStatusChip.setBackgroundColor(ContextCompat.getColor(context, R.color.status_healthy));
        }

        String sensorDetails = String.format(Locale.US,
                "Coolant: %.1fmm | Current: %.1fA | Door: %s",
                r.coolantLevel, r.currentA, r.doorOpen ? "OPEN" : "Closed");
        h.racksLocation.setText(sensorDetails);

        int healthScore = r.getHealthScore(STALE_MS);
        h.racksHealthProgress.setProgress(healthScore);
        h.racksHealthValue.setText(String.valueOf(healthScore));
    }

    public static class VH extends RecyclerView.ViewHolder {
        public final TextView racksName;
        public final TextView racksStatusChip;
        public final TextView racksLocation;
        public final ProgressBar racksHealthProgress;
        public final TextView racksHealthValue;

        public VH(@NonNull View v) {
            super(v);
            racksName = v.findViewById(R.id.racksName);
            racksStatusChip = v.findViewById(R.id.racksStatusChip);
            racksLocation = v.findViewById(R.id.racksLocation);
            racksHealthProgress = v.findViewById(R.id.racksHealthProgress);
            racksHealthValue = v.findViewById(R.id.racksHealthValue);
        }
    }
}