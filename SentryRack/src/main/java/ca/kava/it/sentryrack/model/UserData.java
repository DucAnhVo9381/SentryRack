// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;

public class UserData {
    private String uid;
    private String name;
    private String email;
    private long createdAt;
    private long lastLoginAt;
    private Map<String, Boolean> locations = new HashMap<>();
    private Map<String, Boolean> buildings = new HashMap<>();
    private Map<String, Boolean> feedback = new HashMap<>();

    public UserData() {
    }

    public UserData(String uid, String name, String email, long createdAt, long lastLoginAt) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public static UserData fromFirebaseUser(FirebaseUser firebaseUser) {
        if (firebaseUser == null) {
            return null;
        }
        String name = firebaseUser.getDisplayName();
        if (name == null || name.trim().isEmpty()) {
            if (firebaseUser.getEmail() != null && firebaseUser.getEmail().contains("@")) {
                name = firebaseUser.getEmail().split("@")[0];
            } else {
                name = "UserData";
            }
        }
        return new UserData(firebaseUser.getUid(), name, firebaseUser.getEmail(), 0, 0);
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @com.google.firebase.database.Exclude
    public String getDisplayName() { return name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(long lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public Map<String, Boolean> getLocations() { return locations; }
    public void setLocations(Map<String, Boolean> locations) { this.locations = locations; }

    public Map<String, Boolean> getBuildings() { return buildings; }
    public void setBuildings(Map<String, Boolean> buildings) { this.buildings = buildings; }

    public Map<String, Boolean> getFeedback() { return feedback; }
    public void setFeedback(Map<String, Boolean> feedback) { this.feedback = feedback; }
}