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
     * Saves feedbackData/{id} and the users/{uid}/feedbackData/{id} link in one atomic write.
     */
    public void submitFeedback(String uid, FeedbackData feedbackData, Callback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onError("UserData must be logged in to send feedbackData");
            return;
        }
        if (feedbackData == null || feedbackData.getDeviceInfo() == null) {
            callback.onError("FeedbackData is incomplete");
            return;
        }

        String id = rootRef.child("feedbackData").push().getKey();
        if (id == null) {
            callback.onError("Could not generate feedbackData ID");
            return;
        }

        DeviceInfoData device = feedbackData.getDeviceInfo();
        String base = "/feedbackData/" + id + "/";

        Map<String, Object> updates = new HashMap<>();
        updates.put(base + "fullName", feedbackData.getFullName());
        updates.put(base + "phoneNumber", feedbackData.getPhoneNumber());
        updates.put(base + "email", feedbackData.getEmail());
        updates.put(base + "comment", feedbackData.getComment());
        updates.put(base + "starRating", feedbackData.getStarRating());
        updates.put(base + "deviceInfo/appVersion", device.getAppVersion());
        updates.put(base + "deviceInfo/androidVersion", device.getAndroidVersion());
        updates.put(base + "deviceInfo/manufacturer", device.getManufacturer());
        updates.put(base + "deviceInfo/model", device.getModel());
        updates.put("/users/" + uid + "/feedbackData/" + id, true);

        rootRef.updateChildren(updates, (error, ref) -> {
            if (error != null) {
                callback.onError(error.getMessage());
            } else {
                callback.onSuccess();
            }
        });
    }
}