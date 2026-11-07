// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ca.kava.it.sentryrack.model.RackStatus;

public class RackListController {

    public interface Callback {
        void onRacksChanged(List<RackStatus> racks);
        void onError(String message);
    }

    private final DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    private final Map<String, RackStatus> items = new LinkedHashMap<>();
    private final Map<String, String> names = new HashMap<>();
    private final Map<String, ValueEventListener> liveListeners = new HashMap<>();
    private final Callback callback;

    private DatabaseReference indexRef;
    private ChildEventListener indexListener;

    public RackListController(Callback callback) {
        this.callback = callback;
    }

    /** Call from onStart(). */
    public void start(String roomId) {
        stop();
        if (roomId == null || roomId.trim().isEmpty()) {
            callback.onError("No room selected");
            return;
        }

        indexRef = root.child("rooms").child(roomId).child("racks");
        indexListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot s, @Nullable String prev) {
                attachRack(s.getKey());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot s) {
                detachRack(s.getKey());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot s, @Nullable String prev) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot s, @Nullable String prev) { }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                callback.onError(e.getMessage());
            }
        };
        indexRef.addChildEventListener(indexListener);
    }

    /** Call from onStop(). */
    public void stop() {
        if (indexRef != null && indexListener != null) {
            indexRef.removeEventListener(indexListener);
        }
        for (Map.Entry<String, ValueEventListener> e : liveListeners.entrySet()) {
            root.child("latest").child(e.getKey()).removeEventListener(e.getValue());
        }
        liveListeners.clear();
        items.clear();
        names.clear();
        indexRef = null;
        indexListener = null;
    }

    private void attachRack(final String rackId) {
        if (rackId == null || liveListeners.containsKey(rackId)) return;

        items.put(rackId, RackStatus.placeholder(rackId, rackId));
        publish();

        root.child("racks").child(rackId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot s) {
                        String name = s.getValue(String.class);
                        names.put(rackId, name != null ? name : rackId);
                        RackStatus cur = items.get(rackId);
                        if (cur != null) {
                            items.put(rackId, new RackStatus(rackId, names.get(rackId),
                                    cur.timestamp, cur.coolantLevel, cur.leak, cur.currentA,
                                    cur.powerW, cur.doorOpen, cur.aqi, cur.tvoc, cur.eco2));
                            publish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        callback.onError(e.getMessage());
                    }
                });

        ValueEventListener live = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                if (!items.containsKey(rackId)) return;
                String name = names.containsKey(rackId) ? names.get(rackId) : rackId;
                items.put(rackId, RackStatus.fromSnapshot(rackId, name, s));
                publish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                callback.onError(e.getMessage());
            }
        };
        root.child("latest").child(rackId).addValueEventListener(live);
        liveListeners.put(rackId, live);
    }

    private void detachRack(String rackId) {
        ValueEventListener l = liveListeners.remove(rackId);
        if (l != null) root.child("latest").child(rackId).removeEventListener(l);
        items.remove(rackId);
        names.remove(rackId);
        publish();
    }

    private void publish() {
        callback.onRacksChanged(new ArrayList<>(items.values()));
    }
}