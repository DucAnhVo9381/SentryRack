// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class FeedbackRepository {

    private final DatabaseReference rootRef;

    public interface Callback {
        void onSuccess(String feedbackId);
        void onError(String message);
    }

    public FeedbackRepository() {
        this.rootRef = FirebaseDatabase.getInstance().getReference();
    }

    public void submitFeedback(String userId, String message, int rating, String category,
                               String locationId, String buildingId, Callback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError("User must be logged in to send feedback");
            return;
        }

        String fbId = rootRef.child("feedback").push().getKey();
        if (fbId == null) {
            callback.onError("Could not generate feedback ID");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("/feedback/" + fbId + "/userId", userId);
        updates.put("/feedback/" + fbId + "/message", message);
        updates.put("/feedback/" + fbId + "/rating", rating);
        updates.put("/feedback/" + fbId + "/category", category != null ? category : "Other");
        updates.put("/feedback/" + fbId + "/locationId", locationId != null ? locationId : "");
        updates.put("/feedback/" + fbId + "/buildingId", buildingId != null ? buildingId : "");
        updates.put("/feedback/" + fbId + "/timestamp", ServerValue.TIMESTAMP);
        updates.put("/feedback/" + fbId + "/status", "open");
        updates.put("/users/" + userId + "/feedback/" + fbId, true);

        rootRef.updateChildren(updates, (error, ref) -> {
            if (error != null) {
                callback.onError(error.getMessage());
            } else {
                callback.onSuccess(fbId);
            }
        });
    }
}