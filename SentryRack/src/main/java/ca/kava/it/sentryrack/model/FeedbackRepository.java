// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FeedbackRepository {

    private final DatabaseReference rootRef;

    public interface Callback {
        void onSuccess();
        void onError(String message);
    }

    public FeedbackRepository() {
        this.rootRef = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Saves feedback/{id} and the users/{uid}/feedback/{id} link in one atomic write.
     */
    public void submitFeedback(String uid, Feedback feedback, Callback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onError("User must be logged in to send feedback");
            return;
        }
        if (feedback == null || feedback.getDeviceInfo() == null) {
            callback.onError("Feedback is incomplete");
            return;
        }

        String id = rootRef.child("feedback").push().getKey();
        if (id == null) {
            callback.onError("Could not generate feedback ID");
            return;
        }

        DeviceInfo device = feedback.getDeviceInfo();
        String base = "/feedback/" + id + "/";

        Map<String, Object> updates = new HashMap<>();
        updates.put(base + "fullName", feedback.getFullName());
        updates.put(base + "phoneNumber", feedback.getPhoneNumber());
        updates.put(base + "email", feedback.getEmail());
        updates.put(base + "comment", feedback.getComment());
        updates.put(base + "starRating", feedback.getStarRating());
        updates.put(base + "deviceInfo/appVersion", device.getAppVersion());
        updates.put(base + "deviceInfo/androidVersion", device.getAndroidVersion());
        updates.put(base + "deviceInfo/manufacturer", device.getManufacturer());
        updates.put(base + "deviceInfo/model", device.getModel());
        updates.put("/users/" + uid + "/feedback/" + id, true);

        rootRef.updateChildren(updates, (error, ref) -> {
            if (error != null) {
                callback.onError(error.getMessage());
            } else {
                callback.onSuccess();
            }
        });
    }
}