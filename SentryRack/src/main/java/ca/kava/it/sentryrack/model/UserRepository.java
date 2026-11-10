// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private static final String TAG = "UserRepository";

    private final DatabaseReference usersRef;

    public UserRepository() {
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public void createOrUpdateOnLogin(User user) {
        if (user == null || user.getUid() == null) return;

        DatabaseReference userRef = usersRef.child(user.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Only these fields are ever written, so the locations,
                // buildings and feedback maps are never overwritten.
                Map<String, Object> updates = new HashMap<>();
                updates.put("name", user.getName());
                updates.put("email", user.getEmail());
                updates.put("lastLoginAt", ServerValue.TIMESTAMP);
                if (!snapshot.hasChild("createdAt")) {
                    updates.put("createdAt", ServerValue.TIMESTAMP);
                }

                userRef.updateChildren(updates, (error, ref) -> {
                    if (error != null) {
                        Log.w(TAG, "Could not write user node: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Could not read user node: " + error.getMessage());
            }
        });
    }
}