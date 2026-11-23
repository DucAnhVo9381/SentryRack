// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RackRepository {

    public interface Callback {
        void onSuccess(String rackId, String roomId);
        void onDuplicateRack();
        void onError(String message);
    }

    // Used to report delete success/failure back to the caller
    public interface DeleteRackCallback {
        void onSuccess();
        void onError(String message);
    }

    private static class Existing {
        String locationId;
        String buildingId;
        String floorId;
        String roomId;
    }

    private final DatabaseReference rootRef;

    public RackRepository() {
        this.rootRef = FirebaseDatabase.getInstance().getReference();
    }

    public static void initializeLatestFromTemplate(String rackId, Consumer<String> onError) {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        root.child("sensorTemplate").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Exception error = task.getException();
                onError.accept(error != null ? error.getMessage()
                        : "Could not read the sensor template");
                return;
            }

            DataSnapshot template = task.getResult();
            if (!template.exists() || !template.hasChildren()) return;

            Map<String, Object> initialReadings = copyObject(template);
            initialReadings.put("timestamp", 0L);
            initialReadings.put("simulated", true);
            root.child("latest").child(rackId).runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    if (currentData.getValue() == null) {
                        currentData.setValue(initialReadings);
                    }
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(DatabaseError error, boolean committed,
                                       DataSnapshot currentData) {
                    if (error != null) onError.accept(error.getMessage());
                }
            });
        });
    }

    private static Map<String, Object> copyObject(DataSnapshot snapshot) {
        Map<String, Object> values = new HashMap<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            if (child.hasChildren()) {
                values.put(child.getKey(), copyObject(child));
            } else {
                values.put(child.getKey(), child.getValue());
            }
        }
        return values;
    }

    public void registerRack(RackRegistration request, Callback callback) {
        Existing found = new Existing();

        firstChildKey("users/" + request.uid + "/locations")
                .continueWithTask(task -> {
                    found.locationId = task.getResult();
                    return found.locationId == null ? Tasks.<String>forResult(null)
                            : firstChildKey("locations/" + found.locationId + "/buildings");
                })
                .continueWithTask(task -> {
                    found.buildingId = task.getResult();
                    return found.buildingId == null ? Tasks.<String>forResult(null)
                            : findChildByName("buildings/" + found.buildingId + "/floors",
                            "floors", request.floorName);
                })
                .continueWithTask(task -> {
                    found.floorId = task.getResult();
                    return found.floorId == null ? Tasks.<String>forResult(null)
                            : findChildByName("floors/" + found.floorId + "/rooms",
                            "rooms", request.roomName);
                })
                .continueWithTask(task -> {
                    found.roomId = task.getResult();
                    return found.roomId == null ? Tasks.<String>forResult(null)
                            : findChildByName("rooms/" + found.roomId + "/racks",
                            "racks", request.rackName);
                })
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        callback.onError(e != null ? e.getMessage() : "Unknown error");
                        return;
                    }
                    if (task.getResult() != null) {
                        callback.onDuplicateRack();
                        return;
                    }
                    writeRack(request, found, callback);
                });
    }
    private void writeRack(RackRegistration r, Existing found, Callback callback) {
        String locationId = found.locationId != null ? found.locationId : newKey("locations");
        String buildingId = found.buildingId != null ? found.buildingId : newKey("buildings");
        String floorId = found.floorId != null ? found.floorId : newKey("floors");
        String roomId = found.roomId != null ? found.roomId : newKey("rooms");
        String rackId = newKey("racks");

        Map<String, Object> updates = new HashMap<>();

        if (found.locationId == null) {
            updates.put("/locations/" + locationId + "/name", r.locationName);
            updates.put("/locations/" + locationId + "/members/" + r.uid, r.role);
            updates.put("/users/" + r.uid + "/locations/" + locationId, true);
        }
        if (found.buildingId == null) {
            updates.put("/buildings/" + buildingId + "/locationId", locationId);
            updates.put("/buildings/" + buildingId + "/name", r.buildingName);
            updates.put("/buildings/" + buildingId + "/members/" + r.uid, r.role);
            updates.put("/locations/" + locationId + "/buildings/" + buildingId, true);
            updates.put("/users/" + r.uid + "/buildings/" + buildingId, true);
        }
        if (found.floorId == null) {
            String base = "/floors/" + floorId + "/";
            updates.put(base + "buildingId", buildingId);
            updates.put(base + "locationId", locationId);
            updates.put(base + "name", r.floorName);
            if (r.floorLevel != null) {
                updates.put(base + "level", r.floorLevel);
            }
            updates.put("/buildings/" + buildingId + "/floors/" + floorId, true);
        }
        if (found.roomId == null) {
            String base = "/rooms/" + roomId + "/";
            updates.put(base + "floorId", floorId);
            updates.put(base + "buildingId", buildingId);
            updates.put(base + "locationId", locationId);
            updates.put(base + "name", r.roomName);
            updates.put("/floors/" + floorId + "/rooms/" + roomId, true);
        }

        String rackBase = "/racks/" + rackId + "/";
        updates.put(rackBase + "roomId", roomId);
        updates.put(rackBase + "floorId", floorId);
        updates.put(rackBase + "buildingId", buildingId);
        updates.put(rackBase + "locationId", locationId);
        updates.put(rackBase + "name", r.rackName);
        updates.put(rackBase + "online", false);
        updates.put(rackBase + "lastSeen", 0);
        updates.put("/rooms/" + roomId + "/racks/" + rackId, true);

        rootRef.child("sensorTemplate").get().addOnCompleteListener(templateTask -> {
            if (!templateTask.isSuccessful()) {
                Exception error = templateTask.getException();
                callback.onError(error != null ? error.getMessage()
                        : "Could not read the sensor template");
                return;
            }
            DataSnapshot template = templateTask.getResult();
            if (template.exists() && template.hasChildren()) {
                Map<String, Object> initialReadings = copyObject(template);
                initialReadings.put("timestamp", 0L);
                initialReadings.put("simulated", true);
                updates.put("/latest/" + rackId, initialReadings);
            }
            rootRef.updateChildren(updates, (error, ref) -> {
                if (error != null) {
                    callback.onError(error.getMessage());
                } else {
                    callback.onSuccess(rackId, roomId);
                }
            });
        });
    }

    private Task<String> firstChildKey(String path) {
        return rootRef.child(path).get().continueWith(task -> {
            Iterator<DataSnapshot> children = task.getResult().getChildren().iterator();
            return children.hasNext() ? children.next().getKey() : null;
        });
    }

    private Task<String> findChildByName(String indexPath, String nodeName, String name) {
        return rootRef.child(indexPath).get().continueWithTask(task -> {
            List<Task<DataSnapshot>> reads = new ArrayList<>();
            for (DataSnapshot child : task.getResult().getChildren()) {
                reads.add(rootRef.child(nodeName).child(child.getKey()).child("name").get());
            }
            return Tasks.whenAllSuccess(reads).continueWith(done -> {
                for (Object result : done.getResult()) {
                    DataSnapshot nameSnapshot = (DataSnapshot) result;
                    Object value = nameSnapshot.getValue();
                    if (value != null && name.equalsIgnoreCase(value.toString())) {
                        return nameSnapshot.getRef().getParent().getKey();
                    }
                }
                return null;
            });
        });
    }
    private String newKey(String node) {
        return rootRef.child(node).push().getKey();
    }


    // Deletes a rack and removes all Firebase references to it
    public void deleteRack(String rackId, DeleteRackCallback callback) {
        // Find the room that owns this rack
        rootRef.child("racks")
                .child(rackId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    // Read room id
                    String roomId = snapshot.child("roomId").getValue(String.class);

                    // Paths that will be deleted
                    Map<String, Object> updates = new HashMap<>();

                    // Remove rack information
                    updates.put("/racks/" + rackId, null);
                    // Remove sensor readings
                    updates.put("/latest/" + rackId, null);

                    // Remove rack from its room
                    if (roomId != null) {
                        updates.put("/rooms/" + roomId + "/racks/" + rackId, null);
                    }

                    // Execute all deletions in one Firebase update
                    rootRef.updateChildren(updates, (error, ref) -> {
                        if (error != null) {
                            callback.onError(error.getMessage());   // Notify caller that deletion failed
                        } else {
                            callback.onSuccess();       // Notify caller that deletion succeeded
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}