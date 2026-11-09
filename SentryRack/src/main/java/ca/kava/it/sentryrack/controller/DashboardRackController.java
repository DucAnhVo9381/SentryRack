package ca.kava.it.sentryrack.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.kava.it.sentryrack.model.RackStatus;
import ca.kava.it.sentryrack.model.RackRepository;

public class DashboardRackController {

    public interface Callback {
        void onRacksChanged(List<RackStatus> racks);
        void onError(String message);
    }

    private final DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    private final Callback callback;
    private final Map<String, RackStatus> items = new LinkedHashMap<>();
    private final Map<String, String> names = new HashMap<>();
    private final Map<String, ValueEventListener> latestListeners = new HashMap<>();
    private final Map<String, ChildEventListener> roomListeners = new HashMap<>();
    private final Map<String, Set<String>> roomRacks = new HashMap<>();

    private DatabaseReference locationsRef;
    private ValueEventListener locationsListener;
    private int generation;
    private int locationGeneration;

    public DashboardRackController(Callback callback) {
        this.callback = callback;
    }

    public void start(String uid) {
        stop();
        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("No signed-in user");
            return;
        }

        locationsRef = root.child("users").child(uid).child("locations");
        locationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> locationIds = keys(snapshot);
                final int requestGeneration = generation;
                final int requestLocationGeneration = ++locationGeneration;
                loadRoomIds(locationIds).addOnCompleteListener(task -> {
                    if (requestGeneration != generation
                            || requestLocationGeneration != locationGeneration) return;
                    if (!task.isSuccessful()) {
                        Exception error = task.getException();
                        callback.onError(error != null ? error.getMessage()
                                : "Could not load rack locations");
                        return;
                    }
                    updateRoomListeners(task.getResult());
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        };
        locationsRef.addValueEventListener(locationsListener);
    }

    public void stop() {
        generation++;
        locationGeneration++;
        if (locationsRef != null && locationsListener != null) {
            locationsRef.removeEventListener(locationsListener);
        }
        for (Map.Entry<String, ChildEventListener> entry : roomListeners.entrySet()) {
            root.child("rooms").child(entry.getKey()).child("racks")
                    .removeEventListener(entry.getValue());
        }
        for (Map.Entry<String, ValueEventListener> entry : latestListeners.entrySet()) {
            root.child("latest").child(entry.getKey()).removeEventListener(entry.getValue());
        }
        roomListeners.clear();
        latestListeners.clear();
        roomRacks.clear();
        items.clear();
        names.clear();
        locationsRef = null;
        locationsListener = null;
    }

    private Task<List<String>> loadRoomIds(List<String> locationIds) {
        return childKeys(locationIds, "locations", "buildings")
                .continueWithTask(task -> childKeys(task.getResult(), "buildings", "floors"))
                .continueWithTask(task -> childKeys(task.getResult(), "floors", "rooms"));
    }

    private Task<List<String>> childKeys(List<String> parentIds, String parentNode,
                                         String childIndex) {
        List<Task<DataSnapshot>> reads = new ArrayList<>();
        for (String parentId : parentIds) {
            reads.add(root.child(parentNode).child(parentId).child(childIndex).get());
        }
        if (reads.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }
        return Tasks.whenAllSuccess(reads).continueWith(task -> {
            List<String> result = new ArrayList<>();
            for (Object value : task.getResult()) {
                result.addAll(keys((DataSnapshot) value));
            }
            return result;
        });
    }

    private void updateRoomListeners(List<String> roomIds) {
        Set<String> desiredRooms = new HashSet<>(roomIds);
        for (String roomId : new HashSet<>(roomListeners.keySet())) {
            if (!desiredRooms.contains(roomId)) {
                detachRoom(roomId);
            }
        }
        for (String roomId : desiredRooms) {
            if (roomListeners.containsKey(roomId)) continue;
            ChildEventListener listener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot,
                                         @Nullable String previousChildName) {
                    String rackId = snapshot.getKey();
                    if (rackId == null) return;
                    roomRacks.computeIfAbsent(roomId, key -> new HashSet<>()).add(rackId);
                    attachRack(rackId);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    String rackId = snapshot.getKey();
                    Set<String> racks = roomRacks.get(roomId);
                    if (rackId != null && racks != null) {
                        racks.remove(rackId);
                        if (!isRackReferenced(rackId)) detachRack(rackId);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot,
                                           @Nullable String previousChildName) { }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot,
                                         @Nullable String previousChildName) { }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            };
            roomListeners.put(roomId, listener);
            root.child("rooms").child(roomId).child("racks").addChildEventListener(listener);
        }
    }

    private boolean isRackReferenced(String rackId) {
        for (Set<String> racks : roomRacks.values()) {
            if (racks.contains(rackId)) return true;
        }
        return false;
    }

    private void detachRoom(String roomId) {
        ChildEventListener listener = roomListeners.remove(roomId);
        if (listener != null) {
            root.child("rooms").child(roomId).child("racks").removeEventListener(listener);
        }
        Set<String> racks = roomRacks.remove(roomId);
        if (racks != null) {
            for (String rackId : racks) {
                if (!isRackReferenced(rackId)) detachRack(rackId);
            }
        }
    }

    private void attachRack(String rackId) {
        if (latestListeners.containsKey(rackId)) return;
        final int requestGeneration = generation;
        items.put(rackId, RackStatus.placeholder(rackId, rackId));
        publish();

        root.child("racks").child(rackId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (requestGeneration != generation || !items.containsKey(rackId)) return;
                        String name = snapshot.getValue(String.class);
                        names.put(rackId, name != null ? name : rackId);
                        updateName(rackId);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (requestGeneration != generation || !items.containsKey(rackId)) return;
                if (!snapshot.exists()) {
                    RackRepository.ensureLatestIfMissing(rackId, callback::onError);
                }
                String name = names.containsKey(rackId) ? names.get(rackId) : rackId;
                items.put(rackId, RackStatus.fromSnapshot(rackId, name, snapshot));
                publish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        };
        latestListeners.put(rackId, listener);
        root.child("latest").child(rackId).addValueEventListener(listener);
    }

    private void updateName(String rackId) {
        RackStatus current = items.get(rackId);
        if (current == null) return;
        items.put(rackId, new RackStatus(rackId, names.get(rackId), current.timestamp,
                current.simulated, current.coolantLevel, current.leak, current.currentA,
                current.powerW, current.doorOpen, current.aqi, current.tvoc, current.eco2));
        publish();
    }

    private void detachRack(String rackId) {
        ValueEventListener listener = latestListeners.remove(rackId);
        if (listener != null) {
            root.child("latest").child(rackId).removeEventListener(listener);
        }
        items.remove(rackId);
        names.remove(rackId);
        publish();
    }

    private List<String> keys(DataSnapshot snapshot) {
        List<String> result = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            if (child.getKey() != null) result.add(child.getKey());
        }
        return result;
    }

    private void publish() {
        callback.onRacksChanged(new ArrayList<>(items.values()));
    }
}
