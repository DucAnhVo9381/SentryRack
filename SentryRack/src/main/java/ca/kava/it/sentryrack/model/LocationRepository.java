// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LocationRepository {

    private final DatabaseReference rootRef;

    public interface Callback {
        void onSuccess(String id);
        void onError(String message);
    }

    public LocationRepository() {
        this.rootRef = FirebaseDatabase.getInstance().getReference();
    }

    public void registerRackHierarchy(String rackName, String floorName, String roomName,
                                     String creatorUid, String role, Callback callback) {
        String rack = rackName == null ? "" : rackName.trim();
        String floor = floorName == null ? "" : floorName.trim();
        String room = roomName == null ? "" : roomName.trim();

        if (rack.isEmpty() || floor.isEmpty() || room.isEmpty()) {
            callback.onError("Rack name, floor, and room are required.");
            return;
        }

        final String locationName = "Default Location";
        final String buildingName = "Default Building";

        resolveOrCreateLocation(locationName, "Auto-created for rack registration", creatorUid, role,
                new Callback() {
                    @Override
                    public void onSuccess(String locationId) {
                        resolveOrCreateBuilding(locationId, buildingName, creatorUid, role,
                                new Callback() {
                                    @Override
                                    public void onSuccess(String buildingId) {
                                        resolveOrCreateFloor(buildingId, locationId, floor,
                                                new Callback() {
                                                    @Override
                                                    public void onSuccess(String floorId) {
                                                        resolveOrCreateRoom(floorId, buildingId, locationId, room,
                                                                new Callback() {
                                                                    @Override
                                                                    public void onSuccess(String roomId) {
                                                                        createRack(roomId, floorId, buildingId, locationId, rack, callback);
                                                                    }

                                                                    @Override
                                                                    public void onError(String message) {
                                                                        callback.onError(message);
                                                                    }
                                                                });
                                                    }

                                                    @Override
                                                    public void onError(String message) {
                                                        callback.onError(message);
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onError(String message) {
                                        callback.onError(message);
                                    }
                                });
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
    }

    private void resolveOrCreateLocation(String name, String address, String creatorUid, String role, Callback callback) {
        rootRef.child("locations").orderByChild("name").equalTo(name).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                callback.onSuccess(child.getKey());
                                return;
                            }
                        }
                        createLocation(name, address, creatorUid, role, callback);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    private void resolveOrCreateBuilding(String locationId, String name, String creatorUid, String role, Callback callback) {
        rootRef.child("buildings").orderByChild("locationId").equalTo(locationId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String existingName = child.child("name").getValue(String.class);
                                if (name.equals(existingName)) {
                                    callback.onSuccess(child.getKey());
                                    return;
                                }
                            }
                        }
                        createBuilding(locationId, name, creatorUid, role, callback);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    private void resolveOrCreateFloor(String buildingId, String locationId, String name, Callback callback) {
        rootRef.child("floors").orderByChild("buildingId").equalTo(buildingId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String existingName = child.child("name").getValue(String.class);
                                if (name.equals(existingName)) {
                                    callback.onSuccess(child.getKey());
                                    return;
                                }
                            }
                        }
                        createFloor(buildingId, locationId, 1, name, callback);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    private void resolveOrCreateRoom(String floorId, String buildingId, String locationId, String name, Callback callback) {
        rootRef.child("rooms").orderByChild("floorId").equalTo(floorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String existingName = child.child("name").getValue(String.class);
                                if (name.equals(existingName)) {
                                    callback.onSuccess(child.getKey());
                                    return;
                                }
                            }
                        }
                        createRoom(floorId, buildingId, locationId, name, callback);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void createLocation(String name, String address, String creatorUid, String role, Callback callback) {
        String locId = rootRef.child("locations").push().getKey();
        if (locId == null) {
            callback.onError("Could not generate location key");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("/locations/" + locId + "/name", name);
        updates.put("/locations/" + locId + "/address", address);
        updates.put("/locations/" + locId + "/members/" + creatorUid, role);
        updates.put("/users/" + creatorUid + "/locations/" + locId, true);

        rootRef.updateChildren(updates, (error, ref) -> {
            if (error != null) {
                callback.onError(error.getMessage());
            } else {
                callback.onSuccess(locId);
            }
        });
    }

    public void createBuilding(String locationId, String name, String creatorUid, String role, Callback callback) {
        String bldId = rootRef.child("buildings").push().getKey();
        if (bldId == null) {
            callback.onError("Could not generate building key");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("/buildings/" + bldId + "/locationId", locationId);
        updates.put("/buildings/" + bldId + "/name", name);
        updates.put("/buildings/" + bldId + "/members/" + creatorUid, role);
        updates.put("/locations/" + locationId + "/buildings/" + bldId, true);
        updates.put("/users/" + creatorUid + "/buildings/" + bldId, true);

        rootRef.updateChildren(updates, (error, ref) -> {
            if (error != null) {
                callback.onError(error.getMessage());
            } else {
                callback.onSuccess(bldId);
            }
        });
    }

    public void createFloor(String buildingId, String locationId, int level, String name, Callback callback) {
        String floorId = rootRef.child("floors").push().getKey();
        if (floorId == null) {
            callback.onError("Could not generate floor key");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("/floors/" + floorId + "/buildingId", buildingId);
        updates.put("/floors/" + floorId + "/locationId", locationId);
        updates.put("/floors/" + floorId + "/level", level);
        updates.put("/floors/" + floorId + "/name", name);
        updates.put("/buildings/" + buildingId + "/floors/" + floorId, true);

        rootRef.updateChildren(updates, (error, ref) -> {
            if (error != null) {
                callback.onError(error.getMessage());
            } else {
                callback.onSuccess(floorId);
            }
        });
    }

    public void createRoom(String floorId, String buildingId, String locationId, String name, Callback callback) {
        String roomId = rootRef.child("rooms").push().getKey();
        if (roomId == null) {
            callback.onError("Could not generate room key");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("/rooms/" + roomId + "/floorId", floorId);
        updates.put("/rooms/" + roomId + "/buildingId", buildingId);
        updates.put("/rooms/" + roomId + "/locationId", locationId);
        updates.put("/rooms/" + roomId + "/name", name);
        updates.put("/floors/" + floorId + "/rooms/" + roomId, true);

        rootRef.updateChildren(updates, (error, ref) -> {
            if (error != null) {
                callback.onError(error.getMessage());
            } else {
                callback.onSuccess(roomId);
            }
        });
    }

    public void createRack(String roomId, String floorId, String buildingId, String locationId, String name, Callback callback) {
        String rackId = rootRef.child("racks").push().getKey();
        if (rackId == null) {
            callback.onError("Could not generate rack key");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("/racks/" + rackId + "/roomId", roomId);
        updates.put("/racks/" + rackId + "/floorId", floorId);
        updates.put("/racks/" + rackId + "/buildingId", buildingId);
        updates.put("/racks/" + rackId + "/locationId", locationId);
        updates.put("/racks/" + rackId + "/name", name);
        updates.put("/racks/" + rackId + "/online", false);
        updates.put("/racks/" + rackId + "/lastSeen", 0);
        updates.put("/rooms/" + roomId + "/racks/" + rackId, true);

        rootRef.updateChildren(updates, (error, ref) -> {
            if (error != null) {
                callback.onError(error.getMessage());
            } else {
                callback.onSuccess(rackId);
            }
        });
    }
}