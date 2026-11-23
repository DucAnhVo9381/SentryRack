// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.controller;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.TimeUnit;

import ca.kava.it.sentryrack.R;
import ca.kava.it.sentryrack.model.DashboardModel;

public class DashboardListAdapter extends ListAdapter<DashboardModel, DashboardListAdapter.VH> {
    // Efficiently update RecyclerView items when data changes
    public DashboardListAdapter() {
        super(new DiffUtil.ItemCallback<DashboardModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull DashboardModel oldItem, @NonNull DashboardModel newItem) {
                return oldItem.rackName.equals(newItem.rackName);
            }

            @Override
            public boolean areContentsTheSame(@NonNull DashboardModel oldItem, @NonNull DashboardModel newItem) {
                return oldItem.rackName.equals(newItem.rackName)
                        && oldItem.message.equals(newItem.message)
                        && oldItem.timestamp == newItem.timestamp
                        && oldItem.critical == newItem.critical;
            }
        });
    }

    @NonNull
    @Override
    // Inflate the dashboard recycler view item layout
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.dashboard_rv_item,
                parent,
                false
        );
        return new VH(view);
    }

    @Override
    // Bind DashboardModel data to the recycler view card
    public void onBindViewHolder(@NonNull VH holder, int position) {
        DashboardModel alert = getItem(position);

        holder.rackName.setText(alert.rackName);
        holder.message.setText(alert.message);
        holder.timestamp.setText(formatTimeAgo(alert.timestamp));
        holder.indicator.setBackgroundColor(alert.critical ? Color.RED : Color.YELLOW);
    }

    // Convert timestamp into 5m ago, 1h ago, etc.
    private String formatTimeAgo(long timestamp) {
        if (timestamp <= 0) {
            return "Now";
        }

        long diffMillis = System.currentTimeMillis() - timestamp;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);

        if (minutes < 1) { return "Now"; }
        if (minutes < 60) { return minutes + "m ago"; }

        long hours = minutes / 60;
        if (hours < 24) { return hours + "h ago"; }

        long days = hours / 24;
        return days + "d ago";
    }

    // Holds references to dashboard recycler view item's views
    static class VH extends RecyclerView.ViewHolder {
        final View indicator;
        final TextView rackName;
        final TextView message;
        final TextView timestamp;

        // Initialize views from the dashboard recycler view item's layout
        VH(@NonNull View itemView) {
            super(itemView);

            indicator = itemView.findViewById(R.id.dashboardAttentionIndicator);
            rackName = itemView.findViewById(R.id.dashboardAttentionRackName);
            message = itemView.findViewById(R.id.dashboardAttentionMessage);
            timestamp = itemView.findViewById(R.id.dashboardAttentionTimestamp);
        }
    }
}