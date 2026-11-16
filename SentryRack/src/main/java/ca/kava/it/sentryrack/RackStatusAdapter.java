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
import ca.kava.it.sentryrack.model.SensorUnitConverter;


import ca.kava.it.sentryrack.model.RackStatus;
public class RackStatusAdapter extends ListAdapter<RackStatus, RackStatusAdapter.VH> {
    private static final long STALE_MS = RackStatus.STALE_AFTER_MS;

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

        RackStatus.Condition condition = r.getCondition(STALE_MS);
        if (condition == RackStatus.Condition.WARNING) {
            h.racksStatusChip.setText(R.string.racks_status_offline);
            h.racksStatusChip.setBackgroundColor(ContextCompat.getColor(context, R.color.status_offline));
        } else if (condition == RackStatus.Condition.CRITICAL) {
            h.racksStatusChip.setText(R.string.racks_status_leak);
            h.racksStatusChip.setBackgroundColor(ContextCompat.getColor(context, R.color.status_critical));
        } else {
            h.racksStatusChip.setText(R.string.racks_status_healthy);
            h.racksStatusChip.setBackgroundColor(ContextCompat.getColor(context, R.color.status_healthy));
        }

        String details;
        if (!r.hasSensorData) {
            details = context.getString(R.string.racks_no_sensor_data);
        } else {
            String doorState = context.getString(
                    r.doorOpen ? R.string.racks_door_open : R.string.racks_door_closed);
            // Settings > Sensor units + Decimal places
            details = context.getString(R.string.racks_sensor_details,
                    SettingsFragment.formatReading(context, SensorUnitConverter.Sensor.COOLANT, r.coolantLevel),
                    SettingsFragment.formatReading(context, SensorUnitConverter.Sensor.CURRENT, r.currentA),
                    SettingsFragment.formatReading(context, SensorUnitConverter.Sensor.POWER, r.powerW),
                    SettingsFragment.formatReading(context, SensorUnitConverter.Sensor.TVOC, r.tvoc),
                    SettingsFragment.formatReading(context, SensorUnitConverter.Sensor.ECO2, r.eco2),
                    doorState);
        }
        if (r.simulated) {
            details += " · " + context.getString(R.string.racks_simulated_data);
        }
        h.racksLocation.setText(details);

        int healthScore = r.getHealthScore(STALE_MS);
        h.racksHealthProgress.setProgress(healthScore);
        h.racksHealthValue.setText(String.valueOf(healthScore));

        h.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(r);
            }
            return true;
        });
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

    //--- Long click listener ---
    public interface OnItemLongClickListener {
        void onItemLongClick(RackStatus rack);
    }

    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }
}